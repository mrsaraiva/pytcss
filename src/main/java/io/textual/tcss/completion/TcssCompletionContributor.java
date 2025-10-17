package io.textual.tcss.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import io.textual.tcss.TcssLanguage;
import io.textual.tcss.TcssTokenTypes;
import io.textual.tcss.metadata.TcssPropertyCatalog;
import io.textual.tcss.metadata.TcssPropertyInfo;
import io.textual.tcss.psi.TcssPropertyDeclaration;
import io.textual.tcss.psi.TcssPropertyValue;
import io.textual.tcss.psi.TcssRuleSet;
import io.textual.tcss.psi.TcssVariableDeclaration;
import io.textual.tcss.util.VariableResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides basic code completion for TCSS property names, color keywords, and variables.
 */
public class TcssCompletionContributor extends CompletionContributor {
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
                    }
                }
            });
    }

    private boolean isPropertyNameContext(@NotNull PsiElement element) {
        if (PsiTreeUtil.getParentOfType(element, TcssPropertyValue.class) != null) {
            return false;
        }

        TcssPropertyDeclaration declaration = PsiTreeUtil.getParentOfType(element, TcssPropertyDeclaration.class);
        if (declaration != null) {
            PsiElement colon = null;
            for (PsiElement child : declaration.getChildren()) {
                if (child.getNode().getElementType() == TcssTokenTypes.COLON) {
                    colon = child;
                    break;
                }
            }
            return colon == null || element.getTextOffset() <= colon.getTextOffset();
        }

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
        return PsiTreeUtil.getParentOfType(element, TcssPropertyValue.class) != null;
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
        addVariableSuggestions(file, target);
        addNamedColorSuggestions(target);
    }

    private void addVariableSuggestions(@NotNull PsiFile file, @NotNull CompletionResultSet result) {
        Set<String> seen = new HashSet<>();
        Collection<TcssVariableDeclaration> declarations = VariableResolver.findAllDeclarations(file);
        for (TcssVariableDeclaration declaration : declarations) {
            String name = declaration.getVariableName();
            if (name.isEmpty()) {
                continue;
            }
            if (seen.add(name)) {
                result.addElement(LookupElementBuilder.create("$" + name)
                    .withTypeText("variable", true)
                    .withTailText(" → " + declaration.getVariableNameWithPrefix(), true));
            }
        }
    }

    private void addNamedColorSuggestions(@NotNull CompletionResultSet result) {
        Set<String> keywords = io.textual.tcss.color.NamedColors.getAllColorNames();
        for (String keyword : keywords) {
            result.addElement(LookupElementBuilder.create(keyword)
                .withTypeText("color", true));
        }
        result.addElement(LookupElementBuilder.create("auto").withTypeText("keyword", true));
    }
}
