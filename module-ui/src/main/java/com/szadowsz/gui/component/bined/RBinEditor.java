package com.szadowsz.gui.component.bined;

import com.szadowsz.binary.BinaryData;
import com.szadowsz.binary.EmptyBinaryData;
import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.bined.caret.RCaret;
import com.szadowsz.gui.component.bined.caret.RCaretPos;
import com.szadowsz.gui.component.bined.scroll.RBinScrollPos;
import com.szadowsz.gui.component.bined.settings.*;
import com.szadowsz.gui.component.bined.utils.RBinUtils;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.utils.RComponentScrollbar;
import com.szadowsz.gui.config.text.RFontStore;
import processing.core.PGraphics;

import java.awt.*;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * Binary data viewer/editor component.
 */
public class RBinEditor extends RComponent {

    // The Data
    protected BinaryData contentData = EmptyBinaryData.INSTANCE;

    // How to Display
    protected CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;

    // Display
    protected RBinDraw display;

    // Character Config
    protected Charset charset = Charset.forName(RFontStore.DEFAULT_ENCODING);
    protected CodeCharactersCase codeCharactersCase = CodeCharactersCase.UPPER;
    protected CodeType codeType = CodeType.HEXADECIMAL;
    protected Font codeFont;

    // Row Layout Config
    protected int maxBytesPerRow = 16;
    protected int minRowPositionLength = 0;
    protected int maxRowPositionLength = 0;
    protected int wrappingBytesGroupSize = 0;
    protected RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;

    // Scrollbar Config
    protected final RBinScrollPos scrollPosition = new RBinScrollPos();
    protected ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    protected VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.ROW;
    protected ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    protected HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;

    // Edit Op
    protected EditMode editMode = EditMode.EXPANDING;
    protected EditOperation editOperation = EditOperation.OVERWRITE;

    // Cursor Caret
    protected RCaret caret;
    protected boolean showMirrorCursor = true;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected RBinEditor(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
        init();
    }

    private void init() {
        caret = new RCaret(this);
        caret.setSection(CodeAreaSection.CODE_MATRIX);
    }

    private void reset() {
        // TODO
    }

    private void redraw() {
        // TODO
    }

    public void validateCaret() {
        boolean moved = false;
        if (caret.getDataPosition() > getDataSize()) {
            caret.setDataPosition(getDataSize());
            moved = true;
        }
        if (caret.getSection() == CodeAreaSection.CODE_MATRIX && caret.getCodeOffset() >= codeType.getMaxDigitsForByte()) {
            caret.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
            moved = true;
        }

        if (moved) {
            // TODO
        }
    }

    @Override
    protected void drawBackground(PGraphics pg) {
        // TODO
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        // TODO
    }

    /**
     * Returns current caret position.
     *
     * @return caret position
     */
    public RCaretPos getActiveCaretPosition() {
        return caret.getCaretPosition();
    }

