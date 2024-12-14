package com.szadowsz.gui.component.bined.complex;


import com.szadowsz.binary.BinaryData;
import com.szadowsz.gui.component.bined.RBinSelection;
import com.szadowsz.gui.component.bined.bounds.RBinDimensions;
import com.szadowsz.gui.component.bined.sizing.RBinMetrics;
import com.szadowsz.gui.component.bined.bounds.RBinRect;
import com.szadowsz.gui.component.bined.complex.bounds.RBinLayout;
import com.szadowsz.gui.component.bined.sizing.RBinStructure;
import com.szadowsz.gui.component.bined.RBinVisibility;
import com.szadowsz.gui.component.bined.caret.CursorRenderingMode;
import com.szadowsz.gui.component.bined.caret.CursorShape;
import com.szadowsz.gui.component.bined.caret.RCaret;
import com.szadowsz.gui.component.bined.caret.RCaretPos;
import com.szadowsz.gui.component.bined.complex.scroll.RBinScrollPos;
import com.szadowsz.gui.component.bined.complex.scroll.RBinScrolling;
import com.szadowsz.gui.component.bined.settings.*;
import com.szadowsz.gui.component.bined.utils.RBinUtils;
import com.szadowsz.gui.component.utils.RComponentScrollbar;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.RenderingHints;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

public class RBinDraw {

    protected final RBinEditor editor;

    protected RBinColorAssessor colorAssessor = null;
    protected RBinCharAssessor charAssessor = null;

    protected RowDataCache rowDataCache = null;
    protected CursorDataCache cursorDataCache = null;

    protected final RBinDimensions dimensions = new RBinDimensions();
    protected final RBinScrolling scrolling = new RBinScrolling();
    protected final RBinMetrics metrics = new RBinMetrics();
    protected final RBinStructure structure = new RBinStructure();
    protected final RBinLayout layout = new RBinLayout();
    protected final RBinVisibility visibility = new RBinVisibility();

    protected AntialiasingMode antialiasingMode = AntialiasingMode.AUTO;
    protected BackgroundPaintMode backgroundPaintMode;

    protected EditOperation editOperation;

    protected CodeCharactersCase codeCharactersCase;
    protected Charset charset;
    protected PFont font;

    protected int rowPositionLength;
    protected int minRowPositionLength;
    protected int maxRowPositionLength;

    protected boolean showMirrorCursor;

    protected volatile boolean initialized = false;
    protected volatile boolean layoutChanged = true;
    protected volatile boolean fontChanged = false;
    protected volatile boolean resetColors = true;
    protected volatile boolean caretChanged = true;

    public RBinDraw(RBinEditor editor) {
        this.editor = editor;
    }

    /**
     * Returns cursor rectangle.
     *
     * @param dataPosition data position
     * @param codeOffset code offset
     * @param section section
     * @return cursor rectangle or empty rectangle
     */
    protected RBinRect getCursorPositionRect(long dataPosition, int codeOffset, CodeAreaSection section) {
        RBinRect rect = new RBinRect();
        updateRectToCursorPosition(rect, dataPosition, codeOffset, section);
        return rect;
    }

