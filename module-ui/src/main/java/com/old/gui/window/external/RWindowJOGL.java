package com.old.gui.window.external;

import com.old.gui.RotomGuiSettings;
import processing.core.PConstants;

/**
 * Class for an External Window using the 3D or 2D Processing JOGL renderer.
 */
public class RWindowJOGL extends RWindowExt {

    /**
     * Constructor for External OpenGL (JOGL) Window
     *
     * @param title    title to give the window
     * @param xPos     initial X display location on Screen
     * @param yPos     initial Y display location on Screen
     * @param width    initial window width
     * @param height   initial window height
     * @param settings
     * @param is3D     if true use P3D renderer, otherwise P2D
     */
    public RWindowJOGL(String title, int xPos, int yPos, int width, int height, RotomGuiSettings settings, boolean is3D) {
        super(title, xPos, yPos, width, height, settings);
        this.is3D = is3D;
        renderer = is3D ? PConstants.P3D : PConstants.P2D;
    }
}
