package com.szadowsz.gui.config.theme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * Configuration Class For A GUI Theme
 */
public class RTheme {
    private static final Logger LOGGER = LoggerFactory.getLogger(RTheme.class);

    public Color windowBorder; // TODO LazyGui

    public Color normalBackground; // TODO LazyGui
    public Color focusBackground; // TODO LazyGui

    public Color normalForeground; // TODO LazyGui
    public Color focusForeground; // TODO LazyGui

    /**
     * The only available constructor for this class.
     * Enforces specifying all the available values as parameters.
     *
     * @param windowBorderColor color of the border lines
     * @param normalBackgroundColor background of idle elements
     * @param focusBackgroundColor background of currently selected elements
     * @param normalForegroundColor foreground of idle elements
     * @param focusForegroundColor foreground of currently selected elements
     */
    public RTheme(int windowBorderColor, int normalBackgroundColor, int focusBackgroundColor, int normalForegroundColor, int focusForegroundColor) {
        this.windowBorder = new Color(windowBorderColor);
        this.normalBackground =  new Color(normalBackgroundColor);
        this.focusBackground =  new Color(focusBackgroundColor);
        this.normalForeground =  new Color(normalForegroundColor);
        this.focusForeground =  new Color(focusForegroundColor);
    }
}
