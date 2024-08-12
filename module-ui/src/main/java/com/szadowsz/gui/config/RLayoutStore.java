package com.szadowsz.gui.config;

public class RLayoutStore {
    private static float cell = 22; // cell size

    // Slider Config
    private static boolean displaySquigglyEquals = false;

    public static float getCell() {
        return cell;
    }

    public static boolean shouldDisplaySquigglyEquals() {
        return displaySquigglyEquals;
    }
}
