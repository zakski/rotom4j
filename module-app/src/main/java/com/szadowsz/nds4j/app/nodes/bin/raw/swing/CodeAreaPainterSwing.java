package com.szadowsz.nds4j.app.nodes.bin.raw.swing;

import com.szadowsz.nds4j.app.nodes.bin.raw.*;
import com.szadowsz.nds4j.app.nodes.bin.raw.swing.capability.AntialiasingCapable;
import com.szadowsz.nds4j.file.bin.core.BinaryData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Arrays;

public class CodeAreaPainterSwing extends CodeAreaPainter {

    private final JComponent dataView;
    private final CodeAreaScrollPaneSwing scrollPanel;
    private final CodeAreaMouseListener codeAreaMouseListener;
    private final ComponentListener codeAreaComponentListener;

    private final CodeAreaSwing codeAreaSwing;

    private final CodeAreaMetricsSwing metricsSwing;
    private final CodeAreaDimensionsSwing dimensionsSwing;

    private Font font;

    private CursorDataCache cursorDataCache = null;

    private AntialiasingMode antialiasingMode = AntialiasingMode.AUTO;

    public CodeAreaPainterSwing(CodeAreaSwing codeArea) {
        super(codeArea, new CodeAreaMetricsSwing(), new CodeAreaDimensionsSwing());

        codeAreaSwing = codeArea;
        metricsSwing = (CodeAreaMetricsSwing) metrics;
        dimensionsSwing = (CodeAreaDimensionsSwing) dimensions;

        dataView = new JComponent() {
        };
        dataView.setBorder(null);
        dataView.setVisible(false);
        dataView.setLayout(null);
        dataView.setOpaque(false);
        dataView.setInheritsPopupMenu(true);
        // Fill whole area, no more suitable method found so far
        dataView.setPreferredSize(new Dimension(0, 0));

        scrollPanel = new CodeAreaScrollPaneSwing(codeArea, metrics, structure, dimensions, scrolling);
        scrollPanel.setViewportView(dataView);
        JViewport viewport = scrollPanel.getViewport();
        viewport.setOpaque(false);
        scrolling.setHorizontalExtentChangeListener(this::horizontalExtentChanged);
        scrolling.setVerticalExtentChangeListener(this::verticalExtentChanged);

        codeAreaMouseListener = new CodeAreaMouseListener(codeArea, scrollPanel);
        viewport.addMouseListener(codeAreaMouseListener);
        viewport.addMouseMotionListener(codeAreaMouseListener);
        viewport.addMouseWheelListener(codeAreaMouseListener);
        viewport.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int verticalScrollBarSize = getVerticalScrollBarSize();
                int horizontalScrollBarSize = getHorizontalScrollBarSize();

                if (dimensions.getVerticalScrollBarSize() != verticalScrollBarSize || dimensions.getHorizontalScrollBarSize() != horizontalScrollBarSize) {
                    recomputeDimensions();
                    recomputeScrollState();
                }

                JViewport viewport = scrollPanel.getViewport();
                if (viewDimension != null && (viewDimension.getDataViewWidth() != viewport.getWidth() || viewDimension.getDataViewHeight() != viewport.getHeight())) {
                    updateScrollBars();
                }
            }
        });
        codeAreaComponentListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recomputeLayout();
            }
        };
    }

    @Override
    protected void recomputeLayout() {
        super.recomputeLayout();
        antialiasingMode = ((AntialiasingCapable) codeArea).getAntialiasingMode();
    }
    /**
     * Returns cursor rectangle.
     *
     * @param dataPosition data position
     * @param codeOffset code offset
     * @param section section
     * @return cursor rectangle or empty rectangle
     */
    private Rectangle getCursorPositionRect(long dataPosition, int codeOffset, CodeAreaSection section) {
        Rectangle rect = new Rectangle();
        updateRectToCursorPosition(rect, dataPosition, codeOffset, section);
        return rect;
    }

    /**
     * Returns relative cursor position in code area or null if cursor is not
     * visible.
     *
     * @param dataPosition data position
     * @param codeOffset code offset
     * @param section section
     * @return cursor position or null
     */
    private Point getPositionPoint(long dataPosition, int codeOffset, CodeAreaSection section) {
        int bytesPerRow = structure.getBytesPerRow();
        int rowsPerRect = dimensions.getRowsPerRect();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();

        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        long row = dataPosition / bytesPerRow - scrollPosition.getRowPosition();
        if (row < -1 || row > rowsPerRect) {
            return null;
        }

        int byteOffset = (int) (dataPosition % bytesPerRow);

        Rectangle dataViewRect = dimensionsSwing.getDataViewRectangle();
        int caretY = (int) (dataViewRect.y + row * rowHeight) - scrollPosition.getRowOffset();
        int caretX;
        if (section == CodeAreaSection.TEXT_PREVIEW) {
            caretX = dataViewRect.x + visibility.getPreviewRelativeX() + characterWidth * byteOffset;
        } else {
            caretX = dataViewRect.x + characterWidth * (structure.computeFirstCodeCharacterPos(byteOffset) + codeOffset);
        }
        caretX -= scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();

        return new Point(caretX, caretY);
    }

    private void horizontalExtentChanged() {
        scrollPanel.horizontalExtentChanged();
    }

    private void verticalExtentChanged() {
        scrollPanel.verticalExtentChanged();
    }

    /**
     * Draws characters centering it to cells of the same width.
     *
     * @param g graphics
     * @param drawnChars array of chars
     * @param charOffset index of target character in array
     * @param length number of characters to draw
     * @param cellWidth width of cell to center into
     * @param positionX X position of drawing area start
     * @param positionY Y position of drawing area start
     */
    private void drawCenteredChars(Graphics g, char[] drawnChars, int charOffset, int length, int cellWidth, int positionX, int positionY) {
        int pos = 0;
        int group = 0;
        while (pos < length) {
            char drawnChar = drawnChars[charOffset + pos];
            int charWidth = metricsSwing.getCharWidth(drawnChar);

            boolean groupable;
            if (metricsSwing.hasUniformLineMetrics()) {
                groupable = charWidth == cellWidth;
            } else {
                int charsWidth = metricsSwing.getCharsWidth(drawnChars, charOffset + pos - group, group + 1);
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
                    drawShiftedChars(g, drawnChars, charOffset + pos - group, group, positionX + (pos - group) * cellWidth, positionY);
                    group = 0;
                }
                drawShiftedChars(g, drawnChars, charOffset + pos, 1, positionX + pos * cellWidth + ((cellWidth - charWidth) / 2), positionY);
            }
            pos++;
        }
        if (group > 0) {
            drawShiftedChars(g, drawnChars, charOffset + pos - group, group, positionX + (pos - group) * cellWidth, positionY);
        }
    }

    private void drawShiftedChars(Graphics g, char[] drawnChars, int charOffset, int length, int positionX, int positionY) {
        g.drawChars(drawnChars, charOffset, length, positionX, positionY);
    }

    /**
     * Paints main area background.
     *
     * @param g graphics
     */
    private void paintBackground(Graphics g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = codeArea.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int rowsPerRect = dimensions.getRowsPerRect();
        Rectangle dataViewRect = dimensionsSwing.getDataViewRectangle();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();

        g.setColor(colorsProfile.getTextBackground());
        if (backgroundPaintMode != BackgroundPaintMode.TRANSPARENT) {
            g.fillRect(dataViewRect.x, dataViewRect.y, dataViewRect.width, dataViewRect.height);
        }

        if (backgroundPaintMode == BackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : bytesPerRow);
            int stripePositionY = dataViewRect.y - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setColor(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition > dataSize) {
                    break;
                }

                g.fillRect(dataViewRect.x, stripePositionY, dataViewRect.width, rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }
    }

    private void paintCursorRect(Graphics g, int cursorX, int cursorY, int width, int height, CodeAreaCaret.CursorRenderingMode renderingMode, CodeAreaCaret caret) {
        switch (renderingMode) {
            case PAINT: {
                g.fillRect(cursorX, cursorY, width, height);
                break;
            }
            case XOR: {
                g.setXORMode(colorsProfile.getTextBackground());
                g.fillRect(cursorX, cursorY, width, height);
                g.setPaintMode();
                break;
            }
            case NEGATIVE: {
                int characterWidth = metrics.getCharacterWidth();
                int rowHeight = metrics.getRowHeight();
                int maxBytesPerChar = metrics.getMaxBytesPerChar();
                int subFontSpace = metrics.getSubFontSpace();
                int dataViewX = dimensions.getScrollPanelX();
                int dataViewY = dimensions.getScrollPanelY();
                int previewRelativeX = visibility.getPreviewRelativeX();

                CodeAreaViewMode viewMode = structure.getViewMode();
                CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
                long dataSize = codeArea.getDataSize();
                CodeType codeType = structure.getCodeType();
                g.fillRect(cursorX, cursorY, width, height);
                g.setColor(colorsProfile.getCursorNegativeColor());
                BinaryData contentData = codeArea.getContentData();
                int row = (cursorY + scrollPosition.getRowOffset() - dataViewY) / rowHeight;
                int scrolledX = cursorX + scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();
                int posY = dataViewY + (row + 1) * rowHeight - subFontSpace - scrollPosition.getRowOffset();
                long dataPosition = caret.getDataPosition();
                if (viewMode != CodeAreaViewMode.CODE_MATRIX && caret.getSection() == CodeAreaSection.TEXT_PREVIEW) {
                    int charPos = (scrolledX - previewRelativeX) / characterWidth;
                    if (dataPosition >= dataSize) {
                        break;
                    }

                    if (maxBytesPerChar > 1) {
                        int charDataLength = maxBytesPerChar;
                        if (dataPosition + maxBytesPerChar > dataSize) {
                            charDataLength = (int) (dataSize - dataPosition);
                        }

                        if (contentData == null) {
                            cursorDataCache.cursorChars[0] = ' ';
                        } else {
                            contentData.copyToArray(dataPosition, cursorDataCache.cursorData, 0, charDataLength);
                            String displayString = new String(cursorDataCache.cursorData, 0, charDataLength, charset);
                            if (!displayString.isEmpty()) {
                                cursorDataCache.cursorChars[0] = displayString.charAt(0);
                            }
                        }
                    } else {
                        if (charMappingCharset == null || charMappingCharset != charset) {
                            buildCharMapping(charset);
                        }

                        if (contentData == null) {
                            cursorDataCache.cursorChars[0] = ' ';
                        } else {
                            cursorDataCache.cursorChars[0] = charMapping[contentData.getByte(dataPosition) & 0xFF];
                        }
                    }
                    int posX = previewRelativeX + charPos * characterWidth - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
                    drawCenteredChars(g, cursorDataCache.cursorChars, 0, 1, characterWidth, posX, posY);
                } else {
                    int charPos = (scrolledX - dataViewX) / characterWidth;
                    int byteOffset = structure.computePositionByte(charPos);
                    int codeCharPos = structure.computeFirstCodeCharacterPos(byteOffset);

                    if (contentData != null && dataPosition < dataSize) {
                        byte dataByte = contentData.getByte(dataPosition);
                        CodeAreaUtils.byteToCharsCode(dataByte, codeType, cursorDataCache.cursorChars, 0, codeCharactersCase);
                    } else {
                        Arrays.fill(cursorDataCache.cursorChars, ' ');
                    }
                    int posX = dataViewX + codeCharPos * characterWidth - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
                    int charsOffset = charPos - codeCharPos;
                    drawCenteredChars(g, cursorDataCache.cursorChars, charsOffset, 1, characterWidth, posX + (charsOffset * characterWidth), posY);
                }
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(renderingMode);
        }
    }

    private void paintHeader(Graphics g) {
        int charactersPerCodeSection = visibility.getCharactersPerCodeSection();
        Rectangle headerArea = dimensionsSwing.getHeaderAreaRectangle();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();

        Rectangle clipBounds = g.getClipBounds();
        g.setClip(clipBounds != null ? clipBounds.intersection(headerArea) : headerArea);

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        int dataViewX = dimensions.getScrollPanelX();

        g.setFont(font);
        g.setColor(colorsProfile.getTextBackground());
        g.fillRect(headerArea.x, headerArea.y, headerArea.width, headerArea.height);

        CodeAreaViewMode viewMode = structure.getViewMode();
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            int headerX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
            int headerY = headerArea.y + rowHeight - metrics.getSubFontSpace();

            g.setColor(colorsProfile.getTextColor());
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
                    CodeAreaUtils.longToBaseCode(rowDataCache.headerChars, codePos, index, CodeType.HEXADECIMAL.getBase(), 2, true, codeCharactersCase);
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

                Color color = colorsProfile.getTextColor();

                if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) { // || !colorType.equals(renderColorType)
                    sequenceBreak = true;
                }
                if (sequenceBreak) {
                    if (renderOffset < characterOnRow) {
                        drawCenteredChars(g, rowDataCache.headerChars, renderOffset, characterOnRow - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
                    }

                    if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                        renderColor = color;
                        g.setColor(color);
                    }

                    renderOffset = characterOnRow;
                }
            }

            if (renderOffset < charactersPerCodeSection) {
                drawCenteredChars(g, rowDataCache.headerChars, renderOffset, charactersPerCodeSection - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
            }
        }

        // Decoration lines
        g.setColor(colorsProfile.getDecorationLine());
        g.drawLine(headerArea.x, headerArea.y + headerArea.height - 1, headerArea.x + headerArea.width, headerArea.y + headerArea.height - 1);
        int lineX = dataViewX + visibility.getPreviewRelativeX() - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2 - 1;
        if (lineX >= dataViewX) {
            g.drawLine(lineX, headerArea.y, lineX, headerArea.y + headerArea.height);
        }

        g.setClip(clipBounds);
    }

    //    // Debugging counter
