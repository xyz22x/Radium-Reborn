package vip.radium.module.impl.visuals2;

import io.github.nevalackin.homoBus.annotations.EventLink;
import io.github.nevalackin.homoBus.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
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

    public final EnumProperty<WeaponMode> modeProperty = new EnumProperty<>("Weapon", WeaponMode.KARAMBIT);
    public final DoubleProperty sizeProperty = new DoubleProperty("Size", 1.0, 0.1, 3.0, 0.1);
    public final DoubleProperty rotationSpeedProperty = new DoubleProperty("Spin Speed", 1.0, 0.0, 10.0, 0.1);

    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP self = Wrapper.getPlayer();
        if (mc.theWorld == null || self == null) return;

        ItemStack held = self.getCurrentEquippedItem();
        boolean isKnife = held != null && held.getItem() instanceof ItemSword;
        if (!isKnife) return;

        WeaponMode mode = modeProperty.getValue();
        if (mode != WeaponMode.BUTTERFLY) return;

        double now = System.currentTimeMillis() / 1000.0;
        float spin = (float)((now * rotationSpeedProperty.getValue()) % 1.0) * (float)Math.PI * 2.0f;

        GL11.glPushMatrix();
        GL11.glTranslated(0.6, -0.4, -0.7);
        GL11.glScaled(sizeProperty.getValue(), sizeProperty.getValue(), sizeProperty.getValue());

        drawKnife(spin);

        GL11.glPopMatrix();
    };

    private void drawKnife(float spin) {
        // Handle (gray)
        GL11.glColor4f(0.6f, 0.6f, 0.6f, 1.0f);
        GL11.glPushMatrix();
        GL11.glRotated(spin * 57.2957795, 0, 1, 0);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(-0.05, -0.02, 0);
        GL11.glVertex3d(0.05, -0.02, 0);
        GL11.glVertex3d(0.05, 0.06, 0);
        GL11.glVertex3d(-0.05, 0.06, 0);
        GL11.glEnd();
        // Handle sides
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(-0.05, -0.02, 0);
        GL11.glVertex3d(-0.05, 0.06, 0);
        GL11.glVertex3d(-0.03, 0.06, 0.02);
        GL11.glVertex3d(-0.03, -0.02, 0.02);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(0.05, -0.02, 0);
        GL11.glVertex3d(0.05, 0.06, 0);
        GL11.glVertex3d(0.03, 0.06, 0.02);
        GL11.glVertex3d(0.03, -0.02, 0.02);
        GL11.glEnd();
        GL11.glPopMatrix();

        // Blade (silver)
        GL11.glColor4f(0.9f, 0.95f, 1.0f, 0.9f);
        GL11.glPushMatrix();
        GL11.glRotated(spin * 57.2957795, 0, 1, 0);
        GL11.glTranslated(0, 0.06, 0);
        GL11.glRotated(-45, 1, 0, 0);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(-0.03, 0, 0);
        GL11.glVertex3d(0.03, 0, 0);
        GL11.glVertex3d(0.02, 0.8, 0);
        GL11.glVertex3d(-0.02, 0.8, 0);
        GL11.glEnd();
        GL11.glPopMatrix();

        // Blade edge (highlight)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.6f);
        GL11.glPushMatrix();
        GL11.glRotated(spin * 57.2957795, 0, 1, 0);
        GL11.glTranslated(0.031, 0.06, 0.01);
        GL11.glRotated(-45, 1, 0, 0);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(-0.01, 0, 0);
        GL11.glVertex3d(0.01, 0, 0);
        GL11.glVertex3d(0.01, 0.8, 0);
        GL11.glVertex3d(-0.01, 0.8, 0);
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