    protected float getHorizontalScrollBarSize() {
        RComponentScrollbar horizontalScrollBar = editor.getHorizontalScrollBar();
        return horizontalScrollBar.isVisible() ? horizontalScrollBar.getHeight() : 0;
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
    public PVector getPositionPoint(long dataPosition, int codeOffset, CodeAreaSection section) {
        int bytesPerRow = structure.getBytesPerRow();
        int rowsPerRect = dimensions.getRowsPerRect();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();

        RBinScrollPos scrollPosition = scrolling.getScrollPosition();
        long row = dataPosition / bytesPerRow - scrollPosition.getRowPosition();
        if (row < -1 || row > rowsPerRect) {
            return null;
        }

        int byteOffset = (int) (dataPosition % bytesPerRow);

        RBinRect dataViewRect = dimensions.getDataViewRectangle();
        float caretY =  (dataViewRect.getY() + row * rowHeight) - scrollPosition.getRowOffset();
        float caretX;
        if (section == CodeAreaSection.TEXT_PREVIEW) {
            caretX = dataViewRect.getX() + visibility.getPreviewRelativeX() + characterWidth * byteOffset;
        } else {
            caretX = dataViewRect.getX() + characterWidth * (structure.computeFirstCodeCharacterPos(byteOffset) + codeOffset);
        }
        caretX -= scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();

        return new PVector(caretX, caretY);
    }

    protected int getRowPositionLength() {
        if (minRowPositionLength > 0 && minRowPositionLength == maxRowPositionLength) {
            return minRowPositionLength;
        }

        long dataSize = editor.getDataSize();
        if (dataSize == 0) {
            return 1;
        }

        double natLog = Math.log(dataSize == Long.MAX_VALUE ? dataSize : dataSize + 1);
        int positionLength = (int) Math.ceil(natLog / PositionCodeType.HEXADECIMAL.getBaseLog());
        if (minRowPositionLength > 0 && positionLength < minRowPositionLength) {
            positionLength = minRowPositionLength;
        }
        if (maxRowPositionLength > 0 && positionLength > maxRowPositionLength) {
            positionLength = maxRowPositionLength;
        }

        return positionLength == 0 ? 1 : positionLength;
    }

    protected float getVerticalScrollBarSize() {
        RComponentScrollbar verticalScrollBar = editor.getVerticalScrollBar();
        return verticalScrollBar.isVisible() ? verticalScrollBar.getWidth() : 0;
    }

    protected void recomputeScrollState() {
        scrolling.setScrollPosition(editor.getScrollPosition());
        int characterWidth = metrics.getCharacterWidth();

        if (characterWidth > 0) {
            scrolling.updateCache(editor, getHorizontalScrollBarSize(), getVerticalScrollBarSize());

            recomputeCharPositions();
        }
    }

    protected void recomputeCharPositions() {
//        visibility.recomputeCharPositions(metrics, structure, dimensions, layout, scrolling);
        updateRowDataCache();
    }

    protected void recomputeDimensions() {
        float verticalScrollBarSize = getVerticalScrollBarSize();
        float horizontalScrollBarSize = getHorizontalScrollBarSize();
//        Insets insets = codeArea.getInsets();
        float componentWidth = editor.getWidth() ;//- insets.left - insets.right;
        float componentHeight = editor.getHeight() ;//- insets.top - insets.bottom;
        dimensions.recomputeSizes(metrics, 0.0f, 0.0f, componentWidth, componentHeight, rowPositionLength, verticalScrollBarSize, horizontalScrollBarSize);
    }

    protected void recomputeLayout() {
        rowPositionLength = getRowPositionLength();
        recomputeDimensions();

        int charactersPerPage = dimensions.getCharactersPerPage();
        //structure.updateCache(editor, charactersPerPage);
        codeCharactersCase = editor.getCodeCharactersCase();
        backgroundPaintMode = editor.getBackgroundPaintMode();
        showMirrorCursor = editor.isShowMirrorCursor();
        antialiasingMode = editor.getAntialiasingMode();
        minRowPositionLength = editor.getMinRowPositionLength();
        maxRowPositionLength = editor.getMaxRowPositionLength();

        int rowsPerPage = dimensions.getRowsPerPage();
        long rowsPerDocument = structure.getRowsPerDocument();
        int charactersPerRow = structure.getCharactersPerRow();

        if (metrics.isInitialized()) {
            scrolling.updateMaximumScrollPosition(rowsPerDocument, rowsPerPage, charactersPerRow, charactersPerPage, dimensions.getLastCharOffset(), dimensions.getLastRowOffset());
        }

        updateScrollBars();

        layoutChanged = false;
    }

    /**
     * Returns true if painter was initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns scroll position so that provided caret position is visible in
     * scrolled area.
     * <p>
     * Performs minimal scrolling and tries to preserve current vertical /
     * horizontal scrolling if possible. If given position cannot be fully
     * shown, top left corner is preferred.
     *
     * @param caretPosition caret position
     * @return scroll position or null if caret position is already visible /
     * scrolled to the best fit
     */
    public Optional<RBinScrollPos> computeRevealScrollPosition(RCaretPos caretPosition) {
        int bytesPerRow = structure.getBytesPerRow();
        int previewCharPos = visibility.getPreviewCharPos();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        float dataViewWidth = dimensions.getDataViewWidth();
        float dataViewHeight = dimensions.getDataViewHeight();
        int rowsPerPage = dimensions.getRowsPerPage();
        int charactersPerPage = dimensions.getCharactersPerPage();

        long shiftedPosition = caretPosition.getDataPosition();
        long rowPosition = shiftedPosition / bytesPerRow;
        int byteOffset = (int) (shiftedPosition % bytesPerRow);
        int charPosition;
        CodeAreaSection section = caretPosition.getSection().orElse(CodeAreaSection.CODE_MATRIX);
        if (section == CodeAreaSection.TEXT_PREVIEW) {
            charPosition = previewCharPos + byteOffset;
        } else {
            charPosition = structure.computeFirstCodeCharacterPos(byteOffset) + caretPosition.getCodeOffset();
        }

        return scrolling.computeRevealScrollPosition(rowPosition, charPosition, bytesPerRow, rowsPerPage, charactersPerPage, dataViewWidth % characterWidth, dataViewHeight % rowHeight, characterWidth, rowHeight);
    }

    /**
     * Notify scroll position was modified.
     * <p>
     * This is to assist detection of scrolling from outside compare to
     * scrolling by scrollbar controls.
     */
    public void scrollPositionModified() {
        scrolling.clearLastVerticalScrollingValue();
        recomputeScrollState();
    }

    protected synchronized void updateCache() {
        if (resetColors) {
            resetColors = false;
            //rebuildColors();
        }
    }
    protected void updateCaret() {
        editOperation = editor.getActiveOperation();
        caretChanged = false;
    }

    protected void updateMirrorCursorRect(long dataPosition, CodeAreaSection section) {
        CodeType codeType = structure.getCodeType();
        PVector mirrorCursorPoint = getPositionPoint(dataPosition, 0, section == CodeAreaSection.CODE_MATRIX ? CodeAreaSection.TEXT_PREVIEW : CodeAreaSection.CODE_MATRIX);
        if (mirrorCursorPoint == null) {
            cursorDataCache.mirrorCursorRect.setSize(0, 0);
        } else {
            cursorDataCache.mirrorCursorRect.setBounds(mirrorCursorPoint.x, mirrorCursorPoint.y, metrics.getCharacterWidth() * (section == CodeAreaSection.TEXT_PREVIEW ? codeType.getMaxDigitsForByte() : 1), metrics.getRowHeight());
        }
    }

    protected void updateRectToCursorPosition(RBinRect rect, long dataPosition, int codeOffset, CodeAreaSection section) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        PVector cursorPoint = getPositionPoint(dataPosition, codeOffset, section);
        if (cursorPoint == null) {
            rect.setBounds(0, 0, 0, 0);
        } else {
            CursorShape cursorShape = editOperation == EditOperation.INSERT ? CursorShape.INSERT : CursorShape.OVERWRITE;
            int cursorThickness = RCaret.getCursorThickness(cursorShape, characterWidth, rowHeight);
            rect.setBounds(cursorPoint.x, cursorPoint.y, cursorThickness, rowHeight);
        }
    }

