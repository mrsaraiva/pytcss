package org.msaraiva.pytcss.color;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing TCSS color values into java.awt.Color objects.
 *
 * Supports:
 * - Hex colors: #RGB, #RGBA, #RRGGBB, #RRGGBBAA
 * - RGB/RGBA functions: rgb(r,g,b), rgba(r,g,b,a)
 * - HSL/HSLA functions: hsl(h,s%,l%), hsla(h,s%,l%,a)
 * - Named colors: via NamedColors registry
 */
public class ColorUtil {
    // RGB/RGBA patterns - allow spaces around commas
    private static final Pattern RGB_PATTERN = Pattern.compile(
        "rgb\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern RGBA_PATTERN = Pattern.compile(
        "rgba\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*([\\d.]+)\\s*\\)",
        Pattern.CASE_INSENSITIVE
    );

    // HSL/HSLA patterns - require % for saturation and lightness
    private static final Pattern HSL_PATTERN = Pattern.compile(
        "hsl\\s*\\(\\s*([\\d.]+)\\s*,\\s*([\\d.]+)%\\s*,\\s*([\\d.]+)%\\s*\\)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern HSLA_PATTERN = Pattern.compile(
        "hsla\\s*\\(\\s*([\\d.]+)\\s*,\\s*([\\d.]+)%\\s*,\\s*([\\d.]+)%\\s*,\\s*([\\d.]+)\\s*\\)",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Parse any TCSS color string into a Color object.
     * Auto-detects format (hex, rgb, hsl, keyword).
     *
     * @param colorString Color string (e.g., "#ff0000", "rgb(255,0,0)", "red")
     * @return Color object or null if parsing fails
     */
    @Nullable
    public static Color parse(@NotNull String colorString) {
        String trimmed = colorString.trim();

        // Try hex color
        if (trimmed.startsWith("#")) {
            try {
                return parseHex(trimmed);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        // Try RGB/RGBA
        if (trimmed.toLowerCase().startsWith("rgb")) {
            Color color = parseRgba(trimmed);
            if (color != null) return color;
            return parseRgb(trimmed);
        }

        // Try HSL/HSLA
        if (trimmed.toLowerCase().startsWith("hsl")) {
            Color color = parseHsla(trimmed);
            if (color != null) return color;
            return parseHsl(trimmed);
        }

        // Try named color
        return NamedColors.getColorByName(trimmed);
    }

    /**
     * Parse hex color string.
     * Supports: #RGB, #RGBA, #RRGGBB, #RRGGBBAA
     *
     * @param hex Hex color string (must start with #)
     * @return Color object
     * @throws IllegalArgumentException if format is invalid
     */
    @NotNull
    public static Color parseHex(@NotNull String hex) throws IllegalArgumentException {
        if (!hex.startsWith("#")) {
            throw new IllegalArgumentException("Hex color must start with #");
        }

        String hexDigits = hex.substring(1);
        int length = hexDigits.length();

        // Validate hex characters
        if (!hexDigits.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException("Invalid hex digits: " + hex);
        }

        int r, g, b, a;

        switch (length) {
            case 3: // #RGB -> #RRGGBB
                r = Integer.parseInt(hexDigits.substring(0, 1), 16);
                g = Integer.parseInt(hexDigits.substring(1, 2), 16);
                b = Integer.parseInt(hexDigits.substring(2, 3), 16);
                // Expand: F -> FF (15 -> 255)
                r = r * 17;
                g = g * 17;
                b = b * 17;
                return new Color(r, g, b);

            case 4: // #RGBA -> #RRGGBBAA
                r = Integer.parseInt(hexDigits.substring(0, 1), 16);
                g = Integer.parseInt(hexDigits.substring(1, 2), 16);
                b = Integer.parseInt(hexDigits.substring(2, 3), 16);
                a = Integer.parseInt(hexDigits.substring(3, 4), 16);
                // Expand: F -> FF (15 -> 255)
                r = r * 17;
                g = g * 17;
                b = b * 17;
                a = a * 17;
                return new Color(r, g, b, a);

            case 6: // #RRGGBB
                r = Integer.parseInt(hexDigits.substring(0, 2), 16);
                g = Integer.parseInt(hexDigits.substring(2, 4), 16);
                b = Integer.parseInt(hexDigits.substring(4, 6), 16);
                return new Color(r, g, b);

            case 8: // #RRGGBBAA
                r = Integer.parseInt(hexDigits.substring(0, 2), 16);
                g = Integer.parseInt(hexDigits.substring(2, 4), 16);
                b = Integer.parseInt(hexDigits.substring(4, 6), 16);
                a = Integer.parseInt(hexDigits.substring(6, 8), 16);
                return new Color(r, g, b, a);

            default:
                throw new IllegalArgumentException("Invalid hex color length: " + hex);
        }
    }

    /**
     * Parse RGB color function.
     * Format: rgb(r, g, b) where r,g,b are 0-255
     *
     * @param rgb RGB string
     * @return Color object or null if format is invalid
     */
    @Nullable
    public static Color parseRgb(@NotNull String rgb) {
        Matcher matcher = RGB_PATTERN.matcher(rgb);
        if (!matcher.matches()) {
            return null;
        }

        try {
            int r = Integer.parseInt(matcher.group(1));
            int g = Integer.parseInt(matcher.group(2));
            int b = Integer.parseInt(matcher.group(3));

            // Validate range
            if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
                return null;
            }

            return new Color(r, g, b);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse RGBA color function.
     * Format: rgba(r, g, b, a) where r,g,b are 0-255, a is 0.0-1.0
     *
     * @param rgba RGBA string
     * @return Color object or null if format is invalid
     */
    @Nullable
    public static Color parseRgba(@NotNull String rgba) {
        Matcher matcher = RGBA_PATTERN.matcher(rgba);
        if (!matcher.matches()) {
            return null;
        }

        try {
            int r = Integer.parseInt(matcher.group(1));
            int g = Integer.parseInt(matcher.group(2));
            int b = Integer.parseInt(matcher.group(3));
            float alpha = Float.parseFloat(matcher.group(4));

            // Validate range
            if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
                return null;
            }
            if (alpha < 0.0f || alpha > 1.0f) {
                return null;
            }

            // Convert alpha from 0.0-1.0 to 0-255
            int a = Math.round(alpha * 255);

            return new Color(r, g, b, a);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse HSL color function.
     * Format: hsl(h, s%, l%) where h is 0-360, s and l are 0-100%
     *
     * @param hsl HSL string
     * @return Color object or null if format is invalid
     */
    @Nullable
    public static Color parseHsl(@NotNull String hsl) {
        Matcher matcher = HSL_PATTERN.matcher(hsl);
        if (!matcher.matches()) {
            return null;
        }

        try {
            float h = Float.parseFloat(matcher.group(1));
            float s = Float.parseFloat(matcher.group(2));
            float l = Float.parseFloat(matcher.group(3));

            // Validate range
            if (h < 0 || h > 360 || s < 0 || s > 100 || l < 0 || l > 100) {
                return null;
            }

            // Normalize to 0-1 range
            h = h / 360.0f;
            s = s / 100.0f;
            l = l / 100.0f;

            return hslToRgb(h, s, l, 1.0f);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse HSLA color function.
     * Format: hsla(h, s%, l%, a) where h is 0-360, s and l are 0-100%, a is 0.0-1.0
     *
     * @param hsla HSLA string
     * @return Color object or null if format is invalid
     */
    @Nullable
    public static Color parseHsla(@NotNull String hsla) {
        Matcher matcher = HSLA_PATTERN.matcher(hsla);
        if (!matcher.matches()) {
            return null;
        }

        try {
            float h = Float.parseFloat(matcher.group(1));
            float s = Float.parseFloat(matcher.group(2));
            float l = Float.parseFloat(matcher.group(3));
            float alpha = Float.parseFloat(matcher.group(4));

            // Validate range
            if (h < 0 || h > 360 || s < 0 || s > 100 || l < 0 || l > 100) {
                return null;
            }
            if (alpha < 0.0f || alpha > 1.0f) {
                return null;
            }

            // Normalize to 0-1 range
            h = h / 360.0f;
            s = s / 100.0f;
            l = l / 100.0f;

            return hslToRgb(h, s, l, alpha);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convert HSL values to RGB color.
     * Standard HSL to RGB conversion algorithm.
     *
     * @param h Hue (0-1)
     * @param s Saturation (0-1)
     * @param l Lightness (0-1)
     * @param alpha Alpha (0-1)
     * @return Color object
     */
    @NotNull
    private static Color hslToRgb(float h, float s, float l, float alpha) {
        float r, g, b;

        if (s == 0) {
            // Achromatic (gray)
            r = g = b = l;
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1.0f / 3.0f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1.0f / 3.0f);
        }

        // Convert from 0-1 to 0-255
        int ri = Math.round(r * 255);
        int gi = Math.round(g * 255);
        int bi = Math.round(b * 255);
        int ai = Math.round(alpha * 255);

        return new Color(ri, gi, bi, ai);
    }

    /**
     * Helper function for HSL to RGB conversion.
     *
     * @param p Temporary value
     * @param q Temporary value
     * @param t Temporary hue value
     * @return RGB component (0-1)
     */
    private static float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1.0f / 6.0f) return p + (q - p) * 6 * t;
        if (t < 1.0f / 2.0f) return q;
        if (t < 2.0f / 3.0f) return p + (q - p) * (2.0f / 3.0f - t) * 6;
        return p;
    }
}
