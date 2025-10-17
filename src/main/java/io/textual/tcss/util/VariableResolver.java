package io.textual.tcss.util;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
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
}