    protected void updateRowDataCache() {
        if (rowDataCache == null) {
            rowDataCache = new RowDataCache();
        }

        rowDataCache.headerChars = new char[visibility.getCharactersPerCodeSection()];
        rowDataCache.rowData = new byte[structure.getBytesPerRow() + metrics.getMaxBytesPerChar() - 1];
        rowDataCache.rowPositionCode = new char[rowPositionLength];
        rowDataCache.rowCharacters = new char[structure.getCharactersPerRow()];
    }

    /**
     * Performs update of scrollbars after change in data size or position.
     */
    public void updateScrollBars() {
        // TODO
//        int verticalScrollBarPolicy = scrolling.getVerticalScrollBarVisibility().ordinal();
//        if (scrollPanel.getVerticalScrollBarPolicy() != verticalScrollBarPolicy) {
//            scrollPanel.setVerticalScrollBarPolicy(verticalScrollBarPolicy);
//        }
//        int horizontalScrollBarPolicy = scrolling.getHorizontalScrollBarVisibility().ordinal();
//        if (scrollPanel.getHorizontalScrollBarPolicy() != horizontalScrollBarPolicy) {
//            scrollPanel.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
//        }
//
//        int characterWidth = metrics.getCharacterWidth();
//        int rowHeight = metrics.getRowHeight();
//        long rowsPerDocument = structure.getRowsPerDocument();
//
//        recomputeScrollState();
//
//        boolean revalidate = false;
//        PVector scrollPanelRectangle = dimensions.getScrollPanelRectangle();
//        PVector oldRect = scrollPanel.getBounds();
//        if (!oldRect.equals(scrollPanelRectangle)) {
//            scrollPanel.setBounds(scrollPanelRectangle);
//            revalidate = true;
//        }
//
//        JViewport viewport = scrollPanel.getViewport();
//
//        if (rowHeight > 0 && characterWidth > 0) {
//            viewDimension = scrolling.computeViewDimension(viewport.getWidth(), viewport.getHeight(), layout, structure, characterWidth, rowHeight);
//            if (dataView.getWidth() != viewDimension.getWidth() || dataView.getHeight() != viewDimension.getHeight()) {
//                Dimension dataViewSize = new Dimension(viewDimension.getWidth(), viewDimension.getHeight());
//                dataView.setPreferredSize(dataViewSize);
//                dataView.setSize(dataViewSize);
//
//                recomputeDimensions();
//
//                scrollPanelRectangle = dimensions.getScrollPanelRectangle();
//                if (!oldRect.equals(scrollPanelRectangle)) {
//                    scrollPanel.setBounds(scrollPanelRectangle);
//                }
//
//                revalidate = true;
//            }
//
//            int verticalScrollValue = scrolling.getVerticalScrollValue(rowHeight, rowsPerDocument);
//            int horizontalScrollValue = scrolling.getHorizontalScrollValue(characterWidth);
//            scrollPanel.updateScrollBars(verticalScrollValue, horizontalScrollValue);
//        }
//
//        if (revalidate) {
//            horizontalExtentChanged();
//            verticalExtentChanged();
//            codeArea.revalidate();
//        }
    }

