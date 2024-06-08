package com.szadowsz.ui.node;

public enum LayoutType {
    HORIZONAL,
    VERTICAL_1_COL,
    VERTICAL_X_COL;

    public static boolean isVertical(LayoutType t){
        return t == VERTICAL_X_COL || t == VERTICAL_1_COL;
    }
}
