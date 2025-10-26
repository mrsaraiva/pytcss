package org.msaraiva.pytcss;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.ui.JBColor;
import org.msaraiva.pytcss.color.ColorFormat;
import org.msaraiva.pytcss.psi.TcssColorKeyword;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Line marker provider for the "auto" keyword.
 * Shows a special icon (not a color preview) to indicate automatic color value.
 */
public class TcssAutoKeywordLineMarker implements LineMarkerProvider {
    /**
     * Get line marker info for an element.
     * Returns marker for "auto" keyword only.
     *
     * IMPORTANT: Line markers must be registered on LEAF elements only (tokens),
     * not on composite PSI elements. We check for parent TcssColorKeyword, but
     * register the marker on the leaf token element.
     */
    @Nullable
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        // Line markers should only be registered on leaf elements (tokens)
        // Skip if this is a composite element
        if (element.getFirstChild() != null) {
            return null;
        }

        // Check if parent is TcssColorKeyword
        PsiElement parent = element.getParent();
        if (!(parent instanceof TcssColorKeyword)) {
            return null;
        }

        TcssColorKeyword keyword = (TcssColorKeyword) parent;

        // Only show marker for "auto" keyword
        if (keyword.getColorFormat() != ColorFormat.AUTO) {
            return null;
        }

        // Create a special icon for "auto"
        Icon autoIcon = createAutoIcon();

        // Register marker on the leaf element, not the composite parent
        return new LineMarkerInfo<>(
                element,  // Leaf token element
                element.getTextRange(),
                autoIcon,
                null, // tooltip function
                null, // navigation handler
                GutterIconRenderer.Alignment.LEFT,
                () -> "Automatic color value"
        );
    }

    /**
     * Create a simple icon to represent "auto" keyword.
     * Uses a gray square with "A" text.
     */
    @NotNull
    private Icon createAutoIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                // Draw gray square background
                g.setColor(JBColor.GRAY);
                g.fillRect(x, y, 12, 12);

                // Draw white border
                g.setColor(JBColor.WHITE);
                g.drawRect(x, y, 11, 11);

                // Draw "A" text
                g.setFont(new Font("Sans-Serif", Font.BOLD, 9));
                g.drawString("A", x + 3, y + 10);
            }

            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 12;
            }
        };
    }
}
