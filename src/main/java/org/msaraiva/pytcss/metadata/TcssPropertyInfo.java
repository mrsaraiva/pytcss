package org.msaraiva.pytcss.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Describes a single TCSS property.
 */
public final class TcssPropertyInfo {
    public enum ValueType {
        COLOR,
        LENGTH,
        NUMBER,
        BOOLEAN,
        STRING,
        ENUM,
        OTHER
    }

    private final String name;
    private final String description;
    private final ValueType valueType;
    private final String propertyDocUrl;
    private final String typeDocUrl;

    public TcssPropertyInfo(@NotNull String name,
                            @NotNull String description,
                            @NotNull ValueType valueType,
                            @Nullable String propertyDocUrl,
                            @Nullable String typeDocUrl) {
        this.name = name;
        this.description = description;
        this.valueType = valueType;
        this.propertyDocUrl = propertyDocUrl;
        this.typeDocUrl = typeDocUrl;
    }

    // Legacy constructor for backward compatibility
    public TcssPropertyInfo(@NotNull String name,
                            @NotNull String description,
                            @NotNull ValueType valueType,
                            @Nullable String documentationUrl) {
        this(name, description, valueType, documentationUrl, null);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @NotNull
    public ValueType getValueType() {
        return valueType;
    }

    @Nullable
    public String getDocumentationUrl() {
        return propertyDocUrl;
    }

    @Nullable
    public String getPropertyDocUrl() {
        return propertyDocUrl;
    }

    @Nullable
    public String getTypeDocUrl() {
        return typeDocUrl;
    }

    /**
     * Determines the CSS type name for this property based on its ValueType and name.
     * This is used to link to the appropriate css_types documentation.
     *
     * @return the CSS type name (e.g., "color", "scalar"), or null if no specific type applies
     */
    @Nullable
    public static String getCssTypeName(@NotNull String propertyName, @NotNull ValueType valueType) {
        String lowerName = propertyName.toLowerCase(Locale.US);

        switch (valueType) {
            case COLOR:
                return "color";

            case LENGTH:
                return "scalar";

            case NUMBER:
                return "number";

            case STRING:
                // Check for specific string-based types
                if (lowerName.equals("border") || lowerName.startsWith("border-") && !lowerName.contains("title") && !lowerName.contains("subtitle") && !lowerName.contains("color") && !lowerName.contains("background") && !lowerName.contains("align")) {
                    return "border";
                }
                if (lowerName.equals("outline") || lowerName.startsWith("outline-") && !lowerName.contains("color")) {
                    return "border";  // outline uses same type docs as border
                }
                if (lowerName.equals("hatch")) {
                    return "hatch";
                }
                if (lowerName.equals("keyline")) {
                    return "keyline";
                }
                if (lowerName.equals("text-align")) {
                    return "text_align";
                }
                if (lowerName.contains("text-style") || lowerName.contains("link-style") || lowerName.contains("border-title-style") || lowerName.contains("border-subtitle-style")) {
                    return "text_style";
                }
                if (lowerName.equals("layer") || lowerName.equals("layers")) {
                    return "name";
                }
                return null;

            case ENUM:
                // Check for enum types with specific documentation
                if (lowerName.equals("overflow") || lowerName.equals("overflow-x") || lowerName.equals("overflow-y")) {
                    return "overflow";
                }
                if (lowerName.equals("position")) {
                    return "position";
                }
                if (lowerName.equals("align-horizontal") || lowerName.equals("content-align-horizontal")) {
                    return "horizontal";
                }
                if (lowerName.equals("align-vertical") || lowerName.equals("content-align-vertical")) {
                    return "vertical";
                }
                return null;

            default:
                return null;
        }
    }
}
