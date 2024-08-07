package com.szadowsz.nds4j.app.nodes.bin.raw.swing;

import com.szadowsz.nds4j.app.nodes.bin.raw.*;
import com.szadowsz.nds4j.file.bin.core.BinaryData;

import javax.accessibility.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.nio.charset.Charset;

/**
 * Binary viewer/editor component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaSwing extends JTextComponent implements CodeAreaSwingControl, Accessible {

    private CodeAreaPainterSwing painter;

    private Font codeFont;
     private AntialiasingMode antialiasingMode = AntialiasingMode.AUTO;

    protected CodeAreaCore core;


    /**
     * Creates new instance
     */
    public CodeAreaSwing() {
        super();
        CodeAreaCommandHandler.CodeAreaCommandHandlerFactory factory = CodeAreaCommandHandler.createDefaultCodeAreaCommandHandlerFactory();
        this.core = new CodeAreaCore(factory.createCommandHandler(this),
                new CodeAreaPainterSwing(this),
                new CodeAreaCaret(this::notifyCaretChanged)
        );
        painter = (CodeAreaPainterSwing) core.getPainter();
        painter.attach();
        init();
    }

    private void init() {
        enableEvents(AWTEvent.KEY_EVENT_MASK);
        setName("CodeArea");
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        registerControlListeners();
        UIManager.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            resetColors();
        });
        getCodeAreaCaret().setSection(CodeAreaSection.CODE_MATRIX);
    }

    private void registerControlListeners() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateLayout();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                getCommandHandler().keyTyped(keyEvent);
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                getCommandHandler().keyPressed(keyEvent);
            }
        });

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    protected void notifyCaretChanged() {
        if (painter != null) {
            painter.resetCaret();
        }
        repaint();
    }

    public void notifyDataChanged() {
        core.notifyDataChanged();
        updateLayout();
    }

    protected void notifyScrolled() {
       core.notifyScrolled();
    }

    protected void notifySelectionChanged() {
        core.notifySelectionChanged();
    }

    public void updateLayout(){
        core.updateLayout();
        repaint();
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new CodeAreaSwing.AccessibleComponent();
        }
        return accessibleContext;
    }

    @Override
    public EditOperation getActiveOperation() {
        return core.getActiveOperation();
    }

    @Override
    public CodeAreaSection getActiveSection() {
        return core.getActiveSection();
    }

    @Override
    public CodeAreaCaretPosition getActiveCaretPosition() {
        return core.getActiveCaretPosition();
    }

    @Override
    public AntialiasingMode getAntialiasingMode() {
        return antialiasingMode;
    }

    @Override
    public BackgroundPaintMode getBackgroundPaintMode() {
        return core.getBackgroundPaintMode();
    }

    @Override
    public CodeAreaColorsProfile getBasicColors() {
        return core.getBasicColors();
    }

    @Override
    public Charset getCharset() {
        return core.getCharset();
    }

    @Override
    public ClipboardHandlingMode getClipboardHandlingMode() {
        return core.getClipboardHandlingMode();
    }

    @Override
    public CodeAreaCaret getCodeAreaCaret() {
        return core.getCodeAreaCaret();
    }

    @Override
    public CodeCharactersCase getCodeCharactersCase() {
        return core.getCodeCharactersCase();
    }

    @Override
    public Font getCodeFont() {
        return codeFont == null ? super.getFont() : codeFont;
    }

    @Override
    public int getCodeOffset() {
        return core.getCodeOffset();
    }

    @Override
    public CodeType getCodeType() {
        return core.getCodeType();
    }

    public CodeAreaCommandHandler getCommandHandler() {
        return core.getCommandHandler();
    }

    @Override
    public BinaryData getContentData() {
        return core.getContentData();
    }

    @Override
    public long getDataPosition() {
        return core.getDataPosition();
    }

    @Override
    public long getDataSize() {
        return core.getDataSize();
    }

    @Override
    public EditMode getEditMode() {
        return core.getEditMode();
    }

    @Override
    public EditOperation getEditOperation() {
        return core.getEditOperation();
    }

    @Override
    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return core.getHorizontalScrollBarVisibility();
    }

    @Override
    public HorizontalScrollUnit getHorizontalScrollUnit() {
        return core.getHorizontalScrollUnit();
    }

    @Override
    public int getMaxBytesPerRow() {
        return core.getMaxBytesPerRow();
    }

    @Override
    public int getMaxRowPositionLength() {
        return core.getMaxRowPositionLength();
    }

    @Override
    public int getMinRowPositionLength() {
        return core.getMinRowPositionLength();
    }

    @Override
    public int getMouseCursorShape(int positionX, int positionY) {
        return core.getMouseCursorShape(positionX, positionY);
    }

    public CodeAreaPainter getPainter() {
        return painter;
    }

    @Override
    public RowWrappingMode getRowWrapping() {
        return core.getRowWrapping();
    }

    @Override
    public CodeAreaScrollPosition getScrollPosition() {
        return core.getScrollPosition();
    }

    @Override
    public SelectionRange getSelection() {
        return core.getSelection();
    }

    @Override
    public CodeAreaSelection getSelectionHandler() {
        return core.getSelectionHandler();
    }

    @Override
    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return core.getVerticalScrollBarVisibility();
    }

    @Override
    public VerticalScrollUnit getVerticalScrollUnit() {
        return core.getVerticalScrollUnit();
    }

    @Override
    public CodeAreaViewMode getViewMode() {
        return core.getViewMode();
    }

    @Override
    public int getWrappingBytesGroupSize() {
        return core.getWrappingBytesGroupSize();
    }

    @Override
    public boolean canPaste() {
        return core.canPaste();
    }

    @Override
    public boolean hasSelection() {
        return core.hasSelection();
    }

    public boolean isInitialized() {
        return core.isInitialized();
    }

    @Override
    public boolean isEditable() {
        return core.isEditable();
    }

    @Override
    public boolean isShowMirrorCursor() {
        return core.isShowMirrorCursor();
    }

    @Override
    public void setActiveCaretPosition(CodeAreaCaretPosition caretPosition) {
        core.setActiveCaretPosition(caretPosition);
    }

    @Override
    public void setActiveCaretPosition(long dataPosition) {
        core.setActiveCaretPosition(dataPosition);
    }

    @Override
    public void setActiveCaretPosition(long dataPosition, int codeOffset) {
        core.setActiveCaretPosition(dataPosition, codeOffset);
    }

    @Override
    public void setAntialiasingMode(AntialiasingMode antialiasingMode) {
        this.antialiasingMode = antialiasingMode;
        reset();
        repaint();
    }

    @Override
    public void setBackgroundPaintMode(BackgroundPaintMode backgroundPaintMode) {
        core.setBackgroundPaintMode(backgroundPaintMode);
        repaint();
    }

    @Override
    public void setBasicColors(CodeAreaColorsProfile colorsProfile) {
        core.setBasicColors(colorsProfile);
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(border);
        updateLayout();
    }

    @Override
    public void setCharset(Charset charset) {
       core.setCharset(charset);
       reset();
       repaint();
    }

    @Override
    public void setClipboardHandlingMode(ClipboardHandlingMode clipboardHandlingMode) {
        core.setClipboardHandlingMode(clipboardHandlingMode);
    }

    @Override
    public void setCodeCharactersCase(CodeCharactersCase codeCharactersCase) {
        core.setCodeCharactersCase(codeCharactersCase);
        repaint();
    }

    @Override
    public void setCodeFont(Font codeFont) {
        this.codeFont = codeFont;
        painter.resetFont();
        repaint();
    }

    @Override
    public void setCodeType(CodeType codeType) {
        core.setCodeType(codeType);
        repaint();
    }

    public void setCommandHandler(CodeAreaCommandHandler commandHandler) {
        core.setCommandHandler(commandHandler);
    }

    public void setContentData(BinaryData contentData) {
        core.setContentData(contentData);
        repaint();
    }

    @Override
    public void setEditMode(EditMode editMode) {
        core.setEditMode(editMode);
    }

    @Override
    public void setEditOperation(EditOperation editOperation) {
        core.setEditOperation(editOperation);
    }

    @Override
    public void setHorizontalScrollBarVisibility(ScrollBarVisibility horizontalScrollBarVisibility) {
        core.setHorizontalScrollBarVisibility(horizontalScrollBarVisibility);
        repaint();
    }

    @Override
    public void setHorizontalScrollUnit(HorizontalScrollUnit horizontalScrollUnit) {
        core.setHorizontalScrollUnit(horizontalScrollUnit);
        repaint();
    }

    @Override
    public void setMaxBytesPerRow(int maxBytesPerRow) {
        core.setMaxBytesPerRow(maxBytesPerRow);
        repaint();
    }

    @Override
    public void setMaxRowPositionLength(int maxRowPositionLength) {
        core.setMaxRowPositionLength(maxRowPositionLength);
        repaint();
    }

    @Override
    public void setMinRowPositionLength(int minRowPositionLength) {
        core.setMinRowPositionLength(minRowPositionLength);
        repaint();
    }

    public void setPainter(CodeAreaPainterSwing painter) {
        this.painter.detach();
        core.setPainter(painter);
        this.painter = (CodeAreaPainterSwing) core.getPainter();
        painter.attach();
        reset();
        repaint();
    }

    @Override
    public void setRowWrapping(RowWrappingMode rowWrapping) {
        core.setRowWrapping(rowWrapping);
        repaint();
    }

    @Override
    public void setScrollPosition(CodeAreaScrollPosition scrollPosition) {
        if (!scrollPosition.equals(this.getScrollPosition())) {
            getScrollPosition().setScrollPosition(scrollPosition);
            painter.scrollPositionModified();
            updateScrollBars();
            notifyScrolled();
        }
    }

    @Override
    public void setSelection(SelectionRange selectionRange) {
        core.setSelection(selectionRange);
        notifySelectionChanged();
        repaint();
    }

    @Override
    public void setSelection(long start, long end) {
        core.setSelection(start, end);
        notifySelectionChanged();
        repaint();
    }

    @Override
    public void setShowMirrorCursor(boolean showMirrorCursor) {
        core.setShowMirrorCursor(showMirrorCursor);
        repaint();
    }

    @Override
    public void setVerticalScrollBarVisibility(ScrollBarVisibility verticalScrollBarVisibility) {
        core.setVerticalScrollBarVisibility(verticalScrollBarVisibility);
        repaint();
    }

    @Override
    public void setVerticalScrollUnit(VerticalScrollUnit verticalScrollUnit) {
        setVerticalScrollUnit(verticalScrollUnit);
        repaint();
    }

    @Override
    public void setViewMode(CodeAreaViewMode viewMode) {
        if (viewMode != this.getViewMode()) {
            core.setViewMode(viewMode);
            repaint();
        }
    }

    @Override
    public void setWrappingBytesGroupSize(int groupSize) {
        core.setWrappingBytesGroupSize(groupSize);
        repaint();
    }

    @Override
    public void addCaretMovedListener(CodeAreaCaretListener caretMovedListener) {
        core.addCaretMovedListener(caretMovedListener);
    }

    public void addDataChangedListener(DataChangedListener dataChangedListener) {
        core.addDataChangedListener(dataChangedListener);
    }

    @Override
    public void addEditModeChangedListener(EditModeChangedListener editModeChangedListener) {
        core.addEditModeChangedListener(editModeChangedListener);
    }

    @Override
    public void addScrollingListener(ScrollingListener scrollingListener) {
        core.addScrollingListener(scrollingListener);
    }

    @Override
    public void addSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        core.addSelectionChangedListener(selectionChangedListener);
    }

    @Override
    public void centerOnCursor() {
        core.centerOnCursor();
    }

    @Override
    public void centerOnPosition(CodeAreaCaretPosition caretPosition) {
        core.centerOnPosition(caretPosition);
    }

    public void centerOnPosition(long dataPosition, int dataOffset, CodeAreaSection section) {
        centerOnPosition(new CodeAreaCaretPosition(dataPosition, dataOffset, section));
    }

    @Override
    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition position, MovementDirection direction) {
        return core.computeMovePosition(position, direction);
    }

    @Override
    public CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition startPosition, ScrollingDirection scrollingShift) {
        return core.computeScrolling(startPosition, scrollingShift);
    }

    @Override
    public CodeAreaCaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, CaretOverlapMode overflowMode) {
        return core.mousePositionToClosestCaretPosition(positionX, positionY, overflowMode);
    }

    @Override
    public void paintComponent(Graphics g) {
        painter.paintComponent(g);
    }

    @Override
    public void revealCursor() {
        core.revealCursor();
    }

    @Override
    public void revealPosition(CodeAreaCaretPosition caretPosition) {
        core.revealPosition(caretPosition);
    }

    public void revealPosition(long dataPosition, int dataOffset, CodeAreaSection section) {
        revealPosition(new CodeAreaCaretPosition(dataPosition, dataOffset, section));
    }

    public void updateScrollBars() {
        painter.updateScrollBars();
        repaint();
    }

    @Override
    public void updateScrollPosition(CodeAreaScrollPosition scrollPosition) {
        if (!scrollPosition.equals(this.core.getScrollPosition())) {
            core.updateScrollPosition(scrollPosition);
            repaint();
            core.notifyScrolled();
        }
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
    public void removeCaretMovedListener(CodeAreaCaretListener caretMovedListener) {
        core.removeCaretMovedListener(caretMovedListener);
    }

    public void removeDataChangedListener(DataChangedListener dataChangedListener) {
        core.removeDataChangedListener(dataChangedListener);
    }

    @Override
    public void removeEditModeChangedListener(EditModeChangedListener editModeChangedListener) {
        core.removeEditModeChangedListener(editModeChangedListener);
    }

    @Override
    public void removeScrollingListener(ScrollingListener scrollingListener) {
        core.removeScrollingListener(scrollingListener);
    }

    @Override
    public void removeSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        core.removeSelectionChangedListener(selectionChangedListener);
    }

    @Override
    public void repaint() {
        super.repaint();
    }

    @Override
    public void reset() {
        core.reset();
    }

    @Override
    public void resetColors() {
        painter.resetColors();
        repaint();
    }

    @Override
    public void copy() {
        core.copy();
    }

    public void copyAsCode() {
        core.copyAsCode();
    }

    @Override
    public void cut() {
        core.cut();
    }

    @Override
    public void paste() {
        core.paste();
    }

    public void pasteFromCode() {
        core.pasteFromCode();
    }

    @Override
    public void delete() {
        core.delete();
    }

    @Override
    public void selectAll() {
        core.selectAll();
    }

    @Override
    public void clearSelection() {
        core.clearSelection();
        notifySelectionChanged();
        repaint();
    }

    public class AccessibleComponent extends AccessibleJComponent {
        /**
         * Gets the role of this object.
         *
         * @return an instance of AccessibleRole describing the role of the
         * object (AccessibleRole.TEXT)
         * @see AccessibleRole
         */
        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TEXT;
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            states.add(AccessibleState.MULTI_LINE);
            if (CodeAreaSwing.this.isEditable()) {
                states.add(AccessibleState.EDITABLE);
            }
            return states;
        }
    }
}
