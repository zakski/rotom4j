package com.szadowsz.gui.config.theme;

import java.awt.*;

public class RTheme {
    private final RThemePair normal;
    private final RThemePair focused;
    private final RThemePair selected;
    private final RThemePair active;
    private final RThemePair disabled;
    private final RThemePair cursor;
    private final Color windowBorder;

    private RTheme(RThemePair normal, RThemePair focused, RThemePair cursor, Color border) {
        this.normal = normal;
        this.focused = focused;
        this.selected = focused;
        this.active = focused;
        this.disabled = normal;
        this.cursor = cursor;
        this.windowBorder = border;
    }
     RTheme(int normalBackgroundColor, int normalForegroundColor, int focusBackgroundColor, int focusForegroundColor, int windowBorderColor) {
        this(
                new RThemePair(normalForegroundColor,normalBackgroundColor),
                new RThemePair(focusForegroundColor,focusBackgroundColor),
                new RThemePair(focusForegroundColor,focusBackgroundColor),
                new Color(windowBorderColor)
        );
    }

    /**
     * The normal style of the theme, which can be considered the default to be used.
     *
     * @return RThemeStyle representation for the normal style
     */
    synchronized RThemePair getNormal() {
        return normal;
    }

    /**
     * The focused style of this theme, which can be used when a component has input focus but isn't active or
     * selected
     *
     * @return RThemeStyle representation for the focused style
     */
    synchronized RThemePair getFocused() {
        return focused;
    }

    /**
     * The "selected" style of this theme, which can used when a component has been actively selected in some way.
     *
     * @return RThemeStyle representation for the selected style
     */
    synchronized RThemePair getSelected() {
        return selected;
    }

    /**
     * The "active" style of this theme, which can be used when a component is being directly interacted with
     *
     * @return RThemeStyle representation for the active style
     */
    synchronized RThemePair getActive() {
        return active;
    }

    /**
     * The disabled style of this theme, which can be used when a component has been disabled or in some other
     * way isn't able to be interacted with.
     *
     * @return RThemeStyle representation for the insensitive style
     */
    synchronized RThemePair getDisabled() {
        return disabled;
    }

    /**
     * The style for the cursor of this theme, to be used when a component has the ability to modify text.
     *
     * @return RThemeStyle representation for the cursor
     */
    synchronized RThemePair getCursor() {
        return cursor;
    }

    public Color getWindowBorder() {
        return windowBorder;
    }
}