//    private long paintDebugCounter = 0;
//
//    private void paintDebugInfo(Graphics g, Rectangle mainAreaRect, CodeAreaScrollPosition scrollPosition) {
//        int rowHeight = metrics.getRowHeight();
//        int x = mainAreaRect.x + mainAreaRect.width - 220;
//        int y = mainAreaRect.y + mainAreaRect.height - 20;
//        g.setColor(Color.YELLOW);
//        g.fillRect(x, y, 200, 16);
//        g.setColor(Color.BLACK);
//        char[] headerCode = (String.valueOf(scrollPosition.getCharPosition()) + "+" + String.valueOf(scrollPosition.getCharOffset()) + " : " + String.valueOf(scrollPosition.getRowPosition()) + "+" + String.valueOf(scrollPosition.getRowOffset()) + " P: " + String.valueOf(paintDebugCounter)).toCharArray();
//        g.drawChars(headerCode, 0, headerCode.length, x, y + rowHeight);
//
//        paintDebugCounter++;
//    }
//

    private void paintOutsideArea(Graphics g) {
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        Rectangle componentRect = dimensionsSwing.getComponentRectangle();
        int characterWidth = metrics.getCharacterWidth();
        g.setColor(colorsProfile.getTextBackground());
        g.fillRect(componentRect.x, componentRect.y, componentRect.width, headerAreaHeight);

        // Decoration lines
        g.setColor(colorsProfile.getDecorationLine());
        g.drawLine(componentRect.x, componentRect.y + headerAreaHeight - 1, componentRect.x + rowPositionAreaWidth, componentRect.y + headerAreaHeight - 1);

        int lineX = componentRect.x + rowPositionAreaWidth - (characterWidth / 2);
        if (lineX >= componentRect.x) {
            g.drawLine(lineX, componentRect.y, lineX, componentRect.y + headerAreaHeight);
        }
    }

    private void paintRows(Graphics g) {
        int bytesPerRow = structure.getBytesPerRow();
        int rowHeight = metrics.getRowHeight();
        int dataViewX = dimensions.getScrollPanelX();
        int dataViewY = dimensions.getScrollPanelY();
        int rowsPerRect = dimensions.getRowsPerRect();
        long dataSize = codeArea.getDataSize();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
        int characterWidth = metrics.getCharacterWidth();
        int rowPositionX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
        int rowPositionY = dataViewY - scrollPosition.getRowOffset();

        g.setColor(colorsProfile.getTextColor());
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            prepareRowData(dataPosition);
            paintRowBackground(g, dataPosition, rowPositionX, rowPositionY);
            paintRowText(g, dataPosition, rowPositionX, rowPositionY);

            rowPositionY += rowHeight;
            if (Long.MAX_VALUE - dataPosition < bytesPerRow) {
                dataPosition = Long.MAX_VALUE;
            } else {
                dataPosition += bytesPerRow;
            }
        }
    }


    private void paintRowPosition(Graphics g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = codeArea.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();
        int rowsPerRect = dimensions.getRowsPerRect();
        Rectangle rowPosRectangle = dimensionsSwing.getRowPositionAreaRectangle();
        Rectangle dataViewRectangle = dimensionsSwing.getDataViewRectangle();
        Rectangle clipBounds = g.getClipBounds();
        g.setClip(clipBounds != null ? clipBounds.intersection(rowPosRectangle) : rowPosRectangle);

        g.setFont(font);
        g.setColor(colorsProfile.getTextBackground());
        g.fillRect(rowPosRectangle.x, rowPosRectangle.y, rowPosRectangle.width, rowPosRectangle.height);

        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        if (backgroundPaintMode == BackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : bytesPerRow);
            int stripePositionY = rowPosRectangle.y - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.setColor(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition > dataSize) {
                    break;
                }

                g.fillRect(rowPosRectangle.x, stripePositionY, rowPosRectangle.width, rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }

        long dataPosition = bytesPerRow * scrollPosition.getRowPosition();
        int positionY = rowPosRectangle.y + rowHeight - subFontSpace - scrollPosition.getRowOffset();
        g.setColor(colorsProfile.getTextColor());
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            CodeAreaUtils.longToBaseCode(rowDataCache.rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, CodeType.HEXADECIMAL.getBase(), rowPositionLength, true, codeCharactersCase);
            drawCenteredChars(g, rowDataCache.rowPositionCode, 0, rowPositionLength, characterWidth, rowPosRectangle.x, positionY);

            positionY += rowHeight;
            dataPosition += bytesPerRow;
            if (dataPosition < 0) {
                break;
            }
        }

        // Decoration lines
        g.setColor(colorsProfile.getDecorationLine());
        int lineX = rowPosRectangle.x + rowPosRectangle.width - (characterWidth / 2);
        if (lineX >= rowPosRectangle.x) {
            g.drawLine(lineX, dataViewRectangle.y, lineX, dataViewRectangle.y + dataViewRectangle.height);
        }
        g.drawLine(dataViewRectangle.x, dataViewRectangle.y - 1, dataViewRectangle.x + dataViewRectangle.width, dataViewRectangle.y - 1);

        g.setClip(clipBounds);
    }

    /**
     * Paints row background.
     *
     * @param g graphics
     * @param rowDataPosition row data position
     * @param rowPositionX row position X
     * @param rowPositionY row position Y
     */
    private void paintRowBackground(Graphics g, long rowDataPosition, int rowPositionX, int rowPositionY) {
        int previewCharPos = visibility.getPreviewCharPos();
        CodeAreaViewMode viewMode = structure.getViewMode();
        int charactersPerRow = structure.getCharactersPerRow();
        int skipToChar = visibility.getSkipToChar();
        int skipRestFromChar = visibility.getSkipRestFromChar();

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

            Color color = getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section);
            if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                sequenceBreak = true;
            }
            if (sequenceBreak) {
                if (renderOffset < charOnRow) {
                    if (renderColor != null) {
                        renderBackgroundSequence(g, renderOffset, charOnRow, rowPositionX, rowPositionY);
                    }
                }

                if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                    renderColor = color;
                    if (color != null) {
                        g.setColor(color);
                    }
                }

                renderOffset = charOnRow;
            }
        }

        if (renderOffset < charactersPerRow) {
            if (renderColor != null) {
                renderBackgroundSequence(g, renderOffset, charactersPerRow, rowPositionX, rowPositionY);
            }
        }
    }

    /**
     * Paints row text.
     *
     * @param g graphics
     * @param rowDataPosition row data position
     * @param rowPositionX row position X
     * @param rowPositionY row position Y
     */
    private void paintRowText(Graphics g, long rowDataPosition, int rowPositionX, int rowPositionY) {
        int previewCharPos = visibility.getPreviewCharPos();
        int charactersPerRow = structure.getCharactersPerRow();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();

        g.setFont(font);
        int positionY = rowPositionY + rowHeight - subFontSpace;

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

            Color color = getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section);
            if (color == null) {
                color = colorsProfile.getTextColor();
            }

            boolean sequenceBreak = false;
            if (!CodeAreaSwingUtils.areSameColors(color, renderColor)) {
                if (renderColor == null) {
                    renderColor = color;
                }

                sequenceBreak = true;
            }

            if (sequenceBreak) {
                if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                    g.setColor(renderColor);
                    lastColor = renderColor;
                }

                if (charOnRow > renderOffset) {
                    drawCenteredChars(g, rowDataCache.rowCharacters, renderOffset, charOnRow - renderOffset, characterWidth, rowPositionX + renderOffset * characterWidth, positionY);
                }

                renderColor = color;
                if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                    g.setColor(renderColor);
                    lastColor = renderColor;
                }

                renderOffset = charOnRow;
            }
        }

        if (renderOffset < charactersPerRow) {
            if (!CodeAreaSwingUtils.areSameColors(lastColor, renderColor)) {
                g.setColor(renderColor);
            }

            drawCenteredChars(g, rowDataCache.rowCharacters, renderOffset, charactersPerRow - renderOffset, characterWidth, rowPositionX + renderOffset * characterWidth, positionY);
        }
    }

    /**
     * Renders sequence of background rectangles.
     * <p>
     * Doesn't include character at offset end.
     */
    private void renderBackgroundSequence(Graphics g, int startOffset, int endOffset, int rowPositionX, int positionY) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        g.fillRect(rowPositionX + startOffset * characterWidth, positionY, (endOffset - startOffset) * characterWidth, rowHeight);
    }

    private void updateMirrorCursorRect(long dataPosition, CodeAreaSection section) {
        CodeType codeType = structure.getCodeType();
        Point mirrorCursorPoint = getPositionPoint(dataPosition, 0, section == CodeAreaSection.CODE_MATRIX ? CodeAreaSection.TEXT_PREVIEW : CodeAreaSection.CODE_MATRIX);
        if (mirrorCursorPoint == null) {
            cursorDataCache.mirrorCursorRect.setSize(0, 0);
        } else {
            cursorDataCache.mirrorCursorRect.setBounds(mirrorCursorPoint.x, mirrorCursorPoint.y, metrics.getCharacterWidth() * (section == CodeAreaSection.TEXT_PREVIEW ? codeType.getMaxDigitsForByte() : 1), metrics.getRowHeight());
        }
    }

    private void updateRectToCursorPosition(Rectangle rect, long dataPosition, int codeOffset, CodeAreaSection section) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        Point cursorPoint = getPositionPoint(dataPosition, codeOffset, section);
        if (cursorPoint == null) {
            rect.setBounds(0, 0, 0, 0);
        } else {
            CodeAreaCaret.CursorShape cursorShape = editOperation == EditOperation.INSERT ? CodeAreaCaret.CursorShape.INSERT : CodeAreaCaret.CursorShape.OVERWRITE;
            int cursorThickness = CodeAreaCaret.getCursorThickness(cursorShape, characterWidth, rowHeight);
            rect.setBounds(cursorPoint.x, cursorPoint.y, cursorThickness, rowHeight);
        }
    }

    @Override
    protected void recomputeDimensions() {
        int verticalScrollBarSize = getVerticalScrollBarSize();
        int horizontalScrollBarSize = getHorizontalScrollBarSize();
        Insets insets = codeAreaSwing.getInsets();
        int componentWidth = codeAreaSwing.getWidth() - insets.left - insets.right;
        int componentHeight = codeAreaSwing.getHeight() - insets.top - insets.bottom;
        dimensions.recomputeSizes(metrics, insets.right, insets.top, componentWidth, componentHeight, rowPositionLength, verticalScrollBarSize, horizontalScrollBarSize);
    }

    @Override
    protected int getHorizontalScrollBarSize() {
        JScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
        return horizontalScrollBar.isVisible() ? horizontalScrollBar.getHeight() : 0;
    }

    @Override
    protected int getVerticalScrollBarSize() {
        JScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
        return verticalScrollBar.isVisible() ? verticalScrollBar.getWidth() : 0;
    }

    @Override
    public void attach() {
        codeAreaSwing.add(scrollPanel);
        codeAreaSwing.addMouseListener(codeAreaMouseListener);
        codeAreaSwing.addMouseMotionListener(codeAreaMouseListener);
        codeAreaSwing.addMouseWheelListener(codeAreaMouseListener);
        codeAreaSwing.addComponentListener(codeAreaComponentListener);
        codeAreaSwing.addDataChangedListener(codeAreaDataChangeListener);
    }

    @Override
    public void detach() {
        codeAreaSwing.remove(scrollPanel);
        codeAreaSwing.removeMouseListener(codeAreaMouseListener);
        codeAreaSwing.removeMouseMotionListener(codeAreaMouseListener);
        codeAreaSwing.removeMouseWheelListener(codeAreaMouseListener);
        codeAreaSwing.removeComponentListener(codeAreaComponentListener);
        codeAreaSwing.removeDataChangedListener(codeAreaDataChangeListener);
    }

    public void paintComponent(Graphics g) {
        if (!initialized) {
            reset();
        }
        updateCache();
        if (font == null) {
            fontChanged(g);
        }
        if (rowDataCache == null) {
            return;
        }

        if (antialiasingMode != AntialiasingMode.OFF && g instanceof Graphics2D) {
            Object antialiasingHint = antialiasingMode.getAntialiasingHint((Graphics2D) g);
            ((Graphics2D) g).setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    antialiasingHint);
        }
        if (layoutChanged) {
            recomputeLayout();
            recomputeCharPositions();
        }

        paintOutsideArea(g);
        paintHeader(g);
        paintRowPosition(g);
        paintMainArea(g);
    }

    public void paintCursor(Graphics g) {
        if (!codeAreaSwing.hasFocus()) {
            return;
        }

        if (caretChanged) {
            updateCaret();
        }

        int maxBytesPerChar = metrics.getMaxBytesPerChar();
        Rectangle mainAreaRect = dimensionsSwing.getMainAreaRectangle();
        CodeType codeType = structure.getCodeType();
        CodeAreaViewMode viewMode = structure.getViewMode();
        if (cursorDataCache == null) {
            cursorDataCache = new CursorDataCache();
        }
        int cursorCharsLength = codeType.getMaxDigitsForByte();
        if (cursorDataCache.cursorCharsLength != cursorCharsLength) {
            cursorDataCache.cursorCharsLength = cursorCharsLength;
            cursorDataCache.cursorChars = new char[cursorCharsLength];
        }
        int cursorDataLength = maxBytesPerChar;
        if (cursorDataCache.cursorDataLength != cursorDataLength) {
            cursorDataCache.cursorDataLength = cursorDataLength;
            cursorDataCache.cursorData = new byte[cursorDataLength];
        }

        CodeAreaCaret caret = codeArea.getCodeAreaCaret();
        Rectangle cursorRect = getCursorPositionRect(caret.getDataPosition(), caret.getCodeOffset(), caret.getSection());
        if (cursorRect.isEmpty()) {
            return;
        }

        Rectangle scrolledCursorRect = new Rectangle(cursorRect.x, cursorRect.y, cursorRect.width, cursorRect.height);
        Rectangle clipBounds = g.getClipBounds();
        Rectangle intersection = scrolledCursorRect.intersection(mainAreaRect);
        boolean cursorVisible = caret.isCursorVisible() && !intersection.isEmpty();

        if (cursorVisible) {
            g.setClip(intersection);
            CodeAreaCaret.CursorRenderingMode renderingMode = caret.getRenderingMode();
            g.setColor(colorsProfile.getCursorColor());

            paintCursorRect(g, intersection.x, intersection.y, intersection.width, intersection.height, renderingMode, caret);
        }

        // Paint mirror cursor
        if (viewMode == CodeAreaViewMode.DUAL && showMirrorCursor) {
            updateMirrorCursorRect(caret.getDataPosition(), caret.getSection());
            Rectangle mirrorCursorRect = cursorDataCache.mirrorCursorRect;
            if (!mirrorCursorRect.isEmpty()) {
                intersection = mainAreaRect.intersection(mirrorCursorRect);
                boolean mirrorCursorVisible = !intersection.isEmpty();
                if (mirrorCursorVisible) {
                    g.setClip(intersection);
                    g.setColor(colorsProfile.getCursorColor());
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setStroke(cursorDataCache.dashedStroke);
                    g2d.drawRect(mirrorCursorRect.x, mirrorCursorRect.y, mirrorCursorRect.width - 1, mirrorCursorRect.height - 1);
                    g2d.dispose();
                }
            }
        }
        g.setClip(clipBounds);
    }

    public void paintMainArea(Graphics g) {
        if (!initialized) {
            reset();
        }
        if (fontChanged) {
            fontChanged(g);
            fontChanged = false;
        }

        Rectangle mainAreaRect = dimensionsSwing.getMainAreaRectangle();
        Rectangle dataViewRectangle = dimensionsSwing.getDataViewRectangle();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        int characterWidth = metrics.getCharacterWidth();
        int previewRelativeX = visibility.getPreviewRelativeX();

        Rectangle clipBounds = g.getClipBounds();
        g.setClip(clipBounds != null ? clipBounds.intersection(mainAreaRect) : mainAreaRect);
        paintBackground(g);

        // Decoration lines
        g.setColor(colorsProfile.getDecorationLine());
        int lineX = dataViewRectangle.x + previewRelativeX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2 - 1;
        if (lineX >= dataViewRectangle.x) {
            g.drawLine(lineX, dataViewRectangle.y, lineX, dataViewRectangle.y + dataViewRectangle.height);
        }

        paintRows(g);
        g.setClip(clipBounds);
        paintCursor(g);

//        paintDebugInfo(g, mainAreaRect, scrollPosition);
    }

    @Override
    protected void updateScrollBars() {
        int verticalScrollBarPolicy = CodeAreaSwingUtils.getVerticalScrollBarPolicy(scrolling.getVerticalScrollBarVisibility());
        if (scrollPanel.getVerticalScrollBarPolicy() != verticalScrollBarPolicy) {
            scrollPanel.setVerticalScrollBarPolicy(verticalScrollBarPolicy);
        }
        int horizontalScrollBarPolicy = CodeAreaSwingUtils.getHorizontalScrollBarPolicy(scrolling.getHorizontalScrollBarVisibility());
        if (scrollPanel.getHorizontalScrollBarPolicy() != horizontalScrollBarPolicy) {
            scrollPanel.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
        }

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        long rowsPerDocument = structure.getRowsPerDocument();

        recomputeScrollState();

        boolean revalidate = false;
        Rectangle scrollPanelRectangle = dimensionsSwing.getScrollPanelRectangle();
        Rectangle oldRect = scrollPanel.getBounds();
        if (!oldRect.equals(scrollPanelRectangle)) {
            scrollPanel.setBounds(scrollPanelRectangle);
            revalidate = true;
        }

        JViewport viewport = scrollPanel.getViewport();

        if (rowHeight > 0 && characterWidth > 0) {
            viewDimension = scrolling.computeViewDimension(viewport.getWidth(), viewport.getHeight(), layout, structure, characterWidth, rowHeight);
            if (dataView.getWidth() != viewDimension.getWidth() || dataView.getHeight() != viewDimension.getHeight()) {
                Dimension dataViewSize = new Dimension(viewDimension.getWidth(), viewDimension.getHeight());
                dataView.setPreferredSize(dataViewSize);
                dataView.setSize(dataViewSize);

                recomputeDimensions();

                scrollPanelRectangle = dimensionsSwing.getScrollPanelRectangle();
                if (!oldRect.equals(scrollPanelRectangle)) {
                    scrollPanel.setBounds(scrollPanelRectangle);
                }

                revalidate = true;
            }

            int verticalScrollValue = scrolling.getVerticalScrollValue(rowHeight, rowsPerDocument);
            int horizontalScrollValue = scrolling.getHorizontalScrollValue(characterWidth);
            scrollPanel.updateScrollBars(verticalScrollValue, horizontalScrollValue);
        }

        if (revalidate) {
            horizontalExtentChanged();
            verticalExtentChanged();
            codeAreaSwing.revalidate();
        }
    }

    public void fontChanged(Graphics g) {
        if (font == null) {
            reset();
        }

        charset = codeArea.getCharset();
        font = codeAreaSwing.getCodeFont();
        metricsSwing.recomputeMetrics(g.getFontMetrics(font), charset);

        recomputeDimensions();
        recomputeCharPositions();
        initialized = true;
    }

    private static class CursorDataCache {

        Rectangle caretRect = new Rectangle();
        Rectangle mirrorCursorRect = new Rectangle();
        final Stroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
        int cursorCharsLength;
        char[] cursorChars;
        int cursorDataLength;
        byte[] cursorData;
    }
}
