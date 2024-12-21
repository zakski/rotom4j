package com.szadowsz.gui.component.bined.bounds;

import com.szadowsz.gui.component.bined.RBinEditor;
import com.szadowsz.gui.component.bined.sizing.RBinMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RBinDimensions {
    private static Logger LOGGER = LoggerFactory.getLogger(RBinDimensions.class);

    protected final RBinRect componentRectangle = new RBinRect();
    protected final RBinRect mainAreaRectangle = new RBinRect();
    protected final RBinRect headerAreaRectangle = new RBinRect();
    protected final RBinRect rowPositionAreaRectangle = new RBinRect();
    protected final RBinRect scrollPanelRectangle = new RBinRect();
    protected final RBinRect dataViewRectangle = new RBinRect();

    protected int charactersPerRect;
    protected int charactersPerPage;

    protected float scrollPanelX;
    protected float scrollPanelY;

    protected float scrollPanelWidth;
    protected float scrollPanelHeight;

    protected float dataViewWidth;
    protected float dataViewHeight;

    protected int rowsPerPage;
    protected int rowsPerRect;

    protected int rowPositionAreaWidth;
    protected int headerAreaHeight;

    protected float lastCharOffset;
    protected float lastRowOffset;

    protected float verticalScrollBarSize;
    protected float horizontalScrollBarSize;

    private int computeCharactersPerRectangle(RBinMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : Math.round((dataViewWidth + characterWidth - 1) / characterWidth);
    }

    private int computeCharactersPerPage(RBinMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : Math.round(dataViewWidth / characterWidth);
    }

    private int computeRowsPerRectangle(RBinMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : Math.round((dataViewHeight + rowHeight - 1) / rowHeight);
    }

    private int computeRowsPerPage(RBinMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : Math.round(dataViewHeight / rowHeight);
    }

    public int getCharactersPerPage() {
        return charactersPerPage;
    }

    public RBinRect getComponentRectangle() {
        return componentRectangle;
    }
    
    public float getDataViewWidth() {
        return dataViewWidth;
    }

    public float getDataViewHeight() {
        return dataViewHeight;
    }

    public RBinRect getDataViewRectangle() {
        return dataViewRectangle;
    }

    public int getHeaderAreaHeight() {
        return headerAreaHeight;
    }

    public RBinRect getHeaderAreaRectangle() {
        return headerAreaRectangle;
    }

    public float getHorizontalScrollBarSize() {
        return horizontalScrollBarSize;
    }

    public Object getLastCharOffset() {
        return lastCharOffset;
    }

    public Object getLastRowOffset() {
        return lastRowOffset;
    }

    public RBinRect getMainAreaRectangle() {
        return mainAreaRectangle;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public int getRowsPerRect() {
        return rowsPerRect;
    }

    public RBinRect getRowPositionAreaRectangle() {
        return rowPositionAreaRectangle;
    }

    public int getRowPositionAreaWidth() {
        return rowPositionAreaWidth;
    }

    public RBinRect getScrollPanelRectangle() {
        return scrollPanelRectangle;
    }

    public float getScrollPanelX() {
        return scrollPanelX;
    }

    public float getScrollPanelY() {
        return scrollPanelY;
    }

    public float getVerticalScrollBarSize() {
        return verticalScrollBarSize;
    }

    public void recomputeSizes(RBinMetrics metrics,
                               float componentX,
                               float componentY,
                               float componentWidth,
                               float componentHeight,
                               int rowPositionLength,
                               float verticalScrollBarSize,
                               float horizontalScrollBarSize) {
        componentRectangle.setBounds(componentX, componentY, componentWidth, componentHeight);
        LOGGER.info("Editor Rectangle: [{},{},{},{}]", componentRectangle.getX(),componentRectangle.getY(),componentRectangle.getWidth(),componentRectangle.getHeight());
        LOGGER.info("Editor Row Position Length: [{}]", rowPositionLength);

        this.verticalScrollBarSize = verticalScrollBarSize;
        this.horizontalScrollBarSize = horizontalScrollBarSize;

        rowPositionAreaWidth = metrics.getCharacterWidth() * (rowPositionLength + 1);
        LOGGER.info("Editor Row Position Area Width: [{}]", rowPositionAreaWidth);
        headerAreaHeight = metrics.getFontHeight() + metrics.getFontHeight() / 4;
        LOGGER.info("Editor Header Area Height: [{}]", headerAreaHeight);

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
}
