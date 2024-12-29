package com.szadowsz.gui.component.bined.cursor;

/**
 * Method for rendering cursor into RBinMain component.
 */
public enum RCursorRenderingMode {
    /**
     * Cursor is just painted.
     */
    PAINT,
    /**
     * Cursor is painted using pixels inversion.
     */
    XOR,
    /**
     * Underlying character is painted using negative color to cursor
     * cursor.
     */
    NEGATIVE
}