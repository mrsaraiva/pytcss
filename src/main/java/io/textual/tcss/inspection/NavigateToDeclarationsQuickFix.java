package io.textual.tcss.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.textual.tcss.psi.TcssVariableDeclaration;
import io.textual.tcss.util.VariableResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Quick-fix that shows a popup listing all declarations of a variable,
 * allowing the user to navigate to any of them.
 */
public class NavigateToDeclarationsQuickFix implements LocalQuickFix {
    private final String variableName;

    public NavigateToDeclarationsQuickFix(@NotNull String variableName) {
        this.variableName = variableName;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Navigate to all declarations";
    }

    @NotNull
    @Override
    public String getName() {
        return "Show all declarations of '$" + variableName + "'";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        Collection<TcssVariableDeclaration> declarations =
                VariableResolver.findDeclarationsCrossFile(variableName, project);

        if (declarations.isEmpty()) {
            return;
        }

        if (declarations.size() == 1) {
            // Single declaration - navigate directly
            TcssVariableDeclaration declaration = declarations.iterator().next();
            PsiElement nameIdentifier = declaration.getNameIdentifier();
            if (nameIdentifier instanceof Navigatable) {
                ((Navigatable) nameIdentifier).navigate(true);
            }
        } else {
            // Multiple declarations - show popup
            TcssVariableDeclaration[] targets = declarations.toArray(new TcssVariableDeclaration[0]);

            if (targets.length > 0) {
                JBPopupFactory.getInstance()
                        .createPopupChooserBuilder(java.util.Arrays.asList(targets))
                        .setTitle("Choose Declaration of $" + variableName)
                        .setItemChosenCallback(declaration -> {
                            PsiElement nameIdentifier = declaration.getNameIdentifier();
                            if (nameIdentifier instanceof Navigatable) {
                                ((Navigatable) nameIdentifier).navigate(true);
                            }
                        })
                        .setRenderer(new PsiElementListCellRenderer<TcssVariableDeclaration>() {
                            @Override
                            public String getElementText(TcssVariableDeclaration declaration) {
                                PsiFile file = declaration.getContainingFile();
                                return file != null ? file.getName() : "Unknown";
                            }

                            @Override
                            public String getContainerText(TcssVariableDeclaration declaration, String name) {
                                return declaration.getVariableNameWithPrefix();
                            }

                            @Override
                            public int getIconFlags() {
                                return 0;
                            }
                        })
                        .createPopup()
                        .showInFocusCenter();
            }
        }
    }
}
