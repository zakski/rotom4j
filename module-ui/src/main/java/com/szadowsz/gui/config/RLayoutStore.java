package com.szadowsz.gui.config;

public class RLayoutStore {
    private static float cell = 22; // cell size

    // Slider Config
    private static boolean displaySquigglyEquals = false;

    public static float getCell() {
        return cell;
    }

    public static float getHorizontalSeparatorStrokeWeight() {
        return 0;
    }

    public static float getResizeRectangleSize() {
        return 0;
    }


    public static int getSmoothingValue() {
        return 0;
    }

    public static float getWindowWidthInCells() {
        return 0;
    }

    public static boolean shouldDisplaySquigglyEquals() {
        return displaySquigglyEquals;
    }


    public static boolean shouldDrawResizeIndicator() {
        return false;
    }

    public static boolean shouldKeepWindowsInBounds() {
        return false;
    }

    public static boolean shouldFolderRowClickCloseWindowIfOpen() {
        return false;
    }

    public static boolean shouldShowPathTooltips() {
        return false;
    }

    public static boolean shouldSuggestWindowWidth() {
        return false;
    }

    public static boolean isGuiHidden() {
        return false;
    }

    public static boolean isShowHorizontalSeparators() {
        return false;
    }

    public static boolean isWindowResizeEnabled() {
        return false;
    }



}
