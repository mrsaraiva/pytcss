package io.textual.tcss.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Configurable for TCSS plugin settings.
 * Appears in Settings → Languages & Frameworks → Textual CSS.
 */
public class TcssSettingsConfigurable implements Configurable {
    private TcssSettingsComponent settingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Textual CSS";
    }

    @Override
    public @Nullable JComponent createComponent() {
        settingsComponent = new TcssSettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        TcssPluginSettings settings = TcssPluginSettings.getInstance();
        return settingsComponent.getShowAllColorsWithDollarPrefix() != settings.showAllColorsWithDollarPrefix;
    }

    @Override
    public void apply() {
        TcssPluginSettings settings = TcssPluginSettings.getInstance();
        settings.showAllColorsWithDollarPrefix = settingsComponent.getShowAllColorsWithDollarPrefix();
    }

    @Override
    public void reset() {
        TcssPluginSettings settings = TcssPluginSettings.getInstance();
        settingsComponent.setShowAllColorsWithDollarPrefix(settings.showAllColorsWithDollarPrefix);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
