package org.msaraiva.pytcss;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.msaraiva.pytcss.psi.TcssVariableDeclaration;
import org.msaraiva.pytcss.psi.TcssVariableReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility factory for creating lightweight TCSS PSI elements.
 */
public final class TcssElementFactory {
    private TcssElementFactory() {
    }

    @Nullable
    public static PsiElement createVariableToken(@NotNull Project project, @NotNull String name) {
        TcssVariableDeclaration declaration = createVariableDeclaration(project, name);
        return declaration != null ? declaration.getVariableToken() : null;
    }

    @Nullable
    public static PsiElement createVariableReference(@NotNull Project project, @NotNull String name) {
        String text = "Widget { color: $" + name + "; }";
        PsiFile file = PsiFileFactory.getInstance(project)
            .createFileFromText("dummy.tcss", TcssLanguage.INSTANCE, text);
        TcssVariableReference reference = PsiTreeUtil.findChildOfType(file, TcssVariableReference.class);
        return reference != null ? reference : null;
    }

    @Nullable
    private static TcssVariableDeclaration createVariableDeclaration(@NotNull Project project, @NotNull String name) {
        String text = "$" + name + ": #000;";
        PsiFile file = PsiFileFactory.getInstance(project)
            .createFileFromText("dummy.tcss", TcssLanguage.INSTANCE, text);
        return PsiTreeUtil.findChildOfType(file, TcssVariableDeclaration.class);
    }
}
