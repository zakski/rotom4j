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
package com.szadowsz.gui.component.bined.bounds;


import com.szadowsz.gui.component.bined.RBinEditor;
import com.szadowsz.gui.component.bined.cursor.RCaretPos;
import com.szadowsz.gui.component.bined.settings.*;
import com.szadowsz.gui.component.bined.utils.RBinUtils;

/**
 * Code area data representation structure for basic variant.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class RBinStructure {

    private RBinEditor editor;

    private int bytesPerRow;
    private int charactersPerRow;

    private long rowsPerDocument;

    protected int computeBytesPerRow(int charactersPerPage) {
        RBinViewMode viewMode = editor.getViewMode();
        int maxBytesPerRow = editor.getMaxBytesPerRow();
        int wrappingBytesGroupSize = editor.getWrappingBytesGroupSize();
        int computedBytesPerRow;
        if (editor.getRowWrapping() == RRowWrappingMode.WRAPPING) {
            int charactersPerByte = 0;
            if (viewMode != RBinViewMode.TEXT_PREVIEW) {
                charactersPerByte += editor.getCodeType().getMaxDigitsForByte() + 1;
            }
            if (viewMode != RBinViewMode.CODE_MATRIX) {
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
        RBinViewMode viewMode = editor.getViewMode();
        int charsPerRow = 0;
        if (viewMode != RBinViewMode.TEXT_PREVIEW) {
            charsPerRow += computeLastCodeCharPos( bytesPerRow - 1) + 1;
        }
        if (viewMode != RBinViewMode.CODE_MATRIX) {
            charsPerRow += bytesPerRow;
            if (viewMode == RBinViewMode.DUAL) {
                charsPerRow++;
            }
        }
        return charsPerRow;
    }

    protected int computeLastCodeCharPos(int byteOffset) {
        return byteOffset * (editor.getCodeType().getMaxDigitsForByte() + 1) + editor.getCodeType().getMaxDigitsForByte() - 1;
    }

    protected long computeRowsPerDocument() {
         return editor.getDataSize() / bytesPerRow + (editor.getDataSize() % bytesPerRow > 0 ? 1 : 0);
    }

    public int getBytesPerRow() {
        return bytesPerRow;
    }

    public int getCharactersPerRow() {
        return charactersPerRow;
    }

    public long getRowsPerDocument() {
        return rowsPerDocument;
    }

    public void setEditor(RBinEditor editor) {
        this.editor = editor;
    }

    public int computeFirstCodeCharacterPos(int byteOffset) {
        return byteOffset * (editor.getCodeType().getMaxDigitsForByte() + 1);
    }

    public int computePositionByte(int rowCharPosition) {
        return rowCharPosition / (editor.getCodeType().getMaxDigitsForByte() + 1);
    }

    public RCaretPos computeMovePosition(RCaretPos position, RMovementDirection direction, long totalRows) {
        long dataSize = editor.getDataSize();
        RCodeType codeType = editor.getCodeType();
        RCodeAreaSection section = position.getSection().orElse(RCodeAreaSection.CODE_MATRIX);
        RCaretPos target = new RCaretPos(position.getDataPosition(), position.getCodeOffset(), section);
        switch (direction) {
            case LEFT: {
                if (section != RCodeAreaSection.TEXT_PREVIEW) {
                    int codeOffset = position.getCodeOffset();
                    if (codeOffset > 0) {
                        target.setCodeOffset(codeOffset - 1);
                    } else if (position.getDataPosition() > 0) {
                        target.setDataPosition(position.getDataPosition() - 1);
                        target.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
                    }
                } else if (position.getDataPosition() > 0) {
                    target.setDataPosition(position.getDataPosition() - 1);
                }
                break;
            }
            case RIGHT: {
                if (section != RCodeAreaSection.TEXT_PREVIEW) {
                    int codeOffset = position.getCodeOffset();
                    if (position.getDataPosition() < dataSize && codeOffset < codeType.getMaxDigitsForByte() - 1) {
                        target.setCodeOffset(codeOffset + 1);
                    } else if (position.getDataPosition() < dataSize) {
                        target.setDataPosition(position.getDataPosition() + 1);
                        target.setCodeOffset(0);
                    }
                } else if (position.getDataPosition() < dataSize) {
                    target.setDataPosition(position.getDataPosition() + 1);
                }
                break;
            }
            case UP: {
                if (position.getDataPosition() >= bytesPerRow) {
                    target.setDataPosition(position.getDataPosition() - bytesPerRow);
                }
                break;
            }
            case DOWN: {
                if (position.getDataPosition() < dataSize - bytesPerRow || (position.getDataPosition() == dataSize - bytesPerRow && position.getCodeOffset() == 0)) {
                    target.setDataPosition(position.getDataPosition() + bytesPerRow);
                }
                break;
            }
            case ROW_START: {
                long dataPosition = position.getDataPosition();
                dataPosition -= (dataPosition % bytesPerRow);
                target.setDataPosition(dataPosition);
                target.setCodeOffset(0);
                break;
            }
            case ROW_END: {
                long dataPosition = position.getDataPosition();
                long increment = bytesPerRow - 1 - (dataPosition % bytesPerRow);
                if (dataPosition > Long.MAX_VALUE - increment || dataPosition + increment > dataSize) {
                    target.setDataPosition(dataSize);
                } else {
                    target.setDataPosition(dataPosition + increment);
                }
                if (section != RCodeAreaSection.TEXT_PREVIEW) {
                    if (target.getDataPosition() == dataSize) {
                        target.setCodeOffset(0);
                    } else {
                        target.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
                    }
                }
                break;
            }
            case PAGE_UP: {
                long dataPosition = position.getDataPosition();
                long increment = (long) bytesPerRow * totalRows;
                if (dataPosition < increment) {
                    target.setDataPosition(dataPosition % bytesPerRow);
                } else {
                    target.setDataPosition(dataPosition - increment);
                }
                break;
            }
            case PAGE_DOWN: {
                long dataPosition = position.getDataPosition();
                long increment = (long) bytesPerRow * totalRows;
                if (dataPosition > dataSize - increment) {
                    long positionOnRow = dataPosition % bytesPerRow;
                    long lastRowDataStart = dataSize - (dataSize % bytesPerRow);
                    if (lastRowDataStart == dataSize - positionOnRow) {
                        target.setDataPosition(dataSize);
                        target.setCodeOffset(0);
                    } else if (lastRowDataStart > dataSize - positionOnRow) {
                        if (lastRowDataStart > bytesPerRow) {
                            lastRowDataStart -= bytesPerRow;
                            target.setDataPosition(lastRowDataStart + positionOnRow);
                        }
                    } else {
                        target.setDataPosition(lastRowDataStart + positionOnRow);
                    }
                } else {
                    target.setDataPosition(dataPosition + increment);
                }
                break;
            }
            case DOC_START: {
                target.setDataPosition(0);
                target.setCodeOffset(0);
                break;
            }
            case DOC_END: {
                target.setDataPosition(dataSize);
                target.setCodeOffset(0);
                break;
            }
            case SWITCH_SECTION: {
                RCodeAreaSection activeSection = section == RCodeAreaSection.TEXT_PREVIEW ? RCodeAreaSection.CODE_MATRIX : RCodeAreaSection.TEXT_PREVIEW;
                if (activeSection == RCodeAreaSection.TEXT_PREVIEW) {
                    target.setCodeOffset(0);
                }
                target.setSection(activeSection);
                break;
            }
            default:
                throw RBinUtils.getInvalidTypeException(direction);
        }

        return target;
    }

    public void updateCache(int charactersPerPage) {
        bytesPerRow = computeBytesPerRow(charactersPerPage);
        charactersPerRow = computeCharactersPerRow();
        rowsPerDocument = computeRowsPerDocument();
    }
}