    protected void fontChanged(PGraphics g) {
        if (font == null) {
            reset();
        }

        charset = editor.getCharset();
        font = editor.getCodeFont();
        metrics.recomputeMetrics(((Graphics2D)g.getNative()).getFontMetrics((Font) font.getNative()), charset);

        recomputeDimensions();
        recomputeCharPositions();
        initialized = true;
    }

    /**
     * Renders sequence of background rectangles.
     * <p>
     * Doesn't include character at offset end.
     */
    protected void renderBackgroundSequence(PGraphics g, int startOffset, int endOffset, float rowPositionX, float positionY) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        g.rect(rowPositionX + startOffset * characterWidth, positionY, (endOffset - startOffset) * characterWidth, rowHeight);
    }

    protected void drawShiftedChars(PGraphics g, char[] drawnChars, int charOffset, int length, float positionX, float positionY) {
        g.text(drawnChars, charOffset, length, positionX, positionY);
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
    protected void drawCenteredChars(PGraphics g, char[] drawnChars, int charOffset, int length, int cellWidth, float positionX, float positionY) {
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

    protected void prepareRowData(long dataPosition) {
        int maxBytesPerChar = metrics.getMaxBytesPerChar();
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = editor.getDataSize();
        int previewCharPos = visibility.getPreviewCharPos();
        CodeType codeType = structure.getCodeType();
        CodeAreaViewMode viewMode = structure.getViewMode();

        int rowBytesLimit = bytesPerRow;
        int rowStart = 0;
        if (dataPosition < dataSize) {
            int rowDataSize = bytesPerRow + maxBytesPerChar - 1;
            if (dataSize - dataPosition < rowDataSize) {
                rowDataSize = (int) (dataSize - dataPosition);
            }
            if (dataPosition < 0) {
                rowStart = (int) -dataPosition;
            }
            BinaryData data = editor.getContentData();
            data.copyToArray(dataPosition + rowStart, rowDataCache.rowData, rowStart, rowDataSize - rowStart);
            if (dataSize - dataPosition < rowBytesLimit) {
                rowBytesLimit = (int) (dataSize - dataPosition);
            }
        } else {
            rowBytesLimit = 0;
        }

        // Fill codes
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            int skipToCode = visibility.getSkipToCode();
            int skipRestFromCode = visibility.getSkipRestFromCode();
            int endCode = Math.min(skipRestFromCode, rowBytesLimit);
            for (int byteOnRow = Math.max(skipToCode, rowStart); byteOnRow < endCode; byteOnRow++) {
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

        if (previewCharPos > 0) {
            rowDataCache.rowCharacters[previewCharPos - 1] = ' ';
        }

        // Fill preview characters
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
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
    }

    protected void paintCursorRect(PGraphics g, float cursorX, float cursorY, float width, float height, CursorRenderingMode renderingMode, RCaret caret) {
        switch (renderingMode) {
            case PAINT: {
                g.rect(cursorX, cursorY, width, height);
                break;
            }
            case XOR: {
                Graphics2D g2d = ((Graphics2D) g.getNative());
                g2d.setXORMode(RThemeStore.getColor(RColorType.NORMAL_BACKGROUND) /*  g.setColor(colorsProfile.getTextBackground()*/);
                g.rect(cursorX, cursorY, width, height);
                g2d.setPaintMode();
                break;
            }
            case NEGATIVE: {
                int characterWidth = metrics.getCharacterWidth();
                int rowHeight = metrics.getRowHeight();
                int maxBytesPerChar = metrics.getMaxBytesPerChar();
                int subFontSpace = metrics.getSubFontSpace();
                float dataViewX = dimensions.getScrollPanelX();
                float dataViewY = dimensions.getScrollPanelY();
                int previewRelativeX = visibility.getPreviewRelativeX();

                CodeAreaViewMode viewMode = structure.getViewMode();
                RBinScrollPos scrollPosition = scrolling.getScrollPosition();
                long dataSize = editor.getDataSize();
                CodeType codeType = structure.getCodeType();
                g.rect(cursorX, cursorY, width, height);
                g.fill(RThemeStore.getRGBA(RColorType.CURSOR_NEGATIVE)); // g.setColor(colorsProfile.getCursorNegativeColor());
                BinaryData contentData = editor.getContentData();
                int row = Math.round((cursorY + scrollPosition.getRowOffset() - dataViewY) / rowHeight);
                float scrolledX = cursorX + scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();
                float posY = dataViewY + (row + 1) * rowHeight - subFontSpace - scrollPosition.getRowOffset();
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
                    drawCenteredChars(g, cursorDataCache.cursorChars, 0, 1, characterWidth, posX, posY);
                } else {
                    int charPos = Math.round((scrolledX - dataViewX) / characterWidth);
                    int byteOffset = structure.computePositionByte(charPos);
                    int codeCharPos = structure.computeFirstCodeCharacterPos(byteOffset);

                    if (dataPosition < dataSize) {
                        byte dataByte = contentData.getByte(dataPosition);
                        RBinUtils.byteToCharsCode(dataByte, codeType, cursorDataCache.cursorChars, 0, codeCharactersCase);
                    } else {
                        Arrays.fill(cursorDataCache.cursorChars, ' ');
                    }
                    float posX = dataViewX + codeCharPos * characterWidth - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
                    int charsOffset = charPos - codeCharPos;
                    drawCenteredChars(g, cursorDataCache.cursorChars, charsOffset, 1, characterWidth, posX + (charsOffset * characterWidth), posY);
                }
                break;
            }
            default:
                throw RBinUtils.getInvalidTypeException(renderingMode);
        }
    }

   protected void paintCursor(PGraphics g) {
        if (!editor.hasFocus()) {
            return;
        }

        if (caretChanged) {
            updateCaret();
        }

        int maxBytesPerChar = metrics.getMaxBytesPerChar();
        RBinRect mainAreaRect = dimensions.getMainAreaRectangle();
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

        RCaret caret = editor.getCaret();
        RBinRect cursorRect = getCursorPositionRect(caret.getDataPosition(), caret.getCodeOffset(), caret.getSection());
        if (cursorRect.isEmpty()) {
            return;
        }

        RBinRect scrolledCursorRect = new RBinRect(cursorRect.getX(), cursorRect.getY(), cursorRect.getWidth(), cursorRect.getHeight());
       // RBinRect clipBounds = g.getClipBounds();
        RBinRect intersection = scrolledCursorRect.intersection(mainAreaRect);
        boolean cursorVisible = caret.isVisible() && !intersection.isEmpty();

        if (cursorVisible) {
         //   g.setClip(intersection);
            CursorRenderingMode renderingMode = caret.getRenderingMode();
            g.stroke(RThemeStore.getRGBA(RColorType.CURSOR)); // g.setColor(colorsProfile.getCursorColor());

            paintCursorRect(g, intersection.getX(), intersection.getY(), intersection.getWidth(), intersection.getHeight(), renderingMode, caret);
        }

        // Paint mirror cursor
        if (viewMode == CodeAreaViewMode.DUAL && showMirrorCursor) {
            updateMirrorCursorRect(caret.getDataPosition(), caret.getSection());
            RBinRect mirrorCursorRect = cursorDataCache.mirrorCursorRect;
            if (!mirrorCursorRect.isEmpty()) {
                intersection = mainAreaRect.intersection(mirrorCursorRect);
                boolean mirrorCursorVisible = !intersection.isEmpty();
                if (mirrorCursorVisible) {
                    //g.setClip(intersection);
                    g.stroke(RThemeStore.getRGBA(RColorType.CURSOR)); // g.setColor(colorsProfile.getCursorColor());
                    Graphics2D g2d = (Graphics2D) ((Graphics) g.getNative()).create();
                    g2d.setStroke(cursorDataCache.dashedStroke);
                    //g2d.drawRect(mirrorCursorRect.getX(), mirrorCursorRect.getY(), mirrorCursorRect.getWidth() - 1, mirrorCursorRect.getHeight() - 1);
                    g2d.dispose();
                }
            }
        }
        //g.setClip(clipBounds);
    }

    /**
     * Paints row background.
     *
     * @param g graphics
     * @param rowDataPosition row data position
     * @param rowPositionX row position X
     * @param rowPositionY row position Y
     */
    protected void paintRowBackground(PGraphics g, long rowDataPosition, float rowPositionX, float rowPositionY) {
        int previewCharPos = visibility.getPreviewCharPos();
        CodeAreaViewMode viewMode = structure.getViewMode();
        int charactersPerRow = structure.getCharactersPerRow();
        int skipToChar = visibility.getSkipToChar();
        int skipRestFromChar = visibility.getSkipRestFromChar();
        RBinSelection selectionHandler = editor.getSelectionHandler();

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
                        renderBackgroundSequence(g, renderOffset, charOnRow, rowPositionX, rowPositionY);
                    }
                }

                if (!RBinUtils.areSameColors(color, renderColor)) {
                    renderColor = color;
                    if (color != null) {
                        g.fill(color.getRGB());
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
    protected void paintRowText(PGraphics g, long rowDataPosition, float rowPositionX, float rowPositionY) {
        int previewCharPos = visibility.getPreviewCharPos();
        int charactersPerRow = structure.getCharactersPerRow();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();
        RBinSelection selectionHandler = editor.getSelectionHandler();

        g.textFont(font);
        float positionY = rowPositionY + rowHeight - subFontSpace;

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
                    g.stroke(renderColor.getRGB());
                    lastColor = renderColor;
                }

                if (charOnRow > renderOffset) {
                    drawCenteredChars(g, rowDataCache.rowCharacters, renderOffset, charOnRow - renderOffset, characterWidth, rowPositionX + renderOffset * characterWidth, positionY);
                }

                renderColor = color;
                if (!RBinUtils.areSameColors(lastColor, renderColor)) {
                    g.stroke(renderColor.getRGB());
                    lastColor = renderColor;
                }

                renderOffset = charOnRow;
            }
        }

        if (renderOffset < charactersPerRow) {
            if (!RBinUtils.areSameColors(lastColor, renderColor)) {
                g.stroke(renderColor.getRGB());
            }

            drawCenteredChars(g, rowDataCache.rowCharacters, renderOffset, charactersPerRow - renderOffset, characterWidth, rowPositionX + renderOffset * characterWidth, positionY);
        }
    }
    
    protected void paintRows(PGraphics g) {
        int bytesPerRow = structure.getBytesPerRow();
        int rowHeight = metrics.getRowHeight();
        float dataViewX = dimensions.getScrollPanelX();
        float dataViewY = dimensions.getScrollPanelY();
        int rowsPerRect = dimensions.getRowsPerRect();
        long dataSize = editor.getDataSize();
        RBinScrollPos scrollPosition = scrolling.getScrollPosition();
        long dataPosition = scrollPosition.getRowPosition() * bytesPerRow;
        int characterWidth = metrics.getCharacterWidth();
        float rowPositionX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
        float rowPositionY = dataViewY - scrollPosition.getRowOffset();

        g.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); //  g.setColor(colorsProfile.getTextColor());
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

    /**
     * Paints main area background.
     *
     * @param g graphics
     */
    public void paintBackground(PGraphics g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = editor.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int rowsPerRect = dimensions.getRowsPerRect();
        RBinRect dataViewRect = dimensions.getDataViewRectangle();
        RBinScrollPos scrollPosition = scrolling.getScrollPosition();

        g.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND)); //  g.setColor(colorsProfile.getTextBackground());
        if (backgroundPaintMode != BackgroundPaintMode.TRANSPARENT) {
            g.rect(dataViewRect.getX(), dataViewRect.getY(), dataViewRect.getWidth(), dataViewRect.getHeight());
        }

        if (backgroundPaintMode == BackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : bytesPerRow);
            float stripePositionY = dataViewRect.getY() - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.fill(RThemeStore.getRGBA(RColorType.FOCUS_BACKGROUND)); //  g.setColor(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition > dataSize) {
                    break;
                }

                g.rect(dataViewRect.getX(), stripePositionY, dataViewRect.getWidth(), rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }
    }

    protected void paintOutsideArea(PGraphics g) {
        int headerAreaHeight = dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        RBinRect componentRect = dimensions.getComponentRectangle();
        int characterWidth = metrics.getCharacterWidth();
        g.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND)); // g.setColor(colorsProfile.getTextBackground());
        g.rect(componentRect.getX(), componentRect.getY(), componentRect.getWidth(), headerAreaHeight);

        // Decoration lines
        g.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); // g.setColor(colorsProfile.getDecorationLine());
        g.line(componentRect.getX(), componentRect.getY() + headerAreaHeight - 1, componentRect.getX() + rowPositionAreaWidth, componentRect.getY() + headerAreaHeight - 1);

        float lineX = componentRect.getX() + rowPositionAreaWidth - (characterWidth / 2);
        if (lineX >= componentRect.getX()) {
            g.line(lineX, componentRect.getY(), lineX, componentRect.getY() + headerAreaHeight);
        }
    }

    protected void paintHeader(PGraphics g) {
        int charactersPerCodeSection = visibility.getCharactersPerCodeSection();
        RBinRect headerArea = dimensions.getHeaderAreaRectangle();
        RBinScrollPos scrollPosition = scrolling.getScrollPosition();

//        RBinRect clipBounds = g.getClipBounds();
//        g.setClip(clipBounds != null ? clipBounds.intersection(headerArea) : headerArea);

        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        float dataViewX = dimensions.getScrollPanelX();

        g.textFont(font);
        g.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND)); //  g.setColor(colorsProfile.getTextBackground());
        g.rect(headerArea.getX(), headerArea.getY(), headerArea.getWidth(), headerArea.getHeight());

        CodeAreaViewMode viewMode = structure.getViewMode();
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            float headerX = dataViewX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset();
            float headerY = headerArea.getY() + rowHeight - metrics.getSubFontSpace();

            g.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); //  g.setColor(colorsProfile.getTextColor());
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
                        drawCenteredChars(g, rowDataCache.headerChars, renderOffset, characterOnRow - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
                    }

                    if (!RBinUtils.areSameColors(color, renderColor)) {
                        renderColor = color;
                        g.stroke(color.getRGB());
                    }

                    renderOffset = characterOnRow;
                }
            }

            if (renderOffset < charactersPerCodeSection) {
                drawCenteredChars(g, rowDataCache.headerChars, renderOffset, charactersPerCodeSection - renderOffset, characterWidth, headerX + renderOffset * characterWidth, headerY);
            }
        }

        // Decoration lines
        g.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); // g.setColor(colorsProfile.getDecorationLine());
        g.line(headerArea.getX(), headerArea.getY() + headerArea.getHeight() - 1, headerArea.getX() + headerArea.getWidth(), headerArea.getY() + headerArea.getHeight() - 1);
        float lineX = dataViewX + visibility.getPreviewRelativeX() - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2 - 1;
        if (lineX >= dataViewX) {
            g.line(lineX, headerArea.getY(), lineX, headerArea.getY() + headerArea.getHeight());
        }
