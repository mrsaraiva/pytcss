package io.textual.tcss.structure;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import io.textual.tcss.TcssFile;
import io.textual.tcss.psi.TcssPropertyDeclaration;
import io.textual.tcss.psi.TcssRuleSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Structure view model for TCSS files.
 */
public class TcssStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
    public TcssStructureViewModel(@NotNull TcssFile file, @Nullable Editor editor) {
        super(file, editor, new TcssStructureViewElement(file));
        withSuitableClasses(TcssFile.class, TcssRuleSet.class, TcssPropertyDeclaration.class);
    }

    @NotNull
    @Override
    public Sorter @NotNull [] getSorters() {
        return new Sorter[]{Sorter.ALPHA_SORTER};
    }

    @Override
    public boolean isAlwaysShowsPlus(@NotNull StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(@NotNull StructureViewTreeElement element) {
        return false;
    }
}
