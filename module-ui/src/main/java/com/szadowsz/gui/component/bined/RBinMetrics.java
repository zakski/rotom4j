package com.szadowsz.gui.component.bined;

/**
 * Basic code area component dimensions.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class RBinMetrics {

    protected int rowHeight;
    protected int characterWidth;
    protected int fontHeight;
    protected int maxBytesPerChar;
    protected int subFontSpace = 0;

    public int getCharacterWidth() {
        return characterWidth;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public int getMaxBytesPerChar() {
        return maxBytesPerChar;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public int getSubFontSpace() {
        return subFontSpace;
    }

    public boolean isInitialized() {
        return rowHeight != 0 && characterWidth != 0;
    }

}
