package vip.radium.render;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL11;

public final class Camera {

    public double rightX;
    public double rightY;
    public double rightZ;
    public double upX;
    public double upY;
    public double upZ;
    public double forwardX;
    public double forwardY;
    public double forwardZ;
    public double posX;
    public double posY;
    public double posZ;
    public double projectionScaleY;

    public void captureModelView() {
        FloatBuffer matrix = org.lwjgl.BufferUtils.createFloatBuffer(16);
        matrix.clear();
        GL11.glGetFloat(2982, matrix);
        read(matrix);
    }

    public void captureWorld() {
        try {
            Field mv = net.minecraft.client.renderer.ActiveRenderInfo.class.getDeclaredField("MODELVIEW");
            Field pr = net.minecraft.client.renderer.ActiveRenderInfo.class.getDeclaredField("PROJECTION");
            mv.setAccessible(true);
            pr.setAccessible(true);
            FloatBuffer mvBuf = (FloatBuffer) mv.get(null);
            FloatBuffer prBuf = (FloatBuffer) pr.get(null);
            if (mvBuf != null && prBuf != null) {
                read(mvBuf);
                projectionScaleY = prBuf.get(5);
                return;
            }
        } catch (Exception ignored) {
        }

        captureModelView();
        FloatBuffer matrix = org.lwjgl.BufferUtils.createFloatBuffer(16);
        matrix.clear();
        GL11.glGetFloat(2983, matrix);
        projectionScaleY = matrix.get(5);
    }

    private void read(java.nio.FloatBuffer m) {
        float m0 = m.get(0);
        float m1 = m.get(1);
        float m2 = m.get(2);
        float m4 = m.get(4);
        float m5 = m.get(5);
        float m6 = m.get(6);
        float m8 = m.get(8);
        float m9 = m.get(9);
        float m10 = m.get(10);
        float m12 = m.get(12);
        float m13 = m.get(13);
        float m14 = m.get(14);
        double sx = Math.sqrt(m0 * m0 + m4 * m4 + m8 * m8);
        double sy = Math.sqrt(m1 * m1 + m5 * m5 + m9 * m9);
        double sz = Math.sqrt(m2 * m2 + m6 * m6 + m10 * m10);
        if (!(sx < 1.0E-9) && !(sy < 1.0E-9) && !(sz < 1.0E-9)) {
            this.rightX = m0 / sx;
            this.rightY = m4 / sx;
            this.rightZ = m8 / sx;
            this.upX = m1 / sy;
            this.upY = m5 / sy;
            this.upZ = m9 / sy;
            this.forwardX = -m2 / sz;
            this.forwardY = -m6 / sz;
            this.forwardZ = -m10 / sz;
            double s2 = sx * sx;
            this.posX = -(m0 * m12 + m1 * m13 + m2 * m14) / s2;
            this.posY = -(m4 * m12 + m5 * m13 + m6 * m14) / s2;
            this.posZ = -(m8 * m12 + m9 * m13 + m10 * m14) / s2;
        }
    }
}
