package com.szadowsz.gui.component.bined.scroll;

import com.szadowsz.gui.component.bined.RBinEditor;
import com.szadowsz.gui.component.bined.settings.HorizontalScrollUnit;
import com.szadowsz.gui.component.bined.settings.PositionScrollVisibility;
import com.szadowsz.gui.component.bined.settings.ScrollBarVisibility;
import com.szadowsz.gui.component.bined.settings.VerticalScrollUnit;
import com.szadowsz.gui.component.bined.utils.RBinUtils;

import java.util.Optional;

public class RBinScrolling {

    private final RBinScrollPos scrollPosition = new RBinScrollPos();
    private final RBinScrollPos maximumScrollPosition = new RBinScrollPos();

    private int lastVerticalScrollingValue = -1;

    private VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.ROW;
    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;

    private HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;

    private void setHorizontalScrollPosition(RBinScrollPos scrollPosition, int charPos, int pixelOffset, int characterWidth) {
        switch (horizontalScrollUnit) {
            case CHARACTER: {
                scrollPosition.setCharPosition(charPos);
                scrollPosition.setCharOffset(0);
                break;
            }
            case PIXEL: {
                if (pixelOffset > characterWidth) {
                    pixelOffset = pixelOffset % characterWidth;
                }
                scrollPosition.setCharPosition(charPos);
                scrollPosition.setCharOffset(pixelOffset);
                break;
            }
            default:
                throw RBinUtils.getInvalidTypeException(horizontalScrollUnit);
        }
    }
    
    private PositionScrollVisibility checkBottomScrollVisibility(long rowPosition, int rowsPerPage, int rowOffset, int rowHeight) {
        int sumOffset = scrollPosition.getRowOffset() + rowOffset;

        long lastFullRow = scrollPosition.getRowPosition() + rowsPerPage;
        if (rowOffset > 0) {
            lastFullRow--;
        }
        if (sumOffset >= rowHeight) {
            lastFullRow++;
        }

        if (rowPosition <= lastFullRow) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (sumOffset > 0 && sumOffset != rowHeight && rowPosition == lastFullRow + 1) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    private PositionScrollVisibility checkLeftScrollVisibility(int charsPosition) {
        int charPos = scrollPosition.getCharPosition();
        if (horizontalScrollUnit != HorizontalScrollUnit.PIXEL) {
            return charsPosition < charPos ? PositionScrollVisibility.NOT_VISIBLE : PositionScrollVisibility.VISIBLE;
        }

        if (charsPosition > charPos || (charsPosition == charPos && scrollPosition.getCharOffset() == 0)) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (charsPosition == charPos && scrollPosition.getCharOffset() > 0) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    private PositionScrollVisibility checkRightScrollVisibility(int charsPosition, int charsPerPage, int charOffset, int characterWidth) {
        int sumOffset = scrollPosition.getCharOffset() + charOffset;

        int lastFullChar = scrollPosition.getCharPosition() + charsPerPage;
        if (charOffset > 0) {
            lastFullChar--;
        }
        if (sumOffset >= characterWidth) {
            lastFullChar++;
        }

        if (charsPosition <= lastFullChar) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (sumOffset > 0 && sumOffset != characterWidth && charsPosition == lastFullChar + 1) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    private PositionScrollVisibility checkTopScrollVisibility(long rowPosition) {
        if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            return rowPosition < scrollPosition.getRowPosition() ? PositionScrollVisibility.NOT_VISIBLE : PositionScrollVisibility.VISIBLE;
        }

        if (rowPosition > scrollPosition.getRowPosition() || (rowPosition == scrollPosition.getRowPosition() && scrollPosition.getRowOffset() == 0)) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (rowPosition == scrollPosition.getRowPosition() && scrollPosition.getRowOffset() > 0) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }
    
    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    public void setScrollPosition(RBinScrollPos scrollPosition) {
        this.scrollPosition.setScrollPosition(scrollPosition);
        if (scrollPosition.isRowPositionGreaterThan(maximumScrollPosition)) {
            this.scrollPosition.setRowPosition(maximumScrollPosition.getRowPosition());
            this.scrollPosition.setRowOffset(maximumScrollPosition.getRowOffset());
        }
        if (scrollPosition.isCharPositionGreaterThan(maximumScrollPosition)) {
            this.scrollPosition.setCharPosition(maximumScrollPosition.getCharPosition());
            this.scrollPosition.setCharOffset(maximumScrollPosition.getCharOffset());
        }
    }

    public void clearLastVerticalScrollingValue() {
        lastVerticalScrollingValue = -1;
    }

    public Optional<RBinScrollPos> computeRevealScrollPosition(long rowPosition, int charsPosition, int bytesPerRow, int rowsPerPage, int charsPerPage, int charOffset, int rowOffset, int characterWidth, int rowHeight) {
        return null;
    }

    public void updateCache(RBinEditor editor, int horizontalScrollBarSize, int verticalScrollBarSize) {
    }

    public RBinScrollPos getScrollPosition() {
    }

    public void updateMaximumScrollPosition(long rowsPerDocument, int rowsPerPage, int charactersPerRow, int charactersPerPage, Object lastCharOffset, Object lastRowOffset) {
    }

    public int getHorizontalScrollX(int characterWidth) {
        return 0;
    }
}
