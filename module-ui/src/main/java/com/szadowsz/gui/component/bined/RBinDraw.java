package com.szadowsz.gui.component.bined;


import com.szadowsz.gui.component.bined.bounds.RBinDimensions;
import com.szadowsz.gui.component.bined.bounds.RBinStructure;
import com.szadowsz.gui.component.bined.bounds.RBinVisibility;
import com.szadowsz.gui.component.bined.caret.RCaretPos;
import com.szadowsz.gui.component.bined.scroll.RBinScrollPos;
import com.szadowsz.gui.component.bined.scroll.RBinScrolling;
import com.szadowsz.gui.component.bined.settings.CodeAreaSection;
import com.szadowsz.gui.component.utils.RComponentScrollbar;

import java.util.Optional;

public class RBinDraw {

    protected final RBinEditor editor;

    protected final RBinDimensions dimensions = new RBinDimensions();
    protected final RBinScrolling scrolling = new RBinScrolling();
    protected final RBinMetrics metrics = new RBinMetrics();
    protected final RBinStructure structure = new RBinStructure();
    protected final RBinVisibility visibility = new RBinVisibility();

    protected volatile boolean initialized = false;

    public RBinDraw(RBinEditor editor) {
        this.editor = editor;
    }
    
    private int getHorizontalScrollBarSize() {
        RComponentScrollbar horizontalScrollBar = editor.getHorizontalScrollBar();
        return horizontalScrollBar.isVisible() ? horizontalScrollBar.getHeight() : 0;
    }

    private int getVerticalScrollBarSize() {
        RComponentScrollbar verticalScrollBar = editor.getVerticalScrollBar();
        return verticalScrollBar.isVisible() ? verticalScrollBar.getWidth() : 0;
    }
    
    private void recomputeScrollState() {
        scrolling.setScrollPosition(editor.getScrollPosition());
        int characterWidth = metrics.getCharacterWidth();

        if (characterWidth > 0) {
            scrolling.updateCache(editor, getHorizontalScrollBarSize(), getVerticalScrollBarSize());

            recomputeCharPositions();
        }
    }

    private void recomputeCharPositions() {
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
        int dataViewWidth = dimensions.getDataViewWidth();
        int dataViewHeight = dimensions.getDataViewHeight();
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
}
