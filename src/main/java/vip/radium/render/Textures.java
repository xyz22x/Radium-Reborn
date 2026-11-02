package vip.radium.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

public final class Textures {

    public static int WHITE;
    public static int GLOW;
    public static int SPARK;
    public static int FLARE;
    public static int LINE_GLOW;
    public static int LINE_CORE;
    public static int WAVE;
    public static int GROUND_GLOW;
    public static int HEART;
    public static int STAR;
    public static int RUNES;
    public static int STREAKS;
    public static int RAY;
    public static int STREAK;
    public static int SMOKE;
    public static int BROKEN_HEART;
    public static int SPARKLE;
    public static int SKULL;
    public static int FEATHER;
    public static int PETAL;
    public static int LEAF;
    public static int SNOWFLAKE;
    public static int RADIAL_FILL;
    public static int HEX_FILL;
    public static int BUBBLE;
    public static int SNOW_ATLAS;
    public static final int SNOW_SOFT_CELL = 0;
    public static final int SNOW_CRYSTAL_CELL = 1;
    public static final int SNOW_BLUR_CELL = 2;
    public static final int SNOW_CELLS = 3;
    public static int FOLIAGE_ATLAS;
    public static final int FOLIAGE_PETAL = 0;
    public static final int FOLIAGE_LEAF = 1;
    public static final int FOLIAGE_MAPLE = 2;
    public static final int FOLIAGE_CELLS = 3;
    public static int BOKEH_CIRCLE;
    public static int BOKEH_CIRCLE_SOFT;
    public static int BOKEH_HEX;
    public static int BOKEH_HEX_SOFT;
    public static int MIST_ATLAS;
    public static final int MIST_CELLS = 4;
    public static int KILL_X;
    public static int KILL_CROWN;
    public static int STONE;
    public static int VIGNETTE;
    public static final int RUNE_COUNT = 8;
    public static final int NEON_COUNT = 5;
    public static final int[] NEON_BODY = new int[5];
    public static final int[] NEON_CORE = new int[5];
    public static final int[] NEON_GLOW = new int[5];
    public static final float NEON_SYMBOL_FRACTION = 0.64F;

    private static boolean loaded;

    public static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;

