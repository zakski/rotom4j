package com.szadowsz.gui.window.sketch;

import com.szadowsz.gui.RotomGuiSettings;
import processing.core.PConstants;

/**
 * Class for an External Window using the default JAVA2D renderer.
 */
public class RWindowAWT extends RWindowSketch {

    /**
     * Constructor for External JAVA2D Window
     *
     * @param title    title to give the window
     * @param xPos     initial X display location on Screen
     * @param yPos     initial Y display location on Screen
     * @param width    initial window width
     * @param height   initial window height
     * @param settings
     */
    public RWindowAWT(String title, int xPos, int yPos, int width, int height, RotomGuiSettings settings) {
        super(title, xPos, yPos, width, height, settings);
        renderer = PConstants.JAVA2D;
    }
}
