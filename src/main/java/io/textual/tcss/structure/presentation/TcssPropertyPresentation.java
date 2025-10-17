package io.textual.tcss.structure.presentation;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import io.textual.tcss.TcssTokenTypes;
import io.textual.tcss.psi.TcssPropertyDeclaration;
import io.textual.tcss.psi.TcssRuleSet;
import io.textual.tcss.psi.TcssVariableDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class TcssPropertyPresentation implements ItemPresentation {
    private final ItemPresentation delegate;
    private final PsiElement element;

    private TcssPropertyPresentation(@Nullable ItemPresentation delegate, @NotNull PsiElement element) {
        this.delegate = delegate;
        this.element = element;
    }

    public static @NotNull ItemPresentation wrap(@NotNull PsiElement element) {
        ItemPresentation presentation = null;
        if (element instanceof NavigationItem) {
            presentation = ItemPresentationProviders.getItemPresentation((NavigationItem) element);
        }
        return new TcssPropertyPresentation(presentation, element);
    }

    private @NotNull String selectText() {
        if (element instanceof TcssPropertyDeclaration) {
            String name = ((TcssPropertyDeclaration) element).getPropertyNameText();
            return name.isEmpty() ? "<property>" : name;
        }
        if (element instanceof TcssVariableDeclaration) {
            String name = ((TcssVariableDeclaration) element).getVariableNameWithPrefix();
            return name.isEmpty() ? "<variable>" : name;
        }
        if (element instanceof TcssRuleSet) {
            return ((TcssRuleSet) element).getSelectorDisplayName();
        }
        if (delegate != null) {
            String text = delegate.getPresentableText();
            if (text != null && !text.isBlank() && !text.equals(TcssTokenTypes.PROPERTY_NAME.toString())) {
                return text;
            }
        }
        String fallback = element.getText();
        return fallback != null && !fallback.isBlank() ? fallback.trim() : "";
    }

    @Override
    public @NlsSafe String getPresentableText() {
        return selectText();
    }

    @Override
    public @Nullable String getLocationString() {
        return delegate != null ? delegate.getLocationString() : null;
    }

    @Override
    public @Nullable Icon getIcon(boolean unused) {
        if (delegate != null) {
            return delegate.getIcon(unused);
        }
        return element.getIcon(0);
    }
}
