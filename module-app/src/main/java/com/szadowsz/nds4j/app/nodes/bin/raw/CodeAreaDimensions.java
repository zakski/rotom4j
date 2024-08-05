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
 * Basic code area component dimensions.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaDimensions {

    protected int scrollPanelX;
    protected int scrollPanelY;
    protected int scrollPanelWidth;
    protected int scrollPanelHeight;
    protected int verticalScrollBarSize;
    protected int horizontalScrollBarSize;
    protected int dataViewWidth;
    protected int dataViewHeight;
    protected int lastCharOffset;
    protected int lastRowOffset;

    protected int headerAreaHeight;
    protected int rowPositionAreaWidth;
    protected int rowsPerRect;
    protected int rowsPerPage;
    protected int charactersPerPage;
    protected int charactersPerRect;



    public void recomputeSizes(CodeAreaMetrics metrics, int componentX, int componentY, int componentWidth, int componentHeight, int rowPositionLength, int verticalScrollBarSize, int horizontalScrollBarSize) {
        this.verticalScrollBarSize = verticalScrollBarSize;
        this.horizontalScrollBarSize = horizontalScrollBarSize;
        rowPositionAreaWidth = metrics.getCharacterWidth() * (rowPositionLength + 1);
        headerAreaHeight = metrics.getFontHeight() + metrics.getFontHeight() / 4;

        scrollPanelX = componentX + rowPositionAreaWidth;
        scrollPanelY = componentY + headerAreaHeight;
        scrollPanelWidth = componentWidth - rowPositionAreaWidth;
        scrollPanelHeight = componentHeight - headerAreaHeight;
        dataViewWidth = scrollPanelWidth - verticalScrollBarSize;
        dataViewHeight = scrollPanelHeight - horizontalScrollBarSize;
        charactersPerRect = computeCharactersPerRectangle(metrics);
        charactersPerPage = computeCharactersPerPage(metrics);
        rowsPerRect = computeRowsPerRectangle(metrics);
        rowsPerPage = computeRowsPerPage(metrics);
        lastCharOffset = metrics.isInitialized() ? dataViewWidth % metrics.getCharacterWidth() : 0;
        lastRowOffset = metrics.isInitialized() ? dataViewHeight % metrics.getRowHeight() : 0;
    }

    public CodeAreaZone getPositionZone(int positionX, int positionY) {
        if (positionY <= scrollPanelY) {
            if (positionX < rowPositionAreaWidth) {
                return CodeAreaZone.TOP_LEFT_CORNER;
            } else {
                return CodeAreaZone.HEADER;
            }
        }

        if (positionX < rowPositionAreaWidth) {
            if (positionY >= scrollPanelY + scrollPanelHeight) {
                return CodeAreaZone.BOTTOM_LEFT_CORNER;
            } else {
                return CodeAreaZone.ROW_POSITIONS;
            }
        }

        if (positionX >= scrollPanelX + scrollPanelWidth && positionY < scrollPanelY + scrollPanelHeight) {
            return CodeAreaZone.VERTICAL_SCROLLBAR;
        }

        if (positionY >= scrollPanelY + scrollPanelHeight) {
            if (positionX >= scrollPanelX + scrollPanelWidth) {
                return CodeAreaZone.SCROLLBAR_CORNER;
            } else {
                return CodeAreaZone.HORIZONTAL_SCROLLBAR;
            }
        }

        return CodeAreaZone.CODE_AREA;
    }

    protected int computeCharactersPerRectangle(CodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : (dataViewWidth + characterWidth - 1) / characterWidth;
    }

    protected int computeCharactersPerPage(CodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : dataViewWidth / characterWidth;
    }

    protected int computeRowsPerRectangle(CodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : (dataViewHeight + rowHeight - 1) / rowHeight;
    }

    protected int computeRowsPerPage(CodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : dataViewHeight / rowHeight;
    }

    public int getScrollPanelX() {
        return scrollPanelX;
    }

    public int getScrollPanelY() {
        return scrollPanelY;
    }

    public int getVerticalScrollBarSize() {
        return verticalScrollBarSize;
    }

    public int getHorizontalScrollBarSize() {
        return horizontalScrollBarSize;
    }

    public int getScrollPanelWidth() {
        return scrollPanelWidth;
    }

    public int getScrollPanelHeight() {
        return scrollPanelHeight;
    }

    public int getDataViewWidth() {
        return dataViewWidth;
    }

    public int getDataViewHeight() {
        return dataViewHeight;
    }

    public int getHeaderAreaHeight() {
        return headerAreaHeight;
    }

    public int getRowPositionAreaWidth() {
        return rowPositionAreaWidth;
    }

    public int getRowsPerRect() {
        return rowsPerRect;
    }

    public int getCharactersPerRect() {
        return charactersPerRect;
    }

    public int getCharactersPerPage() {
        return charactersPerPage;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public int getLastCharOffset() {
        return lastCharOffset;
    }

    public int getLastRowOffset() {
        return lastRowOffset;
    }
}
