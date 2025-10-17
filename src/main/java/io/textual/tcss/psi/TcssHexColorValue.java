package io.textual.tcss.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import io.textual.tcss.color.ColorFormat;
import io.textual.tcss.color.ColorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * PSI element for hex color values: #RGB, #RGBA, #RRGGBB, #RRGGBBAA
 */
public class TcssHexColorValue extends ASTWrapperPsiElement implements TcssColorValue {
    public TcssHexColorValue(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @Nullable
    public Color resolveColor() {
        String text = getText();
        try {
            return ColorUtil.parseHex(text);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    @Nullable
    public ColorFormat getColorFormat() {
        String text = getText();
        if (!text.startsWith("#")) {
            return null;
        }

        int length = text.length() - 1; // Exclude the #
        switch (length) {
            case 3:
                return ColorFormat.HEX_3;
            case 4:
                return ColorFormat.HEX_4;
            case 6:
                return ColorFormat.HEX_6;
            case 8:
                return ColorFormat.HEX_8;
            default:
                return null;
        }
    }
}
