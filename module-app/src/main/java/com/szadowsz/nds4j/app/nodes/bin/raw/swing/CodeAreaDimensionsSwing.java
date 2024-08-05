package com.szadowsz.nds4j.app.nodes.bin.raw.swing;

import com.szadowsz.nds4j.app.nodes.bin.raw.CodeAreaDimensions;
import com.szadowsz.nds4j.app.nodes.bin.raw.CodeAreaMetrics;

import java.awt.*;

public class CodeAreaDimensionsSwing extends CodeAreaDimensions {

    private final Rectangle componentRectangle = new Rectangle();
    private final Rectangle mainAreaRectangle = new Rectangle();
    private final Rectangle headerAreaRectangle = new Rectangle();
    private final Rectangle rowPositionAreaRectangle = new Rectangle();
    private final Rectangle scrollPanelRectangle = new Rectangle();
    private final Rectangle dataViewRectangle = new Rectangle();

    @Override
    public void recomputeSizes(CodeAreaMetrics metrics, int componentX, int componentY, int componentWidth, int componentHeight, int rowPositionLength, int verticalScrollBarSize, int horizontalScrollBarSize) {
        componentRectangle.setBounds(componentX, componentY, componentWidth, componentHeight);
        super.recomputeSizes(metrics,componentX,componentY,componentWidth,componentHeight,rowPositionLength,verticalScrollBarSize,horizontalScrollBarSize);

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