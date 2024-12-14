package com.szadowsz.gui.component.bined.sizing;

import processing.core.PFont;
import processing.core.PGraphics;

import java.awt.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Basic code area component dimensions.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class RBinMetrics {

    protected FontMetrics fontMetrics;

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

    public int getCharWidth(char drawnChar) {
        return fontMetrics.charWidth(drawnChar);
    }

    public boolean hasUniformLineMetrics() {
        return fontMetrics.hasUniformLineMetrics();
    }

    public int getCharsWidth(char[] data, int offset, int length) {
        return fontMetrics.charsWidth(data, offset, length);
    }

    public void recomputeMetrics(FontMetrics fontMetrics, Charset charset) {
        this.fontMetrics = fontMetrics;
        if (fontMetrics == null) {
            characterWidth = 0;
            fontHeight = 0;
        } else {
            fontHeight = fontMetrics.getHeight();
            rowHeight = fontHeight;

            /*
             * Use small 'm' character to guess normal font width.
             */
            characterWidth = fontMetrics.charWidth('m');
            int fontSize = fontMetrics.getFont().getSize();
            subFontSpace = rowHeight - fontSize;
        }

        try {
            CharsetEncoder encoder = charset.newEncoder();
            maxBytesPerChar = (int) encoder.maxBytesPerChar();
        } catch (UnsupportedOperationException ex) {
            maxBytesPerChar = CharsetStreamTranslator.DEFAULT_MAX_BYTES_PER_CHAR;
        }
    }

    public void recomputeMetrics(PGraphics pg, PFont font, Charset charset) {
        Object pgNative = pg.getNative();
        recomputeMetrics(((Graphics2D)pgNative).getFontMetrics((Font) font.getNative()),charset);
    }
}
