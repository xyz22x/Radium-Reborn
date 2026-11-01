package vip.radium.module.impl.visuals;

import io.github.nevalackin.homoBus.annotations.EventLink;
import io.github.nevalackin.homoBus.Listener;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;
import vip.radium.event.impl.render.Render3DEvent;
import vip.radium.module.Module;
import vip.radium.module.ModuleCategory;
import vip.radium.module.ModuleInfo;
import vip.radium.property.Property;
import vip.radium.property.impl.DoubleProperty;
import vip.radium.property.impl.EnumProperty;
import vip.radium.utils.Wrapper;
import vip.radium.utils.render.Colors;
import vip.radium.utils.render.RenderingUtils;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(label = "Hats", category = ModuleCategory.VISUALS)
public final class Hats extends Module {

    private final EnumProperty<HatMode> modeProperty = new EnumProperty<>("Mode", HatMode.CONE);
    private final DoubleProperty heightProperty = new DoubleProperty("Height", 0.2, 0.0, 2.0, 0.01);
    private final Property<Boolean> followRotationProperty = new Property<>("Follow Rotation", false);
    private final Property<Boolean> firstPersonProperty = new Property<>("First Person", true);
    private final Property<Boolean> damageColorProperty = new Property<>("Damage Color", true);
    private final Property<Boolean> friendsProperty = new Property<>("Friends Only", false);
    private final DoubleProperty friendDistanceProperty = new DoubleProperty("Friend Distance", 64.0, 8.0, 512.0, 1.0);
    private final Property<Boolean> syncColorsProperty = new Property<>("Sync Colors", true);
    private final Property<Integer> color1Property = new Property<>("Color 1", Colors.BLUE);
    private final Property<Integer> color2Property = new Property<>("Color 2", Colors.BLUE);
    private final DoubleProperty spinSpeedProperty = new DoubleProperty("Spin Speed", 1.0, -10.0, 10.0, 0.1);

    private final List<EntityLivingBase> renderCache = new ArrayList<>();

    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        final float partialTicks = event.getPartialTicks();
        final boolean firstPerson = Wrapper.isInFirstPerson() || firstPersonProperty.getValue();

        if (!firstPerson)
            return;

        renderCache.clear();
        for (EntityLivingBase entity : Wrapper.getLivingEntities(e -> true)) {
            if (!(entity instanceof net.minecraft.entity.player.EntityPlayer))
                continue;

            if (friendsProperty.getValue()) {
                if (!isFriend(entity))
                    continue;
                if (entity.getDistanceToEntity(Wrapper.getPlayer()) > friendDistanceProperty.getValue())
                    continue;
            }

            renderCache.add(entity);
        }

        if (renderCache.isEmpty())
            return;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        final double viewerX = RenderManager.viewerPosX;
        final double viewerY = RenderManager.viewerPosY;
        final double viewerZ = RenderManager.viewerPosZ;

