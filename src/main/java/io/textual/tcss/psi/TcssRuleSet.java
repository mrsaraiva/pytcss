package io.textual.tcss.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import io.textual.tcss.TcssElementTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * PSI element for rule sets: selector { declarations }
 */
public class TcssRuleSet extends ASTWrapperPsiElement {
    public TcssRuleSet(@NotNull ASTNode node) {
        super(node);
    }

    /**
     * Get all property declarations in this rule set.
     *
     * @return List of property declarations
     */
    @NotNull
    public List<TcssPropertyDeclaration> getPropertyDeclarations() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, TcssPropertyDeclaration.class);
    }

    /**
     * Get all selectors for this rule set.
     *
     * @return List of selector elements
     */
    @NotNull
    public List<PsiElement> getSelectors() {
        List<PsiElement> selectors = new ArrayList<>();
        var nodes = getNode().getChildren(TokenSet.create(TcssElementTypes.SELECTOR));
        for (var node : nodes) {
            PsiElement psi = node.getPsi();
            if (psi != null) {
                selectors.add(psi);
            }
        }
        return selectors;
    }

    /**
     * Convenience helper to return selector text (used by structure view).
     */
    @NotNull
    public List<String> getSelectorTexts() {
        String text = getText();
        int braceIndex = text.indexOf('{');
        if (braceIndex <= 0) {
            return List.of();
        }

        String header = text.substring(0, braceIndex).trim();
        if (header.isEmpty()) {
            return List.of();
        }

        String[] parts = header.split(",");
        List<String> result = new ArrayList<>(parts.length);
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    @NotNull
    public String getSelectorDisplayName() {
        List<String> selectors = getSelectorTexts();
        if (selectors.isEmpty()) {
            return "<rule>";
        }
        return String.join(", ", selectors);
    }
}