//        g.setClip(clipBounds);
    }

    protected void paintRowPosition(PGraphics g) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = editor.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();
        int rowsPerRect = dimensions.getRowsPerRect();
        RBinRect rowPosRectangle = dimensions.getRowPositionAreaRectangle();
        RBinRect dataViewRectangle = dimensions.getDataViewRectangle();
        //RBinRect clipBounds = g.getClipBounds();
        //g.setClip(clipBounds != null ? clipBounds.intersection(rowPosRectangle) : rowPosRectangle);

        g.textFont(font);
        g.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND)); //  g.setColor(colorsProfile.getTextBackground());
        g.rect(rowPosRectangle.getX(), rowPosRectangle.getY(), rowPosRectangle.getWidth(), rowPosRectangle.getHeight());

        RBinScrollPos scrollPosition = scrolling.getScrollPosition();
        if (backgroundPaintMode == BackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : bytesPerRow);
            float stripePositionY = rowPosRectangle.getY() - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            g.fill(RThemeStore.getRGBA(RColorType.FOCUS_BACKGROUND)); //  g.setColor(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition > dataSize) {
                    break;
                }

                g.rect(rowPosRectangle.getX(), stripePositionY, rowPosRectangle.getWidth(), rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }

        long dataPosition = bytesPerRow * scrollPosition.getRowPosition();
        float positionY = rowPosRectangle.getY() + rowHeight - subFontSpace - scrollPosition.getRowOffset();
        g.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); //  g.setColor(colorsProfile.getTextColor());
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            RBinUtils.longToBaseCode(rowDataCache.rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, CodeType.HEXADECIMAL.getBase(), rowPositionLength, true, codeCharactersCase);
            drawCenteredChars(g, rowDataCache.rowPositionCode, 0, rowPositionLength, characterWidth, rowPosRectangle.getX(), positionY);

            positionY += rowHeight;
            dataPosition += bytesPerRow;
            if (dataPosition < 0) {
                break;
            }
        }

        // Decoration lines
        g.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); // g.setColor(colorsProfile.getDecorationLine());
        float lineX = rowPosRectangle.getX() + rowPosRectangle.getWidth() - (characterWidth / 2);
        if (lineX >= rowPosRectangle.getX()) {
            g.line(lineX, dataViewRectangle.getY(), lineX, dataViewRectangle.getY() + dataViewRectangle.getHeight());
        }
        g.line(dataViewRectangle.getX(), dataViewRectangle.getY() - 1, dataViewRectangle.getX() + dataViewRectangle.getWidth(), dataViewRectangle.getY() - 1);

