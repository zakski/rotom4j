package com.szadowsz.gui.config.theme;

import java.awt.*;

/**
 * RThemePair is the lowest entry in the theme hierarchy, containing the actual colors to use. When drawing a
 * component, you would grab the current {@link RTheme} that applies to the GUI and then choose a
 * foreground/background {@link RThemePair} based on the context.
 */
public class RThemePair {
    private final Color foreground;
    private final Color background;

    public RThemePair(Color foreground, Color background) {
        this.foreground = foreground;
        this.background = background;
    }

    public RThemePair(int foreground, int background) {
        this(new Color(foreground), new Color(background));
      }
    /**
     * Returns the foreground color associated with this style
     *
     * @return foreground color associated with this style
     */
    Color getForeground() {
        return foreground;
    }

    /**
     * Returns the background color associated with this style
     *
     * @return background color associated with this style
     */
    Color getBackground() {
        return background;
    }
}
