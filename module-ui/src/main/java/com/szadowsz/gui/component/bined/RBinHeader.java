package com.szadowsz.gui.component.bined;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.bined.bounds.RBinDimensions;
import com.szadowsz.gui.component.bined.bounds.RBinRect;
import com.szadowsz.gui.component.bined.complex.scroll.RBinScrollPos;
import com.szadowsz.gui.component.bined.settings.CodeAreaViewMode;
import com.szadowsz.gui.component.bined.settings.CodeCharactersCase;
import com.szadowsz.gui.component.bined.settings.CodeType;
import com.szadowsz.gui.component.bined.sizing.RBinMetrics;
import com.szadowsz.gui.component.bined.sizing.RBinStructure;
import com.szadowsz.gui.component.bined.utils.RBinUtils;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import processing.core.PFont;
import processing.core.PGraphics;

import java.awt.*;
import java.util.Arrays;

public class RBinHeader extends RComponent {

    protected RBinEditor editor;

    protected final RBinDimensions dimensions;
    protected final RBinMetrics metrics;
    protected final RBinStructure structure;
    protected final RBinVisibility visibility;
    protected final RBinScrollPos scrollPosition;

    protected PFont font;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param editor the parent component reference
     */
    protected RBinHeader(RotomGui gui, String path, RBinEditor editor) {
        super(gui, path, editor);
        this.editor = editor;
        dimensions = editor.getDimensions();
        metrics = editor.getMetrics();
        structure = editor.getStructure();
        visibility = editor.getVisibility();
        scrollPosition = editor.getScrollPos();
        font = RFontStore.getMainFont();
    }

//    protected void paintHeader(PGraphics g) {
//        int charactersPerCodeSection = visibility.getCharactersPerCodeSection();
//        RBinRect headerArea = dimensions.getHeaderAreaRectangle();
//        RBinScrollPos scrollPosition = scrolling.getScrollPosition();
//
////        RBinRect clipBounds = g.getClipBounds();
////        g.setClip(clipBounds != null ? clipBounds.intersection(headerArea) : headerArea);
//
//        int characterWidth = metrics.getCharacterWidth();
//        int rowHeight = metrics.getRowHeight();
//        float dataViewX = dimensions.getScrollPanelX();
//
//        g.textFont(font);
//        g.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND)); //  g.setColor(colorsProfile.getTextBackground());
//        g.rect(headerArea.getX(), headerArea.getY(), headerArea.getWidth(), headerArea.getHeight());
//
//        CodeAreaViewMode viewMode = structure.getViewMode();
//        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
//            float headerX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
//            float headerY = headerArea.getY() + rowHeight - metrics.getSubFontSpace();
//
//            g.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); //  g.setColor(colorsProfile.getTextColor());
//            Arrays.fill(rowDataCache.headerChars, ' ');
//
//            boolean interleaving = false;
//            int lastPos = 0;
//            int skipToCode = visibility.getSkipToCode();
//            int skipRestFromCode = visibility.getSkipRestFromCode();
//            for (int index = skipToCode; index < skipRestFromCode; index++) {
//                int codePos = structure.computeFirstCodeCharacterPos(index);
//                if (codePos == lastPos + 2 && !interleaving) {
//                    interleaving = true;
//                } else {
//                    RBinUtils.longToBaseCode(rowDataCache.headerChars, codePos, index, CodeType.HEXADECIMAL.getBase(), 2, true, codeCharactersCase);
//                    lastPos = codePos;
//                    interleaving = false;
//                }
//            }
//
//            int skipToChar = visibility.getSkipToChar();
//            int skipRestFromChar = visibility.getSkipRestFromChar();
//            int codeCharEnd = Math.min(skipRestFromChar, visibility.getCharactersPerCodeSection());
//            int renderOffset = skipToChar;
//            Color renderColor = null;
//            for (int characterOnRow = skipToChar; characterOnRow < codeCharEnd; characterOnRow++) {
//                boolean sequenceBreak = false;
//
//                char currentChar = rowDataCache.headerChars[characterOnRow];
//                if (currentChar == ' ' && renderOffset == characterOnRow) {
//                    renderOffset++;
//                    continue;
//                }
//
//                Color color = RThemeStore.getColor(RColorType.NORMAL_FOREGROUND);//colorsProfile.getTextColor();
//
//                if (!RBinUtils.areSameColors(color, renderColor)) { // || !colorType.equals(renderColorType)
//                    sequenceBreak = true;
//                }
//                if (sequenceBreak) {
//                    if (renderOffset < characterOnRow) {
//                        drawCenteredChars(g, rowDataCache.headerChars, renderOffset, characterOnRow - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
//                    }
//
//                    if (!RBinUtils.areSameColors(color, renderColor)) {
//                        renderColor = color;
//                        g.stroke(color.getRGB());
//                    }
//
//                    renderOffset = characterOnRow;
//                }
//            }
//
//            if (renderOffset < charactersPerCodeSection) {
//                drawCenteredChars(g, rowDataCache.headerChars, renderOffset, charactersPerCodeSection - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
//            }
//        }
//
//        // Decoration lines
//        g.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); // g.setColor(colorsProfile.getDecorationLine());
//        g.line(headerArea.getX(), headerArea.getY() + headerArea.getHeight() - 1, headerArea.getX() + headerArea.getWidth(), headerArea.getY() + headerArea.getHeight() - 1);
//        float lineX = dataViewX + visibility.getPreviewRelativeX() - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2 - 1;
//        if (lineX >= dataViewX) {
//            g.line(lineX, headerArea.getY(), lineX, headerArea.getY() + headerArea.getHeight());
//        }
////        g.setClip(clipBounds);
//    }

