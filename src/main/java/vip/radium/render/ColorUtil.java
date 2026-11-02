package vip.radium.render;

public final class ColorUtil {

    private ColorUtil() {
    }

    public static int alpha(int c) {
        return c >>> 24;
    }

    public static int red(int c) {
        return c >> 16 & 0xFF;
    }

    public static int green(int c) {
        return c >> 8 & 0xFF;
    }

    public static int blue(int c) {
        return c & 0xFF;
    }

    public static int argb(int a, int r, int g, int b) {
        return clamp255(a) << 24 | clamp255(r) << 16 | clamp255(g) << 8 | clamp255(b);
    }

    public static int argb(float a, float r, float g, float b) {
        return argb(Math.round(a * 255.0F), Math.round(r * 255.0F), Math.round(g * 255.0F), Math.round(b * 255.0F));
    }

    public static int withAlpha(int c, int alpha) {
        return clamp255(alpha) << 24 | c & 16777215;
    }

    public static int withAlpha(int c, float alpha) {
        return withAlpha(c, Math.round(alpha * 255.0F));
    }

    public static int mulAlpha(int c, float factor) {
        if (factor >= 1.0F) {
            return c;
        } else {
            return factor <= 0.0F ? c & 16777215 : withAlpha(c, (int)(alpha(c) * factor + 0.5F));
        }
    }

    public static int lerp(int c1, int c2, float t) {
        if (t <= 0.0F) {
            return c1;
        } else if (t >= 1.0F) {
            return c2;
        } else {
            int a = (int)(alpha(c1) + (alpha(c2) - alpha(c1)) * t + 0.5F);
            int r = (int)(red(c1) + (red(c2) - red(c1)) * t + 0.5F);
            int g = (int)(green(c1) + (green(c2) - green(c1)) * t + 0.5F);
            int b = (int)(blue(c1) + (blue(c2) - blue(c1)) * t + 0.5F);
            return argb(a, r, g, b);
        }
    }

    public static int lerpOklab(int c1, int c2, float t) {
        if (t <= 0.0F) {
            return c1;
        } else if (t >= 1.0F) {
            return c2;
        } else {
            double[] a = toOklab(c1);
            double[] b = toOklab(c2);
            double l = a[0] + (b[0] - a[0]) * t;
            double ca = a[1] + (b[1] - a[1]) * t;
            double cb = a[2] + (b[2] - a[2]) * t;
            int alpha = (int)(alpha(c1) + (alpha(c2) - alpha(c1)) * t + 0.5F);
            return fromOklab(l, ca, cb, alpha);
        }
    }

    private static double[] toOklab(int c) {
        double r = toLinear(red(c) / 255.0);
        double g = toLinear(green(c) / 255.0);
        double b = toLinear(blue(c) / 255.0);
        double l = 0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b;
        double m = 0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b;
        double s = 0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b;
        l = cbrt(l);
        m = cbrt(m);
        s = cbrt(s);
        return new double[]{
            0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s,
            1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s,
            0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s
        };
    }

    private static int fromOklab(double l, double ca, double cb, int alpha) {
        double l_ = l + 0.3963377774 * ca + 0.2158037573 * cb;
        double m_ = l - 0.1055613458 * ca - 0.0638541728 * cb;
        double s_ = l - 0.0894841775 * ca - 1.2914855480 * cb;
        double r = toSrgb(l_ * l_ * l_);
        double g = toSrgb(m_ * m_ * m_);
        double b = toSrgb(s_ * s_ * s_);
        return argb(alpha, (int)(r * 255.0), (int)(g * 255.0), (int)(b * 255.0));
    }

    private static double toLinear(double c) {
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static double toSrgb(double c) {
        return c <= 0.0031308 ? 12.92 * c : 1.055 * Math.pow(c, 1.0 / 2.4) - 0.055;
    }

    private static double cbrt(double a) {
        return Math.signum(a) * Math.pow(Math.abs(a), 1.0 / 3.0);
    }

    private static int clamp255(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }
}
