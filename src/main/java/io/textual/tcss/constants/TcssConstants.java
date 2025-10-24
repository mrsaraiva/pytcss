package io.textual.tcss.constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Central registry for TCSS validation constants, mirroring Textual's constants.py.
 *
 * <p>This class provides:
 * <ul>
 *   <li>Valid pseudo-class selectors (19 total)</li>
 *   <li>Valid enum values for properties (display, layout, overflow, etc.)</li>
 *   <li>Fuzzy matching utility for helpful error suggestions</li>
 * </ul>
 *
 * <p>Reference: temp_lab_repos/textual/src/textual/css/constants.py
 * <p>Synchronized with Textual v0.47.0 (2025-01-23)
 */
public final class TcssConstants {

    // ===== PSEUDO-CLASSES =====

    /**
     * Valid pseudo-class selectors (19 total).
     * <p>Reference: Textual CSS pseudo-classes
     */
    public static final Set<String> VALID_PSEUDO_CLASSES = Set.of(
        "hover", "focus", "active", "disabled",
        "light", "dark", "blur", "can-focus",
        "has-children", "first-child", "last-child",
        "odd-child", "even-child", "only-child",
        "focus-within", "inline", "inline-block",
        "vertical-scroll", "horizontal-scroll"
    );

    // ===== DISPLAY & VISIBILITY =====

    /**
     * Valid values for visibility property.
     * <p>Reference: constants.py line 8
     */
    public static final Set<String> VALID_VISIBILITY = Set.of("visible", "hidden");

    /**
     * Valid values for display property.
     * <p>Reference: constants.py line 9
     */
    public static final Set<String> VALID_DISPLAY = Set.of("block", "grid", "hidden", "none");

    // ===== LAYOUT =====

    /**
     * Valid values for layout property.
     * <p>Reference: constants.py line 32
     */
    public static final Set<String> VALID_LAYOUT = Set.of("vertical", "horizontal", "grid", "stream");

    /**
     * Valid values for box-sizing property.
     * <p>Reference: constants.py line 34
     */
    public static final Set<String> VALID_BOX_SIZING = Set.of("border-box", "content-box");

    /**
     * Valid values for position property.
     * <p>Reference: constants.py line 38
     */
    public static final Set<String> VALID_POSITION = Set.of("relative", "absolute");

    // ===== BORDERS & EDGES =====

    /**
     * Valid border styles.
     * <p>Reference: constants.py lines 10-30
     */
    public static final Set<String> VALID_BORDER = Set.of(
        "ascii", "blank", "dashed", "double", "heavy", "hidden", "hkey",
        "inner", "none", "outer", "panel", "round", "solid", "tall",
        "tab", "thick", "block", "vkey", "wide"
    );

    /**
     * Valid edge positions for dock and split properties.
     * <p>Reference: constants.py line 31
     */
    public static final Set<String> VALID_EDGE = Set.of("top", "right", "bottom", "left", "none");

    // ===== OVERFLOW =====

    /**
     * Valid values for overflow, overflow-x, and overflow-y properties.
     * <p>Reference: constants.py line 35
     */
    public static final Set<String> VALID_OVERFLOW = Set.of("scroll", "hidden", "auto");

    // ===== ALIGNMENT =====

    /**
     * Valid horizontal alignment values.
     * <p>Reference: constants.py line 36
     */
    public static final Set<String> VALID_ALIGN_HORIZONTAL = Set.of("left", "center", "right");

    /**
     * Valid vertical alignment values.
     * <p>Reference: constants.py line 37
     */
    public static final Set<String> VALID_ALIGN_VERTICAL = Set.of("top", "middle", "bottom");

    /**
     * Valid text alignment values.
     * <p>Reference: constants.py lines 39-46
     */
    public static final Set<String> VALID_TEXT_ALIGN = Set.of(
        "start", "end", "left", "right", "center", "justify"
    );

    // ===== SCROLLBAR =====

    /**
     * Valid scrollbar gutter values.
     * <p>Reference: constants.py line 47
     */
    public static final Set<String> VALID_SCROLLBAR_GUTTER = Set.of("auto", "stable");

    /**
     * Valid scrollbar visibility values.
     * <p>Reference: constants.py line 93
     */
    public static final Set<String> VALID_SCROLLBAR_VISIBILITY = Set.of("visible", "hidden");

    // ===== TEXT STYLING =====

    /**
     * Valid text style flags.
     * <p>Reference: constants.py lines 48-64
     */
    public static final Set<String> VALID_STYLE_FLAGS = Set.of(
        "b", "blink", "bold", "dim", "i", "italic", "none", "not",
        "o", "overline", "reverse", "strike", "u", "underline", "uu"
    );

    /**
     * Valid text wrap values.
     * <p>Reference: constants.py line 90
     */
    public static final Set<String> VALID_TEXT_WRAP = Set.of("wrap", "nowrap");

    /**
     * Valid text overflow values.
     * <p>Reference: constants.py line 91
     */
    public static final Set<String> VALID_TEXT_OVERFLOW = Set.of("clip", "fold", "ellipsis");

    // ===== OVERLAY & CONSTRAINTS =====

    /**
     * Valid overlay values.
     * <p>Reference: constants.py line 86
     */
    public static final Set<String> VALID_OVERLAY = Set.of("none", "screen");

