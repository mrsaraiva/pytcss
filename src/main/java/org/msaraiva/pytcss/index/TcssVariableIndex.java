package org.msaraiva.pytcss.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.msaraiva.pytcss.TcssFileType;
import org.msaraiva.pytcss.psi.TcssVariableDeclaration;
import org.msaraiva.pytcss.util.VariableResolver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * File-based index for TCSS variable declarations.
 * Enables fast cross-file variable lookups by indexing variable names.
 *
 * <p>Index semantics:
 * <ul>
 *   <li>Maps variable name → files containing that declaration</li>
 *   <li>Only indexes top-level variable declarations</li>
 *   <li>Filters empty variable names</li>
 *   <li>Automatically invalidates on file changes</li>
 * </ul>
 */
public class TcssVariableIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> INDEX_ID = ID.create("TcssVariableIndex");
    private static final int VERSION = 2;

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return INDEX_ID;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return fileContent -> {
            Map<String, Void> result = new HashMap<>();
            PsiFile psiFile = fileContent.getPsiFile();

            // Only index TCSS files
            if (psiFile == null) {
                return result;
            }

            // Extract all variable declarations
            Collection<TcssVariableDeclaration> declarations =
                PsiTreeUtil.findChildrenOfType(psiFile, TcssVariableDeclaration.class);

            // Index variable names (filter empty names)
            for (TcssVariableDeclaration decl : declarations) {
                String name = decl.getVariableName();
                if (!name.isEmpty()) {
                    result.put(name, null); // ScalarIndexExtension uses null values
                }
            }

            return result;
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new DefaultFileTypeSpecificInputFilter(TcssFileType.INSTANCE);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    // ========== Public API for Index Queries ==========

    /**
     * Get all files containing a declaration for the given variable name.
     *
     * @param variableName Variable name (without $)
     * @param scope Search scope (typically project or module scope)
     * @return Collection of virtual files containing the variable
     */
    @NotNull
    public static Collection<VirtualFile> getFilesDeclaringVariable(
            @NotNull String variableName,
            @NotNull GlobalSearchScope scope) {
        return FileBasedIndex.getInstance()
                .getContainingFiles(INDEX_ID, variableName, scope);
    }

    /**
     * Get all variable names indexed in the project.
     * Useful for completion and validation.
     *
     * @param project Current project
     * @return Collection of all variable names across all TCSS files
     */
    @NotNull
    public static Collection<String> getAllVariableNames(@NotNull Project project) {
        return FileBasedIndex.getInstance()
                .getAllKeys(INDEX_ID, project);
    }

    /**
     * Find all declarations for a variable name across files in the given scope.
     * This resolves the index results to actual PSI elements.
     *
     * @param variableName Variable name (without $)
     * @param project Current project
     * @param scope Search scope
     * @return Collection of TcssVariableDeclaration elements
     */
    @NotNull
    public static Collection<TcssVariableDeclaration> findDeclarations(
            @NotNull String variableName,
            @NotNull Project project,
            @NotNull GlobalSearchScope scope) {
        if (variableName.isEmpty()) {
            return new ArrayList<>();
        }

        Collection<VirtualFile> files = getFilesDeclaringVariable(variableName, scope);
        Collection<TcssVariableDeclaration> result = new ArrayList<>();
        PsiManager psiManager = PsiManager.getInstance(project);

        for (VirtualFile file : files) {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile != null) {
                // Use existing VariableResolver to find declaration in each file
                // This handles "last declaration wins" within each file
                TcssVariableDeclaration decl = VariableResolver.findDeclaration(variableName, psiFile);
                if (decl != null) {
                    result.add(decl);
                }
            }
        }

        return result;
    }
}
