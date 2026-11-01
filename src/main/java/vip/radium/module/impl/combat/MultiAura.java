package vip.radium.module.impl.combat;

import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.RandomUtils;
import vip.radium.RadiumClient;
import vip.radium.event.impl.player.UpdatePositionEvent;
import vip.radium.module.Module;
import vip.radium.module.ModuleCategory;
import vip.radium.module.ModuleInfo;
import vip.radium.module.ModuleManager;
import vip.radium.module.impl.player.AutoTool;
import vip.radium.module.impl.player.Scaffold;
import vip.radium.property.impl.DoubleProperty;
import vip.radium.property.impl.EnumProperty;
import vip.radium.property.impl.MultiSelectEnumProperty;
import vip.radium.property.impl.Representation;
import vip.radium.utils.PlayerUtils;
import vip.radium.utils.RotationUtils;
import vip.radium.utils.TimerUtil;
import vip.radium.utils.Wrapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ModuleInfo(label = "Multi Aura", category = ModuleCategory.COMBAT)
public final class MultiAura extends Module {

    private final EnumProperty<AttackMode> attackModeProperty = new EnumProperty<>("Mode", AttackMode.PACKET);
    private final DoubleProperty maxAngleChangeProperty = new DoubleProperty("Max Angle Step", 45.0,
            () -> this.attackModeProperty.getValue() == AttackMode.ROTATIONS, 1.0, 201.25, 0.25);
    private final DoubleProperty minApsProperty = new DoubleProperty("Min APS", 8.0, 1.0, 20.0, 1.0);
    private final DoubleProperty maxApsProperty = new DoubleProperty("Max APS", 10.0, 1.0, 20.0, 1.0);
    private final DoubleProperty rangeProperty = new DoubleProperty("Range", 4.25, 3.0,
            6.0, 0.05, Representation.DISTANCE);
    private final MultiSelectEnumProperty<Checks> checksProperty =
            new MultiSelectEnumProperty<>("Checks", Checks.ALIVE, Checks.FOV);
    private final DoubleProperty fovProperty = new DoubleProperty("Scan Degrees", 201.25,
            () -> this.checksProperty.isSelected(Checks.FOV), 0.0, 201.25, 0.25);
    private final DoubleProperty maxTargetsProperty = new DoubleProperty("Max Targets", 3.0,
            () -> this.attackModeProperty.getValue() == AttackMode.PACKET, 1.0, 5.0, 1.0, Representation.INT);
    private final MultiSelectEnumProperty<Targets> targetsProperty =
            new MultiSelectEnumProperty<>("Targets", Targets.PLAYERS);

    private final TimerUtil attackTimer = new TimerUtil();

    private final List<EntityLivingBase> switchPool = new ArrayList<>();

    private Scaffold scaffold;

    @EventLink
    public final Listener<UpdatePositionEvent> onUpdatePositionEvent = event -> {
        if (event.isPre()) {
            if (this.attackModeProperty.getValue() == AttackMode.ROTATIONS)
                this.updateRotations(event);
            return;
        }

        if (this.attackModeProperty.getValue() == AttackMode.PACKET)
            this.attackSilently();
        else
            this.attackWithRotations(event);
    };

    private void attackSilently() {
        if (isOccupied())
            return;

        final List<EntityLivingBase> targets = gatherTargets();
        if (targets.isEmpty() || !hasAttackedRecently())
            return;

        final EntityPlayer player = Wrapper.getPlayer();
        final int maxTargets = this.maxTargetsProperty.getValue().intValue();

        ModuleManager.getInstance(AutoTool.class).doSwordSwap();

        // Silent: each look packet points the server at one target while the camera
        // stays put. Capping hits per cycle keeps the packet burst below limiters.
        for (int i = 0; i < Math.min(maxTargets, targets.size()); i++) {
            final EntityLivingBase entity = targets.get(i);
            final float[] rotations = RotationUtils.getRotationsToEntity(entity);

            Wrapper.sendPacket(new C03PacketPlayer.C05PacketPlayerLook(
                    rotations[0], rotations[1], player.onGround));

            player.swingItem();
            Wrapper.sendPacket(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK));
        }

