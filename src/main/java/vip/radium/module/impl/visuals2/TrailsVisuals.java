package vip.radium.module.impl.visuals2;

import io.github.nevalackin.homoBus.annotations.EventLink;
import io.github.nevalackin.homoBus.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import vip.radium.core.GameClock;
import vip.radium.core.PlayerFilter;
import vip.radium.event.impl.render.Render3DEvent;
import vip.radium.module.Module;
import vip.radium.module.ModuleCategory;
import vip.radium.module.ModuleInfo;
import vip.radium.module.ModuleManager;
import vip.radium.property.Property;
import vip.radium.property.impl.DoubleProperty;
import vip.radium.property.impl.EnumProperty;
import vip.radium.render.RenderState;
import vip.radium.render.ParticleSystem;
import vip.radium.render.Vfx;
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

    private final ParticleSystem particles = new ParticleSystem(4096);
    private final List<Point> history = new ArrayList<>();

    public TrailsVisuals() {
        setSuffixListener(modeProperty);
    }

    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP self = Wrapper.getPlayer();
        if (mc.theWorld == null || self == null) {
            return;
        }

        float partialTicks = event.getPartialTicks();
        double now = GameClock.seconds();

        // Record self position
        if (selfProperty.getValue()) {
            record(self, partialTicks);
        }

        // Record others
        if (othersProperty.getValue()) {
            List<EntityLivingBase> entities = Wrapper.getLivingEntities(e -> {
                if (!(e instanceof net.minecraft.entity.player.EntityPlayer)) return false;
                if (e == self) return false;
                if (PlayerFilter.isTeammate(mc, self)) return false;
                return self.getDistanceToEntity(e) <= maxDistProperty.getValue();
            });
            for (EntityLivingBase e : entities) {
                record(e, partialTicks);
            }
        }

        // Spawn trail particles from history
        TrailMode mode = modeProperty.getValue();
        float maxLen = lengthProperty.getValue().floatValue();
        float width = widthProperty.getValue().floatValue();

        for (int i = 0; i < history.size() - 1; i++) {
            Point p0 = history.get(i);
            Point p1 = history.get(i + 1);
            double dx = p1.x - p0.x;
            double dy = p1.y - p0.y;
            double dz = p1.z - p0.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < 0.05) continue;

            int segments = Math.max(1, (int)(dist / 0.3));
            for (int s = 0; s < segments; s++) {
                float t = s / (float)segments;
                double x = p0.x + dx * t;
                double y = p0.y + dy * t;
                double z = p0.z + dz * t;
                float age = (float)(now - p0.time);
                if (age > maxLen) continue;

                float alpha = 1.0f - age / maxLen;
                int color = getColor(mode, age);

                switch (mode) {
                    case LIQUID:
                        particles.spawn(ParticleSystem.GLOW, x, y, z,
                                0.1f, 0.1f, 0.1f,
                                0.6f, width * 0.6f, 0.0f,
                                color, 0.0f, 2.0f, 0.0f, 0.5f);
                        break;
                    case STAR:
                        particles.spawn(ParticleSystem.STAR, x, y, z,
                                0.05f, 0.05f, 0.05f,
                                0.8f, width * 0.4f, 0.0f,
                                color, 0.0f, 1.5f, 0.0f, 0.3f);
                        break;
                    case SPEED:
                        particles.spawn(ParticleSystem.GLOW, x, y, z,
                                (float)(dx * 2), (float)(dy * 2), (float)(dz * 2),
                                0.4f, width * 0.8f, 0.0f,
                                color, 0.0f, 3.0f, 0.2f, 0.7f);
                        break;
                    case DOODLE:
                        if (i % 3 == 0) {
                            particles.spawn(ParticleSystem.GLOW, x, y, z,
                                    (float)(Math.random() - 0.5) * 0.2f,
                                    (float)(Math.random() - 0.5) * 0.2f,
                                    (float)(Math.random() - 0.5) * 0.2f,
                                    0.5f, width * 0.5f, 0.0f,
                                    color, 0.0f, 2.0f, 0.1f, 0.4f);
                        }
                        break;
                    case COMET:
                        particles.spawn(ParticleSystem.GLOW, x, y, z,
                                (float)(-dx * 3), (float)(-dy * 3), (float)(-dz * 3),
                                0.7f, width * 1.0f, 0.0f,
                                color, 0.0f, 4.0f, 0.5f, 0.8f);
                        break;
                    case LIGHTNING:
                        if (i % 2 == 0) {
                            particles.spawn(ParticleSystem.GLOW, x, y, z,
                                    (float)(Math.random() - 0.5) * 0.4f,
                                    (float)(Math.random() - 0.5) * 0.4f,
                                    (float)(Math.random() - 0.5) * 0.4f,
                                    0.3f, width * 0.7f, 0.0f,
                                    color, 0.0f, 5.0f, 0.3f, 0.9f);
                        }
                        break;
                }
            }
        }

        // Render particles
        Vfx.setCamera(new vip.radium.render.Camera());
        ((vip.radium.render.Camera) Vfx.camera()).captureModelView();
        RenderState.begin();
        particles.render(0, 0, 0);
        RenderState.end();

        // Trim history
        double cutoff = now - maxLen;
        while (!history.isEmpty() && history.get(0).time < cutoff) {
            history.remove(0);
        }
    };

    private void record(EntityLivingBase e, float partialTicks) {
        double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * partialTicks;
        double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * partialTicks;
        double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * partialTicks;
        history.add(new Point(x, y, z, (float)GameClock.seconds()));
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
        history.clear();
        particles.clear();
    }

    @Override
    public void onDisable() {
        history.clear();
        particles.clear();
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

    private static class Point {
        final double x, y, z;
        final float time;

        Point(double x, double y, double z, float time) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.time = time;
        }
    }
}
