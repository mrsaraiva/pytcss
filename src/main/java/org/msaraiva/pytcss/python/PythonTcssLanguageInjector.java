package org.msaraiva.pytcss.python;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.jetbrains.python.psi.PyAssignmentStatement;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.psi.PyTargetExpression;
import org.msaraiva.pytcss.TcssLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Injects TCSS language into Python string literals assigned to CSS/DEFAULT_CSS attributes.
 */
public class PythonTcssLanguageInjector implements MultiHostInjector {
    private static final Set<String> SUPPORTED_TARGETS = new HashSet<>();

    static {
        SUPPORTED_TARGETS.add("CSS");
        SUPPORTED_TARGETS.add("DEFAULT_CSS");
    }

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (!(context instanceof PyStringLiteralExpression)) {
            return;
        }

        PyStringLiteralExpression stringLiteral = (PyStringLiteralExpression) context;
        if (!isEligible(stringLiteral)) {
            return;
        }

        List<Pair<TextRange, String>> fragments = stringLiteral.getDecodedFragments();
        if (fragments.isEmpty()) {
            return;
        }

        registrar.startInjecting(TcssLanguage.INSTANCE);
        for (Pair<TextRange, String> fragment : fragments) {
            registrar.addPlace(null, null, (PsiLanguageInjectionHost) stringLiteral, fragment.first);
        }
        registrar.doneInjecting();
    }

    private boolean isEligible(@NotNull PyStringLiteralExpression expression) {
        String value = expression.getStringValue();
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        PyAssignmentStatement assignment = findEnclosingAssignment(expression);
        if (assignment == null) {
            return false;
        }

        PyExpression assignedValue = assignment.getAssignedValue();
        if (assignedValue != expression) {
            return false;
        }

        for (PyExpression target : assignment.getTargets()) {
            if (target instanceof PyTargetExpression) {
                String name = ((PyTargetExpression) target).getName();
                if (name != null && SUPPORTED_TARGETS.contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private PyAssignmentStatement findEnclosingAssignment(@NotNull PyStringLiteralExpression expression) {
        PsiElement parent = expression.getParent();
        while (parent != null && !(parent instanceof PyAssignmentStatement)) {
            parent = parent.getParent();
        }
        return parent instanceof PyAssignmentStatement ? (PyAssignmentStatement) parent : null;
    }

    @NotNull
    @Override
    public List<Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(PyStringLiteralExpression.class);
    }
}
