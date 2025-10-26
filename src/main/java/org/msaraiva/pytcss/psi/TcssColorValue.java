package org.msaraiva.pytcss.psi;

import com.intellij.psi.PsiElement;
import org.msaraiva.pytcss.color.ColorFormat;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * PSI interface for all TCSS color values.
 * Implemented by HexColorValue, ColorFunctionCall, and ColorKeyword.
 */
public interface TcssColorValue extends PsiElement {
    /**
     * Resolve this color value to a java.awt.Color.
     *
     * @return Color object or null if color cannot be resolved
     */
    @Nullable
    Color resolveColor();

    /**
     * Get the format of this color value.
     *
     * @return ColorFormat enum value
     */
    @Nullable
    ColorFormat getColorFormat();
}
