package io.textual.tcss.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import io.textual.tcss.TcssLanguage;
import io.textual.tcss.TcssTokenTypes;
import io.textual.tcss.constants.TcssConstants;
import io.textual.tcss.metadata.TcssPropertyCatalog;
import io.textual.tcss.metadata.TcssPropertyInfo;
import io.textual.tcss.metadata.generated.TcssPropertyDocumentation;
import io.textual.tcss.psi.TcssPropertyDeclaration;
import io.textual.tcss.psi.TcssPropertyValue;
import io.textual.tcss.psi.TcssRuleSet;
import io.textual.tcss.psi.TcssVariableDeclaration;
import io.textual.tcss.settings.TcssPluginSettings;
import io.textual.tcss.util.VariableResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides basic code completion for TCSS property names, color keywords, and variables.
 */
public class TcssCompletionContributor extends CompletionContributor {
    /**
     * Insert handler that removes the $ prefix when a named color is selected.
     * This allows typing "$sea" and selecting "seashell" to insert "seashell" (not "$seashell").
     */
    private static final InsertHandler<LookupElement> REMOVE_DOLLAR_PREFIX_HANDLER = (context, item) -> {
        Document document = context.getDocument();
        int startOffset = context.getStartOffset();

        // Check if there's a $ immediately before the insertion point
        if (startOffset > 0) {
            CharSequence text = document.getCharsSequence();
            if (text.charAt(startOffset - 1) == '$') {
                // Remove the $ character
                document.deleteString(startOffset - 1, startOffset);
            }
        }
    };