//        g.setClip(clipBounds);
    }

    protected void paintMainArea(PGraphics g) {
        if (!initialized) {
            reset();
        }
        if (fontChanged) {
            fontChanged(g);
            fontChanged = false;
        }

        RBinRect mainAreaRect = dimensions.getMainAreaRectangle();
        RBinRect dataViewRectangle = dimensions.getDataViewRectangle();
        RBinScrollPos scrollPosition = scrolling.getScrollPosition();
        int characterWidth = metrics.getCharacterWidth();
        int previewRelativeX = visibility.getPreviewRelativeX();

//        RBinRect clipBounds = g.getClipBounds();
//        g.setClip(clipBounds != null ? clipBounds.intersection(mainAreaRect) : mainAreaRect);
        colorAssessor.startPaint(this);
        charAssessor.startPaint(this);

        paintBackground(g);

        // Decoration lines
        g.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); // g.setColor(colorsProfile.getDecorationLine());
        float lineX = dataViewRectangle.getX() + previewRelativeX - scrollPosition.getCharPosition() * characterWidth - scrollPosition.getCharOffset() - characterWidth / 2 - 1;
        if (lineX >= dataViewRectangle.getX()) {
            g.line(lineX, dataViewRectangle.getY(), lineX, dataViewRectangle.getY() + dataViewRectangle.getHeight());
        }

        paintRows(g);
