package io.textual.tcss.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * PSI element for property values.
 * Container for all value tokens including colors, numbers, strings, etc.
 */
public class TcssPropertyValue extends ASTWrapperPsiElement {
    public TcssPropertyValue(@NotNull ASTNode node) {
        super(node);
    }

    /**
     * Get all color values in this property value.
     * Includes hex colors, color functions, color keywords, and variable references.
     *
     * @return List of color value elements
     */
    @NotNull
    public List<TcssColorValue> getColorValues() {
        List<TcssColorValue> colors = new ArrayList<>();

        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof TcssColorValue) {
                colors.add((TcssColorValue) child);
            }
            child = child.getNextSibling();
        }

        return colors;
    }

    /**
     * Check if this property value contains any color values.
     *
     * @return true if contains at least one color value
     */
    public boolean hasColorValues() {
        return !getColorValues().isEmpty();
    }
}
