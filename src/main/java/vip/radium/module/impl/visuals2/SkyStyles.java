package vip.radium.module.impl.visuals2;

import io.github.nevalackin.homoBus.annotations.EventLink;
import io.github.nevalackin.homoBus.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import vip.radium.event.impl.render.Render3DEvent;
import vip.radium.module.Module;
import vip.radium.module.ModuleCategory;
import vip.radium.module.ModuleInfo;
import vip.radium.property.impl.DoubleProperty;
import vip.radium.property.impl.EnumProperty;
import vip.radium.utils.Wrapper;

@ModuleInfo(label = "Sky Styles", category = ModuleCategory.VISUALS2)
public final class SkyStyles extends Module {

    public final EnumProperty<SkyMode> modeProperty = new EnumProperty<>("Style", SkyMode.KALEIDO);
    public final DoubleProperty speedProperty = new DoubleProperty("Speed", 1.0, 0.0, 5.0, 0.1);
    public final DoubleProperty intensityProperty = new DoubleProperty("Intensity", 1.0, 0.0, 3.0, 0.1);

    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return;

        SkyMode mode = modeProperty.getValue();
        if (mode == SkyMode.KALEIDO) {
            drawKaleido(mc, event.getPartialTicks());
        } else if (mode == SkyMode.NEBULA) {
            drawNebula(mc, event.getPartialTicks());
        }
    };

    private void drawKaleido(Minecraft mc, float pt) {
        double now = System.currentTimeMillis() / 1000.0 * speedProperty.getValue();
        float spin = (float)((now % 1.0) * 360.0);

        Entity view = mc.getRenderViewEntity();
        if (view == null) return;

        GL11.glPushMatrix();
        GL11.glTranslated(view.posX, view.posY, view.posZ);
        GL11.glRotated(spin, 0, 1, 0);

        int segments = 48;
        double radius = 60.0;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);

        int[] colors = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF};

        // Dome superior (hemisfério)
        for (int ring = 0; ring < 4; ring++) {
            double r = radius * (0.3 + ring * 0.2);
            double h = ring * 6.0;

            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (int i = 0; i <= segments; i++) {
                double angle = (i / (double)segments) * Math.PI * 2 + now * 0.3;
                int c = colors[(i + ring * 2) % colors.length];
                float a = (float)(60 * intensityProperty.getValue()) / 255.0f;
                GL11.glColor4f(
                    ((c >> 16 & 0xFF) / 255.0f),
                    ((c >> 8 & 0xFF) / 255.0f),
                    ((c & 0xFF) / 255.0f),
                    a
                );
                GL11.glVertex3d(Math.cos(angle) * r, h, Math.sin(angle) * r);
            }
            GL11.glEnd();
        }

        // Linhas verticais conectando os anéis
        GL11.glBegin(GL11.GL_LINES);
        for (int i = 0; i <= segments; i += 4) {
            double angle = (i / (double)segments) * Math.PI * 2;
            int c = colors[i % colors.length];
            float a = (float)(40 * intensityProperty.getValue()) / 255.0f;
            GL11.glColor4f(
                ((c >> 16 & 0xFF) / 255.0f),
                ((c >> 8 & 0xFF) / 255.0f),
                ((c & 0xFF) / 255.0f),
                a
            );
            for (int ring = 0; ring < 3; ring++) {
                double r1 = radius * (0.3 + ring * 0.2);
                double r2 = radius * (0.3 + (ring + 1) * 0.2);
                double h1 = ring * 6.0;
                double h2 = (ring + 1) * 6.0;
                GL11.glVertex3d(Math.cos(angle) * r1, h1, Math.sin(angle) * r1);
                GL11.glVertex3d(Math.cos(angle) * r2, h2, Math.sin(angle) * r2);
            }
        }
        GL11.glEnd();

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void drawNebula(Minecraft mc, float pt) {
        double now = System.currentTimeMillis() / 1000.0 * speedProperty.getValue();

        Entity view = mc.getRenderViewEntity();
        if (view == null) return;

        GL11.glPushMatrix();
        GL11.glTranslated(view.posX, view.posY, view.posZ);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDepthMask(false);

        int[] colors = {0xFF4400FF, 0xFF0044FF, 0xFF0088FF, 0xFF44FF88, 0xFF88FF44, 0xFFFFFF};

        GL11.glBegin(GL11.GL_QUADS);
        for (int i = 0; i < 16; i++) {
            double angle = (i / 16.0) * Math.PI * 2 + now * 0.15;
            double r = 50 + Math.sin(now + i * 0.7) * 15;
            double h = 20 + Math.cos(now * 0.4 + i) * 10;
            double size = 12 + Math.sin(now * 0.6 + i) * 6;

            int c = colors[(i + (int)(now)) % colors.length];
            float a = (float)(0.12 + 0.08 * Math.sin(now + i)) * intensityProperty.getValue().floatValue();

            GL11.glColor4f(
                ((c >> 16 & 0xFF) / 255.0f),
                ((c >> 8 & 0xFF) / 255.0f),
                ((c & 0xFF) / 255.0f),
                a
            );

            GL11.glVertex3d(Math.cos(angle) * r, h, Math.sin(angle) * r);
            GL11.glVertex3d(Math.cos(angle + 0.4) * r, h, Math.sin(angle + 0.4) * r);
            GL11.glVertex3d(Math.cos(angle + 0.4) * (r + size), h + size * 0.5, Math.sin(angle + 0.4) * (r + size));
            GL11.glVertex3d(Math.cos(angle) * (r + size), h + size * 0.5, Math.sin(angle) * (r + size));
        }
        GL11.glEnd();

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    public SkyStyles() {
        setSuffixListener(modeProperty);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    public enum SkyMode {
        KALEIDO("Kaleido"), NEBULA("Nebula"), WATCHER("Watcher"),
        VOID("Void"), SUNSET("Sunset"), MIDNIGHT("Midnight"),
        SOLAR("Solar"), GALACTIC("Galactic"), PRISM("Prism"),
        OCEAN("Ocean"), FOREST("Forest"), CYBER("Cyber"),
        CRYSTAL("Crystal"), ECLIPSE("Eclipse");

        public final String label;
        SkyMode(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }
}
