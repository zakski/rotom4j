package com.szadowsz.gui.component.bined;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.bined.bounds.RBinRect;
import com.szadowsz.gui.component.bined.settings.CodeAreaViewMode;
import com.szadowsz.gui.component.bined.settings.CodeCharactersCase;
import com.szadowsz.gui.component.bined.settings.CodeType;
import com.szadowsz.gui.component.bined.utils.RBinUtils;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

import java.awt.*;
import java.util.Arrays;

public class RBinHeader extends RBinComponent {

    private static Logger LOGGER = LoggerFactory.getLogger(RBinHeader.class);

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
        CodeCharactersCase codeCharactersCase = editor.getCodeCharactersCase();
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
