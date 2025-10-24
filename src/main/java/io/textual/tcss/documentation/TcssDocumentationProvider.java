package io.textual.tcss.documentation;

import com.intellij.lang.documentation.DocumentationProvider;
//import com.intellij.lang.documentation.LanguageDocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import io.textual.tcss.TcssTokenTypes;
import io.textual.tcss.metadata.TcssPropertyCatalog;
import io.textual.tcss.metadata.TcssPropertyInfo;
import io.textual.tcss.psi.TcssPropertyDeclaration;
import io.textual.tcss.psi.TcssRuleSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Provides quick documentation for TCSS properties.
 */
public class TcssDocumentationProvider implements DocumentationProvider {
    @Override
    public @Nullable PsiElement getCustomDocumentationElement(@NotNull com.intellij.openapi.editor.Editor editor,
                                                               @NotNull PsiFile file,
                                                               @Nullable PsiElement contextElement,
                                                               int targetOffset) {
        // Return the element that documentation should be generated for
        if (contextElement == null) {
            return null;
        }

        // If we're on a PROPERTY_NAME token, return it directly
        if (contextElement.getNode() != null && contextElement.getNode().getElementType() == TcssTokenTypes.PROPERTY_NAME) {
            return contextElement;
        }

        // If we're inside a property declaration, find and return the property name
        TcssPropertyDeclaration declaration = findDeclaration(contextElement);
        if (declaration != null) {
            PsiElement propertyName = declaration.getPropertyName();
            if (propertyName != null) {
                return propertyName;
            }
        }

        return contextElement;
    }

    @Override
    public @Nullable String generateDoc(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        String propertyName = extractPropertyName(element, originalElement);
        if (propertyName == null) {
            return null;
        }

        TcssPropertyInfo info = TcssPropertyCatalog.get(propertyName);
        if (info == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<h3>").append(info.getName()).append("</h3>");
        builder.append("<p>").append(info.getDescription()).append("</p>");
        builder.append("<p><b>Value type:</b> ").append(info.getValueType().name()).append("</p>");

        // Show type documentation link if available
        if (info.getTypeDocUrl() != null) {
            builder.append("<p><a href=\"").append(info.getTypeDocUrl()).append("\">Textual CSS Type documentation ↗</a></p>");
        }

        // Show property documentation link
        if (info.getPropertyDocUrl() != null) {
            builder.append("<p><a href=\"").append(info.getPropertyDocUrl()).append("\">Textual CSS Property documentation ↗</a></p>");
        }
        return builder.toString();
    }

    @Override
    public String getQuickNavigateInfo(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        String propertyName = extractPropertyName(element, originalElement);
        if (propertyName == null) {
            return null;
        }

        TcssPropertyInfo info = TcssPropertyCatalog.get(propertyName);
        if (info == null) {
            return null;
        }

        return info.getName() + " — " + info.getDescription();
    }

    @Override
    public @Nullable PsiElement getDocumentationElementForLookupItem(@NotNull PsiManager psiManager,
                                                                      @NotNull Object object,
                                                                      @Nullable PsiElement element) {
        return element;
    }

    @Override
    public @Nullable PsiElement getDocumentationElementForLink(@NotNull PsiManager psiManager,
                                                                @NotNull String link,
                                                                @Nullable PsiElement context) {
        if (context == null) {
            return null;
        }
        PsiFile file = context.getContainingFile();
        if (file == null) {
            return null;
        }
        return findPropertyDeclaration(file, link);
    }

    @Nullable
    private PsiElement findPropertyDeclaration(@NotNull PsiFile file, @NotNull String name) {
        for (PsiElement child : file.getChildren()) {
            PsiElement result = findPropertyDeclaration(child, name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Nullable
    private PsiElement findPropertyDeclaration(@NotNull PsiElement element, @NotNull String name) {
        if (element instanceof TcssPropertyDeclaration) {
            if (((TcssPropertyDeclaration) element).getPropertyNameText().equalsIgnoreCase(name)) {
                return element;
            }
        }
        for (PsiElement child : element.getChildren()) {
            PsiElement result = findPropertyDeclaration(child, name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Nullable
    private String extractPropertyName(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        PsiElement target = originalElement != null ? originalElement : element;

        if (target.getNode() != null && target.getNode().getElementType() == TcssTokenTypes.PROPERTY_NAME) {
            String text = target.getText();
            return text != null && !text.isBlank() ? text.toLowerCase(Locale.US) : null;
        }

        TcssPropertyDeclaration declaration = findDeclaration(target);
        if (declaration != null) {
            String name = declaration.getPropertyNameText();
            if (!name.isEmpty()) {
                return name.toLowerCase(Locale.US);
            }
        }

        String text = target.getText();
        if (text != null) {
            text = text.trim();
            if (text.endsWith(":")) {
                text = text.substring(0, text.length() - 1);
            }
            if (!text.isEmpty()) {
                return text.toLowerCase(Locale.US);
            }
        }
        return null;
    }

    @Nullable
    private TcssPropertyDeclaration findDeclaration(@NotNull PsiElement element) {
        PsiElement current = element;
        while (current != null && !(current instanceof TcssPropertyDeclaration)) {
            current = current.getParent();
        }
        return current instanceof TcssPropertyDeclaration ? (TcssPropertyDeclaration) current : null;
    }
}
