package vip.radium.module.impl.visuals2;

import io.github.nevalackin.homoBus.annotations.EventLink;
import io.github.nevalackin.homoBus.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;
import vip.radium.core.GameClock;
import vip.radium.core.PlayerFilter;
import vip.radium.event.impl.render.Render3DEvent;
import vip.radium.module.Module;
import vip.radium.module.ModuleCategory;
import vip.radium.module.ModuleInfo;
import vip.radium.property.Property;
import vip.radium.property.impl.DoubleProperty;
import vip.radium.property.impl.EnumProperty;
import vip.radium.utils.Wrapper;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(label = "Trails Visuals", category = ModuleCategory.VISUALS2)
public final class TrailsVisuals extends Module {

    public final EnumProperty<TrailMode> modeProperty = new EnumProperty<>("Trail", TrailMode.LIQUID);
    public final DoubleProperty lengthProperty = new DoubleProperty("Length", 1.0, 0.1, 10.0, 0.1);
    public final DoubleProperty widthProperty = new DoubleProperty("Width", 0.5, 0.1, 3.0, 0.1);
    public final Property<Boolean> selfProperty = new Property<>("Self", true);
    public final Property<Boolean> othersProperty = new Property<>("Others", false);
    public final DoubleProperty maxDistProperty = new DoubleProperty("Max Distance", 32.0, 8.0, 128.0, 1.0);

    private final List<TrailPoint> selfHistory = new ArrayList<>();

    public TrailsVisuals() {
        setSuffixListener(modeProperty);
    }

    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP self = Wrapper.getPlayer();
        if (mc.theWorld == null || self == null) return;

        float partialTicks = event.getPartialTicks();
        double now = GameClock.seconds();
        double maxLen = lengthProperty.getValue();

        selfHistory.clear();

        // Gravar histórico do self
        if (selfProperty.getValue()) {
            addPoint(selfHistory, self, partialTicks);
        }

        // Desenhar trail do self
        if (!selfHistory.isEmpty()) {
            drawTrail(selfHistory, now, maxLen);
        }

        // Gravar e desenhar outros
        if (othersProperty.getValue()) {
            List<EntityLivingBase> entities = Wrapper.getLivingEntities(e -> {
                if (!(e instanceof net.minecraft.entity.player.EntityPlayer)) return false;
                if (e == self) return false;
                if (PlayerFilter.isTeammate(mc, self)) return false;
                return self.getDistanceToEntity(e) <= maxDistProperty.getValue();
            });
            for (EntityLivingBase e : entities) {
                List<TrailPoint> h = new ArrayList<>();
                addPoint(h, e, partialTicks);
                drawTrail(h, now, maxLen);
            }
        }
    };

    private void addPoint(List<TrailPoint> history, EntityLivingBase e, float pt) {
        double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * pt;
        double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * pt;
        double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * pt;
        history.add(new TrailPoint(x, y, z, (float) GameClock.seconds()));
    }

    private void drawTrail(List<TrailPoint> history, double now, double maxLen) {
        TrailMode mode = modeProperty.getValue();
        float width = widthProperty.getValue().floatValue();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(width);
        GL11.glDepthMask(false);

        for (int i = 0; i < history.size() - 1; i++) {
            TrailPoint p0 = history.get(i);
            TrailPoint p1 = history.get(i + 1);

            double dx = p1.x - p0.x;
            double dy = p1.y - p0.y;
            double dz = p1.z - p0.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < 0.02) continue;

            float age = (float) (now - p0.time);
            if (age > maxLen) continue;

            float alpha = 1.0f - age / (float) maxLen;
            if (alpha <= 0.0f) continue;

            int color = getColor(mode, age);

            GL11.glBegin(GL11.GL_LINES);
            GL11.glColor4f(
                ((color >> 16 & 0xFF) / 255.0f),
                ((color >> 8 & 0xFF) / 255.0f),
                ((color & 0xFF) / 255.0f),
                alpha
            );
            GL11.glVertex3d(p0.x, p0.y, p0.z);
            GL11.glVertex3d(p1.x, p1.y, p1.z);
            GL11.glEnd();
        }

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private int getColor(TrailMode mode, float age) {
        switch (mode) {
            case LIQUID: return 0xFF00FFFF;
            case STAR: return 0xFFFF00FF;
            case SPEED: return 0xFFFFFF00;
            case DOODLE: return 0xFF00FF00;
            case COMET: return 0xFFFFA500;
            case LIGHTNING: return 0xFF8080FF;
            default: return 0xFFFFFFFF;
        }
    }

    @Override
    public void onEnable() {
        selfHistory.clear();
    }

    @Override
    public void onDisable() {
        selfHistory.clear();
    }

    public enum TrailMode {
        LIQUID("Liquid"),
        STAR("Star"),
        SPEED("Speed"),
        DOODLE("Doodle"),
        COMET("Comet"),
        LIGHTNING("Lightning");

        public final String label;

        TrailMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class TrailPoint {
        final double x, y, z;
        final float time;

        TrailPoint(double x, double y, double z, float time) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.time = time;
        }
    }
}
