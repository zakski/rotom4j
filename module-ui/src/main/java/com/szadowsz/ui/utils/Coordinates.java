package com.szadowsz.ui.utils;

public class Coordinates {

    private Coordinates(){/* NOOP*/}

    /**
     * Method to check if a point is inside the rect
     *
     * @param px point x position
     * @param py point y position
     * @param rx rectangle upper-left x position
     * @param ry rectangle upper-left y position
     * @param rw rectangle width
     * @param rh rectangle height
     * @return true if point is inside the rect, false otherwise
     */
    public static boolean isPointInRect(float px, float py, float rx, float ry, float rw, float rh) {
        return px > rx && px < rx + rw && py >= ry && py <= ry + rh;
    }
}
