package io.textual.tcss.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import io.textual.tcss.index.TcssVariableIndex;
import io.textual.tcss.psi.TcssColorValue;
import io.textual.tcss.psi.TcssVariableDeclaration;
import io.textual.tcss.psi.TcssVariableReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.*;

/**
 * Utility for resolving TCSS variable references to their color values.
 *
 * <p>Semantics:
 * <ul>
 *   <li>File-scope only (variables declared at top level)</li>
 *   <li>Last declaration wins (if variable redefined)</li>
 *   <li>Recursive resolution up to MAX_DEPTH</li>
 *   <li>Circular references return null</li>
 *   <li>Undefined variables return null (no errors - validation is separate feature)</li>
 * </ul>
 *
 * <p>Note: This resolver does not cache results. TCSS files are typically small (50-200 lines),
 * so linear scanning is fast enough. Caching can be added later if profiling shows need.
 */
public class VariableResolver {
    /**
     * Maximum recursion depth for variable resolution.
     * Prevents stack overflow on deep variable chains.
     */
    private static final int MAX_DEPTH = 10;

    /**
     * Resolve a variable name to a color within a file scope.
     *
     * @param variableName Variable name (without $)
     * @param scope        File containing the variable reference
     * @return Resolved color or null if:
     *         - Variable is undefined
     *         - Variable value is not a color
     *         - Circular reference detected
     *         - Maximum depth exceeded
     */
    @Nullable
    public static Color resolveColor(@NotNull String variableName, @NotNull PsiFile scope) {
        if (variableName.isEmpty()) {
            return null;
        }

        TcssVariableDeclaration declaration = findDeclaration(variableName, scope);
        if (declaration == null) {
            return null;
        }

        // Track visited variables to detect circular references
        Set<String> visited = new HashSet<>();
        return resolveColorRecursive(declaration, visited, 0);
    }

    /**
     * Find the declaration for a variable name.
     * Last declaration wins if multiple exist.
     *
     * @param variableName Variable name (without $)
     * @param scope        File to search
     * @return Variable declaration or null if not found
     */
    @Nullable
    public static TcssVariableDeclaration findDeclaration(@NotNull String variableName,
                                                           @NotNull PsiFile scope) {
        Collection<TcssVariableDeclaration> declarations =
                PsiTreeUtil.findChildrenOfType(scope, TcssVariableDeclaration.class);

        TcssVariableDeclaration lastDeclaration = null;
        for (TcssVariableDeclaration decl : declarations) {
            String declName = decl.getVariableName();
            if (declName.isEmpty()) {
                continue;
            }

            if (variableName.equals(declName)) {
                lastDeclaration = decl;  // Keep last one (last declaration wins)
            }
        }

        return lastDeclaration;
    }

    /**
     * Recursively resolve color value from a declaration.
     * Handles variable references, max depth, and circular detection.
     *
     * @param declaration Variable declaration to resolve
     * @param visited     Set of visited variable names (circular detection)
     * @param depth       Current recursion depth
     * @return Resolved color or null
     */
    @Nullable
    private static Color resolveColorRecursive(@NotNull TcssVariableDeclaration declaration,
                                                @NotNull Set<String> visited,
                                                int depth) {
        // Max depth check - prevents stack overflow
        if (depth >= MAX_DEPTH) {
            return null;
        }

        // Circular reference check
        String variableName = declaration.getVariableName();
        if (variableName.isEmpty()) {
            return null;
        }

        if (visited.contains(variableName)) {
            return null;  // Circular reference detected
        }
        visited.add(variableName);

        // Get the color value
        TcssColorValue colorValue = declaration.getColorValue();
        if (colorValue == null) {
            return null;  // Variable value is not color-related
        }

        // If it's a variable reference, recurse
        if (colorValue instanceof TcssVariableReference) {
            TcssVariableReference varRef = (TcssVariableReference) colorValue;
            String referencedVar = varRef.getVariableName();

            // Find the referenced declaration
            PsiFile containingFile = declaration.getContainingFile();
            if (containingFile == null) {
                return null;
            }

            TcssVariableDeclaration referencedDecl = findDeclaration(referencedVar, containingFile);
            if (referencedDecl == null) {
                return null;  // Undefined variable
            }

            // Recurse with incremented depth and same visited set
            return resolveColorRecursive(referencedDecl, visited, depth + 1);
        }

        // Direct color value - resolve it
        return colorValue.resolveColor();
    }

