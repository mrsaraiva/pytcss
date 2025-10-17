package io.textual.tcss.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Central catalog for TCSS property metadata used across completion, documentation, and validation.
 */
public final class TcssPropertyCatalog {
    private static final Map<String, TcssPropertyInfo> PROPERTIES;

    static {
        Map<String, TcssPropertyInfo> map = new LinkedHashMap<>();
        register(map, "align", "Align child widgets within their container.", TcssPropertyInfo.ValueType.ENUM);
        register(map, "background", "Background color or gradient for a widget.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "background-tint", "Tint color applied to the background.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "border", "Shorthand border specification (width style color).", TcssPropertyInfo.ValueType.STRING);
        register(map, "border-color", "Color of the widget border.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "border-style", "Style of the border (solid, dashed, none).", TcssPropertyInfo.ValueType.ENUM);
        register(map, "border-subtitle-align", "Alignment of the border subtitle text.", TcssPropertyInfo.ValueType.ENUM);
        register(map, "border-subtitle-background", "Background color of the border subtitle.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "border-subtitle-color", "Text color of the border subtitle.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "border-subtitle-style", "Text style of the border subtitle.", TcssPropertyInfo.ValueType.STRING);
        register(map, "border-title-align", "Alignment of the border title text.", TcssPropertyInfo.ValueType.ENUM);
        register(map, "border-title-background", "Background color of the border title.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "border-title-color", "Text color of the border title.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "border-title-style", "Text style of the border title.", TcssPropertyInfo.ValueType.STRING);
        register(map, "border-width", "Width of the widget border.", TcssPropertyInfo.ValueType.LENGTH);
        register(map, "box-sizing", "How width and height are calculated (border-box or content-box).", TcssPropertyInfo.ValueType.ENUM);
        register(map, "color", "Foreground text color.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "column-span", "Number of columns a grid cell spans.", TcssPropertyInfo.ValueType.NUMBER);
        register(map, "content-align", "Alignment of child content both horizontally and vertically.", TcssPropertyInfo.ValueType.ENUM);
        register(map, "display", "Widget display mode (block, inline, none).", TcssPropertyInfo.ValueType.ENUM);
        register(map, "dock", "Dock widget to a side of its container.", TcssPropertyInfo.ValueType.ENUM);
        register(map, "grid-columns", "Width specification for grid columns.", TcssPropertyInfo.ValueType.LENGTH);
        register(map, "grid-gutter", "Spacing between grid cells.", TcssPropertyInfo.ValueType.LENGTH);
        register(map, "grid-rows", "Height specification for grid rows.", TcssPropertyInfo.ValueType.LENGTH);
        register(map, "grid-size", "Number of columns and rows in the grid layout.", TcssPropertyInfo.ValueType.NUMBER);
        register(map, "hatch", "Hatch pattern for widget background.", TcssPropertyInfo.ValueType.STRING);
        register(map, "height", "Explicit widget height.", TcssPropertyInfo.ValueType.LENGTH);
        register(map, "keyline", "Keyline style for widget borders.", TcssPropertyInfo.ValueType.STRING);
        register(map, "layer", "Layer name for widget placement.", TcssPropertyInfo.ValueType.STRING);
        register(map, "layers", "Layer definitions for container widgets.", TcssPropertyInfo.ValueType.STRING);
        register(map, "layout", "Layout algorithm (grid, horizontal, vertical).", TcssPropertyInfo.ValueType.ENUM);
        register(map, "link-background", "Background color of link text.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "link-background-hover", "Background color of link text on hover.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "link-color", "Color of link text.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "link-color-hover", "Color of link text on hover.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "link-style", "Text style of link text.", TcssPropertyInfo.ValueType.STRING);
        register(map, "link-style-hover", "Text style of link text on hover.", TcssPropertyInfo.ValueType.STRING);
        register(map, "margin", "Margin on all sides of the widget.", TcssPropertyInfo.ValueType.LENGTH);
        register(map, "max-height", "Maximum widget height.", TcssPropertyInfo.ValueType.LENGTH);
        register(map, "max-width", "Maximum widget width.", TcssPropertyInfo.ValueType.LENGTH);
        register(map, "min-height", "Minimum widget height.", TcssPropertyInfo.ValueType.LENGTH);
        register(map, "min-width", "Minimum widget width.", TcssPropertyInfo.ValueType.LENGTH);
        register(map, "offset", "Offset for positioned widgets.", TcssPropertyInfo.ValueType.LENGTH);
        register(map, "opacity", "Widget opacity from 0 to 1.", TcssPropertyInfo.ValueType.NUMBER);
        register(map, "outline", "Outline style for widgets.", TcssPropertyInfo.ValueType.STRING);
        register(map, "overflow", "How content overflow is handled.", TcssPropertyInfo.ValueType.ENUM);
        register(map, "padding", "Padding on all sides of the widget.", TcssPropertyInfo.ValueType.LENGTH);
        register(map, "position", "Positioning scheme for the widget (relative, absolute).", TcssPropertyInfo.ValueType.ENUM);
        register(map, "row-span", "Number of rows a grid cell spans.", TcssPropertyInfo.ValueType.NUMBER);
        register(map, "scrollbar-background", "Background color of scrollbars.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "scrollbar-background-active", "Background color of active scrollbars.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "scrollbar-background-hover", "Background color of scrollbars on hover.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "scrollbar-color", "Color of custom scrollbars.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "scrollbar-color-active", "Color of active scrollbars.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "scrollbar-color-hover", "Color of scrollbars on hover.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "scrollbar-corner-color", "Color of the scrollbar corner.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "scrollbar-gutter", "Gutter space reserved for scrollbars.", TcssPropertyInfo.ValueType.ENUM);
        register(map, "scrollbar-size", "Size (width/height) of scrollbars.", TcssPropertyInfo.ValueType.LENGTH);
        register(map, "scrollbar-visibility", "Visibility mode for scrollbars (auto, visible, hidden).", TcssPropertyInfo.ValueType.ENUM);
        register(map, "text-align", "Horizontal text alignment.", TcssPropertyInfo.ValueType.ENUM);
        register(map, "text-opacity", "Opacity of text from 0 to 1.", TcssPropertyInfo.ValueType.NUMBER);
        register(map, "text-overflow", "How text overflow is handled (ellipsis, clip).", TcssPropertyInfo.ValueType.ENUM);
        register(map, "text-style", "Named text style reference.", TcssPropertyInfo.ValueType.STRING);
        register(map, "text-wrap", "How text wrapping behaves (wrap, nowrap).", TcssPropertyInfo.ValueType.ENUM);
        register(map, "tint", "Tint color applied to the widget.", TcssPropertyInfo.ValueType.COLOR);
        register(map, "visibility", "Widget visibility (visible, hidden, collapse).", TcssPropertyInfo.ValueType.ENUM);
        register(map, "width", "Explicit widget width.", TcssPropertyInfo.ValueType.LENGTH);
        register(map, "z", "Z-order for overlay stacking.", TcssPropertyInfo.ValueType.NUMBER);
        PROPERTIES = Collections.unmodifiableMap(map);
    }

    private TcssPropertyCatalog() {
    }

    private static void register(@NotNull Map<String, TcssPropertyInfo> map,
                                 @NotNull String name,
                                 @NotNull String description,
                                 @NotNull TcssPropertyInfo.ValueType valueType) {
        map.put(name.toLowerCase(Locale.US), new TcssPropertyInfo(name, description, valueType, "https://textual.textualize.io/guide/CSS/"));
    }

    @Nullable
    public static TcssPropertyInfo get(@NotNull String name) {
        return PROPERTIES.get(name.toLowerCase(Locale.US));
    }

    @NotNull
    public static Collection<TcssPropertyInfo> getAll() {
        return PROPERTIES.values();
    }

    @NotNull
    public static List<TcssPropertyInfo> prefixMatch(@NotNull String prefix) {
        String key = prefix.toLowerCase(Locale.US);
        List<TcssPropertyInfo> result = new ArrayList<>();
        for (Map.Entry<String, TcssPropertyInfo> entry : PROPERTIES.entrySet()) {
            if (entry.getKey().startsWith(key)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }
}
