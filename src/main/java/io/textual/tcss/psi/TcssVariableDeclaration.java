package io.textual.tcss.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import io.textual.tcss.TcssElementFactory;
import io.textual.tcss.TcssTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PSI element for variable declarations: $variable-name: value;
 *
 * Example: $primary: #0066cc;
 *
 * This element represents top-level variable declarations in TCSS files.
 * Variables have file-level scope (TCSS does not have block-scoped variables).
 */
public class TcssVariableDeclaration extends ASTWrapperPsiElement implements PsiNameIdentifierOwner {
    public TcssVariableDeclaration(@NotNull ASTNode node) {
        super(node);
    }

    /**
     * Get the variable name token (includes $).
     *
     * @return Variable token element or null
     */
    @Nullable
    public PsiElement getVariableToken() {
        ASTNode node = getNode().findChildByType(TcssTokenTypes.VARIABLE);
        return node != null ? node.getPsi() : null;
    }

    /**
     * Get the variable name as a string (without $).
     *
     * @return Variable name or empty string
     */
    @NotNull
    public String getVariableName() {
        PsiElement token = getVariableToken();
        if (token == null) {
            return "";
        }
        String text = token.getText();
        if (text.startsWith("$") && text.length() > 1) {
            return text.substring(1);
        }
        return ""; // Invalid variable (e.g., just "$")
    }

    @Override
    public PsiElement getNameIdentifier() {
        return getVariableToken();
    }

    @Override
    public String getName() {
        return getVariableName();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        String sanitized = sanitizeName(name);
        PsiElement token = getVariableToken();
        if (token == null) {
            return this;
        }
        PsiElement replacement = TcssElementFactory.createVariableToken(getProject(), sanitized);
        if (replacement != null) {
            token.replace(replacement);
        }
        return this;
    }

    @NotNull
    public static String sanitizeName(@NotNull String input) {
        String trimmed = input.trim();
        if (trimmed.startsWith("$")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.isEmpty()) {
            throw new IncorrectOperationException("Variable name cannot be empty");
        }
        return trimmed;
    }

    /**
     * Get the variable name with $ prefix (for display).
     *
     * @return Variable name with $ or empty string
     */
    @NotNull
    public String getVariableNameWithPrefix() {
        PsiElement token = getVariableToken();
        return token != null ? token.getText() : "";
    }

    /**
     * Get the property value element.
     *
     * @return Property value element or null
     */
    @Nullable
    public TcssPropertyValue getPropertyValue() {
        return PsiTreeUtil.getChildOfType(this, TcssPropertyValue.class);
    }

    /**
     * Get the first color value in this declaration.
     * Used by variable resolver to determine color.
     *
     * This may return:
     * - Literal color values (hex, rgb, hsl, keyword)
     * - Another variable reference (for chained variables)
     * - null (if the variable value is not color-related)
     *
     * @return First color value or null
     */
    @Nullable
    public TcssColorValue getColorValue() {
        TcssPropertyValue propertyValue = getPropertyValue();
        if (propertyValue == null) {
            return null;
        }

        // Get first color value (could be hex, function, keyword, or variable reference)
        for (TcssColorValue colorValue : propertyValue.getColorValues()) {
            return colorValue;  // Return first color
        }

        return null;
    }
}