        this.attackTimer.reset();
    }

    private void updateRotations(UpdatePositionEvent event) {
        this.switchPool.clear();

        if (isOccupied())
            return;

        this.switchPool.addAll(gatherTargets());

        if (this.switchPool.isEmpty())
            return;

        final float[] rotations = RotationUtils.getRotationsToEntity(this.switchPool.get(0));
        RotationUtils.rotate(event, rotations,
                this.maxAngleChangeProperty.getValue().floatValue(), false);
    }

    private void attackWithRotations(UpdatePositionEvent event) {
        if (isOccupied() || this.switchPool.isEmpty() || !hasAttackedRecently())
            return;

        ModuleManager.getInstance(AutoTool.class).doSwordSwap();

        final EntityLivingBase target = this.switchPool.get(0);

        if (!isFacingTarget(event, target))
            return;

        Wrapper.getPlayer().swingItem();
        Wrapper.sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));

        this.attackTimer.reset();
    }

    private List<EntityLivingBase> gatherTargets() {
        final List<EntityLivingBase> targets = Wrapper.getLivingEntities(this::isValid);
        final EntityPlayer player = Wrapper.getPlayer();

        targets.sort(Comparator.comparingDouble(entity ->
                Math.pow(entity.posX - player.posX, 2) +
                Math.pow(entity.posY - player.posY, 2) +
                Math.pow(entity.posZ - player.posZ, 2)));

        return targets;
    }

    private boolean isFacingTarget(UpdatePositionEvent event, EntityLivingBase target) {
        final EntityPlayer player = Wrapper.getPlayer();
        final double range = this.rangeProperty.getValue();
        final Vec3 src = player.getPositionEyes(1.0F);
        final Vec3 look = Entity.getVectorForRotation(event.getPitch(), event.getYaw());
        final Vec3 dest = src.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);

        final MovingObjectPosition hit = Wrapper.getWorld().rayTraceBlocks(src, dest, false, false, true);
        if (hit == null || hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            return false;

        return target.getEntityBoundingBox().expand(0.1F, 0.1F, 0.1F).calculateIntercept(src, dest) != null;
    }

    private boolean isOccupied() {
        return Wrapper.getCurrentScreen() != null || this.scaffold.isEnabled();
    }

    private boolean hasAttackedRecently() {
        int min = this.minApsProperty.getValue().intValue();
        int max = this.maxApsProperty.getValue().intValue();

        if (min > max) {
            min = max;
            max = min;
        }

        final int cps = min == max ? min : RandomUtils.nextInt(min, max);
        return this.attackTimer.hasElapsed(1000L / cps);
    }

    private boolean fovCheck(EntityLivingBase entity, int fov) {
        final float[] rotations = RotationUtils.getRotationsToEntity(entity);
        final EntityPlayer player = Wrapper.getPlayer();
        final float yawChange = MathHelper.wrapAngleTo180_float(player.rotationYaw - rotations[0]);
        final float pitchChange = MathHelper.wrapAngleTo180_float(player.rotationPitch - rotations[1]);
        return Math.sqrt(yawChange * yawChange + pitchChange * pitchChange) < fov;
    }

    private boolean isValid(EntityLivingBase entity) {
        if (this.checksProperty.isSelected(Checks.ALIVE) && !entity.isEntityAlive())
            return false;
        if (entity.isInvisible() && !this.targetsProperty.isSelected(Targets.INVISIBLES))
            return false;
        if (entity == Wrapper.getPlayer().ridingEntity)
            return false;

        if (entity instanceof EntityOtherPlayerMP) {
            final EntityPlayer player = (EntityPlayer) entity;
            if (!this.targetsProperty.isSelected(Targets.PLAYERS))
                return false;
            if (ModuleManager.getInstance(AntiBot.class).isBot(player))
                return false;
            if (!this.targetsProperty.isSelected(Targets.TEAMMATES) && PlayerUtils.isTeamMate(player))
                return false;
            if (!this.targetsProperty.isSelected(Targets.FRIENDS) &&
                    RadiumClient.getInstance().getPlayerManager().isFriend(player))
                return false;
        } else if (entity instanceof EntityMob) {
            if (!this.targetsProperty.isSelected(Targets.MOBS))
                return false;
        } else if (entity instanceof EntityAnimal) {
            if (!this.targetsProperty.isSelected(Targets.ANIMALS))
                return false;
        } else {
            // Ignore any other types of entities
            return false;
        }

        final EntityPlayer player = Wrapper.getPlayer();
        final double dist = Math.sqrt(
                (entity.posX - player.posX) * (entity.posX - player.posX) +
                (entity.posY - player.posY) * (entity.posY - player.posY) +
                (entity.posZ - player.posZ) * (entity.posZ - player.posZ));
        if (dist > this.rangeProperty.getValue())
            return false;

        return !this.checksProperty.isSelected(Checks.FOV) ||
                this.fovCheck(entity, this.fovProperty.getValue().intValue());
    }

    @Override
    public void onEnable() {
        ModuleManager.getInstance(KillAura.class).setEnabled(false);
        this.scaffold = ModuleManager.getInstance(Scaffold.class);
    }

    @Override
    public void onDisable() {
        this.switchPool.clear();
    }

    private enum AttackMode {
        PACKET,
        ROTATIONS
    }

    private enum Targets {
        PLAYERS,
        TEAMMATES,
        FRIENDS,
        INVISIBLES,
        MOBS,
        ANIMALS
    }

    private enum Checks {
        ALIVE,
        FOV
    }
}
