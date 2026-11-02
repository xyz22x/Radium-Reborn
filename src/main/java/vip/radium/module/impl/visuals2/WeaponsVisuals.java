package vip.radium.module.impl.visuals2;

import io.github.nevalackin.homoBus.annotations.EventLink;
import io.github.nevalackin.homoBus.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import vip.radium.event.impl.render.Render3DEvent;
import vip.radium.module.Module;
import vip.radium.module.ModuleCategory;
import vip.radium.module.ModuleInfo;
import vip.radium.property.impl.DoubleProperty;
import vip.radium.property.impl.EnumProperty;
import vip.radium.utils.Wrapper;

@ModuleInfo(label = "Weapons Visuals", category = ModuleCategory.VISUALS2)
public final class WeaponsVisuals extends Module {

    public final EnumProperty<WeaponMode> modeProperty = new EnumProperty<>("Weapon", WeaponMode.BUTTERFLY);
    public final DoubleProperty sizeProperty = new DoubleProperty("Size", 1.0, 0.1, 3.0, 0.1);
    public final DoubleProperty rotationSpeedProperty = new DoubleProperty("Spin Speed", 1.0, 0.0, 20.0, 0.1);

    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP self = Wrapper.getPlayer();
        if (mc.theWorld == null || self == null) return;

        WeaponMode mode = modeProperty.getValue();
        if (mode != WeaponMode.BUTTERFLY) return;

        double now = System.currentTimeMillis() / 1000.0;
        float spin = (float)((now * rotationSpeedProperty.getValue()) % 1.0) * 360.0f;

        boolean thirdPerson = mc.gameSettings.thirdPersonView != 0;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);

        Entity view = mc.getRenderViewEntity();
        double vx = view.posX;
        double vy = view.posY + view.getEyeHeight();
        double vz = view.posZ;

        if (thirdPerson) {
            GL11.glTranslated(vx, vy, vz);
            GL11.glRotated(-self.rotationYaw, 0, 1, 0);
            GL11.glRotated(self.rotationPitch, 1, 0, 0);
            GL11.glTranslated(0.8, -0.2, -0.6);
        } else {
            GL11.glTranslated(0.6, -0.4, -0.7);
        }

        GL11.glScaled(sizeProperty.getValue(), sizeProperty.getValue(), sizeProperty.getValue());

        drawKnife(spin);

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    };

    private void drawKnife(float spin) {
        // Handle (cinza)
        GL11.glColor4f(0.7f, 0.7f, 0.7f, 1.0f);
        GL11.glPushMatrix();
        GL11.glRotated(spin, 0, 1, 0);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(-0.04, -0.03, 0);
        GL11.glVertex3d(0.04, -0.03, 0);
        GL11.glVertex3d(0.04, 0.08, 0);
        GL11.glVertex3d(-0.04, 0.08, 0);
        GL11.glEnd();

        // Laterais do cabo
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(-0.04, -0.03, 0);
        GL11.glVertex3d(-0.04, 0.08, 0);
        GL11.glVertex3d(-0.02, 0.08, 0.03);
        GL11.glVertex3d(-0.02, -0.03, 0.03);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(0.04, -0.03, 0);
        GL11.glVertex3d(0.04, 0.08, 0);
        GL11.glVertex3d(0.02, 0.08, 0.03);
        GL11.glVertex3d(0.02, -0.03, 0.03);
        GL11.glEnd();
        GL11.glPopMatrix();

        // Lâmina (prateada)
        GL11.glColor4f(0.85f, 0.9f, 1.0f, 0.95f);
        GL11.glPushMatrix();
        GL11.glRotated(spin * 0.5, 0, 1, 0);
        GL11.glTranslated(0, 0.08, 0);
        GL11.glRotated(-50, 1, 0, 0);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(-0.025, 0, 0);
        GL11.glVertex3d(0.025, 0, 0);
        GL11.glVertex3d(0.015, 1.0, 0);
        GL11.glVertex3d(-0.015, 1.0, 0);
        GL11.glEnd();
        GL11.glPopMatrix();

        // Borda da lâmina (destaque branco)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
        GL11.glPushMatrix();
        GL11.glRotated(spin * 0.5, 0, 1, 0);
        GL11.glTranslated(0.026, 0.08, 0.01);
        GL11.glRotated(-50, 1, 0, 0);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(-0.008, 0, 0);
        GL11.glVertex3d(0.008, 0, 0);
        GL11.glVertex3d(0.008, 1.0, 0);
        GL11.glVertex3d(-0.008, 1.0, 0);
        GL11.glEnd();
        GL11.glPopMatrix();
    }

    public WeaponsVisuals() {
        setSuffixListener(modeProperty);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    public enum WeaponMode {
        KARAMBIT("Karambit"), KATANA("Katana"), KUNAI("Kunai"),
        TACTICAL_BAYONET("Tactical Bayonet"), VERDANT_LANCE("Verdant Lance"),
        SCYTHE("Scythe"), INFERNO_SCYTHE("Inferno Scythe"), DAGGER("Dagger"),
        BUTTERFLY("Butterfly"), SAI("Sai"), SABRE("Sabre");

        public final String label;
        WeaponMode(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }
}
