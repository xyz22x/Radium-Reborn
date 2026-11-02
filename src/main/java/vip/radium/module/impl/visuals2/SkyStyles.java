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
        float spin = (float)(now % 1.0) * 360.0f;

        Entity view = mc.getRenderViewEntity();
        if (view == null) return;

        GL11.glPushMatrix();
        GL11.glTranslated(view.posX, view.posY, view.posZ);
        GL11.glRotated(spin, 0, 1, 0);

        int segments = 32;
        double radius = 20.0;
        double height = 10.0;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);

        int[] colors = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF};

        for (int ring = 0; ring < 3; ring++) {
            double r = radius * (0.5 + ring * 0.25);
            double h = height * (ring - 1);

            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (int i = 0; i <= segments; i++) {
                double angle = (i / (double)segments) * Math.PI * 2 + now * 0.5;
                int c = colors[(i + ring * 2) % colors.length];
                float a = (float)(120 * intensityProperty.getValue()) / 255.0f;
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

        GL11.glBegin(GL11.GL_QUADS);
        for (int i = 0; i < 12; i++) {
            double angle = (i / 12.0) * Math.PI * 2 + now * 0.2;
            double r = 40 + Math.sin(now + i) * 10;
            double h = 15 + Math.cos(now * 0.5 + i) * 8;

            int c = nebulaColor(i, now);
            float a = (float)(0.15 + 0.1 * Math.sin(now + i)) * intensityProperty.getValue().floatValue();

            GL11.glColor4f(
                ((c >> 16 & 0xFF) / 255.0f),
                ((c >> 8 & 0xFF) / 255.0f),
                ((c & 0xFF) / 255.0f),
                a
            );

            GL11.glVertex3d(Math.cos(angle) * r, h, Math.sin(angle) * r);
            GL11.glVertex3d(Math.cos(angle + 0.3) * r, h, Math.sin(angle + 0.3) * r);
            GL11.glVertex3d(Math.cos(angle + 0.3) * (r + 8), h + 4, Math.sin(angle + 0.3) * (r + 8));
            GL11.glVertex3d(Math.cos(angle) * (r + 8), h + 4, Math.sin(angle) * (r + 8));
        }
        GL11.glEnd();

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private int nebulaColor(int i, double now) {
        int[] colors = {0xFF4400FF, 0xFF0044FF, 0xFF0088FF, 0xFF44FF88, 0xFF88FF44, 0xFFFFFF};
        return colors[(i + (int)(now * 2)) % colors.length];
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
        NEBULA2("Nebula 2"), VOID("Void"), SUNSET("Sunset"),
        MIDNIGHT("Midnight"), SOLAR("Solar"), GALACTIC("Galactic"),
        PRISM("Prism"), OCEAN("Ocean"), FOREST("Forest"),
        CYBER("Cyber"), CRYSTAL("Crystal"), ECLIPSE("Eclipse");

        public final String label;
        SkyMode(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }
}
