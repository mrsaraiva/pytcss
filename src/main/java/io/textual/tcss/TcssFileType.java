package io.textual.tcss;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TcssFileType extends LanguageFileType {
    public static final TcssFileType INSTANCE = new TcssFileType();
    private static final Icon ICON = IconLoader.getIcon("/icons/tcss.svg", TcssFileType.class);

    private TcssFileType() {
        super(TcssLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Textual CSS";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Textual CSS file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "tcss";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return ICON;
    }
}
