package io.textual.tcss.templates;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import io.textual.tcss.TcssLanguage;
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
