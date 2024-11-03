package com.szadowsz.gui.config;

/**
 * Layout Configuration singleton
 */
public class RLayoutStore {
    private static final float defaultWindowWidthInCells = 10;

    private static float cell = 22; // cell size

    private static float resizeRectangleSize = 4; // window resize bar size

    private static float horizontalSeparatorStrokeWeight = 1; // size of horizontal separator

    private static int smoothingValue = 4; // anti-aliasing smoothing value

    // Draw Option Configurations
    private static boolean isGuiHidden;
    private static boolean shouldDrawResizeIndicator = true;
    private static boolean shouldShowPathTooltips;

    // Window sizing Configurations
    private static boolean isWindowResizeEnabled = true;
    private static boolean shouldKeepWindowsInBounds = true;
    private static boolean shouldSuggestWindowWidth = true;

    // Slider Config
    private static boolean displaySquigglyEquals = false;

    // TODO Check if necessary
    private static boolean showHorizontalSeparators; // TODO, do we need a vertical one?
    private static boolean shouldFolderRowClickCloseWindowIfOpen;
    private static boolean hideRadioValue = false;
    private static String overridingSketchName = null;

    /**
     * Get the size of an individual cell
     *
     * @return the cell size
     */
    public static float getCell() {
        return cell;
    }

    /**
     * Get the horizontal separator size value
     *
     * @return the stroke weight
     */
    public static float getHorizontalSeparatorStrokeWeight() {
        return horizontalSeparatorStrokeWeight;
    }

    /**
     * Get window rectangle resize value
     *
     * @return
     */
    public static float getResizeRectangleSize() {
        return resizeRectangleSize;
    }

    /**
     * Get anti-alias smoothing weight
     *
     * @return anti-alias smoothing value
     */
    public static int getSmoothingValue() {
        return smoothingValue;
    }

    /**
     * Get the default window width in terms of cells
     *
     * @return default window cell width
     */
    public static float getWindowWidthInCells() {
        return defaultWindowWidthInCells;
    }

    /**
     * Check if the Gui should be drawn
     *
     * @return true if hidden, false if visible
     */
    public static boolean isGuiHidden() {
        return isGuiHidden;
    }

    /**
     * Check if the horizontal separators should be drawn
     *
     * @return true if they should be drawn, false otherwise
     */
    public static boolean isShowHorizontalSeparators() {
        return showHorizontalSeparators;
    }

    /**
     * Check if globally, window resizing is allowed
     *
     * @return true if resizing is allowed, false otherwise
     */
    public static boolean isWindowResizeEnabled() {
        return isWindowResizeEnabled;
    }

    /**
     * Check if the slider should display a squiggle equals
     *
     * @return true if it should be displayed, false otherwise
     */
    public static boolean shouldDisplaySquigglyEquals() {
        return displaySquigglyEquals;
    }


    /**
     * Check if the window resize indicator should be drawn
     *
     * @return true if it should be displayed, false otherwise
     */
    public static boolean shouldDrawResizeIndicator() {
        return shouldDrawResizeIndicator;
    }

    /**
     * Check if windows should be kept within bounds
     *
     * @return true if they should be forced to remain wholly inside the window, false otherwise
     */
    public static boolean shouldKeepWindowsInBounds() {
        return shouldKeepWindowsInBounds;
    }

    /**
     * Check if clicking the folder should close the window if it is open
     *
     * @return true if it should close, false otherwise
     */
    public static boolean shouldFolderRowClickCloseWindowIfOpen() {
        return shouldFolderRowClickCloseWindowIfOpen;
    }

    /**
     * Check if tooltips should be displayed
     *
     * @return true if they should be displayed, false otherwise
     */
    public static boolean shouldShowPathTooltips() {
        return shouldShowPathTooltips;
    }

    /**
     * Check if the window width should be suggested
     *
     * @return true if should suggest, false otherwise
     */
    public static boolean shouldSuggestWindowWidth() {
        return shouldSuggestWindowWidth;
    }
}