    public TcssCompletionContributor() {
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(TcssLanguage.INSTANCE),
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters,
                                              @NotNull ProcessingContext context,
                                              @NotNull CompletionResultSet result) {
                    PsiElement position = parameters.getPosition();
                    PsiElement original = parameters.getOriginalPosition();
                    PsiElement element = original != null ? original : position;

                    if (element == null) {
                        return;
                    }

                    if (isPropertyNameContext(element)) {
                        contributePropertyNames(parameters, result);
                    } else if (isPropertyValueContext(element)) {
                        contributePropertyValues(parameters, result);
                    } else {
                        // Fallback: if inside property declaration but not in name context,
                        // assume property value context (even if PropertyValue element doesn't exist yet)
                        TcssPropertyDeclaration declaration = PsiTreeUtil.getParentOfType(element, TcssPropertyDeclaration.class);
                        if (declaration != null) {
                            contributePropertyValues(parameters, result);
                        }
                    }
                }
            });
    }

    private boolean isPropertyNameContext(@NotNull PsiElement element) {
        // If inside property value, definitely NOT property name context
        TcssPropertyValue propertyValue = PsiTreeUtil.getParentOfType(element, TcssPropertyValue.class);
        if (propertyValue != null) {
            return false;
        }

        TcssPropertyDeclaration declaration = PsiTreeUtil.getParentOfType(element, TcssPropertyDeclaration.class);
        if (declaration != null) {
            // Use AST node lookup to find token (not just PSI children)
            // This is the same pattern used in TcssVariableDeclaration#getVariableToken()
            ASTNode colonNode = declaration.getNode().findChildByType(TcssTokenTypes.COLON);
            PsiElement colon = colonNode != null ? colonNode.getPsi() : null;

            // Property name context ONLY if colon not found OR element is BEFORE colon
            // Being AT the colon (or after) means ready to type value
            if (colon == null || element.getTextOffset() < colon.getTextOffset()) {
                return true;
            }

            // If colon exists and element is AFTER colon → NOT property name context
            // This handles "border: <Ctrl+Space>" case where PropertyValue hasn't been created yet
            return false;
        }

        // If inside rule set but not inside any property declaration → property name context
        TcssRuleSet ruleSet = PsiTreeUtil.getParentOfType(element, TcssRuleSet.class);
        if (ruleSet == null) {
            return false;
        }
        // Avoid triggering inside selector list
        for (PsiElement selector : ruleSet.getSelectors()) {
            if (PsiTreeUtil.isAncestor(selector, element, false)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPropertyValueContext(@NotNull PsiElement element) {
        TcssPropertyValue parent = PsiTreeUtil.getParentOfType(element, TcssPropertyValue.class);
        return parent != null;
    }

    private void contributePropertyNames(@NotNull CompletionParameters parameters,
                                         @NotNull CompletionResultSet result) {
        String prefix = CompletionUtil.findReferenceOrAlphanumericPrefix(parameters);
        CompletionResultSet target = prefix.isEmpty() ? result : result.withPrefixMatcher(prefix);
        for (TcssPropertyInfo info : TcssPropertyCatalog.getAll()) {
            target.addElement(LookupElementBuilder.create(info.getName())
                .withTypeText(info.getValueType().name(), true)
                .withTailText(" — " + info.getDescription(), true));
        }
    }

    private void contributePropertyValues(@NotNull CompletionParameters parameters,
                                          @NotNull CompletionResultSet result) {
        PsiFile file = parameters.getOriginalFile();
        String prefix = CompletionUtil.findReferenceOrAlphanumericPrefix(parameters);
        CompletionResultSet target = prefix.isEmpty() ? result : result.withPrefixMatcher(prefix);

        // Get property context
        PsiElement element = parameters.getOriginalPosition() != null ? parameters.getOriginalPosition() : parameters.getPosition();
        TcssPropertyDeclaration declaration = PsiTreeUtil.getParentOfType(element, TcssPropertyDeclaration.class);

        if (declaration != null) {
            String propertyName = declaration.getPropertyNameText();

            // Check if property has enum values
            Set<String> enumValues = TcssConstants.getValidEnumValues(propertyName);
            if (enumValues != null && !enumValues.isEmpty()) {
                // Enum property - show ONLY enum values (no variables, defer typed variables)
                addEnumValueSuggestions(target, declaration);
                return;
            }

            // Non-enum property - always show variables
            addVariableSuggestions(file, target);

            // Check if property is COLOR type
            TcssPropertyInfo propertyInfo = TcssPropertyCatalog.get(propertyName);
            if (propertyInfo != null && propertyInfo.getValueType() == TcssPropertyInfo.ValueType.COLOR) {
                // COLOR property - show colors too
                addColorSuggestionsWithSettings(parameters, target);
            }
            return;
        }

        // No property context - show variables and colors as fallback
        addVariableSuggestions(file, target);
        addColorSuggestionsWithSettings(parameters, target);
    }

    /**
     * Add named color suggestions with user settings consideration.
     */
    private void addColorSuggestionsWithSettings(@NotNull CompletionParameters parameters,
                                                  @NotNull CompletionResultSet target) {
        String prefix = CompletionUtil.findReferenceOrAlphanumericPrefix(parameters);

        // Check if user actually typed $ before the prefix
        int offset = parameters.getOffset();
        Document document = parameters.getEditor().getDocument();

        // Calculate position BEFORE the prefix (not just before current offset)
        int checkOffset = offset - prefix.length() - 1;
        char charBefore = checkOffset >= 0 ? document.getCharsSequence().charAt(checkOffset) : ' ';
        boolean typedDollar = charBefore == '$';

        // Check setting for named colors when $ is typed
        TcssPluginSettings settings = TcssPluginSettings.getInstance();
        boolean showAllColorsWithDollar = settings.showAllColorsWithDollarPrefix;

        // If setting is disabled and user typed $, skip named colors
        if (!showAllColorsWithDollar && typedDollar) {
            return;
        }

        // Show named colors (with smart $ removal if needed)
        addNamedColorSuggestions(target);
    }

    private void addVariableSuggestions(@NotNull PsiFile file, @NotNull CompletionResultSet result) {
        Set<String> seen = new HashSet<>();

        // Local variables first (higher priority)
        Collection<TcssVariableDeclaration> localDeclarations = VariableResolver.findAllDeclarations(file);
        for (TcssVariableDeclaration declaration : localDeclarations) {
            String name = declaration.getVariableName();
            if (name.isEmpty()) {
                continue;
            }
            if (seen.add(name)) {
                // Resolve color and create preview icon
                Color color = VariableResolver.resolveColor(name, file);
                Icon icon = ColorIconProvider.createColorIcon(color);

                result.addElement(LookupElementBuilder.create("$" + name)
                    .withIcon(icon)
                    .withInsertHandler(REMOVE_DOLLAR_PREFIX_HANDLER)
                    .withTypeText("variable (local)", true)
                    .withTailText(" → " + declaration.getVariableNameWithPrefix(), true));
            }
        }

        // Cross-file variables (lower priority)
        Project project = file.getProject();
        Map<String, Collection<TcssVariableDeclaration>> crossFileVars =
                VariableResolver.getAllDeclarationsCrossFile(project);

        for (Map.Entry<String, Collection<TcssVariableDeclaration>> entry : crossFileVars.entrySet()) {
            String name = entry.getKey();
            if (seen.add(name)) {
                Collection<TcssVariableDeclaration> decls = entry.getValue();
                if (!decls.isEmpty()) {
                    TcssVariableDeclaration first = decls.iterator().next();
                    PsiFile sourceFile = first.getContainingFile();
                    String fileName = sourceFile != null ? sourceFile.getName() : "unknown";

                    // Resolve color for preview icon
                    // Use cross-file resolution to respect local shadowing
                    Color color = VariableResolver.resolveColorCrossFile(name, file);
                    Icon icon = ColorIconProvider.createColorIcon(color);

                    result.addElement(LookupElementBuilder.create("$" + name)
                        .withIcon(icon)
                        .withInsertHandler(REMOVE_DOLLAR_PREFIX_HANDLER)
                        .withTypeText("variable (" + fileName + ")", true)
                        .withTailText(" (project-wide)", true));
                }
            }
        }
    }

    private void addNamedColorSuggestions(@NotNull CompletionResultSet result) {
        Set<String> keywords = io.textual.tcss.color.NamedColors.getAllColorNames();
        for (String keyword : keywords) {
            // Get the color and create preview icon
            Color color = io.textual.tcss.color.NamedColors.getColorByName(keyword);
            Icon icon = ColorIconProvider.createColorIcon(color);

            result.addElement(LookupElementBuilder.create(keyword)
                .withIcon(icon)
                .withTypeText("color", true)
                .withInsertHandler(REMOVE_DOLLAR_PREFIX_HANDLER));
        }
        result.addElement(LookupElementBuilder.create("auto")
            .withTypeText("keyword", true)
            .withInsertHandler(REMOVE_DOLLAR_PREFIX_HANDLER));
    }

    /**
     * Add enum value completions based on the current property being edited.
     * For properties with restricted value sets (e.g., display: block|grid|hidden),
     * suggest the valid enum values with descriptions from Textual documentation.
     */
    private void addEnumValueSuggestions(@NotNull CompletionResultSet result,
                                         @NotNull TcssPropertyDeclaration declaration) {
        String propertyName = declaration.getPropertyNameText();
        if (propertyName.isEmpty()) {
            return;
        }

        Set<String> validValues = TcssConstants.getValidEnumValues(propertyName);
        if (validValues == null || validValues.isEmpty()) {
            return;
        }

        for (String value : validValues) {
            // Try to get rich description from generated documentation
            String description = TcssPropertyDocumentation.getEnumValueDescription(propertyName, value);

            LookupElementBuilder element = LookupElementBuilder.create(value)
                .withTypeText("enum value", true);

            // Use rich description if available, otherwise fall back to generic message
            if (description != null && !description.isEmpty()) {
                element = element.withTailText(" — " + description, true);
            } else {
                element = element.withTailText(" — valid for " + propertyName, true);
            }

            result.addElement(element);
        }
    }
}