    protected void drawShiftedChars(PGraphics pg, char[] drawnChars, int charOffset, int length, float positionX, float positionY) {
        pg.text(drawnChars, charOffset, length, positionX, positionY);
    }
    /**
     * Draws characters centering it to cells of the same width.
     *
     * @param pg graphics
     * @param drawnChars array of chars
     * @param charOffset index of target character in array
     * @param length number of characters to draw
     * @param cellWidth width of cell to center into
     * @param positionX X position of drawing area start
     * @param positionY Y position of drawing area start
     */
    protected void drawCenteredChars(PGraphics pg, char[] drawnChars, int charOffset, int length, int cellWidth, float positionX, float positionY) {
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
    protected void drawBackground(PGraphics pg) {

    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        int charactersPerCodeSection = visibility.getCharactersPerCodeSection();
        RBinRect headerArea = dimensions.getHeaderAreaRectangle();
        //RBinScrollPos scrollPosition = scrolling.getScrollPosition();

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        float dataViewX = dimensions.getScrollPanelX();

        pg.textFont(font);
        pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND)); //  g.setColor(colorsProfile.getTextBackground());
        pg.rect(headerArea.getX(), headerArea.getY(), headerArea.getWidth(), headerArea.getHeight());

        CodeAreaViewMode viewMode = structure.getViewMode();
        CodeCharactersCase codeCharactersCase = editor.getCodeCharacterCase();
        RBinEditor.RowDataCache rowDataCache = editor.getRowDataCache();
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            float headerX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
            float headerY = headerArea.getY() + rowHeight - metrics.getSubFontSpace();

            pg.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); //  g.setColor(colorsProfile.getTextColor());
            Arrays.fill(rowDataCache.headerChars, ' ');

            boolean interleaving = false;
            int lastPos = 0;
            int skipToCode = visibility.getSkipToCode();
            int skipRestFromCode = visibility.getSkipRestFromCode();
            for (int index = skipToCode; index < skipRestFromCode; index++) {
                int codePos = structure.computeFirstCodeCharacterPos(index);
                if (codePos == lastPos + 2 && !interleaving) {
                    interleaving = true;
                } else {
                    RBinUtils.longToBaseCode(rowDataCache.headerChars, codePos, index, CodeType.HEXADECIMAL.getBase(), 2, true, codeCharactersCase);
                    lastPos = codePos;
                    interleaving = false;
                }
            }

            int skipToChar = visibility.getSkipToChar();
            int skipRestFromChar = visibility.getSkipRestFromChar();
            int codeCharEnd = Math.min(skipRestFromChar, visibility.getCharactersPerCodeSection());
            int renderOffset = skipToChar;
            Color renderColor = null;
            for (int characterOnRow = skipToChar; characterOnRow < codeCharEnd; characterOnRow++) {
                boolean sequenceBreak = false;

                char currentChar = rowDataCache.headerChars[characterOnRow];
                if (currentChar == ' ' && renderOffset == characterOnRow) {
                    renderOffset++;
                    continue;
                }

                Color color = RThemeStore.getColor(RColorType.NORMAL_FOREGROUND);//colorsProfile.getTextColor();

                if (!RBinUtils.areSameColors(color, renderColor)) { // || !colorType.equals(renderColorType)
                    sequenceBreak = true;
                }
                if (sequenceBreak) {
                    if (renderOffset < characterOnRow) {
                        drawCenteredChars(pg, rowDataCache.headerChars, renderOffset, characterOnRow - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
                    }

                    if (!RBinUtils.areSameColors(color, renderColor)) {
                        renderColor = color;
                        pg.stroke(color.getRGB());
                    }

                    renderOffset = characterOnRow;
                }
            }

            if (renderOffset < charactersPerCodeSection) {
                drawCenteredChars(pg, rowDataCache.headerChars, renderOffset, charactersPerCodeSection - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
            }
        }
    }

    @Override
    public float suggestWidth() {
        return dimensions.getHeaderAreaRectangle().getWidth();
    }
}
