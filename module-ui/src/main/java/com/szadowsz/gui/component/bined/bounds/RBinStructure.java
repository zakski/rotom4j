package com.szadowsz.gui.component.bined.bounds;

import com.szadowsz.gui.component.bined.RBinEditor;
import com.szadowsz.gui.component.bined.settings.CodeAreaViewMode;
import com.szadowsz.gui.component.bined.settings.CodeType;
import com.szadowsz.gui.component.bined.settings.RowWrappingMode;


public class RBinStructure {

    private final RBinLayout layout = new RBinLayout();
    private CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;

    private CodeType codeType = CodeType.HEXADECIMAL;

    private long dataSize;
    private RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    private int maxBytesPerLine;
    private int wrappingBytesGroupSize;

    private int bytesPerRow;
    private int charactersPerRow;
    private long rowsPerDocument;

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


    public int getMaxBytesPerLine() {
        return maxBytesPerLine;
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
        return layout.computeFirstCodeCharacterPos(this, byteOffset);
    }

    public int computePositionByte(int rowCharPosition) {
        return layout.computePositionByte(this, rowCharPosition);
    }

    public void updateCache(RBinEditor editor, int charactersPerPage) {
        viewMode = editor.getViewMode();
        codeType = editor.getCodeType();
        dataSize = editor.getDataSize();
        rowWrapping = editor.getRowWrapping();
        maxBytesPerLine = editor.getMaxBytesPerRow();
        wrappingBytesGroupSize = editor.getWrappingBytesGroupSize();

        bytesPerRow = layout.computeBytesPerRow(this, charactersPerPage);
        charactersPerRow = layout.computeCharactersPerRow(this);
        rowsPerDocument = layout.computeRowsPerDocument(this);
    }
}
