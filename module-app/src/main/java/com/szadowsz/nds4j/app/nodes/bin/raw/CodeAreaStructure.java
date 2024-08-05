/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.szadowsz.nds4j.app.nodes.bin.raw;


/**
 * Code area data representation structure for basic variant.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaStructure {

    private final CodeAreaLayout layout = new CodeAreaLayout();
    private CodeAreaViewMode viewMode;

    private CodeType codeType = CodeType.HEXADECIMAL;

    private long dataSize;
    private RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    private int maxBytesPerLine;
    private int wrappingBytesGroupSize;

    private long rowsPerDocument;
    private int bytesPerRow;
    private int charactersPerRow;

    public CodeAreaStructure() {
        viewMode = CodeAreaViewMode.DUAL;
    }

    public void updateCache(DataProvider codeArea, int charactersPerPage) {
        viewMode = codeArea.getViewMode();
        codeType = codeArea.getCodeType();
        dataSize = codeArea.getDataSize();
        rowWrapping = codeArea.getRowWrapping();
        maxBytesPerLine = codeArea.getMaxBytesPerRow();
        wrappingBytesGroupSize = codeArea.getWrappingBytesGroupSize();

        bytesPerRow = layout.computeBytesPerRow(this, charactersPerPage);
        charactersPerRow = layout.computeCharactersPerRow(this);
        rowsPerDocument = layout.computeRowsPerDocument(this);
    }

    public int computePositionByte(int rowCharPosition) {
        return layout.computePositionByte(this, rowCharPosition);
    }

    public int computeFirstCodeCharacterPos(int byteOffset) {
        return layout.computeFirstCodeCharacterPos(this, byteOffset);
    }

    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction, int rowsPerPage) {
        return layout.computeMovePosition(this, position, direction, rowsPerPage);
    }

    public CodeAreaViewMode getViewMode() {
        return viewMode;
    }

    public CodeType getCodeType() {
        return codeType;
    }

    public long getDataSize() {
        return dataSize;
    }

    public RowWrappingMode getRowWrapping() {
        return rowWrapping;
    }

    public int getMaxBytesPerLine() {
        return maxBytesPerLine;
    }

    public int getWrappingBytesGroupSize() {
        return wrappingBytesGroupSize;
    }

    public long getRowsPerDocument() {
        return rowsPerDocument;
    }

    public int getBytesPerRow() {
        return bytesPerRow;
    }

    public int getCharactersPerRow() {
        return charactersPerRow;
    }
}
