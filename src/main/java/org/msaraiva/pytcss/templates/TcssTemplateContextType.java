package org.msaraiva.pytcss.templates;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import org.msaraiva.pytcss.TcssLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * Enables live templates inside TCSS files.
 */
public class TcssTemplateContextType extends TemplateContextType {
    public TcssTemplateContextType() {
        super("TCSS", "TCSS");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext context) {
        return context.getFile().getLanguage().isKindOf(TcssLanguage.INSTANCE);
    }
}
