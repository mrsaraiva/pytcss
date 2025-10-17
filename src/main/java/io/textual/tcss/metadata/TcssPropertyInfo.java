package io.textual.tcss.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final String documentationUrl;

    public TcssPropertyInfo(@NotNull String name,
                            @NotNull String description,
                            @NotNull ValueType valueType,
                            @Nullable String documentationUrl) {
        this.name = name;
        this.description = description;
        this.valueType = valueType;
        this.documentationUrl = documentationUrl;
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
        return documentationUrl;
    }
}
