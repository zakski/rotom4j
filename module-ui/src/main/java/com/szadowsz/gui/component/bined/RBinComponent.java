package com.szadowsz.gui.component.bined;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.RComponentBuffer;
import com.szadowsz.gui.component.RSingle;
import com.szadowsz.gui.component.bined.bounds.RBinDimensions;
import com.szadowsz.gui.component.bined.bounds.RBinStructure;
import com.szadowsz.gui.component.bined.bounds.RBinVisibility;
import com.szadowsz.gui.component.bined.settings.RBackgroundPaintMode;
import com.szadowsz.gui.config.text.RFontMetrics;
import com.szadowsz.gui.config.text.RFontStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PFont;
import processing.core.PGraphics;

public abstract class RBinComponent extends RSingle {
    protected final RBinEditor editor;

    protected PFont font;

    protected final RBinDimensions dimensions;
    protected final RBinStructure structure;
    protected final RBinVisibility visibility;
    protected final RFontMetrics metrics;

    protected RBackgroundPaintMode backgroundPaintMode = RBackgroundPaintMode.STRIPED;

    /**
     * Default Binary Sub-Component Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param editor the parent editor reference
     */
    protected RBinComponent(RotomGui gui, String path, RBinEditor editor) {
        super(gui, path, editor);
        this.editor = editor;
        this.dimensions = this.editor.getDimensions();
        this.structure = this.editor.getStructure();
        this.visibility = this.editor.getVisibility();
        this.metrics = this.editor.getMetrics();

        this.font = RFontStore.getMainFont();
    }

    @Override
    protected void drawBackground(PGraphics pg) {
        // NOOP
    }

    protected void drawShiftedChars(PGraphics pg, char[] drawnChars, int charOffset, int length, float positionX, float positionY) {
        pg.text(drawnChars, charOffset, charOffset+length, positionX, positionY);
    }

    /**
     * Draws characters centering it to cells of the same width.
     *
     * @param pg         graphics
     * @param drawnChars array of chars
     * @param charOffset index of target character in array
     * @param length     number of characters to draw
     * @param cellWidth  width of cell to center into
     * @param positionX  X position of drawing area start
     * @param positionY  Y position of drawing area start
     */
    protected void drawCenteredChars(final PGraphics pg, final char[] drawnChars, int charOffset, int length, int cellWidth, float positionX, float positionY) {
        int pos = 0;
        int group = 0;
        while (pos < length) {
            char drawnChar = drawnChars[charOffset + pos];
            int charWidth = metrics.getCharWidth(drawnChar);

            boolean groupable;
            if (metrics.hasUniformLineMetrics()) {
                groupable = charWidth == cellWidth;
            } else {
                int charsWidth = metrics.getCharsWidth(drawnChars, charOffset + pos - group, group + 1);
                groupable = charsWidth == cellWidth * (group + 1);
            }

            switch (Character.getDirectionality(drawnChar)) {
                case Character.DIRECTIONALITY_UNDEFINED:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE:
                case Character.DIRECTIONALITY_POP_DIRECTIONAL_FORMAT:
                case Character.DIRECTIONALITY_BOUNDARY_NEUTRAL:
                case Character.DIRECTIONALITY_OTHER_NEUTRALS:
                    groupable = false;
            }

            if (groupable) {
                group++;
            } else {
                if (group > 0) {
                    drawShiftedChars(pg, drawnChars, charOffset + pos - group, group, positionX + (pos - group) * cellWidth, positionY);
                    group = 0;
                }
                drawShiftedChars(pg, drawnChars, charOffset + pos, 1, positionX + pos * cellWidth + ((cellWidth - charWidth) / 2), positionY);
            }
            pos++;
        }
        if (group > 0) {
            drawShiftedChars(pg, drawnChars, charOffset + pos - group, group, positionX + (pos - group) * cellWidth, positionY);
        }
    }

    @Override
    protected void redrawBuffers() {
        super.redrawBuffers(); // REDRAW-VALID: just carbon copying the redraw so we allow it to be visible
    }
}
