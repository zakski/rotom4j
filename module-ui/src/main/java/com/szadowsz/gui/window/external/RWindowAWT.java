package com.szadowsz.gui.window.external;

import processing.core.PConstants;

/**
 * Class for an External Window using the default JAVA2D renderer.
 */
public class RWindowAWT extends RWindowExt {

    /**
     * Constructor for External JAVA2D Window
     *
     * @param title title to give the window
     * @param xPos initial X display location on Screen
     * @param yPos initial Y display location on Screen
     * @param width initial window width
     * @param height initial window height
     */
    public RWindowAWT(String title, int xPos, int yPos, int width, int height) {
        super(title, xPos, yPos, width, height);
        renderer = PConstants.JAVA2D;
    }
}
