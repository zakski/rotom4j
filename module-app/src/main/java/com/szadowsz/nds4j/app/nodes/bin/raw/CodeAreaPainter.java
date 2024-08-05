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

import com.szadowsz.nds4j.app.nodes.bin.raw.swing.AntialiasingMode;
import com.szadowsz.nds4j.file.bin.core.BinaryData;

import java.awt.Color;
import java.awt.Cursor;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

/**
 * Code area component default painter.
 *
 * @author ExBin Project (https://exbin.org)
 */
public abstract class CodeAreaPainter {

    protected final CodeAreaControl codeArea;
    protected volatile boolean initialized = false;

    protected volatile boolean fontChanged = false;
    protected volatile boolean layoutChanged = true;
    protected volatile boolean resetColors = true;
    protected volatile boolean caretChanged = true;

    protected final DataChangedListener codeAreaDataChangeListener;

    protected final CodeAreaMetrics metrics;
    protected final CodeAreaStructure structure = new CodeAreaStructure();
    protected final CodeAreaScrolling scrolling = new CodeAreaScrolling();
    protected final CodeAreaDimensions dimensions;
    protected final CodeAreaVisibility visibility = new CodeAreaVisibility();

    protected final CodeAreaLayout layout = new CodeAreaLayout();
    protected CodeAreaColorsProfile colorsProfile = new CodeAreaColorsProfile();

    protected CodeCharactersCase codeCharactersCase;
    protected EditOperation editOperation;
    protected BackgroundPaintMode backgroundPaintMode;
    protected ScrollViewDimension viewDimension;
    protected boolean showMirrorCursor;

    protected int rowPositionLength;
    private int minRowPositionLength;
    private int maxRowPositionLength;

    protected Charset charset;

    protected RowDataCache rowDataCache = null;

    protected Charset charMappingCharset = null;
    protected final char[] charMapping = new char[256];

    public CodeAreaPainter(CodeAreaControl codeArea, CodeAreaMetrics metrics, CodeAreaDimensions dimensions) {
        this.codeArea = codeArea;
        this.metrics = metrics;
        this.dimensions = dimensions;
        codeAreaDataChangeListener = this::dataChanged;
        rebuildColors();
    }

    protected void updateCaret() {
        editOperation = codeArea.getActiveOperation();

        caretChanged = false;
    }

    private void validateCaret() {
        CodeAreaCaret caret = codeArea.getCodeAreaCaret();
        CodeAreaCaretPosition caretPosition = caret.getCaretPosition();
        if (caretPosition.getDataPosition() > codeArea.getDataSize()) {
            caret.setCaretPosition(null);
        }
    }

    private void validateSelection() {
        CodeAreaSelection selectionHandler = codeArea.getSelectionHandler();
        if (!selectionHandler.isEmpty()) {
            long dataSize = codeArea.getDataSize();
            if (dataSize == 0) {
                codeArea.clearSelection();
            } else {
                boolean selectionChanged = false;
                long start = selectionHandler.getStart();
                long end = selectionHandler.getEnd();
                if (start >= dataSize) {
                    start = dataSize;
                    selectionChanged = true;
                }
                if (end >= dataSize) {
                    end = dataSize;
                    selectionChanged = true;
                }

                if (selectionChanged) {
                    codeArea.setSelection(start, end);
                }
            }
        }
    }

    protected abstract int getHorizontalScrollBarSize();

    protected abstract int getVerticalScrollBarSize();

    protected abstract void recomputeDimensions();

