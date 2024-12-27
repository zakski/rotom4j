package com.szadowsz.gui.component.bined;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.bined.bounds.RBinRect;
import com.szadowsz.gui.component.bined.bounds.RBinSelection;
import com.szadowsz.gui.component.bined.caret.CursorRenderingMode;
import com.szadowsz.gui.component.bined.caret.RCaret;
import com.szadowsz.gui.component.bined.scroll.RBinScrollPos;
import com.szadowsz.gui.component.bined.settings.*;
import com.szadowsz.gui.component.bined.utils.RBinUtils;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.nds4j.file.bin.core.BinaryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

import java.awt.*;
import java.util.Arrays;

public class RBinMain extends RBinComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RBinMain.class);

    public RBinMain(RotomGui gui, String path, RBinEditor editor) {
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

    protected void fillCodes(int rowBytesLimit, RBinEditor.RowDataCache rowDataCache, CodeType codeType, CodeCharactersCase codeCharactersCase, int bytesPerRow) {
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
            rowDataCache.rowCharacters[previewCharPos + byteOnRow] = charAssessor.getPreviewCharacter(dataPosition, byteOnRow, previewCharPos, CodeAreaSection.TEXT_PREVIEW);
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
        CodeType codeType = structure.getCodeType();
        CodeAreaViewMode viewMode = structure.getViewMode();
        CodeCharactersCase codeCharactersCase = editor.getCodeCharactersCase();
        RBinEditor.RowDataCache rowDataCache = editor.getRowDataCache();

        RBinCharAssessor charAssessor = editor.getCharAssessor();

        int rowBytesLimit = copyDataFromPos(dataPosition, bytesPerRow, dataSize, maxBytesPerChar, rowDataCache);

        // Fill codes
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            fillCodes(rowBytesLimit, rowDataCache, codeType, codeCharactersCase, bytesPerRow);
        }

        if (previewCharPos > 0) {
            rowDataCache.rowCharacters[previewCharPos - 1] = ' ';
        }

        // Fill preview characters
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            fillPreviewChars(dataPosition, rowBytesLimit, rowDataCache, previewCharPos, charAssessor, bytesPerRow);
        }
    }

    /**
     * Renders sequence of background rectangles.
     * <p>
     * Doesn't include character at offset end.
     */
    protected void renderBackgroundSequence(PGraphics pg, int startOffset, int endOffset, float rowPositionX, float positionY) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        pg.rect(rowPositionX + startOffset * characterWidth, positionY, (endOffset - startOffset) * characterWidth, rowHeight);
    }

    /**
     * Paints row background.
     *
     * @param pg              graphics
     * @param rowDataPosition row data position
     * @param rowPositionX    row position X
     * @param rowPositionY    row position Y
     */
    protected void paintRowBackground(PGraphics pg, long rowDataPosition, float rowPositionX, float rowPositionY) {
        int previewCharPos = visibility.getPreviewCharPos();
        CodeAreaViewMode viewMode = structure.getViewMode();
        int charactersPerRow = structure.getCharactersPerRow();
        int skipToChar = visibility.getSkipToChar();
        int skipRestFromChar = visibility.getSkipRestFromChar();
        RBinSelection selectionHandler = editor.getSelectionHandler();

        RBinColorAssessor colorAssessor = editor.getColorAssessor();

        int renderOffset = skipToChar;
        Color renderColor = null;
        for (int charOnRow = skipToChar; charOnRow < skipRestFromChar; charOnRow++) {
            CodeAreaSection section;
            int byteOnRow;
            if (charOnRow >= previewCharPos && viewMode != CodeAreaViewMode.CODE_MATRIX) {
                byteOnRow = charOnRow - previewCharPos;
                section = CodeAreaSection.TEXT_PREVIEW;
            } else {
                byteOnRow = structure.computePositionByte(charOnRow);
                section = CodeAreaSection.CODE_MATRIX;
            }
            boolean sequenceBreak = false;

            boolean inSelection = selectionHandler.isInSelection(rowDataPosition + byteOnRow);
            Color color = colorAssessor.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section, inSelection);
            if (!RBinUtils.areSameColors(color, renderColor)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnRow) {
                    if (renderColor != null) {
                        renderBackgroundSequence(pg, renderOffset, charOnRow, rowPositionX, rowPositionY);
                    }
                }

                if (!RBinUtils.areSameColors(color, renderColor)) {
                    renderColor = color;
                    if (color != null) {
                        pg.fill(color.getRGB());
                    }
                }

                renderOffset = charOnRow;
            }
        }

        if (renderOffset < charactersPerRow) {
            if (renderColor != null) {
                renderBackgroundSequence(pg, renderOffset, charactersPerRow, rowPositionX, rowPositionY);
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
            CodeAreaSection section;
            int byteOnRow;
            if (charOnRow >= previewCharPos) {
                byteOnRow = charOnRow - previewCharPos;
                section = CodeAreaSection.TEXT_PREVIEW;
            } else {
                byteOnRow = structure.computePositionByte(charOnRow);
                section = CodeAreaSection.CODE_MATRIX;
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

    protected void drawRows(PGraphics pg) {
        int bytesPerRow = structure.getBytesPerRow();
        int rowHeight = metrics.getRowHeight();
        float dataViewX = dimensions.getRowPositionAreaWidth();
        float dataViewY = dimensions.getHeaderAreaHeight();
        int rowsPerRect = dimensions.getRowsPerRect();
        long dataSize = editor.getDataSize();
        long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
        int characterWidth = metrics.getCharacterWidth();
        float rowPositionX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
        float rowPositionY = dataViewY - scrollPosition.getRowOffset();

        pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); //  pg.setColor(colorsProfile.getTextColor());
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }
            pg.pushMatrix();
            pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); //  pg.setColor(colorsProfile.getTextColor());
            LOGGER.debug("rendering row {} of {} @ [{},{}]", row, rowsPerRect, rowPositionX, rowPositionY);
            prepareRowData(dataPosition);
            LOGGER.debug("row characters: {}", editor.getRowDataCache().rowCharacters);
            //paintRowBackground(pg, dataPosition, rowPositionX, rowPositionY);
            drawRowText(pg, dataPosition, rowPositionX, rowPositionY);
            rowPositionY += rowHeight;
            if (Long.MAX_VALUE - dataPosition < bytesPerRow) {
                dataPosition = Long.MAX_VALUE;
            } else {
                dataPosition += bytesPerRow;
            }
            pg.popMatrix();
        }
    }

    private void drawCursorRect(PGraphics pg, float cursorX, float cursorY, float width, float height, CursorRenderingMode renderingMode, RCaret caret) {
        RBinColorAssessor colorAssessor = editor.getColorAssessor();
        RBinCharAssessor charAssessor = editor.getCharAssessor();
        RBinEditor.CursorDataCache cursorDataCache = editor.getCursorDataCache();

        switch (renderingMode) {
            case PAINT: {
                pg.rect(cursorX, cursorY, width, height);
                break;
            }
            case XOR: {
                Graphics2D g2d = ((Graphics2D) pg.getNative());
                g2d.setXORMode(RThemeStore.getColor(RColorType.NORMAL_BACKGROUND) /*  pg.setColor(colorsProfile.getTextBackground()*/);
                pg.rect(cursorX, cursorY, width, height);
                g2d.setPaintMode();
                break;
            }
            case NEGATIVE: {
                int characterWidth = metrics.getCharacterWidth();
                int rowHeight = metrics.getRowHeight();
                int maxBytesPerChar = metrics.getMaxBytesPerChar();
                int subFontSpace = metrics.getSubFontSpace();
                float dataViewX = dimensions.getRowPositionAreaWidth();
                float dataViewY = dimensions.getHeaderAreaHeight();
                int previewRelativeX = visibility.getPreviewRelativeX();

                CodeAreaViewMode viewMode = structure.getViewMode();
                RBinScrollPos scrollPosition = editor.getScrollPos();
                long dataSize = editor.getDataSize();
                CodeType codeType = structure.getCodeType();
                pg.rect(cursorX, cursorY, width, height);
                pg.fill(RThemeStore.getRGBA(RColorType.CURSOR_NEGATIVE)); // g.setColor(colorsProfile.getCursorNegativeColor());
                BinaryData contentData = editor.getContentData();
                int row = Math.round((cursorY + scrollPosition.getRowOffset() - dataViewY) / rowHeight);
                float scrolledX = cursorX + scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();
                //float posY = dataViewY + (row + 1) * rowHeight - subFontSpace - scrollPosition.getRowOffset();
                float posY = dataViewY + (row) * rowHeight + (float) (rowHeight - subFontSpace)/2 ;//- scrollPosition.getRowOffset();
                long dataPosition = caret.getDataPosition();
                if (viewMode != CodeAreaViewMode.CODE_MATRIX && caret.getSection() == CodeAreaSection.TEXT_PREVIEW) {
                    int charPos = Math.round((scrolledX - previewRelativeX) / characterWidth);
                    if (dataPosition >= dataSize) {
                        break;
                    }

                    int byteOnRow = (int) (dataPosition % structure.getBytesPerRow());
                    int previewCharPos = visibility.getPreviewCharPos();

                    if (contentData.isEmpty()) {
                        cursorDataCache.cursorChars[0] = charAssessor.getPreviewCursorCharacter(dataPosition, byteOnRow, previewCharPos, cursorDataCache.cursorData, 0, CodeAreaSection.TEXT_PREVIEW);
                    } else {
                        if (maxBytesPerChar > 1) {
                            int charDataLength = maxBytesPerChar;
                            if (dataPosition + maxBytesPerChar > dataSize) {
                                charDataLength = (int) (dataSize - dataPosition);
                            }

                            contentData.copyToArray(dataPosition, cursorDataCache.cursorData, 0, charDataLength);
                            cursorDataCache.cursorChars[0] = charAssessor.getPreviewCursorCharacter(dataPosition, byteOnRow, previewCharPos, cursorDataCache.cursorData, charDataLength, CodeAreaSection.TEXT_PREVIEW);
                        } else {
                            cursorDataCache.cursorData[0] = contentData.getByte(dataPosition);
                            cursorDataCache.cursorChars[0] = charAssessor.getPreviewCursorCharacter(dataPosition, byteOnRow, previewCharPos, cursorDataCache.cursorData, 1, CodeAreaSection.TEXT_PREVIEW);
                        }
                    }
                    int posX = previewRelativeX + charPos * characterWidth - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
                    drawCenteredChars(pg, cursorDataCache.cursorChars, 0, 1, characterWidth, posX, posY);
                } else {
                    int charPos = Math.round((scrolledX - dataViewX) / characterWidth);
                    int byteOffset = structure.computePositionByte(charPos);
                    int codeCharPos = structure.computeFirstCodeCharacterPos(byteOffset);

                    if (dataPosition < dataSize) {
                        byte dataByte = contentData.getByte(dataPosition);
                        RBinUtils.byteToCharsCode(dataByte, codeType, cursorDataCache.cursorChars, 0, editor.getCodeCharactersCase());
                    } else {
                        Arrays.fill(cursorDataCache.cursorChars, ' ');
                    }
                    float posX = dataViewX + codeCharPos * characterWidth - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
                    int charsOffset = charPos - codeCharPos;
                    drawCenteredChars(pg, cursorDataCache.cursorChars, charsOffset, 1, characterWidth, posX + (charsOffset * characterWidth), posY);
                }
                break;
            }
            default:
                throw RBinUtils.getInvalidTypeException(renderingMode);
        }
    }

    protected void drawCursor(PGraphics pg) {
        if (!gui.hasFocus(editor)) {
            LOGGER.info("No Cursor Focus for {}",editor.getName());
            return;
        }

        int maxBytesPerChar = metrics.getMaxBytesPerChar();
        RBinRect mainAreaRect = dimensions.getMainAreaRectangle();
        CodeType codeType = structure.getCodeType();
        CodeAreaViewMode viewMode = structure.getViewMode();
        RBinEditor.CursorDataCache cursorDataCache = editor.getCursorDataCache();

        RCaret caret = editor.getCaret();
        RBinRect cursorRect = editor.getCursorPositionRect(caret.getDataPosition(), caret.getCodeOffset(), caret.getSection());
        if (cursorRect.isEmpty()) {
            return;
        }

        RBinRect scrolledCursorRect = new RBinRect(cursorRect.getX(), cursorRect.getY(), cursorRect.getWidth(), cursorRect.getHeight());
        RBinRect intersection = scrolledCursorRect.intersection(mainAreaRect);
        boolean cursorVisible = caret.isVisible() && !intersection.isEmpty();

        if (cursorVisible) {
            CursorRenderingMode renderingMode = caret.getRenderingMode();
            pg.stroke(RThemeStore.getRGBA(RColorType.CURSOR)); // pg.setColor(colorsProfile.getCursorColor());

            drawCursorRect(pg, intersection.getX(), intersection.getY(), intersection.getWidth(), intersection.getHeight(), renderingMode, caret);
        }

        // Paint mirror cursor
        if (viewMode == CodeAreaViewMode.DUAL && editor.isMirrorCursorShowing()) {
            editor.updateMirrorCursorRect(caret.getDataPosition(), caret.getSection());
            RBinRect mirrorCursorRect = cursorDataCache.mirrorCursorRect;
            if (!mirrorCursorRect.isEmpty()) {
                intersection = mainAreaRect.intersection(mirrorCursorRect);
                boolean mirrorCursorVisible = !intersection.isEmpty();
                if (mirrorCursorVisible) {
//                    //pg.setClip(intersection);
//                    pg.stroke(RThemeStore.getRGBA(RColorType.CURSOR)); // pg.setColor(colorsProfile.getCursorColor());
//                    Graphics2D g2d = (Graphics2D) ((Graphics) pg.getNative()).create();
//                    g2d.setStroke(cursorDataCache.dashedStroke);
//                    //g2d.drawRect(mirrorCursorRect.getX(), mirrorCursorRect.getY(), mirrorCursorRect.getWidth() - 1, mirrorCursorRect.getHeight() - 1);
//                    g2d.dispose();
                }
            }
        }
        //pg.setClip(clipBounds);
    }

    protected void drawHeader(PGraphics pg) {
        int charactersPerCodeSection = visibility.getCharactersPerCodeSection();
        RBinRect headerArea = dimensions.getHeaderAreaRectangle();
        //RBinScrollPos scrollPosition = scrolling.getScrollPosition();

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        //float dataViewX = dimensions.getScrollPanelX();

        pg.textFont(font);
        pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND)); //  g.setColor(colorsProfile.getTextBackground());
        pg.rect(headerArea.getX(), headerArea.getY(), headerArea.getWidth(), headerArea.getHeight());

        CodeAreaViewMode viewMode = structure.getViewMode();
        CodeCharactersCase codeCharactersCase = editor.getCodeCharactersCase();
        RBinEditor.RowDataCache rowDataCache = editor.getRowDataCache();
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            float headerX = dimensions.getRowPositionAreaWidth();//dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
            float headerY = headerArea.getY() + rowHeight - metrics.getSubFontSpace();

            pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); //  g.setColor(colorsProfile.getTextColor());
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
                        pg.fill(color.getRGB());
                    }

                    renderOffset = characterOnRow;
                }
            }

            if (renderOffset < charactersPerCodeSection) {
                drawCenteredChars(pg, rowDataCache.headerChars, renderOffset, charactersPerCodeSection - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
            }
        }
    }


    protected void drawRowPosition(PGraphics pg) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = editor.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();
        int rowsPerRect = dimensions.getRowsPerRect();

        RBinRect rowPosRectangle = dimensions.getRowPositionAreaRectangle();
        RBinRect dataViewRectangle = dimensions.getDataViewRectangle();
        RBinEditor.RowDataCache rowDataCache = editor.getRowDataCache();
        int rowPositionLength = editor.getRowPositionLength();
        BackgroundPaintMode backgroundPaintMode = editor.getBackgroundPaintMode();

        pg.textFont(font);
        pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND)); //  g.setColor(colorsProfile.getTextBackground());
        pg.rect(rowPosRectangle.getX(), rowPosRectangle.getY(), rowPosRectangle.getWidth(), rowPosRectangle.getHeight());

        if (backgroundPaintMode == BackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : bytesPerRow);
            float stripePositionY = rowPosRectangle.getY() - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            pg.fill(RThemeStore.getRGBA(RColorType.FOCUS_BACKGROUND)); //  g.setColor(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition > dataSize) {
                    break;
                }

                pg.rect(rowPosRectangle.getX(), stripePositionY, rowPosRectangle.getWidth(), rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }

        long dataPosition = bytesPerRow * scrollPosition.getRowPosition();
        float positionY = rowPosRectangle.getY() + (float) (rowHeight - subFontSpace)/2;// - scrollPosition.getRowOffset();
        pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); //  g.setColor(colorsProfile.getTextColor());
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            RBinUtils.longToBaseCode(rowDataCache.rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, CodeType.HEXADECIMAL.getBase(), rowPositionLength, true, editor.getCodeCharactersCase());
            drawCenteredChars(pg, rowDataCache.rowPositionCode, 0, rowPositionLength, characterWidth, rowPosRectangle.getX(), positionY);

            positionY += rowHeight;
            dataPosition += bytesPerRow;
            if (dataPosition < 0) {
                break;
            }
        }

        // Decoration lines
        pg.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); // g.setColor(colorsProfile.getDecorationLine());
        float lineX = rowPosRectangle.getX() + rowPosRectangle.getWidth() - (characterWidth / 2);
        if (lineX >= rowPosRectangle.getX()) {
            pg.line(lineX, dataViewRectangle.getY(), lineX, dataViewRectangle.getY() + dataViewRectangle.getHeight());
        }
        pg.line(dataViewRectangle.getX(), dataViewRectangle.getY() - 1, dataViewRectangle.getX() + dataViewRectangle.getWidth(), dataViewRectangle.getY() - 1);
    }

    @Override
    protected void drawBackground(PGraphics pg) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = editor.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int rowsPerRect = dimensions.getRowsPerRect();
        RBinRect dataViewRect = dimensions.getDataViewRectangle();
        BackgroundPaintMode backgroundPaintMode = editor.getBackgroundPaintMode();

        pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND)); //  pg.setColor(colorsProfile.getTextBackground());
        if (backgroundPaintMode != BackgroundPaintMode.TRANSPARENT) {
            pg.rect(dataViewRect.getX(), dataViewRect.getY(), dataViewRect.getWidth(), dataViewRect.getHeight());
        }

        if (backgroundPaintMode == BackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : bytesPerRow);
            float stripePositionY = dataViewRect.getY() - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            pg.fill(RThemeStore.getRGBA(RColorType.FOCUS_BACKGROUND)); //  pg.setColor(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition > dataSize) {
                    break;
                }

                pg.rect(dataViewRect.getX(), stripePositionY, dataViewRect.getWidth(), rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
//        RBinRect mainAreaRect = dimensions.getMainAreaRectangle();
//        RBinRect dataViewRectangle = dimensions.getDataViewRectangle();
//        int characterWidth = metrics.getCharacterWidth();
//        int previewRelativeX = visibility.getPreviewRelativeX();
        editor.updateAssessors();

        drawHeader(pg);
        drawRowPosition(pg);

        // Decoration lines
        pg.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); // pg.setColor(colorsProfile.getDecorationLine());
//        float lineX = dataViewRectangle.getX() + previewRelativeX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2 - 1;
//        if (lineX >= dataViewRectangle.getX()) {
//            pg.line(lineX, dataViewRectangle.getY(), lineX, dataViewRectangle.getY() + dataViewRectangle.getHeight());
//        }

        drawRows(pg);
        drawCursor(pg);

//        paintDebugInfo(pg, mainAreaRect, scrollPosition);
    }

    @Override
    public float suggestWidth() {
        return size.x;
    }
}
