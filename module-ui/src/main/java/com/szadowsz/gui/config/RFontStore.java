package com.szadowsz.gui.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;

/**
 * Storage Class For Current and Preset GUI Fonts
 */
public class RFontStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(RFontStore.class);

    // Font Text margins
    private final static float textMarginX = 5;
    private final static float textMarginY = 13;

    // TODO do we want these configured via theme?
    // Font path defaults
    private final static String mainFontPathDefault = "JetBrainsMono-Regular.ttf";
    private final static String sideFontPathDefault = "JetBrainsMono-Regular.ttf";

    // Font size defaults
    private final static int mainFontSizeDefault = 16;
    private final static int sideFontSizeDefault = 15;

    private static PGraphics mainFontUtilsProvider = null;

    // Loaded Font path Info
    private static String lastMainFontPath = "";
    private static String lastSideFontPath = "";

    // Loaded Font size Info
    private static int lastMainFontSize = -1;
    private static int lastSideFontSize = -1;

    // Loaded Font Info
    private static PFont mainFont = null;
    private static PFont sideFont = null;

    private RFontStore(){
        // NOOP // TODO consider if Per Window Font Config is wanted
    }

    /**
     * Initialise the store using a PApplet
     *
     * @param sketch to initialise with
     */
    public static void init(PApplet sketch) {
        if (mainFontUtilsProvider == null) {
            mainFontUtilsProvider = sketch.createGraphics(64, 64);
            lastMainFontPath = mainFontPathDefault; // TODO provide way to set this
            lastMainFontSize = mainFontSizeDefault; // TODO provide way to set this
            try {
                    mainFont = sketch.createFont(lastMainFontPath, lastMainFontSize);
                } catch (RuntimeException ex) {
                    if (ex.getMessage().contains("createFont() can only be used inside setup() or after setup() has been called")) {
                        throw new RuntimeException("the new Gui(this) constructor can only be used inside setup() or after setup() has been called");
                    }
                }
            lastSideFontPath = sideFontPathDefault; // TODO provide way to set this
            lastSideFontSize = sideFontSizeDefault; // TODO provide way to set this
             try {
                 sideFont = sketch.createFont(lastSideFontPath, lastSideFontSize);
             } catch (RuntimeException ex) {
                 if (ex.getMessage().contains("createFont() can only be used inside setup() or after setup() has been called")) {
                     throw new RuntimeException("the new Gui(this) constructor can only be used inside setup() or after setup() has been called");
                 }
             }
        }
    }


    /**
     * Get Horizontal Text Margin
     *
     * @return X Margin
     */
    public static float getMarginX() {
        return textMarginX;
    }

    /**
     * Get Vertical Text Margin
     *
     * @return Y Margin
     */
    public static float getMarginY() {
        return textMarginY;
    }

    /**
     * Get the Main Font
     *
     * @return the font
     */
    public static PFont getMainFont() {
        return mainFont;
    }

    /**
     * Method to calculate text width as a multiple of the cell size
     *
     * @param textToMeasure text to measure pixel length
     * @param cell cell size
     * @return width as a multiple of the cell size
     */
    public static float calcMainTextWidth(String textToMeasure, float cell) { // TODO LazyGui
        mainFontUtilsProvider.beginDraw();
        mainFontUtilsProvider.textFont(mainFont);
        mainFontUtilsProvider.endDraw();

        float leftTextWidth = mainFontUtilsProvider.textWidth(textToMeasure);

        return PApplet.ceil(leftTextWidth / cell) * cell;
    }

    /**
     * Method to truncate text at the end in order to fit the available width
     *
     * @param pg graphics reference
     * @param text text to truncate
     * @param availableWidth available space
     * @return truncated substring of the text
     */
    public static String substringToFit(PGraphics pg, String text, float availableWidth) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if(character == '\n'){
                break; // no new lines allowed in a one-line row name
            }
            float textWidthAfterNewChar = pg.textWidth(result.toString() + character);
            if (textWidthAfterNewChar >= availableWidth) {
                break;
            }
            result.append(character);
        }
        return result.toString();
    }

    /**
     * Method to truncate text at the end in order to fit the available width
     *
     * @param pg graphics reference
     * @param text text to truncate
     * @param availableWidth available space
     * @param margin if true, account for margin
     * @return truncated substring of the text
     */
    public static String substringToFit(PGraphics pg, String text, float availableWidth, boolean margin) { // TODO LazyGui
        return substringToFit(pg, text, availableWidth - ((margin)?textMarginX:0));
    }
}