//        g.setClip(clipBounds);
        paintCursor(g);

//        paintDebugInfo(g, mainAreaRect, scrollPosition);
    }

    /**
     * Paints the main component.
     *
     * @param g graphics
     */
    public void paintComponent(PGraphics g) {
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

        if (antialiasingMode != AntialiasingMode.OFF && g.getNative() instanceof Graphics2D) {
            Object antialiasingHint = antialiasingMode.getAntialiasingHint((Graphics2D) g.getNative());
            ((Graphics2D) g.getNative()).setRenderingHint(
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

    /**
     * Rebuilds colors after UIManager change.
     */
    public void resetColors() {
        resetColors = true;
    }

    /**
     * Updates painter layout state for new painting.
     */
    public void resetLayout() {
        layoutChanged = true;
    }

    /**
     * Resets painter font state for new painting.
     */
    public void resetFont() {
        fontChanged = true;
        resetLayout();
    }

    /**
     * Resets caret state.
     */
    public void resetCaret() {
        caretChanged = true;
    }

    /**
     * Resets complete painter state for new painting.
     */
    public void reset() {
        resetColors();
        resetFont();
        //resetLayout();
        resetCaret();
    }

    protected static class RowDataCache {

        char[] headerChars;
        byte[] rowData;
        char[] rowPositionCode;
        char[] rowCharacters;
    }

    protected static class CursorDataCache {

        RBinRect caretRect = new RBinRect();
        RBinRect mirrorCursorRect = new RBinRect();
        final BasicStroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
        int cursorCharsLength;
        char[] cursorChars;
        int cursorDataLength;
        byte[] cursorData;
    }
}
