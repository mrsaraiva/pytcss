package io.textual.tcss.refactoring;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.usageView.UsageInfo;
import io.textual.tcss.TcssElementFactory;
import io.textual.tcss.psi.TcssVariableDeclaration;
import io.textual.tcss.psi.TcssVariableReference;
import io.textual.tcss.util.VariableResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Ensures variable declaration renames also update all TCSS references.
 */
public class TcssVariableRenameProcessor extends RenamePsiElementProcessor {
    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        return element instanceof TcssVariableDeclaration;
    }

    @Override
    public void renameElement(@NotNull PsiElement element,
                              @NotNull String newName,
                              @NotNull UsageInfo[] usages,
                              @NotNull RefactoringElementListener listener) {
        TcssVariableDeclaration declaration = (TcssVariableDeclaration) element;
        String sanitized = TcssVariableDeclaration.sanitizeName(newName);
        Project project = declaration.getProject();

        // Rename declaration token first
        declaration.setName(newName);

        // Update collected usages (they should already be TcssVariableReference instances)
        for (UsageInfo usage : usages) {
            PsiElement usageElement = usage.getElement();
            if (usageElement instanceof TcssVariableReference) {
                TcssVariableReference reference = (TcssVariableReference) usageElement;
                PsiElement replacement = TcssElementFactory.createVariableReference(project, sanitized);
                if (replacement != null) {
                    reference.replace(replacement);
                }
            } else if (usage.getReference() != null) {
                PsiReference reference = usage.getReference();
                reference.handleElementRename(sanitized);
            }
        }

        // Catch stragglers: scan file for references resolving to this declaration
        Collection<TcssVariableReference> extraRefs = VariableResolver.findReferences(declaration);
        for (TcssVariableReference reference : extraRefs) {
            if (!sanitized.equals(reference.getVariableName())) {
                PsiElement replacement = TcssElementFactory.createVariableReference(project, sanitized);
                if (replacement != null) {
                    reference.replace(replacement);
                }
            }
        }

        listener.elementRenamed(declaration);
    }
}