        WHITE = upload(makeWhite(16));
        GLOW = upload(makeGlow(64));
        SPARK = upload(makeSpark(32));
        FLARE = upload(makeFlare(32));
        LINE_GLOW = upload(makeLineGlow(64, 8));
        LINE_CORE = upload(makeLineCore(32, 4));
        WAVE = upload(makeWave(64));
        GROUND_GLOW = upload(makeGroundGlow(64));
        HEART = upload(makeHeart(64));
        STAR = upload(makeStar(64));
        RUNES = upload(makeRunes(64));
        STREAKS = upload(makeStreaks(64));
        RAY = upload(makeRay(64));
        STREAK = upload(makeStreak(64));
        SMOKE = upload(makeSmoke(64));
        BROKEN_HEART = upload(makeBrokenHeart(64));
        SPARKLE = upload(makeSparkle(32));
        SKULL = upload(makeSkull(64));
        FEATHER = upload(makeFeather(64));
        PETAL = upload(makePetal(64));
        LEAF = upload(makeLeaf(64));
        SNOWFLAKE = upload(makeSnowflake(64));
        RADIAL_FILL = upload(makeRadialFill(64));
        HEX_FILL = upload(makeHexFill(64));
        BUBBLE = upload(makeBubble(64));
        SNOW_ATLAS = upload(makeSnowAtlas(64, 3));
        FOLIAGE_ATLAS = upload(makeFoliageAtlas(64, 3));
        BOKEH_CIRCLE = upload(makeBokehCircle(64));
        BOKEH_CIRCLE_SOFT = upload(makeBokehCircleSoft(64));
        BOKEH_HEX = upload(makeBokehHex(64));
        BOKEH_HEX_SOFT = upload(makeBokehHexSoft(64));
        MIST_ATLAS = upload(makeMistAtlas(64, 4));
        KILL_X = upload(makeKillX(64));
        KILL_CROWN = upload(makeKillCrown(64));
        STONE = upload(makeStone(64));
        VIGNETTE = upload(makeVignette(256));
    }

    private static BufferedImage makeWhite(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                img.setRGB(x, y, 0xFFFFFFFF);
            }
        }
        return img;
    }

    private static BufferedImage makeGlow(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int cx = size / 2;
        int cy = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - cx;
                double dy = y - cy;
                double d = Math.sqrt(dx * dx + dy * dy) / (size / 2.0);
                if (d > 1.0) d = 1.0;
                int a = (int)(255 * (1.0 - d * d));
                int r = (int)(255 * (1.0 - d));
                img.setRGB(x, y, (a << 24) | (r << 16) | (r << 8) | r);
            }
        }
        return img;
    }

    private static BufferedImage makeSpark(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int cx = size / 2;
        int cy = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - cx;
                double dy = y - cy;
                double d = Math.sqrt(dx * dx + dy * dy) / (size / 2.0);
                if (d > 1.0) continue;
                int a = (int)(255 * Math.pow(1.0 - d, 3));
                img.setRGB(x, y, (a << 24) | 0xFFFFFF);
            }
        }
        return img;
    }

    private static BufferedImage makeFlare(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int cx = size / 2;
        int cy = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - cx;
                double dy = y - cy;
                double d = Math.sqrt(dx * dx + dy * dy) / (size / 2.0);
                if (d > 1.0) d = 1.0;
                int a = (int)(255 * Math.pow(1.0 - d, 4));
                img.setRGB(x, y, (a << 24) | 0xFFFFFF);
            }
        }
        return img;
    }

    private static BufferedImage makeLineGlow(int size, int width) {
        BufferedImage img = new BufferedImage(size, width, BufferedImage.TYPE_INT_ARGB);
        int cy = width / 2;
        for (int y = 0; y < width; y++) {
            for (int x = 0; x < size; x++) {
                double dy = y - cy;
                double d = Math.abs(dy) / (width / 2.0);
                if (d > 1.0) d = 1.0;
                int a = (int)(255 * (1.0 - d));
                img.setRGB(x, y, (a << 24) | 0xFFFFFF);
            }
        }
        return img;
    }

    private static BufferedImage makeLineCore(int size, int width) {
        BufferedImage img = new BufferedImage(size, width, BufferedImage.TYPE_INT_ARGB);
        int cy = width / 2;
        for (int y = 0; y < width; y++) {
            for (int x = 0; x < size; x++) {
                img.setRGB(x, y, 0xFFFFFFFF);
            }
        }
        return img;
    }

    private static BufferedImage makeWave(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int cx = size / 2;
        int cy = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - cx;
                double dy = y - cy;
                double d = Math.sqrt(dx * dx + dy * dy) / (size / 2.0);
                if (d > 1.0) d = 1.0;
                double wave = Math.sin(d * Math.PI * 4) * 0.5 + 0.5;
                int a = (int)(255 * wave * (1.0 - d));
                img.setRGB(x, y, (a << 24) | 0xFFFFFF);
            }
        }
        return img;
    }

    private static BufferedImage makeGroundGlow(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int cx = size / 2;
        int cy = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - cx;
                double dy = y - cy;
                double d = Math.sqrt(dx * dx + dy * dy) / (size / 2.0);
                if (d > 1.0) d = 1.0;
                int a = (int)(150 * (1.0 - d * d));
                img.setRGB(x, y, (a << 24) | 0x00AAFF);
            }
        }
        return img;
    }

    private static BufferedImage makeHeart(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int cx = size / 2;
        int cy = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - cx;
                double dy = y - cy;
                double d = Math.sqrt(dx * dx + dy * dy) / (size / 2.0);
                if (d > 1.0) d = 1.0;
                int a = (int)(255 * (1.0 - d * d));
                img.setRGB(x, y, (a << 24) | 0xFF0000);
            }
        }
        return img;
    }

    private static BufferedImage makeStar(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int cx = size / 2;
        int cy = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - cx;
                double dy = y - cy;
                double d = Math.sqrt(dx * dx + dy * dy) / (size / 2.0);
                if (d > 1.0) d = 1.0;
                int a = (int)(255 * (1.0 - d));
                img.setRGB(x, y, (a << 24) | 0xFFFFFF);
            }
        }
        return img;
    }

    private static BufferedImage makeRunes(int size) {
        return makeGlow(size);
    }

    private static BufferedImage makeStreaks(int size) {
        return makeLineGlow(size, size / 4);
    }

    private static BufferedImage makeRay(int size) {
        return makeLineGlow(size * 2, size / 4);
    }

    private static BufferedImage makeStreak(int size) {
        return makeLineGlow(size, size / 2);
    }

    private static BufferedImage makeSmoke(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int cx = size / 2;
        int cy = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - cx;
                double dy = y - cy;
                double d = Math.sqrt(dx * dx + dy * dy) / (size / 2.0);
                if (d > 1.0) d = 1.0;
                int a = (int)(180 * Math.pow(1.0 - d, 2));
                img.setRGB(x, y, (a << 24) | 0xAAAAAA);
            }
        }
        return img;
    }

    private static BufferedImage makeBrokenHeart(int size) {
        return makeHeart(size);
    }

    private static BufferedImage makeSparkle(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int cx = size / 2;
        int cy = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - cx;
                double dy = y - cy;
                double d = Math.sqrt(dx * dx + dy * dy) / (size / 2.0);
                if (d > 1.0) continue;
                int a = (int)(255 * Math.pow(1.0 - d, 2));
                img.setRGB(x, y, (a << 24) | 0xFFFFFF);
            }
        }
        return img;
    }

    private static BufferedImage makeSkull(int size) {
        return makeHeart(size);
    }

    private static BufferedImage makeFeather(int size) {
        return makeSpark(size);
    }

    private static BufferedImage makePetal(int size) {
        return makeSpark(size);
    }

    private static BufferedImage makeLeaf(int size) {
        return makeSpark(size);
    }

    private static BufferedImage makeSnowflake(int size) {
        return makeStar(size);
    }

    private static BufferedImage makeRadialFill(int size) {
        return makeGlow(size);
    }

    private static BufferedImage makeHexFill(int size) {
        return makeGlow(size);
    }

    private static BufferedImage makeBubble(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int cx = size / 2;
        int cy = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - cx;
                double dy = y - cy;
                double d = Math.sqrt(dx * dx + dy * dy) / (size / 2.0);
                if (d > 1.0) d = 1.0;
                int a = (int)(100 * (1.0 - d * d));
                img.setRGB(x, y, (a << 24) | 0xFFFFFF);
            }
        }
        return img;
    }

    private static BufferedImage makeSnowAtlas(int size, int cells) {
        return makeGlow(size);
    }

    private static BufferedImage makeFoliageAtlas(int size, int cells) {
        return makeGlow(size);
    }

    private static BufferedImage makeBokehCircle(int size) {
        return makeGlow(size);
    }

    private static BufferedImage makeBokehCircleSoft(int size) {
        return makeGlow(size);
    }

    private static BufferedImage makeBokehHex(int size) {
        return makeGlow(size);
    }

    private static BufferedImage makeBokehHexSoft(int size) {
        return makeGlow(size);
    }

    private static BufferedImage makeMistAtlas(int size, int cells) {
        return makeSmoke(size);
    }

    private static BufferedImage makeKillX(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int cx = size / 2;
        int cy = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - cx;
                double dy = y - cy;
                double d = Math.abs(dx) + Math.abs(dy);
                if (d < size * 0.15) {
                    int a = (int)(255 * (1.0 - d / (size * 0.15)));
                    img.setRGB(x, y, (a << 24) | 0xFFFFFF);
                }
            }
        }
        return img;
    }

    private static BufferedImage makeKillCrown(int size) {
        return makeStar(size);
    }

    private static BufferedImage makeStone(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        java.util.Random rnd = new java.util.Random(12345);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int v = rnd.nextInt(256);
                img.setRGB(x, y, (255 << 24) | (v << 16) | (v << 8) | v);
            }
        }
        return img;
    }

    private static BufferedImage makeVignette(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int cx = size / 2;
        int cy = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - cx;
                double dy = y - cy;
                double d = Math.sqrt(dx * dx + dy * dy) / (size / 2.0);
                if (d > 1.0) d = 1.0;
                int a = (int)(100 * d * d);
                img.setRGB(x, y, (a << 24) | 0x000000);
            }
        }
        return img;
    }

    private static int upload(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] pixels = new int[w * h];
        image.getRGB(0, 0, w, h, pixels, 0, w);
        ByteBuffer buf = BufferUtils.createByteBuffer(w * h * 4);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pixel = pixels[y * w + x];
                buf.put((byte)((pixel >> 16) & 0xFF));
                buf.put((byte)((pixel >> 8) & 0xFF));
                buf.put((byte)(pixel & 0xFF));
                buf.put((byte)((pixel >> 24) & 0xFF));
            }
        }
        buf.flip();
        int texID = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        return texID;
    }
}
