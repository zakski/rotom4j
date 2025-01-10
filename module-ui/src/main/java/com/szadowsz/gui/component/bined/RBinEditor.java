package com.szadowsz.gui.component.bined;

import com.szadowsz.gui.component.group.RGroupBuffer;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.rotom4j.binary.BinaryData;
import com.szadowsz.rotom4j.binary.EditableBinaryData;
import com.szadowsz.rotom4j.binary.array.ByteArrayData;
import com.szadowsz.rotom4j.binary.array.ByteArrayEditableData;
import com.szadowsz.rotom4j.binary.io.reader.Buffer;
import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.bined.cursor.RCaretPos;
import com.szadowsz.gui.component.bined.cursor.RCursorShape;
import com.szadowsz.gui.component.bined.settings.*;
import com.szadowsz.gui.component.bined.utils.RBinUtils;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.bined.cursor.RCaret;
import com.szadowsz.gui.component.bined.settings.RClipHandlingMode;
import com.szadowsz.gui.component.bined.settings.REnterKeyMode;
import com.szadowsz.gui.component.bined.settings.RScrollDirection;
import com.szadowsz.gui.component.bined.settings.RTabKeyMode;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.nio.ByteBuffer;

/**
 * Editor Level Logic
 */
public class RBinEditor extends RBinEdBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(RBinEditor.class);

    protected static final int LAST_CONTROL_CODE = 31;
    protected static final char DELETE_CHAR = (char) 0x7f;

    protected static final String HEADER = "header";
    protected static final String MAIN = "main";

    protected RBinCharAssessor charAssessor = new RBinCharAssessor();
    protected RBinColorAssessor colorAssessor = new RBinColorAssessor();

    // How to Display
    protected RBinViewMode viewMode = RBinViewMode.DUAL; // TODO implement dual display
    protected RBackgroundPaintMode backgroundPaintMode = RBackgroundPaintMode.STRIPED;

    // Display Buffer
    protected final RGroupBuffer buffer;

    // Cursor Caret
    protected RCaret caret;
    protected boolean showMirrorCursor = true;

    protected REnterKeyMode enterKeyHandlingMode = REnterKeyMode.PLATFORM_SPECIFIC;
    protected RTabKeyMode tabKeyHandlingMode = RTabKeyMode.PLATFORM_SPECIFIC;

    protected RowDataCache rowDataCache = null;
    protected CursorDataCache cursorDataCache = null;

    protected RBinEditor(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
        caret = new RCaret(this);
        buffer = new RGroupBuffer(this,size.x,size.y);
    }

    public RBinEditor(RotomGui gui, String path, RGroup parent, String filePath) {
        super(gui, path, parent);

        LOGGER.info("Loading File \"{}\" for \"{}\" Binary Editor", filePath, name);
        byte[] data = Buffer.readFile(filePath);
        contentData = new ByteArrayEditableData(data);
        caret = new RCaret(this);
        init();
        children.add(new RBinMain(gui, path + "/" + MAIN, this));
        RRect rect = dimensions.getComponentRectangle();
        buffer = new RGroupBuffer(this, rect.getWidth(),rect.getHeight());
    }

    /**
     * Returns current caret position.
     *
     * @return caret position
     */
    protected RCaretPos getActiveCaretPosition() {
        return caret.getCaretPosition();
    }

    /**
     * Returns currently active caret section.
     *
     * @return code area section
     */
    protected RCodeAreaSection getActiveSection() {
        return caret.getSection();
    }

    protected RBinCharAssessor getCharAssessor() {
        return charAssessor;
    }

    protected RBinColorAssessor getColorAssessor() {
        return colorAssessor;
    }

    /**
     * Returns relative cursor position in code area or null if cursor is not
     * visible.
     *
     * @param dataPosition data position
     * @param codeOffset   code offset
     * @param section      section
     * @return cursor position or null
     */
    protected PVector getPositionPoint(long dataPosition, int codeOffset, RCodeAreaSection section) {
        int bytesPerRow = structure.getBytesPerRow();
        int rowsPerRect = dimensions.getRowsPerRect();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();

        long row = dataPosition / bytesPerRow;
        if (row < -1 || row > rowsPerRect) {
            return null;
        }

        int byteOffset = (int) (dataPosition % bytesPerRow);

        RRect dataViewRect = dimensions.getDataViewRectangle();
        float caretY = (dataViewRect.getY() + row * rowHeight);
        float caretX;
        if (section == RCodeAreaSection.TEXT_PREVIEW) {
            caretX = dataViewRect.getX() + visibility.getPreviewRelativeX() + characterWidth * byteOffset;
        } else {
            caretX = dataViewRect.getX() + characterWidth * (structure.computeFirstCodeCharacterPos(byteOffset) + codeOffset);
        }
        return new PVector(caretX, caretY);
    }

    protected RSelectingMode isSelectingMode(RKeyEvent keyEvent) {
        return keyEvent.isShiftDown() ? RSelectingMode.SELECTING : RSelectingMode.NONE;
    }

    protected boolean isValidChar(char value) {
        return getCharset().canEncode();
    }

    protected void setActiveCaretPosition(RCaretPos caretPosition) {
        caret.setCaretPosition(caretPosition);
        redrawWinBuffer();
    }

    protected void setActiveCaretPosition(long dataPosition) {
        caret.setCaretPosition(dataPosition);
        redrawWinBuffer();
    }

    protected void setCodeValue(int value) {
        RCaretPos caretPosition = getActiveCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        byte byteValue = contentData.getByte(dataPosition);
        byte outputValue = RBinUtils.setCodeValue(byteValue, value, codeOffset, codeType);
        ((EditableBinaryData) contentData).setByte(dataPosition, outputValue);
    }

    protected void setEditOperation(REditOperation editOperation) {
        REditOperation previousOperation = this.editOperation;
        this.editOperation = editOperation;
        REditOperation currentOperation = this.editOperation;
        boolean changed = previousOperation != currentOperation;
        if (changed) {
            caret.resetBlink();
            redrawWinBuffer();
        }
    }

    protected boolean changeEditOperation() {
        if (editMode == REditMode.EXPANDING || editMode == REditMode.CAPPED) {
            switch (editOperation) {
                case INSERT: {
                    setEditOperation(REditOperation.OVERWRITE);
                    break;
                }
                case OVERWRITE: {
                    setEditOperation(REditOperation.INSERT);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected void init() {
        LOGGER.info("Initialising \"{}\" Binary Editor", name);

        caret.setSection(RCodeAreaSection.CODE_MATRIX);
        initMetrics();

        long rowsCount = computeRowsCount();
        rowPositionLength = computeRowPositionLength();

        initDimensions(rowsCount);
        structure.setEditor(this);
        structure.updateCache(dimensions.getCharactersPerPage());

        initVisibility(); // use the sizes to figure out the width
        updateRowDataCache();
    }

    /**
     * Calculate Dimensions
     */
    protected void initDimensions(long rowsCount) {
        dimensions.computeRowDimensions(metrics, rowPositionLength, maxRowsPerPage, rowsCount);
        dimensions.computeHeaderAndDataDimensions(metrics, codeType, maxBytesPerRow);
        dimensions.computeOtherMetrics(metrics);

        // Relay the size to the proper place // TODO Bodge job
        size.x = dimensions.getDisplayRectangle().getWidth();
        size.y = dimensions.getDisplayRectangle().getHeight();
    }

    /**
     * Calculate Metrics
     */
    protected void initMetrics() {
        PGraphics fontGraphics = gui.getSketch().createGraphics(800, 600, PConstants.JAVA2D);
        fontGraphics.beginDraw();
        fontGraphics.endDraw();
        metrics.recomputeMetrics(fontGraphics, RFontStore.getMainFont(), charset); // get Font Character sizes
        LOGGER.info("Font Metrics Loaded for \"{}\" Binary Editor", name);
    }

    protected void initVisibility() {
        int charactersPerPage = dimensions.getCharactersPerPage();
        structure.updateCache(charactersPerPage);
        visibility.recomputeCharPositions(this);
    }

    @Override
    protected void loadData(String filePath) {
        LOGGER.info("Loading File \"{}\" for \"{}\" Binary Editor", filePath, name);
        byte[] data = Buffer.readFile(filePath);
        contentData = new ByteArrayData(data);
    }

    protected void updateAssessors() {
        charAssessor.update(this);
    }

    protected void updateMirrorCursorRect(long dataPosition, RCodeAreaSection section) {
        PVector mirrorCursorPoint = getPositionPoint(dataPosition, 0, section == RCodeAreaSection.CODE_MATRIX ? RCodeAreaSection.TEXT_PREVIEW : RCodeAreaSection.CODE_MATRIX);
        if (mirrorCursorPoint == null) {
            cursorDataCache.mirrorCursorRect.setSize(0, 0);
        } else {
            cursorDataCache.mirrorCursorRect.setSize(mirrorCursorPoint.x, mirrorCursorPoint.y, metrics.getCharacterWidth() * (section == RCodeAreaSection.TEXT_PREVIEW ? codeType.getMaxDigitsForByte() : 1), metrics.getRowHeight());
        }
    }

    protected void updateRectToCursorPosition(RRect rect, long dataPosition, int codeOffset, RCodeAreaSection section) {
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        PVector cursorPoint = getPositionPoint(dataPosition, codeOffset, section);
        if (cursorPoint == null) {
            rect.setSize(0, 0, 0, 0);
        } else {
            RCursorShape cursorShape = editOperation == REditOperation.INSERT ? RCursorShape.INSERT : RCursorShape.OVERWRITE;
            int cursorThickness = RCaret.getCursorThickness(cursorShape, characterWidth, rowHeight);
            rect.setSize(cursorPoint.x, cursorPoint.y, cursorThickness, rowHeight);
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

    protected void updateSelection(RSelectingMode selectingMode) {
        long dataPosition = caret.getDataPosition();
        if (selectingMode == RSelectingMode.SELECTING) {
            selection.setSelection(selection.getStart(), dataPosition);
        } else {
            selection.setSelection(dataPosition, dataPosition);
        }
    }

    protected void deleteSelection() {
        if (!(contentData instanceof EditableBinaryData)) {
            throw new IllegalStateException("Data is not editable");
        }

        if (selection.isEmpty()) {
            return;
        }

        long first = selection.getFirst();
        long last = selection.getLast();
        long length = last - first + 1;
        if (editMode == REditMode.INPLACE) {
            ((EditableBinaryData) contentData).fillData(first, length);
        } else {
            ((EditableBinaryData) contentData).remove(first, length);
        }
        setActiveCaretPosition(first);
        clearSelection();
        getParentWindow().reinitialiseBuffer();
    }

    protected void move(RSelectingMode selectingMode, RMovementDirection direction) {
        RCaretPos caretPosition = getActiveCaretPosition();
        RCaretPos movePosition = structure.computeMovePosition(caretPosition, direction, dimensions.getRowsPerPage());
        if (!caretPosition.equals(movePosition)) {
            setActiveCaretPosition(movePosition);
            updateSelection(selectingMode);
        } else if (selectingMode == RSelectingMode.NONE) {
            clearSelection();
        }
    }

    protected void moveCaret(float positionX, float positionY, RSelectingMode selecting) {
        RCaretPos caretPosition = computeClosestCaretPosition(positionX, positionY);
        setActiveCaretPosition(caretPosition);
        updateSelection(selecting);

        getParentWindow().redrawBuffer();
    }

    protected void moveCaret(RMouseEvent me) {
        RSelectingMode selecting = me.isShiftDown() ? RSelectingMode.SELECTING : RSelectingMode.NONE;
        moveCaret(me.getX(), me.getY(), selecting);
    }

    private void pressedCharAsCode(char keyChar) {
        RCaretPos caretPosition = getActiveCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        RCodeType codeType = getCodeType();
        boolean validKey = RBinUtils.isValidCodeKeyValue(keyChar, codeOffset, codeType);
        if (validKey) {
            REditMode editMode = getEditMode();
            if (hasSelection() && editMode != REditMode.INPLACE) {
                deleteSelection();
            }

            int value;
            if (keyChar >= '0' && keyChar <= '9') {
                value = keyChar - '0';
            } else {
                value = Character.toLowerCase(keyChar) - 'a' + 10;
            }

            if (editMode == REditMode.EXPANDING && editOperation == REditOperation.INSERT) {
                if (codeOffset > 0) {
                    byte byteRest = contentData.getByte(dataPosition);
                    switch (codeType) {
                        case BINARY: {
                            byteRest = (byte) (byteRest & (0xff >> codeOffset));
                            break;
                        }
                        case DECIMAL: {
                            byteRest = (byte) (byteRest % (codeOffset == 1 ? 100 : 10));
                            break;
                        }
                        case OCTAL: {
                            byteRest = (byte) (byteRest % (codeOffset == 1 ? 64 : 8));
                            break;
                        }
                        case HEXADECIMAL: {
                            byteRest = (byte) (byteRest & 0xf);
                            break;
                        }
                        default:
                            throw RBinUtils.getInvalidTypeException(codeType);
                    }
                    if (byteRest > 0) {
                        ((EditableBinaryData) contentData).insert(dataPosition + 1, 1);
                        ((EditableBinaryData) contentData).setByte(dataPosition, (byte) (contentData.getByte(dataPosition) - byteRest));
                        ((EditableBinaryData) contentData).setByte(dataPosition + 1, byteRest);
                    }
                } else {
                    ((EditableBinaryData) contentData).insert(dataPosition, 1);
                }
                setCodeValue(value);
            } else {
                if (editMode == REditMode.EXPANDING && editOperation == REditOperation.OVERWRITE && dataPosition == getDataSize()) {
                    ((EditableBinaryData) contentData).insert(dataPosition, 1);
                }
                if (editMode != REditMode.INPLACE || dataPosition < getDataSize()) {
                    setCodeValue(value);
                }
            }
            notifyDataChanged();
            move(RSelectingMode.NONE, RMovementDirection.RIGHT);
            redrawWinBuffer();
        }
    }

    protected void pressedCharInPreview(char keyChar) {
        if (isValidChar(keyChar)) {
            RCaretPos caretPosition = getActiveCaretPosition();

            long dataPosition = caretPosition.getDataPosition();
            byte[] bytes = charToBytes(keyChar);
            if (editMode == REditMode.INPLACE) {
                int length = bytes.length;
                if (dataPosition + length > getDataSize()) {
                    return;
                }
            }
            if (hasSelection() && editMode != REditMode.INPLACE) {
                deleteSelection();
            }

            if ((editMode == REditMode.EXPANDING && editOperation == REditOperation.OVERWRITE) || editMode == REditMode.INPLACE) {
                if (dataPosition < getDataSize()) {
                    int length = bytes.length;
                    if (dataPosition + length > getDataSize()) {
                        length = (int) (getDataSize() - dataPosition);
                    }
                    ((EditableBinaryData) contentData).remove(dataPosition, length);
                }
            }
            ((EditableBinaryData) contentData).insert(dataPosition, bytes);
            notifyDataChanged();
            caret.setCaretPosition(dataPosition + bytes.length - 1);
            move(RSelectingMode.NONE, RMovementDirection.RIGHT);
            redrawWinBuffer();
        }
    }

    protected void pasteBinaryData(BinaryData pastedData) {
        if (hasSelection()) {
            deleteSelection();
            notifyDataChanged();
        }

        long dataPosition = caret.getDataPosition();

        long clipDataSize = pastedData.getDataSize();
        long toReplace = clipDataSize;
        if (editMode == REditMode.INPLACE) {
            if (dataPosition + toReplace > getDataSize()) {
                toReplace = getDataSize() - dataPosition;
            }
            ((EditableBinaryData) contentData).replace(dataPosition, pastedData, 0, toReplace);
        } else {
            if (editMode == REditMode.EXPANDING && editOperation == REditOperation.OVERWRITE) {
                if (dataPosition + toReplace > getDataSize()) {
                    toReplace = getDataSize() - dataPosition;
                }
                ((EditableBinaryData) contentData).remove(dataPosition, toReplace);
            }

            ((EditableBinaryData) contentData).insert(dataPosition, pastedData);
            caret.setCaretPosition(caret.getDataPosition() + clipDataSize);
            updateSelection(RSelectingMode.NONE);
        }

        caret.setCodeOffset(0);
        setActiveCaretPosition(caret.getCaretPosition());
        notifyDataChanged();
        redrawWinBuffer();
        clearSelection();
    }

    protected void scroll(RScrollDirection direction) {
        // NOOP TODO
    }

    protected void enterPressed() {
        if (!checkEditAllowed()) {
            return;
        }

        if (getActiveSection() == RCodeAreaSection.TEXT_PREVIEW) {
            String sequence = enterKeyHandlingMode.getSequence();
            if (!sequence.isEmpty()) {
                pressedCharInPreview(sequence.charAt(0));
                if (sequence.length() == 2) {
                    pressedCharInPreview(sequence.charAt(1));
                }
            }
        }
    }

    protected void tabPressed(RSelectingMode selectingMode) {
        if (!checkEditAllowed()) {
            return;
        }

        if (tabKeyHandlingMode == RTabKeyMode.PLATFORM_SPECIFIC || tabKeyHandlingMode == RTabKeyMode.CYCLE_TO_NEXT_SECTION || tabKeyHandlingMode == RTabKeyMode.CYCLE_TO_PREVIOUS_SECTION) {
            if (getViewMode() == RBinViewMode.DUAL) {
                move(selectingMode, RMovementDirection.SWITCH_SECTION);
                redrawWinBuffer();
            }
        } else if (getActiveSection() == RCodeAreaSection.TEXT_PREVIEW) {
            String sequence = tabKeyHandlingMode == RTabKeyMode.INSERT_TAB ? "\t" : "  ";
            pressedCharInPreview(sequence.charAt(0));
            if (sequence.length() == 2) {
                pressedCharInPreview(sequence.charAt(1));
            }
        }
    }

    protected void backSpacePressed() {
        if (!checkEditAllowed()) {
            return;
        }

        if (hasSelection()) {
            deleteSelection();
            notifyDataChanged();
        } else {
            long dataPosition = caret.getDataPosition();
            if (dataPosition == 0 || dataPosition > getDataSize()) {
                return;
            }

            caret.setCodeOffset(0);
            move(RSelectingMode.NONE, RMovementDirection.LEFT);
            caret.setCodeOffset(0);
            ((EditableBinaryData) contentData).remove(dataPosition - 1, 1);
            notifyDataChanged();
            setActiveCaretPosition(caret.getCaretPosition());
            redrawWinBuffer();
            clearSelection();
        }
    }

    protected void deletePressed() {
        if (!checkEditAllowed()) {
            return;
        }

        if (hasSelection()) {
            deleteSelection();
            notifyDataChanged();
            redrawWinBuffer();
        } else {
            long dataPosition = caret.getDataPosition();
            if (dataPosition >= getDataSize()) {
                return;
            }
            ((EditableBinaryData) contentData).remove(dataPosition, 1);
            notifyDataChanged();
            if (caret.getCodeOffset() > 0) {
                caret.setCodeOffset(0);
            }
            setActiveCaretPosition(caret.getCaretPosition());
            clearSelection();
            redrawWinBuffer();
        }
    }

    public byte[] charToBytes(char value) {
        ByteBuffer buffer = getCharset().encode(Character.toString(value));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        updateAssessors();
        pg.pushMatrix();
        int yDiff = 0;
        pg.image(buffer.draw().get(0, yDiff, (int) size.x, (int) size.y), 0, 0);
        pg.popMatrix();
    }

    boolean isMirrorCursorShowing() {
        return showMirrorCursor;
    }

    public RBackgroundPaintMode getBackgroundPaintMode() {
        return backgroundPaintMode;
    }

    /**
     * Returns handler for caret.
     *
     * @return caret handler
     */
    public RCaret getCaret() {
        return caret;
    }

    /**
     * Returns data or null.
     *
     * @return binary data
     */
    public BinaryData getContentData() {
        return contentData;
    }

    public CursorDataCache getCursorDataCache() {
        if (cursorDataCache == null) {
            cursorDataCache = new CursorDataCache();
        }
        int cursorCharsLength = codeType.getMaxDigitsForByte();
        if (cursorDataCache.cursorCharsLength != cursorCharsLength) {
            cursorDataCache.cursorCharsLength = cursorCharsLength;
            cursorDataCache.cursorChars = new char[cursorCharsLength];
        }
        int cursorDataLength = metrics.getMaxBytesPerChar();
        if (cursorDataCache.cursorDataLength != cursorDataLength) {
            cursorDataCache.cursorDataLength = cursorDataLength;
            cursorDataCache.cursorData = new byte[cursorDataLength];
        }
        return cursorDataCache;
    }

    /**
     * Returns cursor rectangle.
     *
     * @param dataPosition data position
     * @param codeOffset   code offset
     * @param section      section
     * @return cursor rectangle or empty rectangle
     */
    public RRect getCursorPositionRect(long dataPosition, int codeOffset, RCodeAreaSection section) {
        RRect rect = new RRect();
        updateRectToCursorPosition(rect, dataPosition, codeOffset, section);
        return rect;
    }

    public RowDataCache getRowDataCache() {
        return rowDataCache;
    }

    /**
     * Returns current view mode.
     *
     * @return view mode
     */
    public RBinViewMode getViewMode() {
        return viewMode;
    }

    @Override
    public void setLayout(RLayoutBase layout) {
    }

    @Override
    public float suggestWidth() {
        int characterWidth = metrics.getCharacterWidth(); // Get the width of a single character
        int digitsForByte = codeType.getMaxDigitsForByte(); // Get the number of characters for a byte
        return characterWidth * (rowPositionLength + 1) + digitsForByte * characterWidth * maxBytesPerRow; // Get the ideal width of a row based on the max byte width
    }

    @Override
    public void keyPressed(RKeyEvent keyEvent, float mouseX, float mouseY) {
        if (!gui.hasFocus(this)) {
            return;
        }

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_LEFT: {
                move(isSelectingMode(keyEvent), RMovementDirection.LEFT);
                redrawWinBuffer();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_RIGHT: {
                move(isSelectingMode(keyEvent), RMovementDirection.RIGHT);
                redrawWinBuffer();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_UP: {
                move(isSelectingMode(keyEvent), RMovementDirection.UP);
                redrawWinBuffer();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_DOWN: {
                move(isSelectingMode(keyEvent), RMovementDirection.DOWN);
                redrawWinBuffer();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_HOME: {
                if (keyEvent.isControlDown()) {
                    move(isSelectingMode(keyEvent), RMovementDirection.DOC_START);
                } else {
                    move(isSelectingMode(keyEvent), RMovementDirection.ROW_START);
                }
                redrawWinBuffer();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_END: {
                if (keyEvent.isControlDown()) {
                    move(isSelectingMode(keyEvent), RMovementDirection.DOC_END);
                } else {
                    move(isSelectingMode(keyEvent), RMovementDirection.ROW_END);
                }
                redrawWinBuffer();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_PAGE_UP: {
                scroll(RScrollDirection.PAGE_UP);
                move(isSelectingMode(keyEvent), RMovementDirection.PAGE_UP);
                redrawWinBuffer();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_PAGE_DOWN: {
                scroll(RScrollDirection.PAGE_DOWN);
                move(isSelectingMode(keyEvent), RMovementDirection.PAGE_DOWN);
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_INSERT: {
                if (changeEditOperation()) {
                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.VK_TAB: {
                tabPressed(isSelectingMode(keyEvent));
                if (tabKeyHandlingMode != RTabKeyMode.IGNORE) {
                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.VK_ENTER: {
                enterPressed();
                if (enterKeyHandlingMode != REnterKeyMode.IGNORE) {
                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.VK_DELETE: {
                if (editMode == REditMode.EXPANDING) {
                    deletePressed();
                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.VK_BACK_SPACE: {
                if (editMode == REditMode.EXPANDING) {
                    backSpacePressed();
                    keyEvent.consume();
                }
                break;
            }
            default: {
                if (clipboardHandlingMode == RClipHandlingMode.PROCESS) {
                    if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_C) {
                        copy();
                        keyEvent.consume();
                        break;
                    } else if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_X) {
                        cut();
                        keyEvent.consume();
                        break;
                    } else if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_V) {
                        paste();
                        keyEvent.consume();
                        break;
                    } else if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_A) {
                        selectAll();
                        keyEvent.consume();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void keyTyped(RKeyEvent keyEvent, float mouseX, float mouseY) {
        char keyValue = keyEvent.getKey();
        LOGGER.info("Bin Editor key {}",keyValue);
        // TODO Add support for high unicode codes
        if (keyValue == KeyEvent.CHAR_UNDEFINED) {
            return;
        }
        if (!checkEditAllowed()) {
            return;
        }

        RCodeAreaSection section = getActiveSection();
        if (section != RCodeAreaSection.TEXT_PREVIEW) {
            LOGGER.info("Process as Code {}",keyValue);
            if (!keyEvent.hasModifiers() || keyEvent.isShiftDown()) {
                pressedCharAsCode(keyValue);
            }
        } else {
            if (keyValue > LAST_CONTROL_CODE && keyValue != DELETE_CHAR) {
                pressedCharInPreview(keyValue);
            }
        }
    }


    @Override
    public void mousePressed(RMouseEvent mouseEvent, float mouseY) {
        if (!gui.hasFocus(this)) {
            gui.takeFocus(this);
        }
        if (mouseEvent.isLeft()) {
            moveCaret(mouseEvent);
            isDragged = true;
            isMouseOver = true;
            mouseEvent.consume();
        }
    }

    @Override
    public void mouseDragged(RMouseEvent mouseEvent) {
        if (gui.hasFocus(this)) {
            isMouseOver = true;
            moveCaret(mouseEvent.getX(), mouseEvent.getY(), RSelectingMode.SELECTING);
            mouseEvent.consume();
            redrawWinBuffer();
        }
    }

    @Override
    public void updateCoordinates(float bX, float bY, float rX, float rY, float w, float h) {
        super.updateCoordinates(bX, bY, rX, rY, w, h);
        buffer.resetBuffer();
    }

    public static class RowDataCache {

        char[] headerChars;
        byte[] rowData;
        char[] rowPositionCode;
        char[] rowCharacters;
    }

    public static class CursorDataCache {

        RRect caretRect = new RRect();
        RRect mirrorCursorRect = new RRect();
        final BasicStroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
        int cursorCharsLength;
        char[] cursorChars;
        int cursorDataLength;
        byte[] cursorData;
    }
}
