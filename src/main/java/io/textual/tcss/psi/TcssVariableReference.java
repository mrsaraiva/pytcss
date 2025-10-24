package io.textual.tcss.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import io.textual.tcss.TcssElementFactory;
import io.textual.tcss.color.ColorFormat;
import io.textual.tcss.util.VariableResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Collection;

/**
 * PSI element for variable references in property values: $variable-name
 *
 * Example: background: $primary;
 *
 * <p>Implements TcssColorValue to integrate with the color preview system.
 * When a variable reference resolves to a color, it will show a gutter icon
 * and support the color picker.
 *
 * <p>Resolution behavior:
 * <ul>
 *   <li>Delegates to VariableResolver for file-scope resolution</li>
 *   <li>Handles circular references (returns null)</li>
 *   <li>Handles undefined variables (returns null)</li>
 *   <li>Handles max depth (returns null)</li>
 * </ul>
 *
 * <p>Note: This element will be revised once validation is implemented
 * to provide error markers for undefined variables and circular references.
 */
public class TcssVariableReference extends ASTWrapperPsiElement implements TcssColorValue {
    public TcssVariableReference(@NotNull ASTNode node) {
        super(node);
    }

    /**
     * Get the variable name (without $).
     *
     * @return Variable name (empty string if invalid)
     */
    @NotNull
    public String getVariableName() {
        String text = getText();
        if (text.startsWith("$") && text.length() > 1) {
            return text.substring(1);
        }
        return ""; // Invalid variable (e.g., just "$")
    }

    /**
     * Resolve the variable reference to a color with cross-file support.
     * Checks local file first (shadowing), then searches project-wide.
     *
     * @return Resolved color or null if:
     *         - Variable is undefined
     *         - Variable is not a color
     *         - Circular reference detected
     *         - Maximum depth exceeded
     */
    @Override
    @Nullable
    public Color resolveColor() {
        PsiFile containingFile = getContainingFile();
        if (containingFile == null) {
            return null;
        }

        String variableName = getVariableName();
        return VariableResolver.resolveColorCrossFile(variableName, containingFile);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        PsiReference reference = getReference();
        return reference != null ? new PsiReference[]{reference} : PsiReference.EMPTY_ARRAY;
    }

    @Override
    public PsiReference getReference() {
        return new VariablePsiReference(this);
    }

    /**
     * Get the color format.
     *
     * <p>Returns null for variable references. The format is determined by the
     * resolved color value in the declaration, not the reference itself.
     *
     * <p>Note: For color picker updates, TcssColorProvider will redirect to the
     * declaration to update the actual color value, preserving its format.
     *
     * @return null (no inherent format for variables)
     */
    @Override
    @Nullable
    public ColorFormat getColorFormat() {
        // Variables don't have a format - the declaration does
        // Color picker should update the declaration, not the reference
        return null;
    }

    /**
     * Find the declaration for this variable reference with cross-file support.
     * Checks local file first (shadowing), then searches project-wide.
     * Used by color picker to update the declaration instead of the reference.
     *
     * @return Variable declaration or null if not found
     */
    @Nullable
    public TcssVariableDeclaration resolveDeclaration() {
        PsiFile containingFile = getContainingFile();
        if (containingFile == null) {
            return null;
        }

        String variableName = getVariableName();

        // Check local file first (shadowing)
        TcssVariableDeclaration localDecl = VariableResolver.findDeclaration(variableName, containingFile);
        if (localDecl != null) {
            return localDecl;
        }

        // Fall back to cross-file search
        Collection<TcssVariableDeclaration> crossFileDecls =
                VariableResolver.findDeclarationsCrossFile(variableName, getProject());
        return crossFileDecls.isEmpty() ? null : crossFileDecls.iterator().next();
    }

    private static class VariablePsiReference extends PsiReferenceBase<TcssVariableReference> {
        private VariablePsiReference(@NotNull TcssVariableReference element) {
            super(element, createRange(element));
        }

        private static TextRange createRange(@NotNull TcssVariableReference element) {
            String text = element.getText();
            if (text.startsWith("$") && text.length() > 1) {
                return new TextRange(1, text.length());
            }
            return TextRange.from(0, Math.max(0, text.length()));
        }

        @Nullable
        @Override
        public PsiElement resolve() {
            return myElement.resolveDeclaration();
        }

        @Override
        public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
            String sanitized = newElementName.startsWith("$") ? newElementName.substring(1) : newElementName;
            Project project = myElement.getProject();
            PsiElement replacement = TcssElementFactory.createVariableReference(project, sanitized);
            if (replacement != null) {
                return myElement.replace(replacement);
            }
            return myElement;
        }
    }
}
