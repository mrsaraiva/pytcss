package io.textual.tcss;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TcssTokenType extends IElementType {
    public TcssTokenType(@NotNull @NonNls String debugName) {
        super(debugName, TcssLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "TcssTokenType." + super.toString();
    }
}
