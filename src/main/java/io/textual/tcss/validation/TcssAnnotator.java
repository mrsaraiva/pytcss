package io.textual.tcss.validation;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import io.textual.tcss.color.NamedColors;
import io.textual.tcss.metadata.TcssPropertyCatalog;
import io.textual.tcss.metadata.TcssPropertyInfo;
import io.textual.tcss.psi.TcssColorKeyword;
import io.textual.tcss.psi.TcssPropertyDeclaration;
import io.textual.tcss.psi.TcssVariableReference;
import org.jetbrains.annotations.NotNull;

/**
 * Basic validation annotator highlighting common mistakes.
 */
public class TcssAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof TcssPropertyDeclaration) {
            validatePropertyDeclaration((TcssPropertyDeclaration) element, holder);
        } else if (element instanceof TcssColorKeyword) {
            validateColorKeyword((TcssColorKeyword) element, holder);
        } else if (element instanceof TcssVariableReference) {
            validateVariableReference((TcssVariableReference) element, holder);
        }
    }

    private void validatePropertyDeclaration(@NotNull TcssPropertyDeclaration declaration, @NotNull AnnotationHolder holder) {
        String name = declaration.getPropertyNameText();
        if (name.isEmpty()) {
            return;
        }

        TcssPropertyInfo info = TcssPropertyCatalog.get(name);
        if (info == null) {
            PsiElement nameElement = declaration.getPropertyName();
            if (nameElement != null) {
                holder.newAnnotation(HighlightSeverity.WARNING, "Unknown TCSS property '" + name + "'")
                    .range(nameElement)
                    .create();
            }
        }
    }

    private void validateColorKeyword(@NotNull TcssColorKeyword keyword, @NotNull AnnotationHolder holder) {
        String text = keyword.getText();
        if ("auto".equalsIgnoreCase(text)) {
            return;
        }
        if (!NamedColors.isNamedColor(text)) {
            holder.newAnnotation(HighlightSeverity.WARNING, "Unknown color keyword '" + text + "'")
                .range(keyword.getTextRange())
                .create();
        }
    }

    private void validateVariableReference(@NotNull TcssVariableReference reference, @NotNull AnnotationHolder holder) {
        if (reference.resolveDeclaration() == null) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Undefined variable '" + reference.getVariableName() + "'")
                .range(reference.getTextRange())
                .create();
        }
    }
}
