package org.msaraiva.pytcss;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.msaraiva.pytcss.psi.TcssVariableDeclaration;
import org.msaraiva.pytcss.psi.TcssVariableReference;
import org.msaraiva.pytcss.util.VariableResolver;

import java.awt.Color;
import java.util.Collection;

/**
 * Tests for cross-file TCSS variable resolution.
 */
public class CrossFileVariableTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    public void testCrossFileColorResolution() {
        // Create colors.tcss with variable definition
        myFixture.addFileToProject("colors.tcss", "$primary: #0066cc;");

        // Create main.tcss that references the variable
        PsiFile mainFile = myFixture.configureByText("main.tcss",
                "Button {\n" +
                "    background: $primary;\n" +
                "}");

        // Find the variable reference
        TcssVariableReference ref = PsiTreeUtil.findChildOfType(mainFile, TcssVariableReference.class);
        assertNotNull("Variable reference should be found", ref);

        // Resolve color cross-file
        Color color = VariableResolver.resolveColorCrossFile("primary", mainFile);
        assertNotNull("Color should resolve cross-file", color);
        assertEquals("Color should match #0066cc", new Color(0x0066cc), color);
    }

    public void testLocalShadowing() {
        // Create colors.tcss with one definition
        myFixture.addFileToProject("colors.tcss", "$color: #0066cc;");

        // Create main.tcss with local override
        PsiFile mainFile = myFixture.configureByText("main.tcss",
                "$color: #ff0000;\n" +
                "Button {\n" +
                "    background: $color;\n" +
                "}");

        // Resolve should prefer local definition
        Color color = VariableResolver.resolveColorCrossFile("color", mainFile);
        assertNotNull("Color should resolve", color);
        assertEquals("Local variable should shadow cross-file", new Color(0xff0000), color);
    }

    public void testDuplicateDetection() {
        // Create two files with same variable
        myFixture.addFileToProject("file1.tcss", "$primary: #0066cc;");
        myFixture.addFileToProject("file2.tcss", "$primary: #ff0000;");

        Project project = getProject();
        Collection<TcssVariableDeclaration> decls =
                VariableResolver.findDeclarationsCrossFile("primary", project);

        assertTrue("Should find multiple declarations", decls.size() >= 2);
    }

    public void testUndefinedVariableError() {
        // Create file with undefined variable reference
        PsiFile mainFile = myFixture.configureByText("main.tcss",
                "Button {\n" +
                "    background: $undefined;\n" +
                "}");

        // Should not resolve
        Color color = VariableResolver.resolveColorCrossFile("undefined", mainFile);
        assertNull("Undefined variable should return null", color);
    }

    public void testCrossFileCompletion() {
        // Create colors.tcss with variables
        myFixture.addFileToProject("colors.tcss",
                "$primary: #0066cc;\n" +
                "$secondary: #ff6600;");

        // Create main.tcss with cursor position
        myFixture.configureByText("main.tcss",
                "Button {\n" +
                "    background: $<caret>\n" +
                "}");

        // Trigger completion
        myFixture.completeBasic();
        java.util.List<String> completions = myFixture.getLookupElementStrings();

        assertNotNull("Completions should be available", completions);
        assertTrue("Should include $primary", completions.contains("$primary"));
        assertTrue("Should include $secondary", completions.contains("$secondary"));
    }
}
