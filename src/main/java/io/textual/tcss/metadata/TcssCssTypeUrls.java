package io.textual.tcss.metadata;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps CSS type names to their documentation URLs on the Textual website.
 * <p>
 * These types are documented in https://textual.textualize.io/css_types/
 */
public final class TcssCssTypeUrls {
    private static final Map<String, String> TYPE_TO_URL;

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("color", "https://textual.textualize.io/css_types/color/");
        map.put("scalar", "https://textual.textualize.io/css_types/scalar/");
        map.put("border", "https://textual.textualize.io/css_types/border/");
        map.put("hatch", "https://textual.textualize.io/css_types/hatch/");
        map.put("number", "https://textual.textualize.io/css_types/number/");
        map.put("integer", "https://textual.textualize.io/css_types/integer/");
        map.put("percentage", "https://textual.textualize.io/css_types/percentage/");
        map.put("keyline", "https://textual.textualize.io/css_types/keyline/");
        map.put("name", "https://textual.textualize.io/css_types/name/");
        map.put("overflow", "https://textual.textualize.io/css_types/overflow/");
        map.put("position", "https://textual.textualize.io/css_types/position/");
        map.put("text_align", "https://textual.textualize.io/css_types/text_align/");
        map.put("text_style", "https://textual.textualize.io/css_types/text_style/");
        map.put("horizontal", "https://textual.textualize.io/css_types/horizontal/");
        map.put("vertical", "https://textual.textualize.io/css_types/vertical/");
        TYPE_TO_URL = Collections.unmodifiableMap(map);
    }

    private TcssCssTypeUrls() {
        // Utility class
    }

    /**
     * Gets the documentation URL for a given CSS type name.
     *
     * @param cssType the CSS type name (e.g., "color", "scalar", "border")
     * @return the documentation URL, or null if not found
     */
    @Nullable
    public static String getTypeUrl(@Nullable String cssType) {
        if (cssType == null) {
            return null;
        }
        return TYPE_TO_URL.get(cssType.toLowerCase(Locale.US));
    }
}
