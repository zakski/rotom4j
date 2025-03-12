package com.szadowsz.gui.component.bined;

import com.szadowsz.gui.component.bined.settings.RCodeAreaSection;

import java.nio.charset.Charset;

public class RBinCharAssessor {

    protected Charset charMappingCharset = null;
    protected final char[] charMapping = new char[256];

    protected long dataSize;
    protected int maxBytesPerChar;
    protected byte[] rowData;
    protected Charset charset;

    /**
     * Precomputes widths for basic ascii characters.
     *
     * @param charset character set
     */
    private void buildCharMapping(Charset charset) {
        for (int i = 0; i < 256; i++) {
            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
        }
        charMappingCharset = charset;
    }

    /**
     * Returns preview character for particular position.
     *
     * @param rowDataPosition row data position
     * @param byteOnRow byte on current row
     * @param charOnRow character on current row
     * @param section current section
     * @return color or null for default color
     */
    public char getPreviewCharacter(long rowDataPosition, int byteOnRow, int charOnRow, RCodeAreaSection section) {
        if (maxBytesPerChar > 1) {
            if (rowDataPosition + maxBytesPerChar > dataSize) {
                maxBytesPerChar = (int) (dataSize - rowDataPosition);
            }

            int charDataLength = maxBytesPerChar;
            if (byteOnRow + charDataLength > rowData.length) {
                charDataLength = rowData.length - byteOnRow;
            }
            String displayString = new String(rowData, byteOnRow, charDataLength, charset);
            if (!displayString.isEmpty()) {
                return displayString.charAt(0);
            }
        } else {
            if (charMappingCharset == null || charMappingCharset != charset) {
                buildCharMapping(charset);
            }

            return charMapping[rowData[byteOnRow] & 0xFF];
        }
        return ' ';
    }

    /**
     * Returns preview character for cursor position.
     *
     * @param cursorData cursor data
     * @param cursorDataLength cursor data length
     * @return color or null for default color
     */
    public char getPreviewCursorCharacter(byte[] cursorData, int cursorDataLength) {
        if (cursorDataLength == 0) {
            return ' ';
        }

        if (maxBytesPerChar > 1) {
            String displayString = new String(cursorData, 0, cursorDataLength, charset);
            if (!displayString.isEmpty()) {
                return displayString.charAt(0);
            }
        } else {
            if (charMappingCharset == null || charMappingCharset != charset) {
                buildCharMapping(charset);
            }

            return charMapping[cursorData[0] & 0xFF];
        }

        return ' ';

    }

    public void update(RBinEditor editor) {
        dataSize = editor.getDataSize();
        charset = editor.getCharset();
        rowData = editor.getRowDataCache().rowData;
        maxBytesPerChar = editor.getMetrics().getMaxBytesPerChar();
    }
}
