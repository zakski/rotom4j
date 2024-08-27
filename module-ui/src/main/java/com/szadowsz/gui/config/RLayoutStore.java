package com.szadowsz.gui.config;

public class RLayoutStore {
    private static final float defaultWindowWidthInCells = 10;

    private static float cell = 22; // cell size
    private static float horizontalSeparatorStrokeWeight = 1;
    private static float resizeRectangleSize = 4;
    private static int smoothingValue = 4;

    private static boolean shouldDrawResizeIndicator = true;
    private static boolean shouldKeepWindowsInBounds = true;
    private static boolean shouldShowPathTooltips;
    private static boolean shouldSuggestWindowWidth = true;

    private static boolean showHorizontalSeparators;

    private static boolean isGuiHidden;
    private static boolean isWindowResizeEnabled = true;

    // Slider Config
    private static boolean displaySquigglyEquals = false;

    private static boolean shouldFolderRowClickCloseWindowIfOpen;
    private static boolean hideRadioValue = false;
    private static String overridingSketchName = null;

    public static float getCell() {
        return cell;
    }

    public static float getHorizontalSeparatorStrokeWeight() {
        return horizontalSeparatorStrokeWeight;
    }

    public static float getResizeRectangleSize() {
        return resizeRectangleSize;
    }

    public static int getSmoothingValue() {
        return smoothingValue;
    }

    public static float getWindowWidthInCells() {
        return defaultWindowWidthInCells;
    }

    public static boolean shouldDisplaySquigglyEquals() {
        return displaySquigglyEquals;
    }


    public static boolean shouldDrawResizeIndicator() {
        return shouldDrawResizeIndicator;
    }

    public static boolean shouldKeepWindowsInBounds() {
        return shouldKeepWindowsInBounds;
    }

    public static boolean shouldFolderRowClickCloseWindowIfOpen() {
        return shouldFolderRowClickCloseWindowIfOpen;
    }

    public static boolean shouldShowPathTooltips() {
        return shouldShowPathTooltips;
    }

    public static boolean shouldSuggestWindowWidth() {
        return shouldSuggestWindowWidth;
    }

    public static boolean isGuiHidden() {
        return isGuiHidden;
    }

    public static boolean isShowHorizontalSeparators() {
        return showHorizontalSeparators;
    }

    public static boolean isWindowResizeEnabled() {
        return isWindowResizeEnabled;
    }
}
