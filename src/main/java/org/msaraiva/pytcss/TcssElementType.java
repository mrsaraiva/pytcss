package org.msaraiva.pytcss;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TcssElementType extends IElementType {
    public TcssElementType(@NotNull @NonNls String debugName) {
        super(debugName, TcssLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "TcssElementType." + super.toString();
    }
}
