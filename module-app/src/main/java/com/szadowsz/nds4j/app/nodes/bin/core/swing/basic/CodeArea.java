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
package com.szadowsz.nds4j.app.nodes.bin.core.swing.basic;

import java.awt.Font;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.UIManager;
import javax.swing.border.Border;

import com.szadowsz.nds4j.app.nodes.bin.core.*;
import com.szadowsz.nds4j.app.nodes.bin.core.basic.*;
import com.szadowsz.nds4j.app.nodes.bin.core.swing.*;
import com.szadowsz.nds4j.app.nodes.bin.core.swing.basic.color.BasicCodeAreaColorsProfile;
import com.szadowsz.nds4j.app.nodes.bin.core.swing.basic.color.BasicColorsCapableCodeAreaPainter;

/**
 * Code area component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class CodeArea extends CodeAreaCore implements DefaultCodeArea, CodeAreaSwingControl {

    private CodeAreaPainter painter;

    private final DefaultCodeAreaCaret codeAreaCaret;
    private final CodeAreaSelection selection = new CodeAreaSelection();
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();

    private Charset charset = Charset.forName(CodeAreaSwingUtils.DEFAULT_ENCODING);
    private ClipboardHandlingMode clipboardHandlingMode = ClipboardHandlingMode.PROCESS;

    private EditMode editMode = EditMode.EXPANDING;
    private EditOperation editOperation = EditOperation.OVERWRITE;
    private CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;
    private Font codeFont;
    private BasicBackgroundPaintMode backgroundPaintMode = BasicBackgroundPaintMode.STRIPED;
    private AntialiasingMode antialiasingMode = AntialiasingMode.AUTO;
    private CodeType codeType = CodeType.HEXADECIMAL;
    private CodeCharactersCase codeCharactersCase = CodeCharactersCase.UPPER;
    private boolean showMirrorCursor = true;
    private RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    private int minRowPositionLength = 0;
    private int maxRowPositionLength = 0;
    private int wrappingBytesGroupSize = 0;
    private int maxBytesPerRow = 16;

    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.ROW;
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;

    private final List<CodeAreaCaretListener> caretMovedListeners = new ArrayList<>();
    private final List<ScrollingListener> scrollingListeners = new ArrayList<>();
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private final List<EditModeChangedListener> editModeChangedListeners = new ArrayList<>();

    /**
     * Creates new instance with default command handler and painter.
     */
    public CodeArea() {
        this(DefaultCodeAreaCommandHandler.createDefaultCodeAreaCommandHandlerFactory());
    }

    /**
     * Creates new instance with provided command handler factory method.
     *
     * @param commandHandlerFactory command handler or null for default handler
     */
    public CodeArea(CodeAreaCommandHandler.CodeAreaCommandHandlerFactory commandHandlerFactory) {
        super(commandHandlerFactory);

        painter = new DefaultCodeAreaPainter(this);
        codeAreaCaret = new DefaultCodeAreaCaret(this::notifyCaretChanged);
        painter.attach();
        init();
    }

    private void init() {
        UIManager.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            resetColors();
        });
        codeAreaCaret.setSection(BasicCodeAreaSection.CODE_MATRIX);
    }

    public CodeAreaPainter getPainter() {
        return painter;
    }

    public void setPainter(CodeAreaPainter painter) {
        CodeAreaUtils.requireNonNull(painter);

        this.painter.detach();
        this.painter = painter;
        painter.attach();
        reset();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        painter.paintComponent(g);
    }

    @Override
    public void updateUI() {
        // TODO super.updateUI();
        if (getBorder() == null) {
            super.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextAreaUI.border"));
        }
        if (painter != null) {
            painter.rebuildColors();
            painter.resetFont();
            painter.resetColors();
        }
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(border);
        updateLayout();
    }

    @Override
    public DefaultCodeAreaCaret getCodeAreaCaret() {
        return codeAreaCaret;
    }

    @Override
    public boolean isShowMirrorCursor() {
        return showMirrorCursor;
    }

    @Override
    public void setShowMirrorCursor(boolean showMirrorCursor) {
        this.showMirrorCursor = showMirrorCursor;
        updateLayout();
    }

    @Override
    public int getMinRowPositionLength() {
        return minRowPositionLength;
    }

    @Override
    public void setMinRowPositionLength(int minRowPositionLength) {
        this.minRowPositionLength = minRowPositionLength;
        updateLayout();
    }

    @Override
    public int getMaxRowPositionLength() {
        return maxRowPositionLength;
    }

    @Override
    public void setMaxRowPositionLength(int maxRowPositionLength) {
        this.maxRowPositionLength = maxRowPositionLength;
        updateLayout();
    }

    public boolean isInitialized() {
        return painter.isInitialized();
    }

    @Override
    public long getDataPosition() {
        return codeAreaCaret.getDataPosition();
    }

    @Override
    public int getCodeOffset() {
        return codeAreaCaret.getCodeOffset();
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
    public int getMouseCursorShape(int positionX, int positionY) {
        return painter.getMouseCursorShape(positionX, positionY);
    }

        @Override
    public CodeCharactersCase getCodeCharactersCase() {
        return codeCharactersCase;
    }

    @Override
    public void setCodeCharactersCase(CodeCharactersCase codeCharactersCase) {
        this.codeCharactersCase = codeCharactersCase;
        updateLayout();
    }

    @Override
    public void resetColors() {
        painter.resetColors();
        repaint();
    }

    @Override
    public CodeAreaViewMode getViewMode() {
        return viewMode;
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
    public CodeType getCodeType() {
        return codeType;
    }

    @Override
    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
        validateCaret();
        updateLayout();
    }

    public void validateCaret() {
        boolean moved = false;
        if (codeAreaCaret.getDataPosition() > getDataSize()) {
            codeAreaCaret.setDataPosition(getDataSize());
            moved = true;
        }
        if (codeAreaCaret.getSection() == BasicCodeAreaSection.CODE_MATRIX && codeAreaCaret.getCodeOffset() >= codeType.getMaxDigitsForByte()) {
            codeAreaCaret.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
            moved = true;
        }

        if (moved) {
            notifyCaretMoved();
        }
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

    public void revealPosition(long dataPosition, int dataOffset, CodeAreaSection section) {
        revealPosition(new DefaultCodeAreaCaretPosition(dataPosition, dataOffset, section));
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

    public void centerOnPosition(long dataPosition, int dataOffset, CodeAreaSection section) {
        centerOnPosition(new DefaultCodeAreaCaretPosition(dataPosition, dataOffset, section));
    }

        @Override
    public CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, CaretOverlapMode overflowMode) {
        return painter.mousePositionToClosestCaretPosition(positionX, positionY, overflowMode);
    }

        @Override
    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction) {
        return painter.computeMovePosition(position, direction);
    }

        @Override
    public CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection scrollingShift) {
        return painter.computeScrolling(startPosition, scrollingShift);
    }

    public void updateScrollBars() {
        painter.updateScrollBars();
        repaint();
    }

        @Override
    public CodeAreaScrollPosition getScrollPosition() {
        return scrollPosition;
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
    public void updateScrollPosition(CodeAreaScrollPosition scrollPosition) {
        if (!scrollPosition.equals(this.scrollPosition)) {
            this.scrollPosition.setScrollPosition(scrollPosition);
            repaint();
            notifyScrolled();
        }
    }

        @Override
    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    @Override
    public void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility) {
        this.verticalScrollBarVisibility = verticalScrollBarVisibility;
        resetPainter();
        updateScrollBars();
    }

        @Override
    public VerticalScrollUnit getVerticalScrollUnit() {
        return verticalScrollUnit;
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
    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    @Override
    public void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility) {
        this.horizontalScrollBarVisibility = horizontalScrollBarVisibility;
        resetPainter();
        updateScrollBars();
    }

        @Override
    public HorizontalScrollUnit getHorizontalScrollUnit() {
        return horizontalScrollUnit;
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
    public void reset() {
        painter.reset();
    }

    @Override
    public void updateLayout() {
        if (painter != null) {
            painter.resetLayout();
        }
        repaint();
    }

    @Override
    public void repaint() {
        super.repaint();
    }

    @Override
    public void resetPainter() {
        painter.reset();
    }

    protected void notifyCaretChanged() {
        if (painter != null) {
            painter.resetCaret();
        }
        repaint();
    }

    @Override
    public void notifyDataChanged() {
        super.notifyDataChanged();
        updateLayout();
    }

        @Override
    public AntialiasingMode getAntialiasingMode() {
        return antialiasingMode;
    }

    @Override
    public void setAntialiasingMode(AntialiasingMode antialiasingMode) {
        this.antialiasingMode = antialiasingMode;
        reset();
        repaint();
    }

        @Override
    public SelectionRange getSelection() {
        return selection.getRange();
    }

    @Override
    public void setSelection(SelectionRange selectionRange) {
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
    public void clearSelection() {
        this.selection.clearSelection();
        notifySelectionChanged();
        repaint();
    }

    @Override
    public boolean hasSelection() {
        return !selection.isEmpty();
    }

        @Override
    public CodeAreaSelection getSelectionHandler() {
        return selection;
    }

        @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public void setCharset(Charset charset) {
        CodeAreaUtils.requireNonNull(charset);

        this.charset = charset;
        reset();
        repaint();
    }

    @Override
    public boolean isEditable() {
        return editMode != EditMode.READ_ONLY;
    }

        @Override
    public EditMode getEditMode() {
        return editMode;
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
    public EditOperation getEditOperation() {
        return editOperation;
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
    public ClipboardHandlingMode getClipboardHandlingMode() {
        return clipboardHandlingMode;
    }

    @Override
    public void setClipboardHandlingMode(ClipboardHandlingMode clipboardHandlingMode) {
        this.clipboardHandlingMode = clipboardHandlingMode;
    }

        @Override
    public Font getCodeFont() {
        return codeFont == null ? super.getFont() : codeFont;
    }

    @Override
    public void setCodeFont(Font codeFont) {
        this.codeFont = codeFont;
        painter.resetFont();
        repaint();
    }

    @Override
    public BasicBackgroundPaintMode getBackgroundPaintMode() {
        return backgroundPaintMode;
    }

    @Override
    public void setBackgroundPaintMode(BasicBackgroundPaintMode backgroundPaintMode) {
        this.backgroundPaintMode = backgroundPaintMode;
        updateLayout();
    }

    @Override
    public RowWrappingMode getRowWrapping() {
        return rowWrapping;
    }

    @Override
    public void setRowWrapping(RowWrappingMode rowWrapping) {
        this.rowWrapping = rowWrapping;
        updateLayout();
    }

    @Override
    public int getWrappingBytesGroupSize() {
        return wrappingBytesGroupSize;
    }

    @Override
    public void setWrappingBytesGroupSize(int groupSize) {
        wrappingBytesGroupSize = groupSize;
        updateLayout();
    }

    @Override
    public int getMaxBytesPerRow() {
        return maxBytesPerRow;
    }

    @Override
    public void setMaxBytesPerRow(int maxBytesPerRow) {
        this.maxBytesPerRow = maxBytesPerRow;
        updateLayout();
    }

    @Override
    public Optional<BasicCodeAreaColorsProfile> getBasicColors() {
        if (painter instanceof BasicColorsCapableCodeAreaPainter) {
            return Optional.of(((BasicColorsCapableCodeAreaPainter) painter).getBasicColors());
        }
        return Optional.empty();
    }

    @Override
    public void setBasicColors(BasicCodeAreaColorsProfile colorsProfile) {
        if (painter instanceof BasicColorsCapableCodeAreaPainter) {
            ((BasicColorsCapableCodeAreaPainter) painter).setBasicColors(colorsProfile);
        }
    }

    protected void notifySelectionChanged() {
        selectionChangedListeners.forEach(SelectionChangedListener::selectionChanged);
    }

    protected void notifyCaretMoved() {
        caretMovedListeners.forEach((caretMovedListener) -> {
            caretMovedListener.caretMoved(codeAreaCaret.getCaretPosition());
        });
    }

    protected void notifyScrolled() {
        painter.resetLayout();
        scrollingListeners.forEach(ScrollingListener::scrolled);
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
    public void addCaretMovedListener(CodeAreaCaretListener caretMovedListener) {
        caretMovedListeners.add(caretMovedListener);
    }

    @Override
    public void removeCaretMovedListener(CodeAreaCaretListener caretMovedListener) {
        caretMovedListeners.remove(caretMovedListener);
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
    public void addEditModeChangedListener(EditModeChangedListener editModeChangedListener) {
        editModeChangedListeners.add(editModeChangedListener);
    }

    @Override
    public void removeEditModeChangedListener(EditModeChangedListener editModeChangedListener) {
        editModeChangedListeners.remove(editModeChangedListener);
    }
}
