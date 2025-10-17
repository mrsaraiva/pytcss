package io.textual.tcss.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import io.textual.tcss.TcssFile;
import io.textual.tcss.psi.TcssPropertyDeclaration;
import io.textual.tcss.psi.TcssRuleSet;
import io.textual.tcss.psi.TcssVariableDeclaration;
import io.textual.tcss.structure.presentation.TcssPropertyPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Structure view tree element for TCSS PSI nodes.
 */
public class TcssStructureViewElement implements StructureViewTreeElement, SortableTreeElement {
    private final NavigatablePsiElement element;

    public TcssStructureViewElement(@NotNull NavigatablePsiElement element) {
        this.element = element;
    }

    @Override
    public Object getValue() {
        return element;
    }

    @Override
    public void navigate(boolean requestFocus) {
        element.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return element.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return element.canNavigateToSource();
    }

    @NotNull
    @Override
    public StructureViewTreeElement[] getChildren() {
        List<StructureViewTreeElement> children = new ArrayList<>();
        if (element instanceof TcssFile) {
            for (TcssRuleSet ruleSet : PsiTreeUtil.getChildrenOfTypeAsList(element, TcssRuleSet.class)) {
                children.add(new TcssStructureViewElement(ruleSet));
            }
            for (TcssVariableDeclaration declaration : PsiTreeUtil.getChildrenOfTypeAsList(element, TcssVariableDeclaration.class)) {
                children.add(new TcssStructureViewElement(declaration));
            }
            for (TcssPropertyDeclaration declaration : PsiTreeUtil.getChildrenOfTypeAsList(element, TcssPropertyDeclaration.class)) {
                children.add(new TcssStructureViewElement(declaration));
            }
        } else if (element instanceof TcssRuleSet) {
            TcssRuleSet ruleSet = (TcssRuleSet) element;
            for (TcssRuleSet nested : PsiTreeUtil.getChildrenOfTypeAsList(ruleSet, TcssRuleSet.class)) {
                children.add(new TcssStructureViewElement(nested));
            }
            for (TcssPropertyDeclaration declaration : ruleSet.getPropertyDeclarations()) {
                children.add(new TcssStructureViewElement(declaration));
            }
        }
        return children.toArray(StructureViewTreeElement[]::new);
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        return TcssPropertyPresentation.wrap(element);
    }

    @Override
    public @NotNull String getAlphaSortKey() {
        ItemPresentation presentation = getPresentation();
        String text = presentation.getPresentableText();
        return text != null ? text : "";
    }

}
