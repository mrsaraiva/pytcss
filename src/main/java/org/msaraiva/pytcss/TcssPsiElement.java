package org.msaraiva.pytcss;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class TcssPsiElement extends ASTWrapperPsiElement {
    public TcssPsiElement(@NotNull ASTNode node) {
        super(node);
    }
}
