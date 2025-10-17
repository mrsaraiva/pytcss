package io.textual.tcss;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class TcssFile extends PsiFileBase {
    public TcssFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, TcssLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return TcssFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Textual CSS File";
    }
}
