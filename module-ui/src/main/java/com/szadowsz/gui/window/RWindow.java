package com.szadowsz.gui.window;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Base Interface for Internal and External Windows
 */
public interface RWindow {


    /**
     * Getter for Window's Current Height
     *
     * @return height of the Window
     */
    int getHeight();

    /**
     * Getter for Window's Current X Coordinate
     *
     * @return current X position
     */
    float getPosX();

    /**
     * Getter for Window's Current Y Coordinate
     *
     * @return current Y position
     */
    float getPosY();

    /**
     * Getter for Window's Current Coordinates
     *
     * @return PVector that represents the current 2D Coordinates of the Window
     */
    PVector getPos();

    /**
     * Getter for Window's Current Dimensions
     *
     * @return  PVector that represents the current dimensions of the window
     */
    PVector getSize();

    /**
     * Getter for Window's PApplet
     *
     * @return the app the Window is drawn in / represents
     */
    PApplet getSketch();

    /**
     * Getter for Window's Title
     *
     * @return the display name of the window
     */
    String getTitle();

    /**
     * Getter for Window's Current Width
     *
     * @return width of the Window
     */
    int getWidth();
}