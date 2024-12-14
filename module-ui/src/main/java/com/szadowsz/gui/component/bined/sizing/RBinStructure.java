package com.szadowsz.gui.component.bined.sizing;


import com.szadowsz.gui.component.bined.RBinEditor;
import com.szadowsz.gui.component.bined.settings.CodeAreaViewMode;
import com.szadowsz.gui.component.bined.settings.CodeType;
import com.szadowsz.gui.component.bined.settings.RowWrappingMode;

public class RBinStructure {

    private CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;

    private CodeType codeType = CodeType.HEXADECIMAL;

    private long dataSize;

    private RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    private int wrappingBytesGroupSize;

    private int bytesPerRow;
    private int maxBytesPerRow;
    private int charactersPerRow;
    private long rowsPerDocument;

    protected int computeBytesPerRow(int charactersPerPage) {
        int computedBytesPerRow;
        if (rowWrapping == RowWrappingMode.WRAPPING) {
            int charactersPerByte = 0;
            if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
                charactersPerByte += codeType.getMaxDigitsForByte() + 1;
            }
            if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
                charactersPerByte++;
            }
            computedBytesPerRow = charactersPerPage / charactersPerByte;

            if (maxBytesPerRow > 0 && computedBytesPerRow > maxBytesPerRow) {
                computedBytesPerRow = maxBytesPerRow;
            }

            if (wrappingBytesGroupSize > 1) {
                int wrappingBytesGroupOffset = computedBytesPerRow % wrappingBytesGroupSize;
                if (wrappingBytesGroupOffset > 0) {
                    computedBytesPerRow -= wrappingBytesGroupOffset;
                }
            }
        } else {
            computedBytesPerRow = maxBytesPerRow;
        }

        if (computedBytesPerRow < 1) {
            computedBytesPerRow = 1;
        }

        return computedBytesPerRow;
    }

    protected int computeCharactersPerRow() {
        int charsPerRow = 0;
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            charsPerRow += computeLastCodeCharPos( bytesPerRow - 1) + 1;
        }
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            charsPerRow += bytesPerRow;
            if (viewMode == CodeAreaViewMode.DUAL) {
                charsPerRow++;
            }
        }
        return charsPerRow;
    }

    protected int computeLastCodeCharPos(int byteOffset) {
        return byteOffset * (codeType.getMaxDigitsForByte() + 1) + codeType.getMaxDigitsForByte() - 1;
    }

    protected long computeRowsPerDocument() {
         return dataSize / bytesPerRow + (dataSize % bytesPerRow > 0 ? 1 : 0);
    }

    public int getBytesPerRow() {
        return bytesPerRow;
    }

    public int getCharactersPerRow() {
        return charactersPerRow;
    }

    public CodeType getCodeType() {
        return codeType;
    }

    public long getDataSize() {
        return dataSize;
    }

    public int getMaxBytesPerRow() {
        return maxBytesPerRow;
    }

    public long getRowsPerDocument() {
        return rowsPerDocument;
    }

    public RowWrappingMode getRowWrapping() {
        return rowWrapping;
    }

    public CodeAreaViewMode getViewMode() {
        return viewMode;
    }

    public int getWrappingBytesGroupSize() {
        return wrappingBytesGroupSize;
    }

    public int computeFirstCodeCharacterPos(int byteOffset) {
        return byteOffset * (codeType.getMaxDigitsForByte() + 1);
    }

    public int computePositionByte(int rowCharPosition) {
        return rowCharPosition / (codeType.getMaxDigitsForByte() + 1);
    }

    public void updateCache(RBinEditor editor, int charactersPerPage) {
        viewMode = editor.getViewMode();
        codeType = editor.getCodeType();
        dataSize = editor.getDataSize();
        rowWrapping = editor.getRowWrapping();
        maxBytesPerRow = editor.getMaxBytesPerRow();
        wrappingBytesGroupSize = editor.getWrappingBytesGroupSize();

        bytesPerRow = computeBytesPerRow(charactersPerPage);
        charactersPerRow = computeCharactersPerRow();
        rowsPerDocument = computeRowsPerDocument();
    }
}