    /**
     * Valid constraint values.
     * <p>Reference: constants.py line 87
     */
    public static final Set<String> VALID_CONSTRAIN = Set.of("inflect", "inside", "none");

    // ===== KEYLINE & HATCH =====

    /**
     * Valid keyline values.
     * <p>Reference: constants.py line 88
     */
    public static final Set<String> VALID_KEYLINE = Set.of("none", "thin", "heavy", "double");

    /**
     * Valid hatch pattern values.
     * <p>Reference: constants.py line 89
     */
    public static final Set<String> VALID_HATCH = Set.of("left", "right", "cross", "vertical", "horizontal");

    // ===== EXPAND =====

    /**
     * Valid expand values.
     * <p>Reference: constants.py line 92
     */
    public static final Set<String> VALID_EXPAND = Set.of("greedy", "optimal");

    // ===== PROPERTY-TO-ENUM MAPPING =====

    /**
     * Maps property names to their valid enum value sets.
     * Used for enum value validation and completion.
     */
    private static final Map<String, Set<String>> PROPERTY_ENUMS = Map.ofEntries(
        Map.entry("display", VALID_DISPLAY),
        Map.entry("visibility", VALID_VISIBILITY),
        Map.entry("layout", VALID_LAYOUT),
        Map.entry("overflow", VALID_OVERFLOW),
        Map.entry("overflow-x", VALID_OVERFLOW),
        Map.entry("overflow-y", VALID_OVERFLOW),
        Map.entry("border", VALID_BORDER),
        Map.entry("border-style", VALID_BORDER),
        Map.entry("box-sizing", VALID_BOX_SIZING),
        Map.entry("position", VALID_POSITION),
        Map.entry("text-align", VALID_TEXT_ALIGN),
        Map.entry("align-horizontal", VALID_ALIGN_HORIZONTAL),
        Map.entry("content-align-horizontal", VALID_ALIGN_HORIZONTAL),
        Map.entry("align-vertical", VALID_ALIGN_VERTICAL),
        Map.entry("content-align-vertical", VALID_ALIGN_VERTICAL),
        Map.entry("dock", VALID_EDGE),
        Map.entry("split", VALID_EDGE),
        Map.entry("scrollbar-gutter", VALID_SCROLLBAR_GUTTER),
        Map.entry("scrollbar-visibility", VALID_SCROLLBAR_VISIBILITY),
        Map.entry("text-wrap", VALID_TEXT_WRAP),
        Map.entry("text-overflow", VALID_TEXT_OVERFLOW),
        Map.entry("overlay", VALID_OVERLAY),
        Map.entry("constrain", VALID_CONSTRAIN),
        Map.entry("constrain-x", VALID_CONSTRAIN),
        Map.entry("constrain-y", VALID_CONSTRAIN),
        Map.entry("expand", VALID_EXPAND)
    );

    // ===== FUZZY MATCHING UTILITY =====

    private static final int MAX_EDIT_DISTANCE = 2;

    private TcssConstants() {
        // Utility class - prevent instantiation
    }

    /**
     * Get valid enum values for a property, if it has enumerated values.
     *
     * @param propertyName The property name (case-insensitive)
     * @return Set of valid enum values, or null if property doesn't have enum values
     */
    @Nullable
    public static Set<String> getValidEnumValues(@NotNull String propertyName) {
        return PROPERTY_ENUMS.get(propertyName.toLowerCase(Locale.US));
    }

    /**
     * Find closest matching suggestion from a collection of valid values using Levenshtein distance.
     *
     * <p>Returns a suggestion only if the edit distance is <= 2, which catches common typos
     * like transposed letters, missing letters, or single character substitutions.
     *
     * @param input        The input string to match
     * @param validValues  Collection of valid values
     * @return The closest match if distance <= 2, otherwise null
     */
    @Nullable
    public static String getSuggestion(@NotNull String input, @NotNull Collection<String> validValues) {
        String normalizedInput = input.toLowerCase(Locale.US);
        String bestMatch = null;
        int bestDistance = Integer.MAX_VALUE;

        for (String valid : validValues) {
            String normalizedValid = valid.toLowerCase(Locale.US);
            int distance = levenshteinDistance(normalizedInput, normalizedValid);

            if (distance < bestDistance) {
                bestDistance = distance;
                bestMatch = valid;
            }
        }

        return (bestDistance <= MAX_EDIT_DISTANCE) ? bestMatch : null;
    }

    /**
     * Calculate Levenshtein distance between two strings.
     * <p>Simple implementation without external dependencies.
     *
     * @param s1 First string
     * @param s2 Second string
     * @return Edit distance (minimum number of single-character edits to transform s1 into s2)
     */
    private static int levenshteinDistance(@NotNull String s1, @NotNull String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        // Early exit for identical strings
        if (s1.equals(s2)) return 0;

        // Handle empty strings
        if (len1 == 0) return len2;
        if (len2 == 0) return len1;

        // Dynamic programming matrix
        int[][] dp = new int[len1 + 1][len2 + 1];

        // Initialize first row and column
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        // Fill the matrix
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1,      // deletion
                             dp[i][j - 1] + 1),      // insertion
                    dp[i - 1][j - 1] + cost          // substitution
                );
            }
        }

        return dp[len1][len2];
    }
}