        for (EntityLivingBase entity : renderCache) {
            final double x = RenderingUtils.interpolate(entity.prevPosX, entity.posX, partialTicks) - viewerX;
            final double y = RenderingUtils.interpolate(entity.prevPosY, entity.posY, partialTicks) - viewerY;
            final double z = RenderingUtils.interpolate(entity.prevPosZ, entity.posZ, partialTicks) - viewerZ;

            final boolean hurt = damageColorProperty.getValue() && entity.hurtTime > 0;
            final int color = hurt ? 0xFFFF0000 : getCurrentColor(entity);

            GL11.glPushMatrix();
            GL11.glTranslated(x, y + entity.getEyeHeight() + heightProperty.getValue(), z);

            if (followRotationProperty.getValue()) {
                final float yaw = RenderingUtils.interpolate(entity.prevRotationYaw, entity.rotationYaw, partialTicks);
                GL11.glRotated(-yaw, 0.0, 1.0, 0.0);
            }

            drawHat(entity, hurt, color);

            GL11.glPopMatrix();
        }

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    };

    private boolean isFriend(EntityLivingBase entity) {
        return vip.radium.RadiumClient.getInstance().getPlayerManager().isFriend((net.minecraft.entity.player.EntityPlayer) entity);
    }

    private int getCurrentColor(EntityLivingBase entity) {
        final int c1 = color1Property.getValue();
        final int c2 = syncColorsProperty.getValue() ? c1 : color2Property.getValue();

        final double time = (System.currentTimeMillis() / 10000.0) * spinSpeedProperty.getValue() % 1.0;
        final float progress = (float) (Math.sin((float) time * (float) Math.PI * 2.0) * 0.5 + 0.5);

        return interpolateColor(c1, c2, progress);
    }

    private static int interpolateColor(int start, int end, float progress) {
        if (progress > 1.0f)
            progress = 1.0f - (progress % 1.0f);
        final float invert = 1.0f - progress;
        int r = (int) (((start >> 16 & 0xFF) * invert + (end >> 16 & 0xFF) * progress));
        int g = (int) (((start >> 8 & 0xFF) * invert + (end >> 8 & 0xFF) * progress));
        int b = (int) (((start & 0xFF) * invert + (end & 0xFF) * progress));
        int a = (int) (((start >> 24 & 0xFF) * invert + (end >> 24 & 0xFF) * progress));
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    private void drawHat(EntityLivingBase entity, boolean hurt, int color) {
        switch (modeProperty.getValue()) {
            case CONE:
                drawCone(color);
                break;
            case HALO:
                drawHalo(color);
                break;
            case ORBS:
                drawOrbs(entity, color);
                break;
            case FLOWER:
                drawFlower(color);
                break;
            case STAR:
                drawStar(color);
                break;
            case IMAGE:
                drawQuad(color);
                break;
        }
    }

    private void drawCone(int color) {
        final int segments = 32;
        final float radius = 0.3f;
        final float height = 0.4f;

        GL11.glColor4ub(
                (byte) (color >> 16 & 0xFF),
                (byte) (color >> 8 & 0xFF),
                (byte) (color & 0xFF),
                (byte) (color >> 24 & 0xFF));

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex3d(0, height, 0);
        for (int i = 0; i <= segments; i++) {
            final double angle = i * 2.0 * Math.PI / segments;
            GL11.glVertex3d(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
        }
        GL11.glEnd();
    }

    private void drawHalo(int color) {
        final int outerSegments = 64;
        final int innerSegments = 8;
        final float outerRadius = 0.3f;
        final float innerRadius = 0.1f;

        GL11.glColor4ub(
                (byte) (color >> 16 & 0xFF),
                (byte) (color >> 8 & 0xFF),
                (byte) (color & 0xFF),
                (byte) (color >> 24 & 0xFF));

        GL11.glBegin(GL11.GL_QUADS);
        for (int i = 0; i < outerSegments; i++) {
            final double a1 = i * 2.0 * Math.PI / outerSegments;
            final double a2 = (i + 1) * 2.0 * Math.PI / outerSegments;
            for (int j = 0; j < innerSegments; j++) {
                final double t1 = j / (float) innerSegments;
                final double t2 = (j + 1) / (float) innerSegments;

                final double r1 = innerRadius + (outerRadius - innerRadius) * t1;
                final double r2 = innerRadius + (outerRadius - innerRadius) * t2;

                GL11.glVertex3d(Math.cos(a1) * r1, 0, Math.sin(a1) * r1);
                GL11.glVertex3d(Math.cos(a2) * r1, 0, Math.sin(a2) * r1);
                GL11.glVertex3d(Math.cos(a2) * r2, 0, Math.sin(a2) * r2);
                GL11.glVertex3d(Math.cos(a1) * r2, 0, Math.sin(a1) * r2);
            }
        }
        GL11.glEnd();
    }

    private void drawOrbs(EntityLivingBase entity, int color) {
        final int count = 6;
        final float radius = 0.25f;
        final float size = 0.08f;
        final double time = (System.currentTimeMillis() % 1000000L) / 1000.0 * 2.0;

        GL11.glColor4ub(
                (byte) (color >> 16 & 0xFF),
                (byte) (color >> 8 & 0xFF),
                (byte) (color & 0xFF),
                (byte) (color >> 24 & 0xFF));

        GL11.glBegin(GL11.GL_QUADS);
        for (int i = 0; i < count; i++) {
            final double angle = i * 2.0 * Math.PI / count + time;
            final double x = Math.sin(angle) * radius;
            final double z = Math.cos(angle) * radius;

            final float spin = (float) (System.currentTimeMillis() % 360000) * 0.001f;
            final double sx = Math.sin(spin) * size;
            final double sz = Math.cos(spin) * size;

            GL11.glVertex3d(x + sx, size, z + sz);
            GL11.glVertex3d(x - sx, -size, z - sz);
            GL11.glVertex3d(x - sz, -size, z + sx);
            GL11.glVertex3d(x + sz, size, z - sx);
        }
        GL11.glEnd();
    }

    private void drawFlower(int color) {
        final int petals = 8;
        final int segments = 32;
        final float outerRadius = 0.3f;
        final float innerRadius = 0.1f;
        final float sharpness = 0.6f;

        GL11.glColor4ub(
                (byte) (color >> 16 & 0xFF),
                (byte) (color >> 8 & 0xFF),
                (byte) (color & 0xFF),
                (byte) (color >> 24 & 0xFF));

        GL11.glBegin(GL11.GL_QUADS);
        final double spin = getRotationAngle(1.0f);
        for (int i = 0; i < segments; i++) {
            final double a1 = i * 2.0 * Math.PI / segments + spin;
            final double a2 = (i + 1) * 2.0 * Math.PI / segments + spin;

            final double r1 = getFlowerRadius(a1, outerRadius, petals, sharpness);
            final double r2 = getFlowerRadius(a2, outerRadius, petals, sharpness);

            GL11.glVertex3d(Math.cos(a1) * r1, 0, Math.sin(a1) * r1);
            GL11.glVertex3d(Math.cos(a2) * r1, 0, Math.sin(a2) * r1);
            GL11.glVertex3d(Math.cos(a2) * r2, 0, Math.sin(a2) * r2);
            GL11.glVertex3d(Math.cos(a1) * r2, 0, Math.sin(a1) * r2);
        }
        GL11.glEnd();
    }

    private void drawStar(int color) {
        final int points = 6;
        final int segments = 32;
        final float outerRadius = 0.3f;
        final float innerRadius = 0.1f;
        final float sharpness = 0.6f;
        final float exponent = 1.75f;

        GL11.glColor4ub(
                (byte) (color >> 16 & 0xFF),
                (byte) (color >> 8 & 0xFF),
                (byte) (color & 0xFF),
                (byte) (color >> 24 & 0xFF));

        GL11.glBegin(GL11.GL_QUADS);
        final double spin = getRotationAngle(1.0f);
        for (int i = 0; i < segments; i++) {
            final double a1 = i * 2.0 * Math.PI / segments + spin;
            final double a2 = (i + 1) * 2.0 * Math.PI / segments + spin;

            final double r1 = getStarRadius(a1, outerRadius, points, sharpness, exponent);
            final double r2 = getStarRadius(a2, outerRadius, points, sharpness, exponent);

            GL11.glVertex3d(Math.cos(a1) * r1, 0, Math.sin(a1) * r1);
            GL11.glVertex3d(Math.cos(a2) * r1, 0, Math.sin(a2) * r1);
            GL11.glVertex3d(Math.cos(a2) * r2, 0, Math.sin(a2) * r2);
            GL11.glVertex3d(Math.cos(a1) * r2, 0, Math.sin(a1) * r2);
        }
        GL11.glEnd();
    }

    private void drawQuad(int color) {
        final float size = 0.2f;

        GL11.glColor4ub(
                (byte) (color >> 16 & 0xFF),
                (byte) (color >> 8 & 0xFF),
                (byte) (color & 0xFF),
                (byte) (color >> 24 & 0xFF));

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(-size, 0, -size);
        GL11.glVertex3d(size, 0, -size);
        GL11.glVertex3d(size, 0, size);
        GL11.glVertex3d(-size, 0, size);
        GL11.glEnd();
    }

    private static double getRotationAngle(float speed) {
        if (speed == 0.0f)
            return 0.0;
        return (System.currentTimeMillis() % 360000L) * 0.001 * speed;
    }

    private static double getFlowerRadius(double angle, float baseRadius, int points, float sharpness) {
        final float innerRadius = baseRadius * sharpness;
        final float f = (float) (Math.PI / points);
        final float r = Math.abs((float) (angle % (f * 2) - f)) / f;
        return innerRadius + (baseRadius - innerRadius) * (1.0f - r);
    }

    private static double getStarRadius(double angle, float baseRadius, int points, float sharpness, float exponent) {
        final double section = Math.PI * 2.0 / points;
        final double m = (angle % section) / section;
        final double dist = Math.abs(m * 2.0 - 1.0);
        final double linearProgress = 1.0 - dist;
        return baseRadius * (1.0 - sharpness + sharpness * Math.pow(linearProgress, exponent));
    }

    public Hats() {
        setSuffixListener(modeProperty);
    }

    private enum HatMode {
        CONE,
        HALO,
        ORBS,
        FLOWER,
        STAR,
        IMAGE
    }
}
