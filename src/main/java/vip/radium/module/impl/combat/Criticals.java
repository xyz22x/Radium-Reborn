package vip.radium.module.impl.combat;

import io.github.nevalackin.homoBus.annotations.EventLink;
import io.github.nevalackin.homoBus.Listener;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.MovingObjectPosition;
import vip.radium.event.impl.packet.PacketSendEvent;
import vip.radium.event.impl.player.UpdatePositionEvent;
import vip.radium.module.Module;
import vip.radium.module.ModuleCategory;
import vip.radium.module.ModuleInfo;
import vip.radium.property.impl.DoubleProperty;
import vip.radium.property.impl.EnumProperty;
import vip.radium.property.impl.Representation;
import vip.radium.utils.MovementUtils;
import vip.radium.utils.ServerUtils;
import vip.radium.utils.TimerUtil;
import vip.radium.utils.Wrapper;

@ModuleInfo(label = "Criticals", category = ModuleCategory.COMBAT)
public final class Criticals extends Module {

    private final TimerUtil timer = new TimerUtil();
    private final EnumProperty<CriticalsMode> criticalsModeProperty = new EnumProperty<>("Mode", CriticalsMode.WATCHDOG);
    private final DoubleProperty delayProperty = new DoubleProperty("Delay", 490, 0, 1000,
            10, Representation.MILLISECONDS);
    private int groundTicks;
    @EventLink
    public final Listener<UpdatePositionEvent> onUpdatePositionEvent = event -> {
        if (event.isPre())
            groundTicks = MovementUtils.isOnGround() ? groundTicks + 1 : 0;
    };

    public Criticals() {
        setSuffixListener(criticalsModeProperty);
    }

    @Override
    public void onEnable() {
        timer.reset();
        groundTicks = 0;
    }

    private boolean hasTarget() {
        if (KillAura.getInstance().getTarget() != null)
            return true;
        final MovingObjectPosition target = Wrapper.getMinecraft().objectMouseOver;
        return target != null && target.entityHit != null;
    }

    @EventLink
    public final Listener<PacketSendEvent> onPacketSendEvent = event -> {
        final CriticalsMode mode = this.criticalsModeProperty.getValue();

        if (event.getPacket() instanceof C03PacketPlayer) {
            if (mode == CriticalsMode.NO_GROUND) {
                ((C03PacketPlayer) event.getPacket()).setOnGround(false);
            }
            return;
        }

        if (!(event.getPacket() instanceof C0APacketAnimation) || !hasTarget())
            return;

        switch (mode) {
            case WATCHDOG:
            case NCP:
                if (ServerUtils.isOnHypixel() &&
                        this.timer.hasElapsed(this.delayProperty.getValue().longValue()) && this.groundTicks > 1) {
                    for (double offset : mode.offsets) {
                        Wrapper.sendPacketDirect(new C03PacketPlayer.C04PacketPlayerPosition(
                                Wrapper.getPlayer().posX,
                                Wrapper.getPlayer().posY + offset + (StrictMath.random() * 0.0003F),
                                Wrapper.getPlayer().posZ,
                                false));
                    }
                    this.timer.reset();
                }
                break;
            case GRIM:
                // Safe to dip below the block only while floating: still reads as air
                if (MovementUtils.isOnGround())
                    break;
                // fall through shared offsets below
            case VANILLA:
            case NCP_FULL:
            case FALLING:
            case LOW:
            case DOWN:
            case BLOCKSMC:
                if (mode == CriticalsMode.BLOCKSMC && Wrapper.getPlayer().ticksExisted % 4 != 0)
                    break;
                if (!this.timer.hasElapsed(this.delayProperty.getValue().longValue()))
                    break;

                for (int i = 0; i < mode.offsets.length; i++) {
                    Wrapper.sendPacketDirect(new C03PacketPlayer.C04PacketPlayerPosition(
                            Wrapper.getPlayer().posX,
                            Wrapper.getPlayer().posY + mode.offsets[i],
                            Wrapper.getPlayer().posZ,
                            mode.grounds[i]));
                }
                this.timer.reset();
                break;
        }
    };

    private enum CriticalsMode {
        WATCHDOG(new double[]{0.056f, 0.016f, 0.003f}),
        NCP(new double[]{0.06252f, 0.0f}),
        NO_GROUND,
        VANILLA(new double[]{0.2D, 0.01D}),
        NCP_FULL(new double[]{0.11D, 0.1100013579D, 0.0000013579D}),
        FALLING(new double[]{0.0625D, 0.0625013579D, 0.0000013579D}),
        LOW(new double[]{1.0E-9D, 0.0D}),
        DOWN(new double[]{-1.0E-9D}),
        GRIM(new double[]{-0.000001D}),
        BLOCKSMC(new double[]{0.0011D, 0.0D}, new boolean[]{true, false});

        private final double[] offsets;
        private final boolean[] grounds;

        CriticalsMode(double[] offsets) {
            this(offsets, new boolean[offsets.length]);
        }

        CriticalsMode() {
            this(new double[0]);
        }

        CriticalsMode(double[] offsets, boolean[] grounds) {
            this.offsets = offsets;
            this.grounds = grounds;
        }
    }

}
