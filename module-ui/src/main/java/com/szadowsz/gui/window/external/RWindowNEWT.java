package com.szadowsz.gui.window.external;

import processing.core.PConstants;

/**
 * Class for an External Window using the 3D or 2D Processing JOGL renderer.
 */
public class RWindowNEWT extends RWindowExt {

    /**
     * Constructor for External OpenGL (JOGL) Window
     *
     * @param title title to give the window
     * @param xPos initial X display location on Screen
     * @param yPos initial Y display location on Screen
     * @param width initial window width
     * @param height initial window height
     * @param is3D if true use P3D renderer, otherwise P2D
     */
    public RWindowNEWT(String title, int xPos, int yPos, int width, int height, boolean is3D) {
        super(title, xPos, yPos, width, height);
        this.is3D = is3D;
        renderer = is3D ? PConstants.P3D : PConstants.P2D;
    }
}
