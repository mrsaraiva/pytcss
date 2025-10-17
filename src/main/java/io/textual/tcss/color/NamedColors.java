package io.textual.tcss.color;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry of all TCSS named color keywords.
 * Extracted from TextMate grammar (tcss.tmLanguage.json lines 31-46).
 *
 * Includes:
 * - 16 ANSI colors (ansi_black, ansi_red, ansi_bright_*, etc.)
 * - 17 W3C standard colors (aqua, black, blue, etc.)
 * - 140+ W3C extended colors (aliceblue, antiquewhite, etc.)
 * - Special: transparent
 *
 * Note: "auto" is a special keyword handled separately (not a color value).
 */
public class NamedColors {
    private static final Map<String, Color> COLORS = new HashMap<>();

    static {
        // ANSI Colors (16 colors)
        // Dark variants
        COLORS.put("ansi_black", new Color(0, 0, 0));
        COLORS.put("ansi_red", new Color(170, 0, 0));
        COLORS.put("ansi_green", new Color(0, 170, 0));
        COLORS.put("ansi_yellow", new Color(170, 85, 0));
        COLORS.put("ansi_blue", new Color(0, 0, 170));
        COLORS.put("ansi_magenta", new Color(170, 0, 170));
        COLORS.put("ansi_cyan", new Color(0, 170, 170));
        COLORS.put("ansi_white", new Color(170, 170, 170));

        // Bright variants
        COLORS.put("ansi_bright_black", new Color(85, 85, 85));
        COLORS.put("ansi_bright_red", new Color(255, 85, 85));
        COLORS.put("ansi_bright_green", new Color(85, 255, 85));
        COLORS.put("ansi_bright_yellow", new Color(255, 255, 85));
        COLORS.put("ansi_bright_blue", new Color(85, 85, 255));
        COLORS.put("ansi_bright_magenta", new Color(255, 85, 255));
        COLORS.put("ansi_bright_cyan", new Color(85, 255, 255));
        COLORS.put("ansi_bright_white", new Color(255, 255, 255));

        // W3C Standard Colors (17 colors)
        COLORS.put("aqua", new Color(0, 255, 255));
        COLORS.put("black", new Color(0, 0, 0));
        COLORS.put("blue", new Color(0, 0, 255));
        COLORS.put("fuchsia", new Color(255, 0, 255));
        COLORS.put("gray", new Color(128, 128, 128));
        COLORS.put("green", new Color(0, 128, 0));
        COLORS.put("lime", new Color(0, 255, 0));
        COLORS.put("maroon", new Color(128, 0, 0));
        COLORS.put("navy", new Color(0, 0, 128));
        COLORS.put("olive", new Color(128, 128, 0));
        COLORS.put("orange", new Color(255, 165, 0));
        COLORS.put("purple", new Color(128, 0, 128));
        COLORS.put("red", new Color(255, 0, 0));
        COLORS.put("silver", new Color(192, 192, 192));
        COLORS.put("teal", new Color(0, 128, 128));
        COLORS.put("white", new Color(255, 255, 255));
        COLORS.put("yellow", new Color(255, 255, 0));

        // W3C Extended Colors (140+ colors)
        COLORS.put("aliceblue", new Color(240, 248, 255));
        COLORS.put("antiquewhite", new Color(250, 235, 215));
        COLORS.put("aquamarine", new Color(127, 255, 212));
        COLORS.put("azure", new Color(240, 255, 255));
        COLORS.put("beige", new Color(245, 245, 220));
        COLORS.put("bisque", new Color(255, 228, 196));
        COLORS.put("blanchedalmond", new Color(255, 235, 205));
        COLORS.put("blueviolet", new Color(138, 43, 226));
        COLORS.put("brown", new Color(165, 42, 42));
        COLORS.put("burlywood", new Color(222, 184, 135));
        COLORS.put("cadetblue", new Color(95, 158, 160));
        COLORS.put("chartreuse", new Color(127, 255, 0));
        COLORS.put("chocolate", new Color(210, 105, 30));
        COLORS.put("coral", new Color(255, 127, 80));
        COLORS.put("cornflowerblue", new Color(100, 149, 237));
        COLORS.put("cornsilk", new Color(255, 248, 220));
        COLORS.put("crimson", new Color(220, 20, 60));
        COLORS.put("cyan", new Color(0, 255, 255));
        COLORS.put("darkblue", new Color(0, 0, 139));
        COLORS.put("darkcyan", new Color(0, 139, 139));
        COLORS.put("darkgoldenrod", new Color(184, 134, 11));
        COLORS.put("darkgray", new Color(169, 169, 169));
        COLORS.put("darkgreen", new Color(0, 100, 0));
        COLORS.put("darkgrey", new Color(169, 169, 169));
        COLORS.put("darkkhaki", new Color(189, 183, 107));
        COLORS.put("darkmagenta", new Color(139, 0, 139));
        COLORS.put("darkolivegreen", new Color(85, 107, 47));
        COLORS.put("darkorange", new Color(255, 140, 0));
        COLORS.put("darkorchid", new Color(153, 50, 204));
        COLORS.put("darkred", new Color(139, 0, 0));
        COLORS.put("darksalmon", new Color(233, 150, 122));
        COLORS.put("darkseagreen", new Color(143, 188, 143));
        COLORS.put("darkslateblue", new Color(72, 61, 139));
        COLORS.put("darkslategray", new Color(47, 79, 79));
        COLORS.put("darkslategrey", new Color(47, 79, 79));
        COLORS.put("darkturquoise", new Color(0, 206, 209));
        COLORS.put("darkviolet", new Color(148, 0, 211));
        COLORS.put("deeppink", new Color(255, 20, 147));
        COLORS.put("deepskyblue", new Color(0, 191, 255));
        COLORS.put("dimgray", new Color(105, 105, 105));
        COLORS.put("dimgrey", new Color(105, 105, 105));
        COLORS.put("dodgerblue", new Color(30, 144, 255));
        COLORS.put("firebrick", new Color(178, 34, 34));
        COLORS.put("floralwhite", new Color(255, 250, 240));
        COLORS.put("forestgreen", new Color(34, 139, 34));
        COLORS.put("gainsboro", new Color(220, 220, 220));
        COLORS.put("ghostwhite", new Color(248, 248, 255));
        COLORS.put("gold", new Color(255, 215, 0));
        COLORS.put("goldenrod", new Color(218, 165, 32));
        COLORS.put("greenyellow", new Color(173, 255, 47));
        COLORS.put("grey", new Color(128, 128, 128));
        COLORS.put("honeydew", new Color(240, 255, 240));
        COLORS.put("hotpink", new Color(255, 105, 180));
        COLORS.put("indianred", new Color(205, 92, 92));
        COLORS.put("indigo", new Color(75, 0, 130));
        COLORS.put("ivory", new Color(255, 255, 240));
        COLORS.put("khaki", new Color(240, 230, 140));
        COLORS.put("lavender", new Color(230, 230, 250));
        COLORS.put("lavenderblush", new Color(255, 240, 245));
        COLORS.put("lawngreen", new Color(124, 252, 0));
        COLORS.put("lemonchiffon", new Color(255, 250, 205));
        COLORS.put("lightblue", new Color(173, 216, 230));
        COLORS.put("lightcoral", new Color(240, 128, 128));
        COLORS.put("lightcyan", new Color(224, 255, 255));
        COLORS.put("lightgoldenrodyellow", new Color(250, 250, 210));
        COLORS.put("lightgray", new Color(211, 211, 211));
        COLORS.put("lightgreen", new Color(144, 238, 144));
        COLORS.put("lightgrey", new Color(211, 211, 211));
        COLORS.put("lightpink", new Color(255, 182, 193));
        COLORS.put("lightsalmon", new Color(255, 160, 122));
        COLORS.put("lightseagreen", new Color(32, 178, 170));
        COLORS.put("lightskyblue", new Color(135, 206, 250));
        COLORS.put("lightslategray", new Color(119, 136, 153));
        COLORS.put("lightslategrey", new Color(119, 136, 153));
        COLORS.put("lightsteelblue", new Color(176, 196, 222));
        COLORS.put("lightyellow", new Color(255, 255, 224));
        COLORS.put("limegreen", new Color(50, 205, 50));
        COLORS.put("linen", new Color(250, 240, 230));
        COLORS.put("magenta", new Color(255, 0, 255));
        COLORS.put("mediumaquamarine", new Color(102, 205, 170));
        COLORS.put("mediumblue", new Color(0, 0, 205));
        COLORS.put("mediumorchid", new Color(186, 85, 211));
        COLORS.put("mediumpurple", new Color(147, 112, 219));
        COLORS.put("mediumseagreen", new Color(60, 179, 113));
        COLORS.put("mediumslateblue", new Color(123, 104, 238));
        COLORS.put("mediumspringgreen", new Color(0, 250, 154));
        COLORS.put("mediumturquoise", new Color(72, 209, 204));
        COLORS.put("mediumvioletred", new Color(199, 21, 133));
        COLORS.put("midnightblue", new Color(25, 25, 112));
        COLORS.put("mintcream", new Color(245, 255, 250));
        COLORS.put("mistyrose", new Color(255, 228, 225));
        COLORS.put("moccasin", new Color(255, 228, 181));
        COLORS.put("navajowhite", new Color(255, 222, 173));
        COLORS.put("oldlace", new Color(253, 245, 230));
        COLORS.put("olivedrab", new Color(107, 142, 35));
        COLORS.put("orangered", new Color(255, 69, 0));
        COLORS.put("orchid", new Color(218, 112, 214));
        COLORS.put("palegoldenrod", new Color(238, 232, 170));
        COLORS.put("palegreen", new Color(152, 251, 152));
        COLORS.put("paleturquoise", new Color(175, 238, 238));
        COLORS.put("palevioletred", new Color(219, 112, 147));
        COLORS.put("papayawhip", new Color(255, 239, 213));
        COLORS.put("peachpuff", new Color(255, 218, 185));
        COLORS.put("peru", new Color(205, 133, 63));
        COLORS.put("pink", new Color(255, 192, 203));
        COLORS.put("plum", new Color(221, 160, 221));
        COLORS.put("powderblue", new Color(176, 224, 230));
        COLORS.put("rebeccapurple", new Color(102, 51, 153));
        COLORS.put("rosybrown", new Color(188, 143, 143));
        COLORS.put("royalblue", new Color(65, 105, 225));
        COLORS.put("saddlebrown", new Color(139, 69, 19));
        COLORS.put("salmon", new Color(250, 128, 114));
        COLORS.put("sandybrown", new Color(244, 164, 96));
        COLORS.put("seagreen", new Color(46, 139, 87));
        COLORS.put("seashell", new Color(255, 245, 238));
        COLORS.put("sienna", new Color(160, 82, 45));
        COLORS.put("skyblue", new Color(135, 206, 235));
        COLORS.put("slateblue", new Color(106, 90, 205));
        COLORS.put("slategray", new Color(112, 128, 144));
        COLORS.put("slategrey", new Color(112, 128, 144));
        COLORS.put("snow", new Color(255, 250, 250));
        COLORS.put("springgreen", new Color(0, 255, 127));
        COLORS.put("steelblue", new Color(70, 130, 180));
        COLORS.put("tan", new Color(210, 180, 140));
        COLORS.put("thistle", new Color(216, 191, 216));
        COLORS.put("tomato", new Color(255, 99, 71));
        COLORS.put("transparent", new Color(0, 0, 0, 0));
        COLORS.put("turquoise", new Color(64, 224, 208));
        COLORS.put("violet", new Color(238, 130, 238));
        COLORS.put("wheat", new Color(245, 222, 179));
        COLORS.put("whitesmoke", new Color(245, 245, 245));
        COLORS.put("yellowgreen", new Color(154, 205, 50));
    }

    /**
     * Get color by name (case-insensitive).
     *
     * @param name Color name (e.g., "red", "ansi_bright_blue", "aliceblue")
     * @return Color object or null if not found
     */
    @Nullable
    public static Color getColorByName(@NotNull String name) {
        return COLORS.get(name.toLowerCase());
    }

    /**
     * Check if a name is a valid color keyword.
     *
     * @param name Color name to check
     * @return true if valid color keyword, false otherwise
     */
    public static boolean isNamedColor(@NotNull String name) {
        return COLORS.containsKey(name.toLowerCase());
    }

    /**
     * Get all supported color names.
     *
     * @return Set of all color keyword names
     */
    @NotNull
    public static Set<String> getAllColorNames() {
        return COLORS.keySet();
    }

    /**
     * Check if a color is an ANSI color.
     *
     * @param name Color name
     * @return true if ANSI color, false otherwise
     */
    public static boolean isAnsiColor(@NotNull String name) {
        return name.toLowerCase().startsWith("ansi_");
    }
}