    /**
     * Returns currently active caret section.
     *
     * @return code area section
     */
    public CodeAreaSection getActiveSection() {
        return caret.getSection();
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
     * Returns currently used charset.
     *
     * @return charset
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Returns current code characters case.
     *
     * @return code characters case
     */
    public CodeCharactersCase getCodeCharactersCase() {
        return codeCharactersCase;
    }

    /**
     * Returns font used for text painting.
     *
     * @return font
     */
    public Font getCodeFont() {
        return codeFont == null ? (Font) RFontStore.getMainFont().getNative() : codeFont;
    }

    /**
     * Returns current caret code offset.
     *
     * @return code offset
     */
    public int getCodeOffset() {
        return caret.getCodeOffset();
    }

    /**
     * Returns current code type.
     *
     * @return code type
     */
    public CodeType getCodeType() {
        return codeType;
    }

    /**
     * Returns current caret data position.
     *
     * @return data position
     */
    public long getDataPosition() {
        return caret.getDataPosition();
    }

    /**
     * Returns size of data or 0 if no data is present.
     *
     * @return size of data
     */
    public long getDataSize() {
        return contentData.getDataSize();
    }

    /**
     * Returns edit mode.
     *
     * @return edit mode
     */
    public EditMode getEditMode() {
        return editMode;
    }

    /**
     * Returns currently active operation as set or enforced by current edit
     * mode.
     *
     * @return active edit operation
     */
    public EditOperation getActiveOperation() {
        switch (editMode) {
            case READ_ONLY:
                return EditOperation.INSERT;
            case INPLACE:
                return EditOperation.OVERWRITE;
            case CAPPED:
            case EXPANDING:
                return editOperation;
            default:
                throw RBinUtils.getInvalidTypeException(editMode);
        }
    }

    /**
     * Returns currently enforced edit operation.
     *
     * @return edit operation
     */
    public EditOperation getEditOperation() {
        return editOperation;
    }

    /**
     * Returns horizontal scrollbar visibility mode.
     *
     * @return scrollbar visibility mode
     */
    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    /**
     * Returns horizontal scrolling unit.
     *
     * @return horizontal scrolling unit
     */
    public HorizontalScrollUnit getHorizontalScrollUnit() {
        return horizontalScrollUnit;
    }

    /**
     * Returns maximum number of bytes per row.
     *
     * @return bytes per row
     */
    public int getMaxBytesPerRow() {
        return maxBytesPerRow;
    }

    /**
     * Returns maximum length of position section of the code area.
     *
     * @return maximum length
     */
    public int getMaxRowPositionLength() {
        return maxRowPositionLength;
    }

    /**
     * Returns minimum length of position section of the code area.
     *
     * @return minimum length
     */
    public int getMinRowPositionLength() {
        return minRowPositionLength;
    }

    /**
     * Returns row wrapping mode.
     *
     * @return row wrapping mode
     */
    RowWrappingMode getRowWrapping() {
        return rowWrapping;
    }

    public RBinScrollPos getScrollPosition() {
        return scrollPosition;
    }

    /**
     * Returns vertical scrollbar visibility mode.
     *
     * @return scrollbar visibility mode
     */
    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    /**
     * Returns vertical scrolling unit.
     *
     * @return vertical scrolling unit
     */
    public VerticalScrollUnit getVerticalScrollUnit() {
        return verticalScrollUnit;
    }

    /**
     * Returns current view mode.
     *
     * @return view mode
     */
    public CodeAreaViewMode getViewMode() {
        return viewMode;
    }

    /**
     * Returns size of the byte group.
     *
     * @return size of the byte group
     */
    public int getWrappingBytesGroupSize() {
        return wrappingBytesGroupSize;
    }

    public boolean isEditable() {
        return editMode != EditMode.READ_ONLY;
    }

    public boolean isInitialized() {
        return display.isInitialized();
    }

    /**
     * Sets current caret position to given position.
     *
     * @param caretPosition caret position
     */
    public void setActiveCaretPosition(RCaretPos caretPosition) {
        caret.setCaretPosition(caretPosition);
    }

    /**
     * Sets current caret position to given data position.
     *
     * @param dataPosition data position
     */
    public void setActiveCaretPosition(long dataPosition) {
        caret.setCaretPosition(dataPosition);
    }

    /**
     * Sets current caret position to given data position and offset.
     *
     * @param dataPosition data position
     * @param codeOffset code offset
     */
    public void setActiveCaretPosition(long dataPosition, int codeOffset) {
        caret.setCaretPosition(dataPosition, codeOffset);
    }

    /**
     * Sets charset to use for characters decoding.
     *
     * @param charset charset
     */
    public void setCharset(Charset charset) {
        this.charset = RBinUtils.requireNonNull(charset);
        reset();
    }

    /**
     * Sets current code characters case.
     *
     * @param codeCharactersCase code characters case
     */
    public void setCodeCharactersCase(CodeCharactersCase codeCharactersCase) {
        this.codeCharactersCase = codeCharactersCase;
        redraw();
    }

    /**
     * Sets font used for text painting.
     *
     * @param codeFont font
     */
    public void setCodeFont(Font codeFont) {
        this.codeFont = codeFont;
        reset();
    }

    /**
     * Sets current code type.
     *
     * @param codeType code type
     */
    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
        validateCaret();
        reset();
    }

    /**
     * Sets edit mode.
     *
     * @param editMode edit mode
     */
    public void setEditMode(EditMode editMode) {
        boolean changed = editMode != this.editMode;
        this.editMode = editMode;
        if (changed) {
            caret.resetBlink();
            redraw();
        }
    }

    /**
     * Sets currently enforced edit operation.
     *
     * @param editOperation edit operation
     */
    public void setEditOperation(EditOperation editOperation) {
        EditOperation previousOperation = getActiveOperation();
        this.editOperation = editOperation;
        EditOperation currentOperation = getActiveOperation();
        boolean changed = previousOperation != currentOperation;
        if (changed) {
            caret.resetBlink();
            redraw();
        }
    }