    protected void prepareRowData(long dataPosition) {
        int maxBytesPerChar = metrics.getMaxBytesPerChar();
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = codeArea.getDataSize();
        int previewCharPos = visibility.getPreviewCharPos();
        CodeType codeType = structure.getCodeType();
        CodeAreaViewMode viewMode = structure.getViewMode();

        int rowBytesLimit = bytesPerRow;
        int rowStart = 0;
        if (dataPosition < dataSize) {
            int rowDataSize = bytesPerRow + maxBytesPerChar - 1;
            if (dataSize - dataPosition < rowDataSize) {
                rowDataSize = (int) (dataSize - dataPosition);
            }
            if (dataPosition < 0) {
                rowStart = (int) -dataPosition;
            }
            BinaryData data = codeArea.getContentData();
            data.copyToArray(dataPosition + rowStart, rowDataCache.rowData, rowStart, rowDataSize - rowStart);
            if (dataSize - dataPosition < rowBytesLimit) {
                rowBytesLimit = (int) (dataSize - dataPosition);
            }
        } else {
            rowBytesLimit = 0;
        }

        // Fill codes
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            int skipToCode = visibility.getSkipToCode();
            int skipRestFromCode = visibility.getSkipRestFromCode();
            int endCode = Math.min(skipRestFromCode, rowBytesLimit);
            for (int byteOnRow = Math.max(skipToCode, rowStart); byteOnRow < endCode; byteOnRow++) {
                byte dataByte = rowDataCache.rowData[byteOnRow];

                int byteRowPos = structure.computeFirstCodeCharacterPos(byteOnRow);
                if (byteRowPos > 0) {
                    rowDataCache.rowCharacters[byteRowPos - 1] = ' ';
                }
                CodeAreaUtils.byteToCharsCode(dataByte, codeType, rowDataCache.rowCharacters, byteRowPos, codeCharactersCase);
            }

            if (bytesPerRow > rowBytesLimit) {
                Arrays.fill(rowDataCache.rowCharacters, structure.computeFirstCodeCharacterPos(rowBytesLimit), rowDataCache.rowCharacters.length, ' ');
            }
        }

        if (previewCharPos > 0) {
            rowDataCache.rowCharacters[previewCharPos - 1] = ' ';
        }

