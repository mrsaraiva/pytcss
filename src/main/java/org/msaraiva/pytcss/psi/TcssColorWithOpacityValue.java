package org.msaraiva.pytcss.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.msaraiva.pytcss.TcssTokenTypes;
import org.msaraiva.pytcss.color.ColorFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Locale;

/**
 * PSI element representing a color value with an opacity suffix (e.g. "red 50%").
 */
public class TcssColorWithOpacityValue extends ASTWrapperPsiElement implements TcssColorValue {
    public TcssColorWithOpacityValue(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    public TcssColorValue getBaseColor() {
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof TcssColorValue && !(child instanceof TcssColorWithOpacityValue)) {
                return (TcssColorValue) child;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    @Nullable
    public PsiElement getOpacityToken() {
        PsiElement child = getLastChild();
        while (child != null) {
            if (child.getNode().getElementType() == TcssTokenTypes.NUMBER) {
                return child;
            }
            child = child.getPrevSibling();
        }
        return null;
    }

    @Nullable
    public OpacityContext createOpacityContext() {
        TcssColorValue baseColor = getBaseColor();
        PsiElement opacity = getOpacityToken();
        if (baseColor == null || opacity == null) {
            return null;
        }

        TextRange totalRange = getTextRange();
        TextRange baseRange = baseColor.getTextRange();
        TextRange opacityRange = opacity.getTextRange();
        if (totalRange == null || baseRange == null || opacityRange == null) {
            return null;
        }

        int relativeBaseEnd = baseRange.getEndOffset() - totalRange.getStartOffset();
        int relativeOpacityStart = opacityRange.getStartOffset() - totalRange.getStartOffset();
        if (relativeBaseEnd < 0 || relativeOpacityStart < relativeBaseEnd) {
            return null;
        }

        String text = getText();
        if (relativeBaseEnd > text.length() || relativeOpacityStart > text.length()) {
            return null;
        }

        String separator = text.substring(relativeBaseEnd, relativeOpacityStart);
        String opacityText = opacity.getText();
        return new OpacityContext(separator, opacityText);
    }

    @Override
    @Nullable
    public Color resolveColor() {
        TcssColorValue baseColor = getBaseColor();
        if (baseColor == null) {
            return null;
        }

        Color base = baseColor.resolveColor();
        if (base == null) {
            return null;
        }

        Float opacity = parseOpacityFraction();
        if (opacity == null) {
            return base;
        }

        float clamped = Math.max(0f, Math.min(1f, opacity));
        int alpha = Math.round(clamped * 255f);
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    @Override
    @Nullable
    public ColorFormat getColorFormat() {
        TcssColorValue base = getBaseColor();
        return base != null ? base.getColorFormat() : null;
    }

    @Nullable
    private Float parseOpacityFraction() {
        PsiElement opacity = getOpacityToken();
        if (opacity == null) {
            return null;
        }

        String text = opacity.getText();
        if (text == null) {
            return null;
        }

        text = text.trim();
        if (!text.endsWith("%")) {
            return null;
        }

        String numeric = text.substring(0, text.length() - 1).trim();
        if (numeric.isEmpty()) {
            return null;
        }

        try {
            float value = Float.parseFloat(numeric);
            return value / 100f;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Formatting context extracted from the original opacity suffix.
     */
    public static final class OpacityContext {
        private final String separator;
        private final int decimalPlaces;
        private final boolean hasDecimal;

        private OpacityContext(@NotNull String separator, @NotNull String opacityText) {
            this.separator = separator;

            String trimmed = opacityText.trim();
            int percentIndex = trimmed.indexOf('%');
            int decimals = 0;
            boolean decimal = false;
            if (percentIndex >= 0) {
                int dotIndex = trimmed.indexOf('.');
                if (dotIndex >= 0 && dotIndex < percentIndex) {
                    decimal = true;
                    decimals = Math.max(0, percentIndex - dotIndex - 1);
                }
            }
            this.decimalPlaces = decimals;
            this.hasDecimal = decimal && decimals > 0;
        }

        @NotNull
        public String render(@NotNull String baseText, int alpha) {
            float fraction = alpha / 255f;
            float percent = fraction * 100f;
            String pattern;
            if (hasDecimal) {
                pattern = "%." + decimalPlaces + "f%%";
            } else {
                pattern = "%.0f%%";
            }
            String formatted = String.format(Locale.US, pattern, percent);
            return baseText + separator + formatted;
        }
    }
}
