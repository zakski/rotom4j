package com.szadowsz.nds4j.app.nodes.bin.raw.swing;

import com.szadowsz.nds4j.app.nodes.bin.core.CharsetStreamTranslator;
import com.szadowsz.nds4j.app.nodes.bin.raw.CodeAreaMetrics;

import java.awt.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class CodeAreaMetricsSwing extends CodeAreaMetrics {

    private FontMetrics fontMetrics;

    public int getCharWidth(char value) {
        return fontMetrics.charWidth(value);
    }

    public int getCharsWidth(char[] data, int offset, int length) {
        return fontMetrics.charsWidth(data, offset, length);
    }

    public FontMetrics getFontMetrics() {
        return fontMetrics;
    }

    public boolean hasUniformLineMetrics() {
        return fontMetrics.hasUniformLineMetrics();
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

}
