package com.szadowsz.gui.component.bined;


import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.binary.*;
import com.szadowsz.gui.component.binary.basic.*;
import com.szadowsz.gui.component.binary.basic.color.BasicCodeAreaColorsProfile;
import com.szadowsz.gui.component.binary.basic.color.BasicColorsCapableCodeAreaPainter;
import com.szadowsz.gui.component.binary.swing.CodeAreaPainter;
import com.szadowsz.gui.component.binary.swing.CodeAreaSwingUtils;
import com.szadowsz.gui.component.binary.swing.basic.AntialiasingMode;
import com.szadowsz.gui.component.binary.swing.basic.DefaultCodeAreaCaret;
import com.szadowsz.gui.component.bined.basic.DefaultCodeArea;
import com.szadowsz.gui.component.bined.command.CodeAreaCommandHandler;
import com.szadowsz.gui.component.group.folder.RFolder;

import java.awt.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Binary data viewer/editor component.
 */
public class RBinedArea extends RBinedAreaCore implements DefaultCodeArea { // TODO Determine Exact Relationship

    // Listeners TODO, neccessary in my ui?
    private final List<CodeAreaCaretListener> caretMovedListeners = new ArrayList<>();
    private final List<EditModeChangedListener> editModeChangedListeners = new ArrayList<>();
    private final List<ScrollingListener> scrollingListeners = new ArrayList<>();
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();

    protected CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;

    // Character Config
    protected Charset charset = Charset.forName(CodeAreaSwingUtils.DEFAULT_ENCODING);
    protected CodeCharactersCase codeCharactersCase = CodeCharactersCase.UPPER;
    protected CodeType codeType = CodeType.HEXADECIMAL;
    protected Font codeFont;

    // Row Layout Config
    protected int maxBytesPerRow = 16;
    protected int minRowPositionLength = 0;
    protected int maxRowPositionLength = 0;
    protected int wrappingBytesGroupSize = 0;
    protected RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;

    // TODO name this section
    protected final CodeAreaSelection selection = new CodeAreaSelection();
    protected final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    protected ClipboardHandlingMode clipboardHandlingMode = ClipboardHandlingMode.PROCESS;

    // Drawing Config
    protected AntialiasingMode antialiasingMode = AntialiasingMode.AUTO;
    protected CodeAreaPainter painter;
    protected BasicBackgroundPaintMode backgroundPaintMode = BasicBackgroundPaintMode.STRIPED;

    // Scrollbar Config
    protected ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    protected VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.ROW;
    protected ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    protected HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;

    // Edit Op
    protected EditMode editMode = EditMode.EXPANDING;
    protected EditOperation editOperation = EditOperation.OVERWRITE;

    // Cursor Caret
    protected final DefaultCodeAreaCaret codeAreaCaret;
    protected boolean showMirrorCursor = true;


    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parentFolder the parent component folder reference // TODO consider if needed
     */
    protected RBinedArea(RotomGui gui, String path, RFolder parentFolder, CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        super(gui, path, parentFolder,commandHandlerFactory);
        codeAreaCaret = new DefaultCodeAreaCaret(this::notifyCaretChanged);
    }

    @Override
    public CodeAreaCaretPosition getActiveCaretPosition() {
        return codeAreaCaret.getCaretPosition();
    }

