package io.textual.tcss.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

/**
 * PSI element for the 'initial' keyword.
 * Represents a property value reset to initial/default value.
 *
 * <p>The 'initial' keyword is universal - it works for any TCSS property
 * and resets the property to its default value.
 *
 * <p>Example: {@code background: initial;}
 */
public class TcssInitialKeyword extends ASTWrapperPsiElement {
    public TcssInitialKeyword(@NotNull ASTNode node) {
        super(node);
    }

    /**
     * Get the keyword text (always "initial").
     *
     * @return The keyword text
     */
    @NotNull
    public String getKeyword() {
        return getText();
    }
}
