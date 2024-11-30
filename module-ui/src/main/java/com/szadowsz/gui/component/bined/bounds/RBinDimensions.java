package com.szadowsz.gui.component.bined.bounds;

import processing.core.PVector;

public class RBinDimensions {

    protected final PVector scrollPanelRectangle = new PVector();

    protected int charactersPerPage;

    protected int dataViewWidth;
    protected int dataViewHeight;

    protected int rowsPerPage;

    public int getCharactersPerPage() {
        return charactersPerPage;
    }

    public int getDataViewWidth() {
        return dataViewWidth;
    }

    public int getDataViewHeight() {
        return dataViewHeight;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public PVector getScrollPanelRectangle() {
        return scrollPanelRectangle;
    }
}