    @Override
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
                throw CodeAreaUtils.getInvalidTypeException(editMode);
        }
    }

    @Override
    public CodeAreaSection getActiveSection() {
        return codeAreaCaret.getSection();
    }

    @Override
    public AntialiasingMode getAntialiasingMode() {
        return antialiasingMode;
    }

    @Override
    public BasicBackgroundPaintMode getBackgroundPaintMode() {
        return backgroundPaintMode;
    }

    @Override
    public Optional<BasicCodeAreaColorsProfile> getBasicColors() {
        if (painter instanceof BasicColorsCapableCodeAreaPainter) {
            return Optional.of(((BasicColorsCapableCodeAreaPainter) painter).getBasicColors());
        }
        return Optional.empty();
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public ClipboardHandlingMode getClipboardHandlingMode() {
        return clipboardHandlingMode;
    }

    @Override
    public CodeAreaCaret getCodeAreaCaret() {
        return codeAreaCaret;
    }

    @Override
    public CodeCharactersCase getCodeCharactersCase() {
        return codeCharactersCase;
    }

    @Override
    public Font getCodeFont() {
        return codeFont == null ? super.getFont() : codeFont;
    }

    @Override
    public int getCodeOffset() {
        return codeAreaCaret.getCodeOffset();
    }

    @Override
    public CodeType getCodeType() {
        return codeType;
    }

    @Override
    public long getDataPosition() {
        return codeAreaCaret.getDataPosition();
    }

    @Override
    public EditMode getEditMode() {
        return editMode;
    }

    @Override
    public EditOperation getEditOperation() {
        return editOperation;
    }

    @Override
    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    @Override
    public HorizontalScrollUnit getHorizontalScrollUnit() {
        return horizontalScrollUnit;
    }

    @Override
    public int getMaxBytesPerRow() {
        return maxBytesPerRow;
    }

    @Override
    public int getMaxRowPositionLength() {
        return maxRowPositionLength;
    }

    @Override
    public int getMinRowPositionLength() {
        return minRowPositionLength;
    }

    @Override
    public int getMouseCursorShape(int positionX, int positionY) {
        return painter.getMouseCursorShape(positionX, positionY);
    }

    @Override
    public RowWrappingMode getRowWrapping() {
        return rowWrapping;
    }

    @Override
    public CodeAreaScrollPosition getScrollPosition() {
        return scrollPosition;
    }

    @Override
    public SelectionRange getSelection() {
        return selection.getRange();
    }

    @Override
    public CodeAreaSelection getSelectionHandler() {
        return selection;
    }

    @Override
    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    @Override
    public VerticalScrollUnit getVerticalScrollUnit() {
        return verticalScrollUnit;
    }

    @Override
    public CodeAreaViewMode getViewMode() {
        return viewMode;
    }

    @Override
    public int getWrappingBytesGroupSize() {
        return wrappingBytesGroupSize;
    }

    @Override
    public boolean isShowMirrorCursor() {
        return showMirrorCursor;
    }

    @Override
    public void setActiveCaretPosition(CodeAreaCaretPosition caretPosition) {
        codeAreaCaret.setCaretPosition(caretPosition);
        notifyCaretMoved();
    }

    @Override
    public void setActiveCaretPosition(long dataPosition) {
        codeAreaCaret.setCaretPosition(dataPosition);
        notifyCaretMoved();

    }

    @Override
    public void setActiveCaretPosition(long dataPosition, int codeOffset) {
        codeAreaCaret.setCaretPosition(dataPosition, codeOffset);
        notifyCaretMoved();
    }

    @Override
    public void setAntialiasingMode(AntialiasingMode antialiasingMode) {
        this.antialiasingMode = antialiasingMode;
        reset();
        repaint();
    }

    @Override
    public void setBackgroundPaintMode(BasicBackgroundPaintMode borderPaintMode) {
        this.backgroundPaintMode = backgroundPaintMode;
        updateLayout();
    }

    @Override
    public void setBasicColors(BasicCodeAreaColorsProfile colorsProfile) {
        if (painter instanceof BasicColorsCapableCodeAreaPainter) {
            ((BasicColorsCapableCodeAreaPainter) painter).setBasicColors(colorsProfile);
        }
    }

    @Override
    public void setCharset(Charset charset) {
        this.charset = CodeAreaUtils.requireNonNull(charset);;
        reset();
        repaint();
    }

    @Override
    public void setClipboardHandlingMode(ClipboardHandlingMode handlingMode) {
        this.clipboardHandlingMode = clipboardHandlingMode;
    }

    @Override
    public void setCodeCharactersCase(CodeCharactersCase codeCharactersCase) {
        this.codeCharactersCase = codeCharactersCase;
        updateLayout();
    }

    @Override
    public void setCodeFont(Font codeFont) {
        this.codeFont = codeFont;
        painter.resetFont();
        repaint();
    }

    @Override
    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
        validateCaret();
        updateLayout();
    }

    @Override
    public void setEditMode(EditMode editMode) {
        boolean changed = editMode != this.editMode;
        this.editMode = editMode;
        if (changed) {
            editModeChangedListeners.forEach((listener) -> {
                listener.editModeChanged(editMode, getActiveOperation());
            });
            codeAreaCaret.resetBlink();
            notifyCaretChanged();
            repaint();
        }
    }

    @Override
    public void setEditOperation(EditOperation editOperation) {
        EditOperation previousOperation = getActiveOperation();
        this.editOperation = editOperation;
        EditOperation currentOperation = getActiveOperation();
        boolean changed = previousOperation != currentOperation;
        if (changed) {
            editModeChangedListeners.forEach((listener) -> {
                listener.editModeChanged(editMode, currentOperation);
            });
            codeAreaCaret.resetBlink();
            notifyCaretChanged();
            repaint();
        }
    }

    @Override
    public void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility) {
        this.horizontalScrollBarVisibility = horizontalScrollBarVisibility;
        resetPainter();
        updateScrollBars();
    }

    @Override
    public void setHorizontalScrollUnit(HorizontalScrollUnit horizontalScrollUnit) {
        this.horizontalScrollUnit = horizontalScrollUnit;
        int charPosition = scrollPosition.getCharPosition();
        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            scrollPosition.setCharOffset(0);
        }
        resetPainter();
        scrollPosition.setCharPosition(charPosition);
        updateScrollBars();
        notifyScrolled();
    }

    @Override
    public void setMaxBytesPerRow(int maxBytesPerRow) {
        this.maxBytesPerRow = maxBytesPerRow;
        updateLayout();
    }

    @Override
    public void setMaxRowPositionLength(int maxRowPositionLength) {
        this.maxRowPositionLength = maxRowPositionLength;
        updateLayout();
    }

    @Override
    public void setMinRowPositionLength(int minRowPositionLength) {
        this.minRowPositionLength = minRowPositionLength;
        updateLayout();
    }

    @Override
    public void setRowWrapping(RowWrappingMode rowWrapping) {
        this.rowWrapping = rowWrapping;
        updateLayout();
    }

    @Override
    public void setScrollPosition(CodeAreaScrollPosition scrollPosition) {
        if (!scrollPosition.equals(this.scrollPosition)) {
            this.scrollPosition.setScrollPosition(scrollPosition);
            painter.scrollPositionModified();
            updateScrollBars();
            notifyScrolled();
        }
    }

    @Override
    public void setSelection(SelectionRange selection) {
        this.selection.setRange(CodeAreaUtils.requireNonNull(selectionRange));
        notifySelectionChanged();
        repaint();
    }

    @Override
    public void setSelection(long start, long end) {
        this.selection.setSelection(start, end);
        notifySelectionChanged();
        repaint();
    }

    @Override
    public void setShowMirrorCursor(boolean showMirrorCursor) {
        this.showMirrorCursor = showMirrorCursor;
        updateLayout();
    }

    @Override
    public void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility) {
        this.verticalScrollBarVisibility = verticalScrollBarVisibility;
        resetPainter();
        updateScrollBars();
    }

    @Override
    public void setVerticalScrollUnit(VerticalScrollUnit verticalScrollUnit) {
        this.verticalScrollUnit = verticalScrollUnit;
        long rowPosition = scrollPosition.getRowPosition();
        if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            scrollPosition.setRowOffset(0);
        }
        resetPainter();
        scrollPosition.setRowPosition(rowPosition);
        updateScrollBars();
        notifyScrolled();
    }

    @Override
    public void setViewMode(CodeAreaViewMode viewMode) {
        if (viewMode != this.viewMode) {
            this.viewMode = viewMode;
            switch (viewMode) {
                case CODE_MATRIX:
                    codeAreaCaret.setSection(BasicCodeAreaSection.CODE_MATRIX);
                    reset();
                    notifyCaretMoved();
                    break;

                case TEXT_PREVIEW:
                    codeAreaCaret.setSection(BasicCodeAreaSection.TEXT_PREVIEW);
                    reset();
                    notifyCaretMoved();
                    break;
                default:
                    reset();
                    break;
            }
            updateLayout();
        }
    }

    @Override
    public void setWrappingBytesGroupSize(int groupSize) {
        wrappingBytesGroupSize = groupSize;
        updateLayout();
    }

    @Override
    public void addCaretMovedListener(CodeAreaCaretListener caretMovedListener) {
        caretMovedListeners.add(caretMovedListener);
    }

    @Override
    public void removeCaretMovedListener(CodeAreaCaretListener caretMovedListener) {
        caretMovedListeners.remove(caretMovedListener);
    }

    @Override
    public void addEditModeChangedListener(EditModeChangedListener editModeChangedListener) {
        editModeChangedListeners.add(editModeChangedListener);
    }

    @Override
    public void removeEditModeChangedListener(EditModeChangedListener editModeChangedListener) {
        editModeChangedListeners.remove(editModeChangedListener);
    }

    @Override
    public void addScrollingListener(ScrollingListener scrollingListener) {
        scrollingListeners.add(scrollingListener);
    }

    @Override
    public void removeScrollingListener(ScrollingListener scrollingListener) {
        scrollingListeners.remove(scrollingListener);
    }

    @Override
    public void addSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.add(selectionChangedListener);
    }

    @Override
    public void removeSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.remove(selectionChangedListener);
    }

    @Override
    public void revealCursor() {
        revealPosition(codeAreaCaret.getCaretPosition());
    }

    @Override
    public void revealPosition(CodeAreaCaretPosition caretPosition) {
        if (!isInitialized()) {
            // Silently ignore if painter is not yet initialized
            return;
        }

        Optional<CodeAreaScrollPosition> revealScrollPosition = painter.computeRevealScrollPosition(caretPosition);
        revealScrollPosition.ifPresent(this::setScrollPosition);
    }

    @Override
    public void centerOnCursor() {
        centerOnPosition(codeAreaCaret.getCaretPosition());
    }

    @Override
    public void centerOnPosition(CodeAreaCaretPosition caretPosition) {
        if (!isInitialized()) {
            // Silently ignore if painter is not yet initialized
            return;
        }

        Optional<CodeAreaScrollPosition> centerOnScrollPosition = painter.computeCenterOnScrollPosition(caretPosition);
        centerOnScrollPosition.ifPresent(this::setScrollPosition);
    }

    @Override
    public CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection direction) {
        return painter.computeScrolling(startPosition, scrollingShift);
    }

    @Override
    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction) {
        return painter.computeMovePosition(position, direction);
    }

    @Override
    public CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, CaretOverlapMode overflowMode) {
        return painter.mousePositionToClosestCaretPosition(positionX, positionY, overflowMode);
    }
}
