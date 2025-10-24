package io.textual.tcss.completion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Provider for color preview icons in code completion.
 * Creates small 16x16 colored square icons with borders.
 */
public class ColorIconProvider {
    private static final int ICON_SIZE = 16;
    private static final int BORDER_SIZE = 1;

    // Cache for icons - named colors never change, so cache indefinitely
    private static final Map<Color, Icon> ICON_CACHE = new HashMap<>();

    /**
     * Create a 16x16 icon showing the given color.
     * Icons are cached for performance.
     *
     * @param color Color to display
     * @return Icon showing the color, or null if color is null
     */
    @Nullable
    public static Icon createColorIcon(@Nullable Color color) {
        if (color == null) {
            return null;
        }

        // Check cache first
        Icon cached = ICON_CACHE.get(color);
        if (cached != null) {
            return cached;
        }

        // Create new icon
        Icon icon = createIconInternal(color);
        ICON_CACHE.put(color, icon);
        return icon;
    }

    /**
     * Internal method to create the actual icon.
     */
    @NotNull
    private static Icon createIconInternal(@NotNull Color color) {
        BufferedImage image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // Enable anti-aliasing for smoother borders
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill with color
        g.setColor(color);
        g.fillRect(BORDER_SIZE, BORDER_SIZE,
                   ICON_SIZE - BORDER_SIZE * 2,
                   ICON_SIZE - BORDER_SIZE * 2);

        // Draw border - use a contrasting color based on brightness
        // For dark colors, use light border; for light colors, use dark border
        int brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        g.setColor(brightness > 128 ? new Color(100, 100, 100) : new Color(180, 180, 180));
        g.drawRect(0, 0, ICON_SIZE - 1, ICON_SIZE - 1);

        g.dispose();

        return new ImageIcon(image);
    }

    /**
     * Clear the icon cache. Useful for memory management in long-running IDEs.
     * Note: Named color icons are safe to cache indefinitely since colors never change.
     */
    public static void clearCache() {
        ICON_CACHE.clear();
    }

    /**
     * Get the current cache size (for debugging/monitoring).
     */
    public static int getCacheSize() {
        return ICON_CACHE.size();
    }
}
