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
package com.szadowsz.gui.component.bined.swing;

import com.szadowsz.gui.component.bined.settings.BasicCodeAreaZone;

import java.awt.*;

/**
 * Basic code area component dimensions.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class SwingCodeAreaDimensions {

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

    protected final Rectangle componentRectangle = new Rectangle();
    protected final Rectangle mainAreaRectangle = new Rectangle();
    protected final Rectangle headerAreaRectangle = new Rectangle();
    protected final Rectangle rowPositionAreaRectangle = new Rectangle();
    protected final Rectangle scrollPanelRectangle = new Rectangle();
    protected final Rectangle dataViewRectangle = new Rectangle();

    public void recomputeSizes(SwingCodeAreaMetrics metrics, int componentX, int componentY, int componentWidth, int componentHeight, int rowPositionLength, int verticalScrollBarSize, int horizontalScrollBarSize) {
        componentRectangle.setBounds(componentX, componentY, componentWidth, componentHeight);
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

        boolean availableWidth = rowPositionAreaWidth + verticalScrollBarSize <= componentWidth;
        boolean availableHeight = scrollPanelY + horizontalScrollBarSize <= componentHeight;

        if (availableWidth && availableHeight) {
            mainAreaRectangle.setBounds(componentX + rowPositionAreaWidth, scrollPanelY, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), componentHeight - scrollPanelY - getHorizontalScrollBarSize());
        } else {
            mainAreaRectangle.setBounds(0, 0, 0, 0);
        }
        if (availableWidth) {
            headerAreaRectangle.setBounds(componentX + rowPositionAreaWidth, componentY, componentWidth - rowPositionAreaWidth - getVerticalScrollBarSize(), headerAreaHeight);
        } else {
            headerAreaRectangle.setBounds(0, 0, 0, 0);
        }
        if (availableHeight) {
            rowPositionAreaRectangle.setBounds(componentX, scrollPanelY, rowPositionAreaWidth, componentHeight - scrollPanelY - getHorizontalScrollBarSize());
        } else {
            rowPositionAreaRectangle.setBounds(0, 0, 0, 0);
        }

        scrollPanelRectangle.setBounds(scrollPanelX, scrollPanelY, scrollPanelWidth, scrollPanelHeight);
        dataViewRectangle.setBounds(scrollPanelX, scrollPanelY, Math.max(dataViewWidth, 0), Math.max(dataViewHeight, 0));
    }

    public BasicCodeAreaZone getPositionZone(int positionX, int positionY) {
        if (positionY <= scrollPanelY) {
            if (positionX < rowPositionAreaWidth) {
                return BasicCodeAreaZone.TOP_LEFT_CORNER;
            } else {
                return BasicCodeAreaZone.HEADER;
            }
        }

        if (positionX < rowPositionAreaWidth) {
            if (positionY >= scrollPanelY + scrollPanelHeight) {
                return BasicCodeAreaZone.BOTTOM_LEFT_CORNER;
            } else {
                return BasicCodeAreaZone.ROW_POSITIONS;
            }
        }

        if (positionX >= scrollPanelX + scrollPanelWidth && positionY < scrollPanelY + scrollPanelHeight) {
            return BasicCodeAreaZone.VERTICAL_SCROLLBAR;
        }

        if (positionY >= scrollPanelY + scrollPanelHeight) {
            if (positionX >= scrollPanelX + scrollPanelWidth) {
                return BasicCodeAreaZone.SCROLLBAR_CORNER;
            } else {
                return BasicCodeAreaZone.HORIZONTAL_SCROLLBAR;
            }
        }

        return BasicCodeAreaZone.CODE_AREA;
    }

    private int computeCharactersPerRectangle(SwingCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : (dataViewWidth + characterWidth - 1) / characterWidth;
    }

    private int computeCharactersPerPage(SwingCodeAreaMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : dataViewWidth / characterWidth;
    }

    private int computeRowsPerRectangle(SwingCodeAreaMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : (dataViewHeight + rowHeight - 1) / rowHeight;
    }

    private int computeRowsPerPage(SwingCodeAreaMetrics metrics) {
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

    public Rectangle getComponentRectangle() {
        return componentRectangle;
    }

    public Rectangle getMainAreaRectangle() {
        return mainAreaRectangle;
    }

    public Rectangle getScrollPanelRectangle() {
        return scrollPanelRectangle;
    }

    public Rectangle getDataViewRectangle() {
        return dataViewRectangle;
    }

    public Rectangle getHeaderAreaRectangle() {
        return headerAreaRectangle;
    }

    public Rectangle getRowPositionAreaRectangle() {
        return rowPositionAreaRectangle;
    }
}
