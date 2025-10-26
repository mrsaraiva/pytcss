package org.msaraiva.pytcss.inspection;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.msaraiva.pytcss.psi.TcssVariableDeclaration;
import org.msaraiva.pytcss.util.VariableResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Inspection that warns when a variable is declared in multiple files.
 * Helps prevent naming conflicts in project-wide variable resolution.
 */
public class TcssDuplicateVariableInspection extends LocalInspectionTool {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);
                if (element instanceof TcssVariableDeclaration) {
                    checkDuplicate((TcssVariableDeclaration) element, holder);
                }
            }
        };
    }

    private void checkDuplicate(@NotNull TcssVariableDeclaration decl, @NotNull ProblemsHolder holder) {
        String varName = decl.getVariableName();
        if (varName.isEmpty()) {
            return;
        }

        Project project = decl.getProject();
        Collection<TcssVariableDeclaration> allDecls =
                VariableResolver.findDeclarationsCrossFile(varName, project);

        // Only warn if variable is declared in multiple different files
        Set<PsiFile> distinctFiles = new HashSet<>();
        for (TcssVariableDeclaration declaration : allDecls) {
            PsiFile file = declaration.getContainingFile();
            if (file != null) {
                distinctFiles.add(file);
            }
        }

        if (distinctFiles.size() > 1) {
            PsiElement nameIdentifier = decl.getNameIdentifier();
            if (nameIdentifier != null) {
                String message = String.format(
                        "Variable '$%s' is declared in %d files",
                        varName,
                        distinctFiles.size()
                );
                holder.registerProblem(
                        nameIdentifier,
                        message,
                        ProblemHighlightType.WARNING,
                        new NavigateToDeclarationsQuickFix(varName)
                );
            }
        }
    }
}
