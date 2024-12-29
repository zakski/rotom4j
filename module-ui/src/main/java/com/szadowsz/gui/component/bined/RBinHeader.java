package com.szadowsz.gui.component.bined;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.bined.settings.RBinViewMode;
import com.szadowsz.gui.component.bined.settings.RCodeCase;
import com.szadowsz.gui.component.bined.settings.RCodeType;
import com.szadowsz.gui.component.bined.utils.RBinUtils;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.layout.RRect;
import processing.core.PGraphics;

import java.awt.*;
import java.util.Arrays;

public class RBinHeader extends RBinComponent {

    /**
     * Default Binary Sub-Component Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param editor the parent editor reference
     */
    protected RBinHeader(RotomGui gui, String path, RBinEditor editor) {
        super(gui, path, editor);
        size.y = dimensions.getHeaderAreaHeight();
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        int charactersPerCodeSection = visibility.getCharactersPerCodeSection();
        RRect headerArea = dimensions.getHeaderAreaRectangle();

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();

        pg.textFont(font);
        pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND));
        pg.rect(headerArea.getX(), headerArea.getY(), headerArea.getWidth(), headerArea.getHeight());

        RBinViewMode viewMode = editor.getViewMode();
        RCodeCase codeCharactersCase = editor.getCodeCharactersCase();
        RBinEditor.RowDataCache rowDataCache = editor.getRowDataCache();
        if (viewMode == RBinViewMode.DUAL || viewMode == RBinViewMode.CODE_MATRIX) {
            float headerX = dimensions.getRowPositionAreaWidth();
            float headerY = headerArea.getY() + rowHeight - metrics.getSubFontSpace();

            pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND));
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
                    RBinUtils.longToBaseCode(rowDataCache.headerChars, codePos, index, RCodeType.HEXADECIMAL.getBase(), 2, true, codeCharactersCase);
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
                        drawCenteredChars(pg,
                                rowDataCache.headerChars,
                                renderOffset,
                                characterOnRow - renderOffset,
                                characterWidth,
                                headerX + renderOffset * characterWidth,
                                headerY
                        );
                    }

                    if (!RBinUtils.areSameColors(color, renderColor)) {
                        renderColor = color;
                        pg.fill(color.getRGB());
                    }

                    renderOffset = characterOnRow;
                }
            }

            if (renderOffset < charactersPerCodeSection) {
                drawCenteredChars(pg,
                        rowDataCache.headerChars,
                        renderOffset,
                        charactersPerCodeSection - renderOffset,
                        characterWidth,
                        headerX + renderOffset * characterWidth,
                        headerY
                );
            }
        }

    }

    @Override
    public float suggestWidth() {
        return dimensions.getComponentRectangle().getWidth();
    }
}
