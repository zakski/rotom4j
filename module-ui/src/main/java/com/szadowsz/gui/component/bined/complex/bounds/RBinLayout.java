package com.szadowsz.gui.component.bined.complex.bounds;


import com.szadowsz.gui.component.bined.settings.CodeAreaViewMode;
import com.szadowsz.gui.component.bined.settings.CodeType;
import com.szadowsz.gui.component.bined.settings.RowWrappingMode;
import com.szadowsz.gui.component.bined.sizing.RBinStructure;

public class RBinLayout {


    public int computeBytesPerRow(RBinStructure structure, int charactersPerPage) {
        CodeAreaViewMode viewMode = structure.getViewMode();
        CodeType codeType = structure.getCodeType();
        int maxBytesPerLine = structure.getMaxBytesPerRow();
        int wrappingBytesGroupSize = structure.getWrappingBytesGroupSize();
        RowWrappingMode rowWrapping = structure.getRowWrapping();
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

            if (maxBytesPerLine > 0 && computedBytesPerRow > maxBytesPerLine) {
                computedBytesPerRow = maxBytesPerLine;
            }

            if (wrappingBytesGroupSize > 1) {
                int wrappingBytesGroupOffset = computedBytesPerRow % wrappingBytesGroupSize;
                if (wrappingBytesGroupOffset > 0) {
                    computedBytesPerRow -= wrappingBytesGroupOffset;
                }
            }
        } else {
            computedBytesPerRow = maxBytesPerLine;
        }

        if (computedBytesPerRow < 1) {
            computedBytesPerRow = 1;
        }

        return computedBytesPerRow;
    }

    public int computeCharactersPerRow(RBinStructure structure) {
        CodeAreaViewMode viewMode = structure.getViewMode();
        int bytesPerRow = structure.getBytesPerRow();
        int charsPerRow = 0;
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            charsPerRow += computeLastCodeCharPos(structure, bytesPerRow - 1) + 1;
        }
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            charsPerRow += bytesPerRow;
            if (viewMode == CodeAreaViewMode.DUAL) {
                charsPerRow++;
            }
        }
        return charsPerRow;
    }

    public int computeFirstCodeCharacterPos(RBinStructure structure, int byteOffset) {
        CodeType codeType = structure.getCodeType();
        return byteOffset * (codeType.getMaxDigitsForByte() + 1);
    }
    public int computeLastCodeCharPos(RBinStructure structure, int byteOffset) {
        CodeType codeType = structure.getCodeType();
        return byteOffset * (codeType.getMaxDigitsForByte() + 1) + codeType.getMaxDigitsForByte() - 1;
    }

    public int computePositionByte(RBinStructure structure, int rowCharPosition) {
        CodeType codeType = structure.getCodeType();
        return rowCharPosition / (codeType.getMaxDigitsForByte() + 1);
    }

    public long computeRowsPerDocument(RBinStructure structure) {
        long dataSize = structure.getDataSize();
        int bytesPerRow = structure.getBytesPerRow();
        return dataSize / bytesPerRow + 1;
    }
}
