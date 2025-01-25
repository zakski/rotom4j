package com.szadowsz.gui.component.bined;

import com.szadowsz.rotom4j.binary.BinaryData;
import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.bined.bounds.RBinSelection;
import com.szadowsz.gui.component.bined.cursor.RCaret;
import com.szadowsz.gui.component.bined.cursor.RCursorRenderingMode;
import com.szadowsz.gui.component.bined.settings.*;
import com.szadowsz.gui.component.bined.utils.RBinUtils;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.layout.RRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

import java.awt.*;
import java.util.Arrays;

public class RBinMain extends RBinComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RBinMain.class);

    /**
     * Default Binary Sub-Component Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param editor the parent editor reference
     */
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
            pg.rect(contentDims.getX(), 0, contentDims.getWidth(), contentDims.getHeight());
        }

        if (backgroundPaintMode == RBackgroundPaintMode.STRIPED) {
            long dataPosition = bytesPerRow;
            float stripePositionY = rowHeight;
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

    protected void drawRows(PGraphics pg) {
        int bytesPerRow = structure.getBytesPerRow();
        int rowHeight = metrics.getRowHeight();
        float contentX = dimensions.getRowPositionWidth();
        long totalRows = dimensions.getTotalRows();

        long dataSize = editor.getDataSize();
        long dataPosition = 0;

        float rowPositionX = contentX;
        float rowPositionY = 0;

        pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); //  pg.setColor(colorsProfile.getTextColor());
        for (int row = 0; row <= totalRows; row++) {
            if (dataPosition > dataSize) {
                break;
            }
            pg.pushMatrix();
            pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); //  pg.setColor(colorsProfile.getTextColor());
            LOGGER.debug("rendering row {} of {} @ [{},{}]", row, totalRows, rowPositionX, rowPositionY);
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

    protected void drawCursorRect(PGraphics pg, float cursorX, float cursorY, float width, float height, RCursorRenderingMode renderingMode, RCaret caret) {
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
                float contentX = dimensions.getRowPositionWidth();
                float contentY = dimensions.getHeaderHeight();
                int previewRelativeX = visibility.getPreviewRelativeX();

                RBinViewMode viewMode = editor.getViewMode();

                long dataSize = editor.getDataSize();
                RCodeType codeType = editor.getCodeType();

                pg.rect(cursorX, cursorY - dimensions.getHeaderHeight(), width, height);
                pg.fill(RThemeStore.getRGBA(RColorType.CURSOR_NEGATIVE)); // g.setColor(colorsProfile.getCursorNegativeColor());

                BinaryData contentData = editor.getContentData();
                int row = Math.round((cursorY - contentY) / rowHeight);
                float scrolledX = cursorX;
                //float posY = dataViewY + (row + 1) * rowHeight - subFontSpace - scrollPosition.getRowOffset();
                float posY = (row) * rowHeight + (float) (rowHeight - subFontSpace)/2 ;//- scrollPosition.getRowOffset();
                long dataPosition = caret.getDataPosition();
                if (viewMode != RBinViewMode.CODE_MATRIX && caret.getSection() == RCodeAreaSection.TEXT_PREVIEW) {
                    int charPos = Math.round((scrolledX - previewRelativeX) / characterWidth);
                    if (dataPosition >= dataSize) {
                        break;
                    }

                    if (contentData.isEmpty()) {
                        cursorDataCache.cursorChars[0] = charAssessor.getPreviewCursorCharacter(cursorDataCache.cursorData, 0);
                    } else {
                        if (maxBytesPerChar > 1) {
                            int charDataLength = maxBytesPerChar;
                            if (dataPosition + maxBytesPerChar > dataSize) {
                                charDataLength = (int) (dataSize - dataPosition);
                            }

                            contentData.copyToArray(dataPosition, cursorDataCache.cursorData, 0, charDataLength);
                            cursorDataCache.cursorChars[0] = charAssessor.getPreviewCursorCharacter(cursorDataCache.cursorData, charDataLength);
                        } else {
                            cursorDataCache.cursorData[0] = contentData.getByte(dataPosition);
                            cursorDataCache.cursorChars[0] = charAssessor.getPreviewCursorCharacter(cursorDataCache.cursorData, 1);
                        }
                    }
                    int posX = previewRelativeX + charPos * characterWidth;
                    drawCenteredChars(pg, cursorDataCache.cursorChars, 0, 1, characterWidth, posX, posY);
                } else {
                    int charPos = Math.round((scrolledX - contentX) / characterWidth);
                    int byteOffset = structure.computePositionByte(charPos);
                    int codeCharPos = structure.computeFirstCodeCharacterPos(byteOffset);

                    if (dataPosition < dataSize) {
                        byte dataByte = contentData.getByte(dataPosition);
                        RBinUtils.byteToCharsCode(dataByte, codeType, cursorDataCache.cursorChars, 0, editor.getCodeCharactersCase());
                    } else {
                        Arrays.fill(cursorDataCache.cursorChars, ' ');
                    }
                    float posX = contentX + codeCharPos * characterWidth;
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

        RRect contentDims = dimensions.getContentDims();
        RBinViewMode viewMode = editor.getViewMode();
        RBinEditor.CursorDataCache cursorDataCache = editor.getCursorDataCache();

        RCaret caret = editor.getCaret();
        RRect cursorRect = editor.getCursorPositionRect(caret.getDataPosition(), caret.getCodeOffset(), caret.getSection());
        if (cursorRect.isEmpty()) {
            return;
        }

        RRect scrolledCursorRect = new RRect(cursorRect.getX(), cursorRect.getY(), cursorRect.getWidth(), cursorRect.getHeight());
        RRect intersection = scrolledCursorRect.intersection(contentDims);
        boolean cursorVisible = caret.isVisible() && !intersection.isEmpty();

        if (cursorVisible) {
            RCursorRenderingMode renderingMode = caret.getRenderingMode();
            pg.stroke(RThemeStore.getRGBA(RColorType.CURSOR)); // pg.setColor(colorsProfile.getCursorColor());

            drawCursorRect(pg, intersection.getX(), intersection.getY(), intersection.getWidth(), intersection.getHeight(), renderingMode, caret);
        }

        // Paint mirror cursor
        if (viewMode == RBinViewMode.DUAL && editor.isMirrorCursorShowing()) {
            editor.updateMirrorCursorRect(caret.getDataPosition(), caret.getSection());
            RRect mirrorCursorRect = cursorDataCache.mirrorCursorRect;
            if (!mirrorCursorRect.isEmpty()) {
                intersection = contentDims.intersection(mirrorCursorRect);
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

    protected void drawRowPosition(PGraphics pg) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = editor.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();
        long totalRows = dimensions.getTotalRows();

        RRect rowPositionDims = dimensions.getRowPositionDims();
        RRect contentDims = dimensions.getContentDims();

        RBinEditor.RowDataCache rowDataCache = editor.getRowDataCache();
        int rowPositionLength = editor.getRowPositionCharacters();

        RBackgroundPaintMode backgroundPaintMode = editor.getBackgroundPaintMode();

        pg.textFont(font);
        pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND)); //  g.setColor(colorsProfile.getTextBackground());
        pg.rect(rowPositionDims.getX(), 0, rowPositionDims.getWidth(), rowPositionDims.getHeight());

        if (backgroundPaintMode == RBackgroundPaintMode.STRIPED) {
            long dataPosition = bytesPerRow;
            float stripePositionY = rowHeight;
            pg.fill(RThemeStore.getRGBA(RColorType.FOCUS_BACKGROUND)); //  g.setColor(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= totalRows / 2; row++) {
                if (dataPosition > dataSize) {
                    break;
                }

                pg.rect(rowPositionDims.getX(), stripePositionY, rowPositionDims.getWidth(), rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }

        long dataPosition = 0;
        float positionY = (float) (rowHeight - subFontSpace)/2;// - scrollPosition.getRowOffset();
        pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); //  g.setColor(colorsProfile.getTextColor());
        for (int row = 0; row <= totalRows; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            RBinUtils.longToBaseCode(rowDataCache.rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, RCodeType.HEXADECIMAL.getBase(), rowPositionLength, true, editor.getCodeCharactersCase());
            drawCenteredChars(pg, rowDataCache.rowPositionCode, 0, rowPositionLength, characterWidth, rowPositionDims.getX(), positionY);

            positionY += rowHeight;
            dataPosition += bytesPerRow;
            if (dataPosition < 0) {
                break;
            }
        }

        // Decoration lines
        pg.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); // g.setColor(colorsProfile.getDecorationLine());
        float lineX = rowPositionDims.getX() + rowPositionDims.getWidth() - (characterWidth / 2);
        if (lineX >= rowPositionDims.getX()) {
            pg.line(lineX, 0, lineX, contentDims.getHeight());
        }
        pg.line(lineX + contentDims.getWidth(), 0, lineX + contentDims.getWidth(), contentDims.getHeight());

    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        drawRowPosition(pg);

        drawRows(pg);

        drawCursor(pg);
    }

//    public void draw(PGraphics pg) {
//        // the component knows its absolute position but here the current matrix is already translated to it
//        int yDiff = editor.getVerticalScroll();
//        PGraphics draw = buffer.draw();
//        pg.image(draw.get(0, yDiff, (int) size.x, (int) size.y), 0, 0);
//    }

    @Override
    protected void redrawBuffers() {
        super.redrawBuffers();
    }

    @Override
    public float suggestWidth() {
        return dimensions.getComponentDims().getWidth();
    }

    public PVector getBufferSize(){
        return new PVector(dimensions.getComponentDims().getWidth(), dimensions.getComponentDims().getHeight());
    }
}
