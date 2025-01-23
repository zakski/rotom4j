package com.szadowsz.gui.component.bined;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.bined.bounds.RBinSelection;
import com.szadowsz.gui.component.bined.settings.*;
import com.szadowsz.gui.component.bined.utils.RBinUtils;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.layout.RRect;
import com.szadowsz.rotom4j.binary.BinaryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

import java.awt.*;
import java.util.Arrays;

public class RBinHeader extends RBinComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RBinHeader.class);

    /**
     * Default Binary Sub-Component Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param editor the parent editor reference
     */
    public RBinHeader(RotomGui gui, String path, RBinEditor editor) {
        super(gui, path, editor);
    }

    protected int copyDataFromPos(long dataPosition, int bytesPerRow, long dataSize, int maxBytesPerChar, RBinEditor.RowDataCache rowDataCache) {
        int rowBytesLimit = bytesPerRow;
        //int rowStart = 0;
        if (dataPosition < dataSize) {
            int rowDataSize = bytesPerRow + maxBytesPerChar - 1;
            if (dataSize - dataPosition < rowDataSize) {
                rowDataSize = (int) (dataSize - dataPosition);
            }
//            if (dataPosition < 0) {
//                rowStart = (int) -dataPosition;
//            }
            BinaryData data = editor.getContentData();
            data.copyToArray(dataPosition /*+ rowStart*/, rowDataCache.rowData, 0/*rowStart*/, rowDataSize /*- rowStart*/);
            if (dataSize - dataPosition < rowBytesLimit) {
                rowBytesLimit = (int) (dataSize - dataPosition);
            }
        } else {
            rowBytesLimit = 0;
        }
        return rowBytesLimit;
    }

    protected void fillCodes(int rowBytesLimit, RBinEditor.RowDataCache rowDataCache, RCodeType codeType, RCodeCase codeCharactersCase, int bytesPerRow) {
        int skipToCode = visibility.getSkipToCode();
        int skipRestFromCode = visibility.getSkipRestFromCode();
        int endCode = Math.min(skipRestFromCode, rowBytesLimit);
        for (int byteOnRow = Math.max(skipToCode, 0/*rowStart*/); byteOnRow < endCode; byteOnRow++) {
            byte dataByte = rowDataCache.rowData[byteOnRow];

            int byteRowPos = structure.computeFirstCodeCharacterPos(byteOnRow);
            if (byteRowPos > 0) {
                rowDataCache.rowCharacters[byteRowPos - 1] = ' ';
            }
            RBinUtils.byteToCharsCode(dataByte, codeType, rowDataCache.rowCharacters, byteRowPos, codeCharactersCase);
        }

        if (bytesPerRow > rowBytesLimit) {
            Arrays.fill(rowDataCache.rowCharacters, structure.computeFirstCodeCharacterPos(rowBytesLimit), rowDataCache.rowCharacters.length, ' ');
        }
    }

    protected void fillPreviewChars(long dataPosition, int rowBytesLimit, RBinEditor.RowDataCache rowDataCache, int previewCharPos, RBinCharAssessor charAssessor, int bytesPerRow) {
        int skipToPreview = visibility.getSkipToPreview();
        int skipRestFromPreview = visibility.getSkipRestFromPreview();
        int endPreview = Math.min(skipRestFromPreview, rowBytesLimit);
        for (int byteOnRow = skipToPreview; byteOnRow < endPreview; byteOnRow++) {
            rowDataCache.rowCharacters[previewCharPos + byteOnRow] = charAssessor.getPreviewCharacter(dataPosition, byteOnRow, previewCharPos, RCodeAreaSection.TEXT_PREVIEW);
        }
        if (bytesPerRow > rowBytesLimit) {
            Arrays.fill(rowDataCache.rowCharacters, previewCharPos + rowBytesLimit, previewCharPos + bytesPerRow, ' ');
        }
    }

    protected void prepareRowData(long dataPosition) {
        int maxBytesPerChar = metrics.getMaxBytesPerChar();
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = editor.getDataSize();
        int previewCharPos = visibility.getPreviewCharPos();
        RCodeType codeType = editor.getCodeType();
        RBinViewMode viewMode = editor.getViewMode();
        RCodeCase codeCharactersCase = editor.getCodeCharactersCase();
        RBinEditor.RowDataCache rowDataCache = editor.getRowDataCache();

        RBinCharAssessor charAssessor = editor.getCharAssessor();

        int rowBytesLimit = copyDataFromPos(dataPosition, bytesPerRow, dataSize, maxBytesPerChar, rowDataCache);

        // Fill codes
        if (viewMode != RBinViewMode.TEXT_PREVIEW) {
            fillCodes(rowBytesLimit, rowDataCache, codeType, codeCharactersCase, bytesPerRow);
        }

        if (previewCharPos > 0) {
            rowDataCache.rowCharacters[previewCharPos - 1] = ' ';
        }

        // Fill preview characters
        if (viewMode != RBinViewMode.CODE_MATRIX) {
            fillPreviewChars(dataPosition, rowBytesLimit, rowDataCache, previewCharPos, charAssessor, bytesPerRow);
        }
    }

    @Override
    protected void drawBackground(PGraphics pg) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = editor.getDataSize();
        int rowHeight = metrics.getRowHeight();
        long totalRows = dimensions.getTotalRows();
        RRect contentDims = dimensions.getContentDims();

        pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND));
        if (backgroundPaintMode != RBackgroundPaintMode.TRANSPARENT) {
            pg.rect(contentDims.getX(), contentDims.getY(), contentDims.getWidth(), contentDims.getHeight());
        }

        if (backgroundPaintMode == RBackgroundPaintMode.STRIPED) {
            long dataPosition = bytesPerRow;
            float stripePositionY = contentDims.getY() + rowHeight;
            pg.fill(RThemeStore.getRGBA(RColorType.FOCUS_BACKGROUND));
            for (int row = 0; row <= totalRows / 2; row++) {
                if (dataPosition > dataSize) {
                    break;
                }

                pg.rect(contentDims.getX(), stripePositionY, contentDims.getWidth(), rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2L;
            }
        }
    }

    /**
     * Paints row text.
     *
     * @param pg              graphics
     * @param rowDataPosition row data position
     * @param rowPositionX    row position X
     * @param rowPositionY    row position Y
     */
    protected void drawRowText(PGraphics pg, long rowDataPosition, float rowPositionX, float rowPositionY) {
        int previewCharPos = visibility.getPreviewCharPos();
        int charactersPerRow = structure.getCharactersPerRow();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();

        RBinSelection selectionHandler = editor.getSelectionHandler();
        RBinEditor.RowDataCache rowDataCache = editor.getRowDataCache();

        RBinColorAssessor colorAssessor = editor.getColorAssessor();

        pg.textFont(font);
        float positionY = rowPositionY + (float) (rowHeight - subFontSpace)/2;

        Color lastColor = null;
        Color renderColor = null;

        int skipToChar = visibility.getSkipToChar();
        int skipRestFromChar = visibility.getSkipRestFromChar();
        int renderOffset = skipToChar;
        for (int charOnRow = skipToChar; charOnRow < skipRestFromChar; charOnRow++) {
            RCodeAreaSection section;
            int byteOnRow;
            if (charOnRow >= previewCharPos) {
                byteOnRow = charOnRow - previewCharPos;
                section = RCodeAreaSection.TEXT_PREVIEW;
            } else {
                byteOnRow = structure.computePositionByte(charOnRow);
                section = RCodeAreaSection.CODE_MATRIX;
            }

            char currentChar = rowDataCache.rowCharacters[charOnRow];
            if (currentChar == ' ' && renderOffset == charOnRow) {
                renderOffset++;
                continue;
            }

            boolean inSelection = selectionHandler.isInSelection(rowDataPosition + byteOnRow);
            Color color = colorAssessor.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
            if (color == null) {
                color = RThemeStore.getColor(RColorType.NORMAL_FOREGROUND); //colorsProfile.getTextColor();
            }

            boolean sequenceBreak = false;
            if (!RBinUtils.areSameColors(color, renderColor)) {
                if (renderColor == null) {
                    renderColor = color;
                }

                sequenceBreak = true;
            }

            if (sequenceBreak) {
                if (!RBinUtils.areSameColors(lastColor, renderColor)) {
                    pg.fill(renderColor.getRGB());
                    lastColor = renderColor;
                }

                if (charOnRow > renderOffset) {
                    drawCenteredChars(pg, rowDataCache.rowCharacters, renderOffset, charOnRow - renderOffset, characterWidth, rowPositionX + renderOffset * characterWidth, positionY);
                }

                renderColor = color;
                if (!RBinUtils.areSameColors(lastColor, renderColor)) {
                    pg.fill(renderColor.getRGB());
                    lastColor = renderColor;
                }

                renderOffset = charOnRow;
            }
        }

        if (renderOffset < charactersPerRow) {
            if (!RBinUtils.areSameColors(lastColor, renderColor)) {
                pg.fill(renderColor.getRGB());
            }

            drawCenteredChars(pg, rowDataCache.rowCharacters, renderOffset, charactersPerRow - renderOffset, characterWidth, rowPositionX + renderOffset * characterWidth, positionY);
        }
    }

    protected void drawHeader(PGraphics pg) {
        int charactersPerCodeSection = visibility.getCharactersPerCodeSection();
        RRect headerDims = dimensions.getHeaderDims();

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();

        pg.textFont(font);
        pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND));
        pg.rect(dimensions.getComponentDims().getX(), headerDims.getY(), dimensions.getComponentDims().getWidth(), headerDims.getHeight());

        RBinViewMode viewMode = editor.getViewMode();
        RCodeCase codeCharactersCase = editor.getCodeCharactersCase();
        RBinEditor.RowDataCache rowDataCache = editor.getRowDataCache();
        if (viewMode == RBinViewMode.DUAL || viewMode == RBinViewMode.CODE_MATRIX) {
            float headerX = dimensions.getRowPositionWidth();
            float headerY = headerDims.getY() + rowHeight - metrics.getSubFontSpace();

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
    protected void drawForeground(PGraphics pg, String name) {

        drawHeader(pg);

        RRect rowPositionDims = dimensions.getRowPositionDims();
        RRect headerDims = dimensions.getHeaderDims();

        // Decoration lines
        pg.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); // g.setColor(colorsProfile.getDecorationLine());
        pg.line(headerDims.getX(), headerDims.getHeight() - 1, headerDims.getWidth(), headerDims.getHeight() - 1);

    }

    @Override
    protected void redrawBuffers() {
        super.redrawBuffers();
    }

    @Override
    public float suggestWidth() {
        return dimensions.getComponentDims().getWidth();
    }
}
