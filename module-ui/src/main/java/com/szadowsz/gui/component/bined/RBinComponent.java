package com.szadowsz.gui.component.bined;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.bined.bounds.RBinDimensions;
import com.szadowsz.gui.component.bined.scroll.RBinScrollPos;
import com.szadowsz.gui.component.bined.sizing.RBinMetrics;
import com.szadowsz.gui.component.bined.sizing.RBinStructure;
import com.szadowsz.gui.config.text.RFontStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PFont;
import processing.core.PGraphics;

public abstract class RBinComponent extends RComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RBinComponent.class);

    protected final RBinDimensions dimensions;
    protected final RBinMetrics metrics;
    protected final RBinStructure structure;
    protected final RBinVisibility visibility;
    protected final RBinScrollPos scrollPosition;
    protected RBinEditor editor;
    protected PFont font;

    public RBinComponent(RotomGui gui, String path, RBinEditor editor) {
        super(gui, path, editor);
        this.editor = editor;
        dimensions = editor.getDimensions();
        metrics = editor.getMetrics();
        structure = editor.getStructure();
        visibility = editor.getVisibility();
        font = RFontStore.getMainFont();
        scrollPosition = editor.getScrollPos();
    }

    protected void drawShiftedChars(PGraphics pg, char[] drawnChars, int charOffset, int length, float positionX, float positionY) {
        pg.text(drawnChars, charOffset, length, positionX, positionY);
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
    protected void drawCenteredChars(PGraphics pg, char[] drawnChars, int charOffset, int length, int cellWidth, float positionX, float positionY) {
        LOGGER.info("Center Chars [offset {}, length {}, cellWidth {}, [{},{}] ] ",charOffset,length,cellWidth,positionX,positionY);
        pg.text(drawnChars, 0, length, positionX, positionY);

//        int pos = 0;
//        int group = 0;
//        while (pos < length) {
//            char drawnChar = drawnChars[charOffset + pos];
//            int charWidth = metrics.getCharWidth(drawnChar);
//
//            boolean groupable;
//            if (metrics.hasUniformLineMetrics()) {
//                groupable = charWidth == cellWidth;
//            } else {
//                int charsWidth = metrics.getCharsWidth(drawnChars, charOffset + pos - group, group + 1);
//                groupable = charsWidth == cellWidth * (group + 1);
//            }
//
//            switch (Character.getDirectionality(drawnChar)) {
//                case Character.DIRECTIONALITY_UNDEFINED:
//                case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
//                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
//                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING:
//                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE:
//                case Character.DIRECTIONALITY_POP_DIRECTIONAL_FORMAT:
//                case Character.DIRECTIONALITY_BOUNDARY_NEUTRAL:
//                case Character.DIRECTIONALITY_OTHER_NEUTRALS:
//                    groupable = false;
//            }
//
//            if (groupable) {
//                group++;
//            } else {
//                if (group > 0) {
//                    drawShiftedChars(pg, drawnChars, charOffset + pos - group, group, positionX + (pos - group) * cellWidth, positionY);
//                    group = 0;
//                }
//                drawShiftedChars(pg, drawnChars, charOffset + pos, 1, positionX + pos * cellWidth + ((cellWidth - charWidth) / 2), positionY);
//            }
//            pos++;
//        }
//        if (group > 0) {
//            drawShiftedChars(pg, drawnChars, charOffset + pos - group, group, positionX + (pos - group) * cellWidth, positionY);
//        }
    }

    @Override
    protected void drawBackground(PGraphics pg) {

    }
}
