package com.old.ui.store;

import processing.core.PFont;
import processing.core.PGraphics;

//
//import com.old.ui.constants.GlobalReferences;
//import processing.core.PFont;
//import processing.core.PGraphics;
//
//import static processing.core.PApplet.println;
//
/**
 * Storage of GUI font
 */
public class FontStore {

    // Font Text margins
    public static float textMarginX = 5;
    public static float textMarginY = 13;
    // Font path defaults
    public final static String sideFontPathDefault = "JetBrainsMono-Regular.ttf";
    public final static String mainFontPathDefault = "JetBrainsMono-Regular.ttf";
    // Font size defaults
    public static int mainFontSizeDefault = 16;
    public static int sideFontSizeDefault = 15;
    // Loaded Font path Info
    private static String lastMainFontPath = "";
    private static String lastSideFontPath = "";
    // Loaded Font size Info
    private static int lastMainFontSize = -1;
    private static int lastSideFontSize = -1;
    // Loaded Font Info
    private static PFont mainFont = null;
    private static PFont sideFont = null;
//
    // Graphics Reference to properly load the font
    private static PGraphics mainFontUtilsProvider;
//
//    /**
//     * Get the Main Font Graphics Reference
//     *
//     * @return graphics Reference
//     */
    public static PGraphics getMainFontUtilsProvider(){
//        if(mainFontUtilsProvider == null){
//            mainFontUtilsProvider = GlobalReferences.app.createGraphics(64, 64);
//        }
//        mainFontUtilsProvider.beginDraw();
//        mainFontUtilsProvider.textFont(mainFont);
//        mainFontUtilsProvider.endDraw();
        return mainFontUtilsProvider;
    }

//    public static void updateFontOptions() {
//        GlobalReferences.gui.pushFolder("font");
//        lazyUpdateFont(
//                GlobalReferences.gui.text("main font", mainFontPathDefault).getStringValue(),
//                GlobalReferences.gui.text("side font", sideFontPathDefault).getStringValue(),
//                GlobalReferences.gui.sliderInt("main font size", mainFontSizeDefault, 2, Integer.MAX_VALUE),
//                GlobalReferences.gui.sliderInt("side font size", sideFontSizeDefault, 2, Integer.MAX_VALUE)
//        );
//        textMarginX = GlobalReferences.gui.slider("x offset", textMarginX);
//        textMarginY = GlobalReferences.gui.slider("y offset", textMarginY);
//        if(GlobalReferences.gui.button("print font list").getBooleanValueAndSetItToFalse()){
//            printAvailableFonts();
//        }
//        GlobalReferences.gui.popFolder();
//    }

    /**
     * Get the Main Font
     *
     * @return a font
     */
    public static PFont getMainFont() {
        return mainFont;
    }

    /**
     * Get the Secondary Font
     *
     * @return a font
     */
    public static PFont getSideFont() {
        return sideFont;
    }

//    /**
//     * Load the Default Fonts
//     */
    public static void updateFont() {
//        updateFont(mainFontPathDefault, sideFontPathDefault, mainFontSizeDefault, sideFontSizeDefault);
    }
//
//    /**
//     * Load the Specified Fonts
//     *
//     * @param _mainFontPath main font path
//     * @param _sideFontPath secondary font path
//     * @param _mainFontSize main font size
//     * @param _sideFontSize secondary font size
//     */
//    private static void updateFont(String _mainFontPath, String _sideFontPath, int _mainFontSize, int _sideFontSize) {
//        boolean mainFontPathChanged = !lastMainFontPath.equals(_mainFontPath);
//        boolean sideFontPathChanged = !lastSideFontPath.equals(_sideFontPath);
//        boolean mainSizeChanged = lastMainFontSize != _mainFontSize;
//        boolean sideSizeChanged = lastSideFontSize != _sideFontSize;
//        lastMainFontSize = _mainFontSize;
//        lastSideFontSize = _sideFontSize;
//        if(mainSizeChanged || mainFontPathChanged){
//            lastMainFontPath = _mainFontPath;
//            try {
//                mainFont = GlobalReferences.app.createFont(lastMainFontPath, lastMainFontSize);
//            } catch (RuntimeException ex) {
//                if (ex.getMessage().contains("createFont() can only be used inside setup() or after setup() has been called")) {
//                    throw new RuntimeException("the new NDSGui(this) constructor can only be used inside setup() or after setup() has been called");
//                }
//            }
//        }
//        if(sideSizeChanged || sideFontPathChanged){
//            lastSideFontPath = _sideFontPath;
//            try {
//                sideFont = GlobalReferences.app.createFont(lastSideFontPath, lastSideFontSize);
//            } catch (RuntimeException ex) {
//                if (ex.getMessage().contains("createFont() can only be used inside setup() or after setup() has been called")) {
//                    throw new RuntimeException("the new Gui(this) constructor can only be used inside setup() or after setup() has been called");
//                }
//            }
//        }
//    }
//
//    /**
//     * Debug method to print available fonts
//     */
//    private static void printAvailableFonts() {
//        String[] fontList = PFont.list();
//        for (String s : fontList) {
//            println(s);
//        }
//    }
//
    /**
     * Method to truncate text from the start in order to fit the available text
     *
     * @param pg graphics reference
     * @param text text to truncate
     * @param availableWidth available space
     * @return truncated substring of the text
     */
    public static String getSubstringFromStartToFit(PGraphics pg, String text, float availableWidth) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if(character == '\n'){
                // no new lines allowed in a one-line row name
                break;
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
     * Method to truncate text from the end in order to fit the available text
     *
     * @param pg graphics reference
     * @param text text to truncate
     * @param availableWidth available space
     * @return truncated substring of the text
     */
    public static String getSubstringFromEndToFit(PGraphics pg, String text, float availableWidth){
        StringBuilder result = new StringBuilder();
        for (int i = text.length() - 1; i >= 0; i--) {
            char character = text.charAt(i);
            float textWidthAfterNewChar = pg.textWidth(result.toString() + character);
            if (textWidthAfterNewChar >= availableWidth) {
                break;
            }
            result.insert(0, character);
        }
        return result.toString();
    }
}
