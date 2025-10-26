package org.msaraiva.pytcss;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class TcssColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Comment", TcssSyntaxHighlighter.COMMENT),
            new AttributesDescriptor("Variable", TcssSyntaxHighlighter.VARIABLE),
            new AttributesDescriptor("ID Selector", TcssSyntaxHighlighter.ID_SELECTOR),
            new AttributesDescriptor("Class Selector", TcssSyntaxHighlighter.CLASS_SELECTOR),
            new AttributesDescriptor("Pseudo Class", TcssSyntaxHighlighter.PSEUDO_CLASS),
            new AttributesDescriptor("Type Selector", TcssSyntaxHighlighter.TYPE_SELECTOR),
            new AttributesDescriptor("Property Name", TcssSyntaxHighlighter.PROPERTY_NAME),
            new AttributesDescriptor("Number", TcssSyntaxHighlighter.NUMBER),
            new AttributesDescriptor("String", TcssSyntaxHighlighter.STRING),
            new AttributesDescriptor("Braces", TcssSyntaxHighlighter.BRACES),
            new AttributesDescriptor("Semicolon", TcssSyntaxHighlighter.SEMICOLON),
            new AttributesDescriptor("Comma", TcssSyntaxHighlighter.COMMA),
            new AttributesDescriptor("Operator", TcssSyntaxHighlighter.OPERATOR),
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return TcssFileType.INSTANCE.getIcon();
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new TcssSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "/* Textual CSS example */\n" +
                "$primary: blue;\n" +
                "$border: wide $primary;\n\n" +
                "#dialog {\n" +
                "    border: $border;\n" +
                "    align: center middle;\n" +
                "    width: 50%;\n" +
                "    height: 20;\n" +
                "}\n\n" +
                "Button {\n" +
                "    width: 1fr;\n" +
                "    padding: 1 2;\n" +
                "    background: $primary;\n" +
                "    color: white;\n" +
                "}\n\n" +
                "Button:hover {\n" +
                "    background: darkblue;\n" +
                "}\n\n" +
                ".success {\n" +
                "    background: green;\n" +
                "}\n\n" +
                ".error.disabled {\n" +
                "    background: darkred;\n" +
                "    opacity: 0.5;\n" +
                "}\n\n" +
                "#questions {\n" +
                "    border: heavy $primary;\n" +
                "    \n" +
                "    .button {\n" +
                "        width: 1fr;\n" +
                "        \n" +
                "        &.affirmative {\n" +
                "            border: heavy green;\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Textual CSS";
    }
}