    /**
     * Get all variable declarations in a file as a map.
     * Useful for code completion and validation features.
     *
     * @param scope File to search
     * @return Map of variable name → declaration (last declaration wins)
     */
    @NotNull
    public static Map<String, TcssVariableDeclaration> getAllDeclarations(@NotNull PsiFile scope) {
        Collection<TcssVariableDeclaration> declarations =
                PsiTreeUtil.findChildrenOfType(scope, TcssVariableDeclaration.class);

        // Use LinkedHashMap to preserve order
        Map<String, TcssVariableDeclaration> result = new LinkedHashMap<>();
        for (TcssVariableDeclaration decl : declarations) {
            String name = decl.getVariableName();
            if (!name.isEmpty()) {
                result.put(name, decl);  // Last declaration wins
            }
        }

        return result;
    }

    @NotNull
    public static Collection<TcssVariableDeclaration> findAllDeclarations(@NotNull PsiFile scope) {
        return getAllDeclarations(scope).values();
    }

    @NotNull
    public static Collection<TcssVariableReference> findReferences(@NotNull TcssVariableDeclaration declaration) {
        PsiFile file = declaration.getContainingFile();
        if (file == null) {
            return Collections.emptyList();
        }

        Collection<TcssVariableReference> references = PsiTreeUtil.findChildrenOfType(file, TcssVariableReference.class);
        List<TcssVariableReference> result = new ArrayList<>();
        for (TcssVariableReference reference : references) {
            if (declaration.equals(reference.resolveDeclaration())) {
                result.add(reference);
            }
        }
        return result;
    }

    // ========== Cross-File Resolution Methods ==========

    /**
     * Resolve variable color with cross-file support.
     * Checks local file first (shadowing), then searches project-wide.
     *
     * @param variableName Variable name (without $)
     * @param contextFile File containing the reference
     * @return Resolved color or null
     */
    @Nullable
    public static Color resolveColorCrossFile(@NotNull String variableName, @NotNull PsiFile contextFile) {
        if (variableName.isEmpty()) {
            return null;
        }

        // 1. Check local file first (shadowing semantics)
        TcssVariableDeclaration localDecl = findDeclaration(variableName, contextFile);
        if (localDecl != null) {
            Set<String> visited = new HashSet<>();
            return resolveColorRecursive(localDecl, visited, 0);
        }

        // 2. Search project-wide via index
        Project project = contextFile.getProject();
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        Collection<TcssVariableDeclaration> declarations =
                TcssVariableIndex.findDeclarations(variableName, project, scope);

        // Return first valid color (arbitrary but deterministic order)
        for (TcssVariableDeclaration decl : declarations) {
            Set<String> visited = new HashSet<>();
            Color color = resolveColorRecursive(decl, visited, 0);
            if (color != null) {
                return color;
            }
        }

        return null;
    }

    /**
     * Find all declarations of a variable across the entire project.
     * Used for conflict detection and validation.
     *
     * @param variableName Variable name (without $)
     * @param project Current project
     * @return All declarations with this name across all files
     */
    @NotNull
    public static Collection<TcssVariableDeclaration> findDeclarationsCrossFile(
            @NotNull String variableName,
            @NotNull Project project) {
        if (variableName.isEmpty()) {
            return Collections.emptyList();
        }

        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        return TcssVariableIndex.findDeclarations(variableName, project, scope);
    }

    /**
     * Get all variable declarations across the project.
     * Returns a map of variable name → collection of declarations.
     * Used for project-wide completion.
     *
     * <p>Optimized to load each file only once, even if it declares multiple variables.
     *
     * @param project Current project
     * @return Map of variable name to all declarations with that name
     */
    @NotNull
    public static Map<String, Collection<TcssVariableDeclaration>> getAllDeclarationsCrossFile(
            @NotNull Project project) {
        Map<String, Collection<TcssVariableDeclaration>> result = new LinkedHashMap<>();

        // Get all variable names and collect unique files
        Collection<String> varNames = TcssVariableIndex.getAllVariableNames(project);
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        Set<com.intellij.openapi.vfs.VirtualFile> allFiles = new HashSet<>();

        // Collect all files that declare any variable
        for (String varName : varNames) {
            Collection<com.intellij.openapi.vfs.VirtualFile> files =
                    TcssVariableIndex.getFilesDeclaringVariable(varName, scope);
            allFiles.addAll(files);
        }

        // Load each file once and extract all declarations
        com.intellij.psi.PsiManager psiManager = com.intellij.psi.PsiManager.getInstance(project);
        for (com.intellij.openapi.vfs.VirtualFile virtualFile : allFiles) {
            PsiFile psiFile = psiManager.findFile(virtualFile);
            if (psiFile == null) {
                continue;
            }

            // Get all declarations from this file
            Collection<TcssVariableDeclaration> declarations =
                    PsiTreeUtil.findChildrenOfType(psiFile, TcssVariableDeclaration.class);

            // Group by variable name
            for (TcssVariableDeclaration decl : declarations) {
                String varName = decl.getVariableName();
                if (!varName.isEmpty()) {
                    result.computeIfAbsent(varName, k -> new ArrayList<>()).add(decl);
                }
            }
        }

        return result;
    }
}
