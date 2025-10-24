package io.textual.tcss.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import io.textual.tcss.TcssElementTypes;
import io.textual.tcss.TcssTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PSI element for property declarations: property-name: value;
 */
public class TcssPropertyDeclaration extends ASTWrapperPsiElement {
    public TcssPropertyDeclaration(@NotNull ASTNode node) {
        super(node);
    }

    /**
     * Get the property name token.
     *
     * @return Property name element or null
     */
    @Nullable
    public PsiElement getPropertyName() {
        ASTNode node = getNode().findChildByType(TcssTokenTypes.PROPERTY_NAME);
        return node != null ? node.getPsi() : null;
    }

    /**
     * Get the property name as a string.
     *
     * @return Property name text or empty string
     */
    @NotNull
    public String getPropertyNameText() {
        PsiElement name = getPropertyName();
        return name != null ? name.getText() : "";
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
     * Check if this property declaration has !important modifier.
     *
     * @return true if property is marked as !important
     */
    public boolean isImportant() {
        return getImportantModifier() != null;
    }

    /**
     * Get the !important modifier element if present.
     *
     * @return PsiElement for !important modifier, or null if not present
     */
    @Nullable
    public PsiElement getImportantModifier() {
        ASTNode node = getNode().findChildByType(TcssElementTypes.IMPORTANT_MODIFIER);
        return node != null ? node.getPsi() : null;
    }
}
