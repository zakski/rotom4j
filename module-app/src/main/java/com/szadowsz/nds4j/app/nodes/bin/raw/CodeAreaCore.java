/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.szadowsz.nds4j.app.nodes.bin.raw;

import com.szadowsz.nds4j.app.nodes.bin.raw.swing.CodeAreaCommandHandler;
import com.szadowsz.nds4j.app.nodes.bin.raw.swing.CodeAreaPainterSwing;
import com.szadowsz.nds4j.app.nodes.bin.raw.swing.CodeAreaSwingUtils;
import com.szadowsz.nds4j.file.bin.core.BinaryData;
import com.szadowsz.nds4j.file.bin.core.EmptyBinaryData;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CodeAreaCore implements CodeAreaControl {

    private BinaryData contentData = EmptyBinaryData.INSTANCE;

    private CodeAreaCommandHandler commandHandler;

    private CodeAreaPainter painter;

    private BackgroundPaintMode backgroundPaintMode = BackgroundPaintMode.STRIPED;

    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    private final CodeAreaSelection selection = new CodeAreaSelection();
    private final CodeAreaCaret codeAreaCaret;

    private Charset charset = Charset.forName(CodeAreaSwingUtils.DEFAULT_ENCODING);

    private ClipboardHandlingMode clipboardHandlingMode = ClipboardHandlingMode.PROCESS;

    private EditMode editMode = EditMode.EXPANDING;
    private EditOperation editOperation = EditOperation.OVERWRITE;

    private CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;
    private CodeType codeType = CodeType.HEXADECIMAL;
    private CodeCharactersCase codeCharactersCase = CodeCharactersCase.UPPER;

    private RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    private int minRowPositionLength = 0;
    private int maxRowPositionLength = 0;
    private int wrappingBytesGroupSize = 0;
    private int maxBytesPerRow = 16;

    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.ROW;
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;

    private boolean showMirrorCursor = true;

    private final java.util.List<CodeAreaCaretListener> caretMovedListeners = new ArrayList<>();
    private final List<DataChangedListener> dataChangedListeners = new ArrayList<>();
    private final java.util.List<ScrollingListener> scrollingListeners = new ArrayList<>();
    private final java.util.List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private final List<EditModeChangedListener> editModeChangedListeners = new ArrayList<>();

    /**
     * Creates new instance with provided command handler.
     *
     * @param commandHandler
     * @param codeAreaCaret
     */
    public CodeAreaCore(CodeAreaCommandHandler commandHandler,
                        CodeAreaPainter painter,
                        CodeAreaCaret codeAreaCaret) {
        this.painter = painter;
        this.codeAreaCaret = codeAreaCaret;
   //     init();

        this.commandHandler = commandHandler;
    }

    public void setCommandHandler(CodeAreaCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public void copy() {
        commandHandler.copy();
    }

    public void copyAsCode() {
        commandHandler.copyAsCode();
    }

    @Override
    public void cut() {
        commandHandler.cut();
    }

    @Override
    public void paste() {
        commandHandler.paste();
    }

    public void pasteFromCode() {
        commandHandler.pasteFromCode();
    }

    @Override
    public void delete() {
        commandHandler.delete();
    }

    @Override
    public void selectAll() {
        commandHandler.selectAll();
    }

    @Override
    public void clearSelection() {
        commandHandler.clearSelection();
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
    public CodeAreaCaretPosition getActiveCaretPosition() {
        return codeAreaCaret.getCaretPosition();
    }

    @Override
    public BackgroundPaintMode getBackgroundPaintMode() {
        return backgroundPaintMode;
    }

    @Override
    public CodeAreaColorsProfile getBasicColors() {
        return painter.getBasicColors();
    }

    @Override
    public Charset getCharset() {
        return charset;
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
    public int getCodeOffset() {
        return codeAreaCaret.getCodeOffset();
    }

    @Override
    public CodeType getCodeType() {
        return codeType;
    }

    public CodeAreaCommandHandler getCommandHandler() {
        return commandHandler;
    }

    @Override
    public BinaryData getContentData() {
        return contentData;
    }

    @Override
    public ClipboardHandlingMode getClipboardHandlingMode() {
        return clipboardHandlingMode;
    }

    @Override
    public long getDataPosition() {
        return codeAreaCaret.getDataPosition();
    }

    @Override
    public long getDataSize() {
        return contentData.getDataSize();
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

    public CodeAreaPainter getPainter() {
        return painter;
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
    public boolean canPaste() {
        return commandHandler.canPaste();
    }

    @Override
    public boolean hasSelection() {
        return !selection.isEmpty();
    }

    public boolean isEditable() {
        return editMode != EditMode.READ_ONLY;
    }

    public boolean isInitialized() {
        return painter.isInitialized();
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
    public void setBackgroundPaintMode(BackgroundPaintMode backgroundPaintMode) {
        this.backgroundPaintMode = backgroundPaintMode;
        updateLayout();
    }

    @Override
    public void setBasicColors(CodeAreaColorsProfile colorsProfile) {
        painter.setBasicColors(colorsProfile);
    }

    @Override
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void setClipboardHandlingMode(ClipboardHandlingMode clipboardHandlingMode) {
        this.clipboardHandlingMode = clipboardHandlingMode;
    }

    @Override
    public void setCodeCharactersCase(CodeCharactersCase codeCharactersCase) {
        this.codeCharactersCase = codeCharactersCase;
        updateLayout();
    }

    @Override
    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
        validateCaret();
        updateLayout();
    }

    public void setContentData(BinaryData contentData) {
        this.contentData = contentData == null ? EmptyBinaryData.INSTANCE : contentData;
        notifyDataChanged();
    }

    @Override
    public void setEditMode(EditMode editMode) {
        boolean changed = editMode != getEditMode();
        if (changed) {
            this.editMode = editMode;
            editModeChangedListeners.forEach((listener) -> {
                listener.editModeChanged(editMode, getActiveOperation());
            });
            codeAreaCaret.resetBlink();
            codeAreaCaret.notifyCaretChanged();
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
                listener.editModeChanged(getEditMode(), currentOperation);
            });
            codeAreaCaret.resetBlink();
            codeAreaCaret.notifyCaretChanged();
        }
    }

    @Override
    public void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility) {
        this.horizontalScrollBarVisibility = horizontalScrollBarVisibility;
        reset();
        updateScrollBars();
    }

    @Override
    public void setHorizontalScrollUnit(HorizontalScrollUnit horizontalScrollUnit) {
        this.horizontalScrollUnit = horizontalScrollUnit;
        int charPosition = getScrollPosition().getCharPosition();
        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            getScrollPosition().setCharOffset(0);
        }
        reset();
        getScrollPosition().setCharPosition(charPosition);
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

    public void setPainter(CodeAreaPainter painter) {
        this.painter = painter;
    }

    @Override
    public void setRowWrapping(RowWrappingMode rowWrapping) {
        this.rowWrapping = rowWrapping;
        updateLayout();
    }

    @Override
    public void setScrollPosition(CodeAreaScrollPosition scrollPosition) {
        if (!scrollPosition.equals(this.scrollPosition)) {
            getScrollPosition().setScrollPosition(scrollPosition);
            painter.scrollPositionModified();
            updateScrollBars();
            notifyScrolled();
        }
    }

    @Override
    public void setSelection(SelectionRange selectionRange) {
        this.selection.setRange(selectionRange);
    }

    @Override
    public void setSelection(long start, long end) {
        this.selection.setSelection(start, end);
    }

    @Override
    public void setShowMirrorCursor(boolean showMirrorCursor) {
        this.showMirrorCursor = showMirrorCursor;
        updateLayout();
    }

    @Override
    public void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility) {
        this.verticalScrollBarVisibility = verticalScrollBarVisibility;
        reset();
        updateScrollBars();
    }

    @Override
    public void setVerticalScrollUnit(VerticalScrollUnit verticalScrollUnit) {
        this.verticalScrollUnit = verticalScrollUnit;
        long rowPosition = getScrollPosition().getRowPosition();
        if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            getScrollPosition().setRowOffset(0);
        }
        reset();
        getScrollPosition().setRowPosition(rowPosition);
        updateScrollBars();
        notifyScrolled();
    }

    @Override
    public void setViewMode(CodeAreaViewMode viewMode) {
        if (viewMode != this.viewMode) {
            this.viewMode = viewMode;
            switch (viewMode) {
                case CODE_MATRIX:
                    codeAreaCaret.setSection(CodeAreaSection.CODE_MATRIX);
                    reset();
                    notifyCaretMoved();
                    break;

                case TEXT_PREVIEW:
                    codeAreaCaret.setSection(CodeAreaSection.TEXT_PREVIEW);
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
    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction) {
        return painter.computeMovePosition(position, direction);
    }

    @Override
    public CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection scrollingShift) {
        return painter.computeScrolling(startPosition, scrollingShift);
    }

    @Override
    public CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, CaretOverlapMode overflowMode) {
        return painter.mousePositionToClosestCaretPosition(positionX, positionY, overflowMode);
    }

    protected void notifyCaretMoved() {
        caretMovedListeners.forEach((caretMovedListener) -> {
            caretMovedListener.caretMoved(codeAreaCaret.getCaretPosition());
        });
    }

    /**
     * Notifies component, that the internal data was changed.
     */
    public void notifyDataChanged() {
        dataChangedListeners.forEach(DataChangedListener::dataChanged);
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
    public void addCaretMovedListener(CodeAreaCaretListener caretMovedListener) {
        caretMovedListeners.add(caretMovedListener);
    }

    public void addDataChangedListener(DataChangedListener dataChangedListener) {
        dataChangedListeners.add(dataChangedListener);
    }

    @Override
    public void addEditModeChangedListener(EditModeChangedListener editModeChangedListener) {
        editModeChangedListeners.add(editModeChangedListener);
    }

    @Override
    public void addScrollingListener(ScrollingListener scrollingListener) {
        scrollingListeners.add(scrollingListener);
    }

    @Override
    public void addSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.add(selectionChangedListener);
    }

    @Override
    public void removeCaretMovedListener(CodeAreaCaretListener caretMovedListener) {
        caretMovedListeners.remove(caretMovedListener);
    }

    public void removeDataChangedListener(DataChangedListener dataChangedListener) {
        dataChangedListeners.remove(dataChangedListener);
    }

    @Override
    public void removeEditModeChangedListener(EditModeChangedListener editModeChangedListener) {
        editModeChangedListeners.remove(editModeChangedListener);
    }

    @Override
    public void removeScrollingListener(ScrollingListener scrollingListener) {
        scrollingListeners.remove(scrollingListener);
    }

    @Override
    public void removeSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        selectionChangedListeners.remove(selectionChangedListener);
    }

    public void validateCaret() {
        boolean moved = false;
        if (codeAreaCaret.getDataPosition() > getDataSize()) {
            codeAreaCaret.setDataPosition(getDataSize());
            moved = true;
        }
        if (codeAreaCaret.getSection() == CodeAreaSection.CODE_MATRIX && codeAreaCaret.getCodeOffset() >= codeType.getMaxDigitsForByte()) {
            codeAreaCaret.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
            moved = true;
        }
        if (moved) {
            notifyCaretMoved();
        }
    }

    public void reset() {
        painter.reset();
    }

    @Override
    public void revealCursor() {
        revealPosition(codeAreaCaret.getCaretPosition());
    }

    @Override
    public void revealPosition(CodeAreaCaretPosition caretPosition) {
        if (!isInitialized()) {
            return; // Silently ignore if painter is not yet initialized
        }

        Optional<CodeAreaScrollPosition> revealScrollPosition = painter.computeRevealScrollPosition(caretPosition);
        revealScrollPosition.ifPresent(this::setScrollPosition);
    }

    public void updateLayout(){
        painter.resetLayout();
    }

    public void updateScrollBars() {
        painter.updateScrollBars();
    }

    public void updateScrollPosition(CodeAreaScrollPosition scrollPosition) {
        if (!scrollPosition.equals(this.scrollPosition)) {
            this.scrollPosition.setScrollPosition(scrollPosition);
        }
    }

    public void notifySelectionChanged() {
        selectionChangedListeners.forEach(SelectionChangedListener::selectionChanged);
    }

    public void notifyScrolled() {
        painter.resetLayout();
        scrollingListeners.forEach(ScrollingListener::scrolled);
    }
}
