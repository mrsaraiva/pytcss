package org.msaraiva.pytcss.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * UI component for TCSS plugin settings.
 * Provides a checkbox to control completion behavior when typing $.
 */
public class TcssSettingsComponent {
    private final JPanel mainPanel;
    private final JBCheckBox showAllColorsCheckBox;

    public TcssSettingsComponent() {
        showAllColorsCheckBox = new JBCheckBox("Show all colors when typing $");
        showAllColorsCheckBox.setToolTipText(
            "When enabled, typing $ shows both variables and named colors. " +
            "Named colors have $ automatically removed when selected."
        );

        // Create explanatory label with HTML formatting
        JBLabel explanationLabel = new JBLabel(
            "<html><body style='width: 500px; margin-top: 5px;'>" +
            "<b>When enabled:</b> Typing <code>$</code> shows both variables and named colors. " +
            "Named colors (like <code>seashell</code>, <code>seagreen</code>) will have the <code>$</code> automatically removed when selected." +
            "<br><br>" +
            "<b>When disabled:</b> Typing <code>$</code> only shows variables. " +
            "Named colors won't appear in the completion list after <code>$</code>." +
            "</body></html>"
        );

        mainPanel = FormBuilder.createFormBuilder()
            .addComponent(showAllColorsCheckBox, 0)
            .addComponentToRightColumn(explanationLabel, 0)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
    }

    @NotNull
    public JPanel getPanel() {
        return mainPanel;
    }

    @NotNull
    public JComponent getPreferredFocusedComponent() {
        return showAllColorsCheckBox;
    }

    public boolean getShowAllColorsWithDollarPrefix() {
        return showAllColorsCheckBox.isSelected();
    }

    public void setShowAllColorsWithDollarPrefix(boolean value) {
        showAllColorsCheckBox.setSelected(value);
    }
}
