package org.msaraiva.pytcss.validation;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.msaraiva.pytcss.color.NamedColors;
import org.msaraiva.pytcss.constants.TcssConstants;
import org.msaraiva.pytcss.metadata.TcssPropertyCatalog;
import org.msaraiva.pytcss.metadata.TcssPropertyInfo;
import org.msaraiva.pytcss.psi.TcssColorKeyword;
import org.msaraiva.pytcss.TcssElementTypes;
import org.msaraiva.pytcss.TcssSyntaxHighlighter;
import org.msaraiva.pytcss.TcssTokenTypes;
import org.msaraiva.pytcss.psi.TcssPropertyDeclaration;
import org.msaraiva.pytcss.psi.TcssPropertyValue;
import org.msaraiva.pytcss.psi.TcssVariableDeclaration;
import org.msaraiva.pytcss.psi.TcssVariableReference;
import org.msaraiva.pytcss.util.VariableResolver;
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
            return;
        }

        // Validate property value type (COLOR vs NUMBER vs LENGTH vs ENUM)
        validatePropertyValueType(declaration, info, holder);
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

    /**
     * Validates that property value type matches the expected type.
     * Reference: Textual's _styles_builder.py implements similar type checking in process_* methods.
     */
    private void validatePropertyValueType(@NotNull TcssPropertyDeclaration declaration,
                                           @NotNull TcssPropertyInfo info,
                                           @NotNull AnnotationHolder holder) {
        TcssPropertyValue propertyValue = declaration.getPropertyValue();
        if (propertyValue == null) {
            return;
        }

        // Skip validation for special cases
        if (containsInitialKeyword(propertyValue) || containsVariableReference(propertyValue)) {
            // initial keyword and variables can have any type
            return;
        }

        String propertyName = declaration.getPropertyNameText();
        TcssPropertyInfo.ValueType expectedType = info.getValueType();
        String actualTypeDescription = detectActualValueType(propertyValue, expectedType);

        if (actualTypeDescription != null) {
            // Type mismatch detected
            String message = String.format("Property '%s' expects %s, got %s",
                    propertyName, getTypeFriendlyName(expectedType), actualTypeDescription);

            holder.newAnnotation(HighlightSeverity.ERROR, message)
                .range(propertyValue.getTextRange())
                .create();
        }
    }

    /**
     * Detects if the actual value type doesn't match the expected type.
     *
     * @return Description of the actual type if there's a mismatch, null if valid
     */
    @Nullable
    private String detectActualValueType(@NotNull TcssPropertyValue propertyValue,
                                         @NotNull TcssPropertyInfo.ValueType expectedType) {
        switch (expectedType) {
            case COLOR:
                return validateColorType(propertyValue);
            case LENGTH:
                return validateLengthType(propertyValue);
            case NUMBER:
                return validateNumberType(propertyValue);
            case BOOLEAN:
                return validateBooleanType(propertyValue);
            case STRING:
                return validateStringType(propertyValue);
            case ENUM:
                return validateEnumType(propertyValue);
            case OTHER:
                // Don't validate OTHER types
                return null;
            default:
                return null;
        }
    }

    /**
     * Validates COLOR type properties.
     * Expected: TcssColorValue (hex, rgb, hsl, color keywords, ansi colors)
     *
     * @return Error description if invalid, null if valid
     */
    @Nullable
    private String validateColorType(@NotNull TcssPropertyValue propertyValue) {
        // Check if value contains any color elements
        if (propertyValue.hasColorValues()) {
            return null; // Valid: contains color
        }

        // Check if it's a plain number
        if (containsNumberToken(propertyValue)) {
            return "NUMBER";
        }

        // Check if it's a string
        if (containsStringToken(propertyValue)) {
            return "STRING";
        }

        // Check if it's an identifier (might be invalid color keyword)
        if (containsIdentifierToken(propertyValue)) {
            return "IDENTIFIER";
        }

        return null; // Unknown type, let other validations handle it
    }

    /**
     * Validates LENGTH type properties.
     * Expected: NUMBER with unit (fr, %, w, h, vw, vh) OR keyword "auto"
     *
     * @return Error description if invalid, null if valid
     */
    @Nullable
    private String validateLengthType(@NotNull TcssPropertyValue propertyValue) {
        String text = propertyValue.getText().trim();

        // Check for "auto" keyword (valid for many length properties)
        if (text.equalsIgnoreCase("auto")) {
            return null; // Valid
        }

        // Check if it contains a number (with or without units)
        if (containsNumberToken(propertyValue)) {
            return null; // Valid: number with optional unit
        }

        // Check if it's a color value
        if (propertyValue.hasColorValues()) {
            return "COLOR";
        }

        // Check if it's a string
        if (containsStringToken(propertyValue)) {
            return "STRING";
        }

        return null; // Could be other valid length notation
    }

    /**
     * Validates NUMBER type properties.
     * Expected: Plain NUMBER token (no units)
     *
     * @return Error description if invalid, null if valid
     */
    @Nullable
    private String validateNumberType(@NotNull TcssPropertyValue propertyValue) {
        // Check if it contains a number
        if (containsNumberToken(propertyValue)) {
            return null; // Valid
        }

        // Check if it's a color value
        if (propertyValue.hasColorValues()) {
            return "COLOR";
        }

        // Check if it's a string
        if (containsStringToken(propertyValue)) {
            return "STRING";
        }

        return null;
    }

    /**
     * Validates BOOLEAN type properties.
     * Expected: "true" or "false" identifiers
     *
     * @return Error description if invalid, null if valid
     */
    @Nullable
    private String validateBooleanType(@NotNull TcssPropertyValue propertyValue) {
        String text = propertyValue.getText().trim();

        if (text.equalsIgnoreCase("true") || text.equalsIgnoreCase("false")) {
            return null; // Valid
        }

        // Check if it's a color value
        if (propertyValue.hasColorValues()) {
            return "COLOR";
        }

        // Check if it's a number
        if (containsNumberToken(propertyValue)) {
            return "NUMBER";
        }

        return null;
    }

    /**
     * Validates STRING type properties.
     * Expected: STRING token or IDENTIFIER token
     *
     * @return Error description if invalid, null if valid
     */
    @Nullable
    private String validateStringType(@NotNull TcssPropertyValue propertyValue) {
        // STRING type is very permissive, accepts strings, identifiers, and compound values
        // Only flag clear type mismatches

        // If it contains only a pure number, that might be suspicious
        String text = propertyValue.getText().trim();
        if (text.matches("^[0-9]+(\\.[0-9]+)?$")) {
            return "NUMBER";
        }

        // If it's a hex color, that's probably wrong
        if (propertyValue.hasColorValues() && text.startsWith("#")) {
            return "COLOR";
        }

        return null; // STRING type is flexible
    }

    /**
     * Validates ENUM type properties.
     * Currently returns null (detailed enum validation is in Phase 4).
     * This method exists as a placeholder for future enum-specific checks.
     *
     * @return Error description if invalid, null if valid
     */
    @Nullable
    private String validateEnumType(@NotNull TcssPropertyValue propertyValue) {
        // Phase 4 will implement detailed enum validation with TcssConstants
        // For now, just check for obvious type mismatches

        // Check if it's a color value
        if (propertyValue.hasColorValues()) {
            return "COLOR";
        }

        // Check if it's a plain number
        if (containsNumberToken(propertyValue)) {
            String text = propertyValue.getText().trim();
            // If it's JUST a number with no identifier, it's likely wrong
            if (text.matches("^[0-9]+(\\.[0-9]+)?.*$")) {
                return "NUMBER";
            }
        }

        return null; // Let Phase 4 handle detailed enum validation
    }

    /**
     * Checks if property value contains the initial keyword.
     */
    private boolean containsInitialKeyword(@NotNull TcssPropertyValue propertyValue) {
        for (PsiElement child : propertyValue.getChildren()) {
            if (child.getNode() != null && child.getNode().getElementType() == TcssTokenTypes.INITIAL_KEYWORD) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if property value contains a variable reference.
     */
    private boolean containsVariableReference(@NotNull TcssPropertyValue propertyValue) {
        for (PsiElement child : propertyValue.getChildren()) {
            if (child.getNode() != null && child.getNode().getElementType() == TcssTokenTypes.VARIABLE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if property value contains a NUMBER token.
     */
    private boolean containsNumberToken(@NotNull TcssPropertyValue propertyValue) {
        ASTNode node = propertyValue.getNode();
        if (node == null) return false;

        // Check all children including leaf nodes
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (child.getElementType() == TcssTokenTypes.NUMBER) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if property value contains a STRING token.
     */
    private boolean containsStringToken(@NotNull TcssPropertyValue propertyValue) {
        ASTNode node = propertyValue.getNode();
        if (node == null) return false;

        // Check all children including leaf nodes
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (child.getElementType() == TcssTokenTypes.STRING) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if property value contains an IDENTIFIER token.
     */
    private boolean containsIdentifierToken(@NotNull TcssPropertyValue propertyValue) {
        ASTNode node = propertyValue.getNode();
        if (node == null) return false;

        // Check all children including leaf nodes
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (child.getElementType() == TcssTokenTypes.IDENTIFIER) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a user-friendly name for the type.
     */
    @NotNull
    private String getTypeFriendlyName(@NotNull TcssPropertyInfo.ValueType type) {
        switch (type) {
            case COLOR:
                return "COLOR";
            case LENGTH:
                return "LENGTH";
            case NUMBER:
                return "NUMBER";
            case BOOLEAN:
                return "BOOLEAN";
            case STRING:
                return "STRING";
            case ENUM:
                return "ENUM";
            case OTHER:
                return "OTHER";
            default:
                return type.name();
        }
    }
}
