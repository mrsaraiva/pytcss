package org.msaraiva.pytcss.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.msaraiva.pytcss.color.ColorFormat;
import org.msaraiva.pytcss.color.ColorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * PSI element for color function calls: rgb(), rgba(), hsl(), hsla()
 */
public class TcssColorFunctionCall extends ASTWrapperPsiElement implements TcssColorValue {
    public TcssColorFunctionCall(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @Nullable
    public Color resolveColor() {
        String text = getText();
        return ColorUtil.parse(text);
    }

    @Override
    @Nullable
    public ColorFormat getColorFormat() {
        String text = getText().toLowerCase();

        if (text.startsWith("rgb(")) {
            return ColorFormat.RGB;
        } else if (text.startsWith("rgba(")) {
            return ColorFormat.RGBA;
        } else if (text.startsWith("hsl(")) {
            return ColorFormat.HSL;
        } else if (text.startsWith("hsla(")) {
            return ColorFormat.HSLA;
        }

        return null;
    }
}
