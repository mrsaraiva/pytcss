package io.textual.tcss.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Persistent settings for the TCSS plugin.
 * Settings are stored application-wide and persist across IDE restarts.
 */
@State(
    name = "io.textual.tcss.settings.TcssPluginSettings",
    storages = @Storage("TcssPluginSettings.xml")
)
public class TcssPluginSettings implements PersistentStateComponent<TcssPluginSettings> {
    /**
     * When true, typing "$" in completion will show both variables AND named colors.
     * Named colors will have the "$" automatically removed when selected.
     *
     * When false, typing "$" will only show variables.
     *
     * Default: true (show all colors, with smart removal)
     */
    public boolean showAllColorsWithDollarPrefix = true;

    /**
     * Get the application-wide settings instance.
     */
    @NotNull
    public static TcssPluginSettings getInstance() {
        return ApplicationManager.getApplication().getService(TcssPluginSettings.class);
    }

    @Nullable
    @Override
    public TcssPluginSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TcssPluginSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