    /**
     * Sets horizotal scrollbar visibility mode.
     *
     * @param horizontalScrollBarVisibility scrollbar visibility mode
     */
    public void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility) {
        this.horizontalScrollBarVisibility = horizontalScrollBarVisibility;
        redraw();
        updateScrollBars();
    }

    /**
     * Sets horizontal scrolling unit.
     *
     * @param horizontalScrollUnit horizontal scrolling unit
     */
    public void setHorizontalScrollUnit(HorizontalScrollUnit horizontalScrollUnit) {
        this.horizontalScrollUnit = horizontalScrollUnit;
        int charPosition = scrollPosition.getCharPosition();
        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            scrollPosition.setCharOffset(0);
        }
        redraw();
        scrollPosition.setCharPosition(charPosition);
        updateScrollBars();
    }

    /**
     * Sets maximum number of bytes per row.
     *
     * @param maxBytesPerRow bytes per row
     */
    public void setMaxBytesPerRow(int maxBytesPerRow) {
        this.maxBytesPerRow = maxBytesPerRow;
        redraw();
    }


    /**
     * Sets maximum length of position section of the code area.
     *
     * @param maxRowPositionLength maximum length
     */
    public void setMaxRowPositionLength(int maxRowPositionLength) {
        this.maxRowPositionLength = maxRowPositionLength;
        redraw();
    }

    /**
     * Sets minimum length of position section of the code area.
     *
     * @param minRowPositionLength minimum length
     */
    public void setMinRowPositionLength(int minRowPositionLength) {
        this.minRowPositionLength = minRowPositionLength;
        redraw();
    }

    /**
     * Sets row wrapping mode.
     *
     * @param rowWrapping row wrapping mode
     */
    public void setRowWrapping(RowWrappingMode rowWrapping) {
        this.rowWrapping = rowWrapping;
        redraw();
    }

    /**
     * Sets current scrolling position.
     *
     * @param scrollPosition scrolling position
     */
    public void setScrollPosition(RBinScrollPos scrollPosition) {
        if (!scrollPosition.equals(this.scrollPosition)) {
            this.scrollPosition.setScrollPosition(scrollPosition);
            display.scrollPositionModified();
            updateScrollBars();
        }
    }

    /**
     * Sets vertical scrollbar visibility mode.
     *
     * @param verticalScrollBarVisibility scrollbar visibility mode
     */
    public void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility) {
        this.verticalScrollBarVisibility = verticalScrollBarVisibility;
        reset();
        updateScrollBars();
    }

    public void setVerticalScrollUnit(VerticalScrollUnit verticalScrollUnit) {
        this.verticalScrollUnit = verticalScrollUnit;
        long rowPosition = scrollPosition.getRowPosition();
        if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            scrollPosition.setRowOffset(0);
        }
        reset();
        scrollPosition.setRowPosition(rowPosition);
        updateScrollBars();
    }

    /**
     * Sets current view mode.
     *
     * @param viewMode view mode
     */
    public void setViewMode(CodeAreaViewMode viewMode) {
        if (viewMode != this.viewMode) {
            this.viewMode = viewMode;
            switch (viewMode) {
                case CODE_MATRIX:
                    caret.setSection(CodeAreaSection.CODE_MATRIX);
                    reset();
                    break;

                case TEXT_PREVIEW:
                    caret.setSection(CodeAreaSection.TEXT_PREVIEW);
                    reset();
                    break;
                default:
                    reset();
                    break;
            }
        }
    }

    /**
     * Sets size of the byte group.
     *
     * @param groupSize size of the byte group
     */
    public void setWrappingBytesGroupSize(int groupSize) {
        wrappingBytesGroupSize = groupSize;
        redraw();
    }

    /**
     * Reveals scrolling area for given caret position.
     *
     * @param caretPosition caret position
     */
    public void revealPosition(RCaretPos caretPosition) {
        if (!isInitialized()) {
            // Silently ignore if painter is not yet initialized
            return;
        }

        Optional<RBinScrollPos> revealScrollPosition = display.computeRevealScrollPosition(caretPosition);
        revealScrollPosition.ifPresent(this::setScrollPosition);
    }

    /**
     * Reveals scrolling area for current cursor position.
     */
    public void revealCursor() {
        revealPosition(caret.getCaretPosition());
    }

    @Override
    public float suggestWidth() {
        return 0; // TODO
    }

    public void updateScrollBars() {
        display.updateScrollBars();
        redraw();
    }

    public RComponentScrollbar getHorizontalScrollBar() {
        return null; // TODO
    }

    public RComponentScrollbar getVerticalScrollBar() {
        return null; // TODO
    }
}
