package org.msaraiva.pytcss.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.msaraiva.pytcss.color.ColorFormat;
import org.msaraiva.pytcss.color.NamedColors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * PSI element for named color keywords: red, blue, ansi_bright_green, etc.
 * Note: "auto" keyword should NOT be treated as a color (returns null).
 */
public class TcssColorKeyword extends ASTWrapperPsiElement implements TcssColorValue {
    public TcssColorKeyword(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @Nullable
    public Color resolveColor() {
        String text = getText();

        // Special case: "auto" is not a color
        if ("auto".equalsIgnoreCase(text)) {
            return null;
        }

        return NamedColors.getColorByName(text);
    }

    @Override
    @Nullable
    public ColorFormat getColorFormat() {
        String text = getText();

        // Special case: "auto" keyword
        if ("auto".equalsIgnoreCase(text)) {
            return ColorFormat.AUTO;
        }

        // Check if it's a valid named color
        if (NamedColors.isNamedColor(text)) {
            return ColorFormat.KEYWORD;
        }

        return null;
    }
}