        // Fill preview characters
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            int skipToPreview = visibility.getSkipToPreview();
            int skipRestFromPreview = visibility.getSkipRestFromPreview();
            int endPreview = Math.min(skipRestFromPreview, rowBytesLimit);
            for (int byteOnRow = skipToPreview; byteOnRow < endPreview; byteOnRow++) {
                byte dataByte = rowDataCache.rowData[byteOnRow];

                if (maxBytesPerChar > 1) {
                    if (dataPosition + maxBytesPerChar > dataSize) {
                        maxBytesPerChar = (int) (dataSize - dataPosition);
                    }

                    int charDataLength = maxBytesPerChar;
                    if (byteOnRow + charDataLength > rowDataCache.rowData.length) {
                        charDataLength = rowDataCache.rowData.length - byteOnRow;
                    }
                    String displayString = new String(rowDataCache.rowData, byteOnRow, charDataLength, charset);
                    if (!displayString.isEmpty()) {
                        rowDataCache.rowCharacters[previewCharPos + byteOnRow] = displayString.charAt(0);
                    }
                } else {
                    if (charMappingCharset == null || charMappingCharset != charset) {
                        buildCharMapping(charset);
                    }

                    rowDataCache.rowCharacters[previewCharPos + byteOnRow] = charMapping[dataByte & 0xFF];
                }
            }
            if (bytesPerRow > rowBytesLimit) {
                Arrays.fill(rowDataCache.rowCharacters, previewCharPos + rowBytesLimit, previewCharPos + bytesPerRow, ' ');
            }
        }
    }

    protected void recomputeLayout() {
        rowPositionLength = getRowPositionLength();
        recomputeDimensions();

        int charactersPerPage = dimensions.getCharactersPerPage();
        structure.updateCache(codeArea, charactersPerPage);
        codeCharactersCase = codeArea.getCodeCharactersCase();
        backgroundPaintMode = codeArea.getBackgroundPaintMode();
        showMirrorCursor = codeArea.isShowMirrorCursor();
        minRowPositionLength = codeArea.getMinRowPositionLength();
        maxRowPositionLength = codeArea.getMaxRowPositionLength();

        int rowsPerPage = dimensions.getRowsPerPage();
        long rowsPerDocument = structure.getRowsPerDocument();
        int charactersPerRow = structure.getCharactersPerRow();

        if (metrics.isInitialized()) {
            scrolling.updateMaximumScrollPosition(rowsPerDocument, rowsPerPage, charactersPerRow, charactersPerPage, dimensions.getLastCharOffset(), dimensions.getLastRowOffset());
        }

        updateScrollBars();

        layoutChanged = false;
    }

    /**
     * Resets complete painter state for new painting.
     */
    public void reset() {
        resetColors();
        resetFont();
        resetLayout();
        resetCaret();
    }

    /**
     * Rebuilds colors after UIManager change.
     */
    public void resetColors() {
        resetColors = true;
    }

    /**
     * Resets painter font state for new painting.
     */
    public void resetFont() {
        fontChanged = true;
        resetLayout();
    }

    /**
     * Updates painter layout state for new painting.
     */
    public void resetLayout() {
        layoutChanged = true;
    }

    /**
     * Resets caret state.
     */
    public void resetCaret() {
        caretChanged = true;
    }

    /**
     * Calls rebuild of the colors profile.
     */
    public void rebuildColors() {
        colorsProfile.reinitialize();
    }

    public void recomputeCharPositions() {
        visibility.recomputeCharPositions(metrics, structure, dimensions, layout, scrolling);
        updateRowDataCache();
    }

    private void updateRowDataCache() {
        if (rowDataCache == null) {
            rowDataCache = new RowDataCache();
        }

        rowDataCache.headerChars = new char[visibility.getCharactersPerCodeSection()];
        rowDataCache.rowData = new byte[structure.getBytesPerRow() + metrics.getMaxBytesPerChar() - 1];
        rowDataCache.rowPositionCode = new char[rowPositionLength];
        rowDataCache.rowCharacters = new char[structure.getCharactersPerRow()];
    }

    protected void recomputeScrollState() {
        scrolling.setScrollPosition(codeArea.getScrollPosition());
        int characterWidth = metrics.getCharacterWidth();

        if (characterWidth > 0) {
            scrolling.updateCache(codeArea, getHorizontalScrollBarSize(), getVerticalScrollBarSize());

            recomputeCharPositions();
        }
    }

    /**
     * Returns true if painter was initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    protected synchronized void updateCache() {
        if (resetColors) {
            resetColors = false;
            rebuildColors();
        }
    }


    /**
     * Returns background color for particular code.
     *
     * @param rowDataPosition row data position
     * @param byteOnRow byte on current row
     * @param charOnRow character on current row
     * @param section current section
     * @return color or null for default color
     */
    public Color getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        CodeAreaSelection selectionHandler = codeArea.getSelectionHandler();
        int codeLastCharPos = visibility.getCodeLastCharPos();
        boolean inSelection = selectionHandler.isInSelection(rowDataPosition + byteOnRow);
        if (inSelection && (section == CodeAreaSection.CODE_MATRIX)) {
            if (charOnRow == codeLastCharPos) {
                inSelection = false;
            }
        }

        if (inSelection) {
            return section == codeArea.getActiveSection() ? colorsProfile.getSelectionBackground() : colorsProfile.getSelectionMirrorBackground();
        }

        return null;
    }

    /**
     * Returns state of the visibility of given caret position within current
     * scrolling window.
     *
     * @param caretPosition caret position
     * @return visibility state
     */
    public PositionScrollVisibility computePositionScrollVisibility(CodeAreaCaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int previewCharPos = visibility.getPreviewCharPos();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
        int rowsPerPage = dimensions.getRowsPerPage();
        int charactersPerPage = dimensions.getCharactersPerPage();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        CodeAreaSection section = caretPosition.getSection().orElse(CodeAreaSection.CODE_MATRIX);
        if (section == CodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computePositionScrollVisibility(rowPosition, charPosition, bytesPerRow, rowsPerPage, charactersPerPage, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
    }

    /**
     * Returns scroll position so that provided caret position is visible in
     * scrolled area.
     * <p>
     * Performs minimal scrolling and tries to preserve current vertical /
     * horizontal scrolling if possible. If given position cannot be fully
     * shown, top left corner is preferred.
     *
     * @param caretPosition caret position
     * @return scroll position or null if caret position is already visible /
     * scrolled to the best fit
     */
    public Optional<CodeAreaScrollPosition> computeRevealScrollPosition(CodeAreaCaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int previewCharPos = visibility.getPreviewCharPos();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
        int rowsPerPage = dimensions.getRowsPerPage();
        int charactersPerPage = dimensions.getCharactersPerPage();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        CodeAreaSection section = caretPosition.getSection().orElse(CodeAreaSection.CODE_MATRIX);
        if (section == CodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeRevealScrollPosition(rowPosition, charPosition, bytesPerRow, rowsPerPage, charactersPerPage, dataViewWidth % characterWidth, dataViewHeight % rowHeight, characterWidth, rowHeight);
    }

    /**
     * Returns scroll position so that provided caret position is visible in the
     * center of the scrolled area.
     * <p>
     * Attempts to center as much as possible while preserving scrolling limits.
     *
     * @param caretPosition caret position
     * @return scroll position or null if desired scroll position is the same as
     * current scroll position.
     */
    public Optional<CodeAreaScrollPosition> computeCenterOnScrollPosition(CodeAreaCaretPosition caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int previewCharPos = visibility.getPreviewCharPos();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
        int rowsPerRect = dimensions.getRowsPerRect();
        int charactersPerRect = dimensions.getCharactersPerRect();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        CodeAreaSection section = caretPosition.getSection().orElse(CodeAreaSection.CODE_MATRIX);
        if (section == CodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeCenterOnScrollPosition(rowPosition, charPosition, bytesPerRow, rowsPerRect, charactersPerRect, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
    }



    /**
     * Returns background color for particular code.
     *
     * @param rowDataPosition row data position
     * @param byteOnRow byte on current row
     * @param charOnRow character on current row
     * @param section current section
     * @return color or null for default color
     */
    protected Color getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section) {
        CodeAreaSelection selectionHandler = codeArea.getSelectionHandler();
        boolean inSelection = selectionHandler.isInSelection(rowDataPosition + byteOnRow);
        if (inSelection) {
            return section == codeArea.getActiveSection() ? colorsProfile.getSelectionColor() : colorsProfile.getSelectionMirrorColor();
        }

        return null;
    }

    /**
     * Returns closest caret position for provided component relative mouse
     * position.
     *
     * @param positionX component relative position X
     * @param positionY component relative position Y
     * @param overflowMode overflow mode
     * @return closest caret position
     */
    public CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, CaretOverlapMode overflowMode) {
        CodeAreaCaretPosition caret = new CodeAreaCaretPosition();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        int headerAreaHeight = dimensions.getHeaderAreaHeight();

        int diffX = 0;
        if (positionX < rowPositionAreaWidth) {
            if (overflowMode == CaretOverlapMode.PARTIAL_OVERLAP) {
                diffX = 1;
            }
            positionX = rowPositionAreaWidth;
        }
        int cursorCharX = (positionX - rowPositionAreaWidth + scrollPosition.getCharOffset()) / characterWidth + scrollPosition.getCharPosition() - diffX;
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        int diffY = 0;
        if (positionY < headerAreaHeight) {
            if (overflowMode == CaretOverlapMode.PARTIAL_OVERLAP) {
                diffY = 1;
            }
            positionY = headerAreaHeight;
        }
        long cursorRowY = (positionY - headerAreaHeight + scrollPosition.getRowOffset()) / rowHeight + scrollPosition.getRowPosition() - diffY;
        if (cursorRowY < 0) {
            cursorRowY = 0;
        }

        CodeAreaViewMode viewMode = structure.getViewMode();
        int previewCharPos = visibility.getPreviewCharPos();
        int bytesPerRow = structure.getBytesPerRow();
        CodeType codeType = structure.getCodeType();
        long dataSize = codeArea.getDataSize();
        long dataPosition;
        int codeOffset = 0;
        int byteOnRow;
        if ((viewMode == CodeAreaViewMode.DUAL && cursorCharX < previewCharPos) || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            caret.setSection(CodeAreaSection.CODE_MATRIX);
            byteOnRow = structure.computePositionByte(cursorCharX);
            if (byteOnRow >= bytesPerRow) {
                codeOffset = 0;
            } else {
                codeOffset = cursorCharX - structure.computeFirstCodeCharacterPos(byteOnRow);
                if (codeOffset >= codeType.getMaxDigitsForByte()) {
                    codeOffset = codeType.getMaxDigitsForByte() - 1;
                }
            }
        } else {
            caret.setSection(CodeAreaSection.TEXT_PREVIEW);
            byteOnRow = cursorCharX;
            if (viewMode == CodeAreaViewMode.DUAL) {
                byteOnRow -= previewCharPos;
            }
        }

        if (byteOnRow >= bytesPerRow) {
            byteOnRow = bytesPerRow - 1;
        }

        dataPosition = byteOnRow + (cursorRowY * bytesPerRow);
        if (dataPosition < 0) {
            dataPosition = 0;
            codeOffset = 0;
        }

        if (dataPosition >= dataSize) {
            dataPosition = dataSize;
            codeOffset = 0;
        }

        caret.setDataPosition(dataPosition);
        caret.setCodeOffset(codeOffset);
        return caret;
    }

    /**
     * Computes position for movement action.
     *
     * @param position source position
     * @param direction movement direction
     * @return target position
     */
    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction) {
        return structure.computeMovePosition(position, direction, dimensions.getRowsPerPage());
    }

    /**
     * Computes scrolling position for given shift action.
     *
     * @param startPosition start position
     * @param direction scrolling direction
     * @return target position
     */
    public CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection direction) {
        int rowsPerPage = dimensions.getRowsPerPage();
        long rowsPerDocument = structure.getRowsPerDocument();
        return scrolling.computeScrolling(startPosition, direction, rowsPerPage, rowsPerDocument);
    }

    /**
     * Returns type of cursor for given painter relative position.
     *
     * @param positionX component relative position X
     * @param positionY component relative position Y
     * @return java.awt.Cursor cursor type value
     */
    public int getMouseCursorShape(int positionX, int positionY) {
        int dataViewX = dimensions.getScrollPanelX();
        int dataViewY = dimensions.getScrollPanelY();
        int scrollPanelWidth = dimensions.getScrollPanelWidth();
        int scrollPanelHeight = dimensions.getScrollPanelHeight();
        if (positionX >= dataViewX && positionX < dataViewX + scrollPanelWidth
                && positionY >= dataViewY && positionY < dataViewY + scrollPanelHeight) {
            return Cursor.TEXT_CURSOR;
        }

        return Cursor.DEFAULT_CURSOR;
    }

    /**
     * Returns zone type for given position.
     *
     * @param positionX x-coordinate
     * @param positionY y-coordinate
     * @return specific zone in component
     */
    public CodeAreaZone getPositionZone(int positionX, int positionY) {
        return dimensions.getPositionZone(positionX, positionY);
    }

    /**
     * Returns basic profile for colors.
     *
     * @return colors profile
     */
    public CodeAreaColorsProfile getBasicColors() {
        return colorsProfile;
    }

    /**
     * Sets basic profile for colors.
     *
     * @param colorsProfile colors profile
     */
    public void setBasicColors(CodeAreaColorsProfile colorsProfile) {
        this.colorsProfile = colorsProfile;
    }

    /**
     * Precomputes widths for basic ascii characters.
     *
     * @param charset character set
     */
    protected void buildCharMapping(Charset charset) {
        for (int i = 0; i < 256; i++) {
            charMapping[i] = new String(new byte[]{(byte) i}, charset).charAt(0);
        }
        charMappingCharset = charset;
    }

    private int getRowPositionLength() {
        if (minRowPositionLength > 0 && minRowPositionLength == maxRowPositionLength) {
            return minRowPositionLength;
        }

        long dataSize = codeArea.getDataSize();
        if (dataSize == 0) {
            return 1;
        }

        double natLog = Math.log(dataSize == Long.MAX_VALUE ? dataSize : dataSize + 1);
        int positionLength = (int) Math.ceil(natLog / PositionCodeType.HEXADECIMAL.getBaseLog());
        if (minRowPositionLength > 0 && positionLength < minRowPositionLength) {
            positionLength = minRowPositionLength;
        }
        if (maxRowPositionLength > 0 && positionLength > maxRowPositionLength) {
            positionLength = maxRowPositionLength;
        }

        return positionLength == 0 ? 1 : positionLength;
    }

    /**
     * Notify scroll position was modified.
     * <p>
     * This is to assist detection of scrolling from outside compare to
     * scrolling by scrollbar controls.
     */
    public void scrollPositionModified() {
        scrolling.clearLastVerticalScrollingValue();
        recomputeScrollState();
    }

    /**
     * Notify scroll position was changed outside of scrolling.
     */
    public void scrollPositionChanged() {
        recomputeScrollState();
        updateScrollBars();
    }


    private void dataChanged() {
        validateCaret();
        validateSelection();
        recomputeLayout();
    }

    protected int getCharactersPerRow() {
        return structure.getCharactersPerRow();
    }

    public int getBytesPerRow() {
        return structure.getBytesPerRow();
    }

    public int getRowHeight() {
        return metrics.getRowHeight();
    }

    /**
     * Performs update of scrollbars after change in data size or position.
     */
    protected abstract void updateScrollBars();

    /**
     * Attaches painter to code area.
     */
    public abstract void attach();

    /**
     * Detaches painter to code area.
     */
    public abstract void detach();

    protected static class RowDataCache {

        public char[] headerChars;
        public byte[] rowData;
        public char[] rowPositionCode;
        public char[] rowCharacters;
    }
}
