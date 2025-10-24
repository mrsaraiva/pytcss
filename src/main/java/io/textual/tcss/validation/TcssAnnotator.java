package io.textual.tcss.validation;

import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.textual.tcss.color.NamedColors;
import io.textual.tcss.constants.TcssConstants;
import io.textual.tcss.metadata.TcssPropertyCatalog;
import io.textual.tcss.metadata.TcssPropertyInfo;
import io.textual.tcss.psi.TcssColorKeyword;
import io.textual.tcss.TcssElementTypes;
import io.textual.tcss.TcssSyntaxHighlighter;
import io.textual.tcss.TcssTokenTypes;
import io.textual.tcss.psi.TcssPropertyDeclaration;
import io.textual.tcss.psi.TcssVariableDeclaration;
import io.textual.tcss.psi.TcssVariableReference;
import io.textual.tcss.util.VariableResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

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
        // Validate pseudo-class tokens directly (no dedicated PSI element)
        else if (element.getNode().getElementType() == TcssTokenTypes.PSEUDO_CLASS) {
            validatePseudoClass(element, holder);
        }
        // Highlight !important modifier with special color (entire element, not just !)
        else if (element.getNode().getElementType() == TcssElementTypes.IMPORTANT_MODIFIER) {
            highlightImportantModifier(element, holder);
        }
    }

    private void validatePropertyDeclaration(@NotNull TcssPropertyDeclaration declaration, @NotNull AnnotationHolder holder) {
        String name = declaration.getPropertyNameText();
        if (name.isEmpty()) {
            return;
        }

        TcssPropertyInfo info = TcssPropertyCatalog.get(name);
        if (info == null) {
            // Property not found - provide fuzzy matching suggestion
            Collection<String> allPropertyNames = TcssPropertyCatalog.getAll().stream()
                .map(TcssPropertyInfo::getName)
                .collect(Collectors.toList());

            String suggestion = TcssConstants.getSuggestion(name, allPropertyNames);
            String message = "Unknown TCSS property '" + name + "'";
            if (suggestion != null) {
                message += ". Did you mean '" + suggestion + "'?";
            }

            PsiElement nameElement = declaration.getPropertyName();
            if (nameElement != null) {
                holder.newAnnotation(HighlightSeverity.WARNING, message)
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
        String varName = reference.getVariableName();
        if (varName.isEmpty()) {
            return;
        }

        PsiFile file = reference.getContainingFile();
        if (file == null) {
            return;
        }

        // Check local file first (fast path)
        if (VariableResolver.findDeclaration(varName, file) != null) {
            return;
        }

        // Check project-wide via index (cross-file support)
        Project project = reference.getProject();
        Collection<TcssVariableDeclaration> crossFileDecls =
                VariableResolver.findDeclarationsCrossFile(varName, project);

        if (crossFileDecls.isEmpty()) {
            // Not found locally or project-wide - undefined variable error
            holder.newAnnotation(HighlightSeverity.ERROR, "Undefined variable '" + varName + "'")
                .range(reference.getTextRange())
                .create();
        }
    }

    private void validatePseudoClass(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        String text = element.getText(); // e.g., ":hover"
        if (text.length() < 2) {
            return; // Invalid pseudo-class format
        }

        // Extract pseudo-class name without leading ':'
        String pseudoClass = text.substring(1);

        if (!TcssConstants.VALID_PSEUDO_CLASSES.contains(pseudoClass)) {
            // Invalid pseudo-class - provide suggestion
            String suggestion = TcssConstants.getSuggestion(pseudoClass, TcssConstants.VALID_PSEUDO_CLASSES);
            String message = "Unknown TCSS pseudo-class '" + text + "'";
            if (suggestion != null) {
                message += ". Did you mean ':" + suggestion + "'?";
            }

            AnnotationBuilder annotation = holder.newAnnotation(HighlightSeverity.ERROR, message)
                .range(element);

            // Add tooltip with error message and all valid pseudo-classes
            annotation.tooltip(buildPseudoClassTooltip(message));
            annotation.create();
        }
    }

    private String buildPseudoClassTooltip(@NotNull String errorMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtil.escapeXmlEntities(errorMessage)).append("<br/><br/>");
        sb.append("Valid pseudo-classes:<br/>");
        TcssConstants.VALID_PSEUDO_CLASSES.stream()
            .sorted()
            .forEach(pc -> sb.append(":<b>").append(StringUtil.escapeXmlEntities(pc)).append("</b><br/>"));
        return sb.toString();
    }

    /**
     * Highlight the entire !important modifier with special color.
     * This ensures both the '!' and 'important' text get the same highlighting.
     */
    private void highlightImportantModifier(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element.getTextRange())
            .textAttributes(TcssSyntaxHighlighter.IMPORTANT_MODIFIER)
            .create();
    }
}
