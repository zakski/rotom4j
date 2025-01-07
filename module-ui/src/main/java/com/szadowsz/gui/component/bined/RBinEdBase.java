package com.szadowsz.gui.component.bined;

import com.szadowsz.rotom4j.binary.BinaryData;
import com.szadowsz.rotom4j.binary.EditableBinaryData;
import com.szadowsz.rotom4j.binary.EmptyBinaryData;
import com.szadowsz.rotom4j.binary.paged.PagedData;
import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.bined.bounds.RBinDimensions;
import com.szadowsz.gui.component.bined.bounds.RBinSelection;
import com.szadowsz.gui.component.bined.bounds.RBinStructure;
import com.szadowsz.gui.component.bined.bounds.RBinVisibility;
import com.szadowsz.gui.component.bined.cursor.RCaretPos;
import com.szadowsz.gui.component.bined.settings.*;
import com.szadowsz.gui.component.bined.utils.CharsetStreamTranslator;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.config.text.RFontMetrics;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.input.clip.BinaryDataClipboardData;
import com.szadowsz.gui.input.clip.ClipboardData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PFont;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Base Binary Data Editor
 */
public abstract class RBinEdBase extends RGroupDrawable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RBinEdBase.class);

    protected static final String MIME_CHARSET = "charset";

    // The Data
    protected BinaryData contentData = EmptyBinaryData.INSTANCE;

    // Editor Structural data
    protected final RBinDimensions dimensions = new RBinDimensions();
    protected final RBinStructure structure = new RBinStructure();
    protected final RBinVisibility visibility = new RBinVisibility();
    protected final RFontMetrics metrics = new RFontMetrics();

    // Character Config
    protected Charset charset = Charset.forName(RFontStore.DEFAULT_ENCODING);
    protected RCodeCase codeCharactersCase = RCodeCase.UPPER;
    protected RCodeType codeType = RCodeType.DECIMAL;
    protected PFont codeFont;

    // Row Layout Config
    protected RRowWrappingMode rowWrapping = RRowWrappingMode.NO_WRAPPING;
    protected int rowPositionLength;
    protected int minRowPositionLength;
    protected int maxRowPositionLength;
    protected int maxBytesPerRow = 16;
    protected int wrappingBytesGroupSize = 0;

    // Current User Selection
    protected final RBinSelection selection = new RBinSelection();

    // User Clipboard
    protected Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    protected RClipHandlingMode clipboardHandlingMode = RClipHandlingMode.PROCESS;
    protected ClipboardData currentClipboardData = null;
    protected DataFlavor binedDataFlavor;
    protected DataFlavor binaryDataFlavor;

    // Edit Op
    protected REditMode editMode = REditMode.EXPANDING;
    protected REditOperation editOperation = REditOperation.OVERWRITE;

    public RBinEdBase(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
    }

    protected boolean checkEditAllowed() {
        return contentData instanceof EditableBinaryData;
    }

    /**
     * Returns true if there is active selection for clipboard handling.
     *
     * @return true if non-empty selection is active
     */
    protected boolean hasSelection() {
        return !selection.isEmpty();
    }

    protected void setClipboardContent(ClipboardData content) {
        clearClipboardData();
        try {
            currentClipboardData = content;
            clipboard.setContents(content, content);
        } catch (IllegalStateException ex) {
            // Clipboard not available - ignore and clear
            clearClipboardData();
        }
    }

    /**
     * Initialise Editor Structural data
     */
    protected abstract void init();

    /**
     * Read the Binary Data
     *
     * @param filePath path to the binary file
     */
    protected abstract void loadData(String filePath);

    /**
     * Compute the number of expected rows
     *
     * @return the number of expected rows
     */
    protected long computeRowsCount() {
        return getDataSize() / maxBytesPerRow + ((getDataSize() % maxBytesPerRow > 0) ? 1 : 0);
    }

    /**
     * Compute the row position length
     *
     * @return the expected row position length
     */
    protected int computeRowPositionLength() {
        if (minRowPositionLength > 0 && minRowPositionLength == maxRowPositionLength) {
            return minRowPositionLength;
        }

        long dataSize = getDataSize();
        if (dataSize == 0) {
            return 1;
        }

        double natLog = Math.log(dataSize == Long.MAX_VALUE ? dataSize : dataSize + 1);
        int positionLength = (int) Math.ceil(natLog / RCodeType.HEXADECIMAL.getBaseLog());
        if (minRowPositionLength > 0 && positionLength < minRowPositionLength) {
            positionLength = minRowPositionLength;
        }
        if (maxRowPositionLength > 0 && positionLength > maxRowPositionLength) {
            positionLength = maxRowPositionLength;
        }

        return positionLength == 0 ? 1 : positionLength;
    }

    protected RCaretPos computeClosestCaretPosition(float positionX, float positionY) {
        float relativeX = positionX - (pos.x + dimensions.getRowPositionAreaWidth());
        float relativeY = positionY - (pos.y + dimensions.getHeaderAreaHeight());
        RCaretPos caret = new RCaretPos();

        if (relativeX < 0 || relativeY < 0 || relativeX > dimensions.getDataViewWidth() || relativeY > dimensions.getDataViewHeight()) {
            return caret;
        }

        int cursorCharX = Math.round(relativeX) / metrics.getCharacterWidth();
        int cursorDataX = cursorCharX / (codeType.getMaxDigitsForByte()+1);
        int cursorY = Math.round(relativeY) / metrics.getRowHeight();

        long dataPosition = cursorDataX + (cursorY * structure.getBytesPerRow());
        int codeOffset = cursorCharX % (codeType.getMaxDigitsForByte()+1);

        if (codeOffset >= codeType.getMaxDigitsForByte()) {
            codeOffset = codeType.getMaxDigitsForByte()-1;
        }

        LOGGER.debug("Mapping Mouse to Caret [{}/{},{}] = [{},{}]",cursorCharX,cursorDataX,cursorY,dataPosition,codeOffset);

        if (dataPosition < 0) {
            dataPosition = 0;
            codeOffset = 0;
        }

        if (dataPosition >= contentData.getDataSize()) {
            dataPosition = contentData.getDataSize()-1;
            codeOffset = codeType.getMaxDigitsForByte()-1;
        }

        caret.setDataPosition(dataPosition);
        caret.setCodeOffset(codeOffset);
        return caret;
    }

    protected void notifyDataChanged() {
        getParentWindow().reinitialiseBuffer();
    }

    /**
     * Clear User selection
     */
    protected void clearSelection() {
        selection.clearSelection();
    }

    protected void clearClipboardData() {
        if (currentClipboardData != null) {
            currentClipboardData.dispose();
            currentClipboardData = null;
        }
    }

    protected void copy() {
        if (!selection.isEmpty()) {
            long first = selection.getFirst();
            long last = selection.getLast();

            BinaryData copy = contentData.copy(first, last - first + 1);

            BinaryDataClipboardData binaryData = new BinaryDataClipboardData(copy, binedDataFlavor, binaryDataFlavor, charset);
            setClipboardContent(binaryData);
        }
    }

    protected void cut() {
        if (!checkEditAllowed()) {
            return;
        }

        if (!selection.isEmpty()) {
            copy();
            if (editMode == REditMode.EXPANDING) {
                deleteSelection();
                notifyDataChanged();
            }
        }
    }

    protected abstract void deleteSelection() ;

    protected abstract void pasteBinaryData(BinaryData pastedData);

    protected void paste() {
        if (!checkEditAllowed()) {
            return;
        }

        try {
            if (!clipboard.isDataFlavorAvailable(binedDataFlavor) && !clipboard.isDataFlavorAvailable(binaryDataFlavor) && !clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor) && !clipboard.isDataFlavorAvailable(DataFlavor.getTextPlainUnicodeFlavor())) {
                return;
            }
        } catch (IllegalStateException ex) {
            return;
        }

        try {
            if (clipboard.isDataFlavorAvailable(binedDataFlavor)) {
                try {
                    Object clipboardData = clipboard.getData(binedDataFlavor);
                    if (clipboardData instanceof BinaryData) {
                        pasteBinaryData((BinaryData) clipboardData);
                    }
                } catch (UnsupportedFlavorException | IllegalStateException | IOException ex) {
                    LOGGER.error(null, ex);
                }
            } else {
                InputStream clipboardData;
                try {
                    // TODO use stream directly without buffer
                    PagedData pastedData = new PagedData();
                    if (clipboard.isDataFlavorAvailable(binaryDataFlavor)) {
                        clipboardData = (InputStream) clipboard.getData(binaryDataFlavor);
                        pastedData.insert(0, clipboardData, -1);
                    } else if (clipboard.isDataFlavorAvailable(DataFlavor.getTextPlainUnicodeFlavor())) {
                        clipboardData = (InputStream) clipboard.getData(DataFlavor.getTextPlainUnicodeFlavor());

                        DataFlavor textPlainUnicodeFlavor = DataFlavor.getTextPlainUnicodeFlavor();
                        String charsetName = textPlainUnicodeFlavor.getParameter(MIME_CHARSET);
                        CharsetStreamTranslator translator = new CharsetStreamTranslator(Charset.forName(charsetName), getCharset(), clipboardData);

                        pastedData.insert(0, translator, -1);
                    } else {
                        String text = (String) clipboard.getData(DataFlavor.stringFlavor);
                        pastedData.insert(0, text.getBytes(getCharset()));
                    }

                    pasteBinaryData(pastedData);
                } catch (UnsupportedFlavorException | IllegalStateException | IOException ex) {
                    LOGGER.error(null, ex);
                }
            }
        } catch (IllegalStateException ex) {
            // Clipboard not available - ignore
        }
    }

    protected void selectAll() {
        long dataSize = getDataSize();
        if (dataSize > 0) {
            selection.setSelection(0, dataSize);
        }
    }

    protected void redrawWinBuffer(){
        getParentWindow().redrawBuffer();
    }

    /**
     * Returns currently used charset.
     *
     * @return charset
     */
    Charset getCharset() {
        return charset;
    }

    /**
     * Returns current code characters case.
     *
     * @return code characters case
     */
    RCodeCase getCodeCharactersCase() {
        return codeCharactersCase;
    }

    /**
     * Returns font used for text painting.
     *
     * @return font
     */
    PFont getCodeFont() {
        return codeFont == null ? RFontStore.getMainFont() : codeFont;
    }

    /**
     * Returns edit mode.
     *
     * @return edit mode
     */
    REditMode getEditMode() {
        return editMode;
    }

    int getRowPositionLength() {
        return rowPositionLength;
    }

    RBinSelection getSelectionHandler() {
        return selection;
    }

    /**
     * Returns current code type.
     *
     * @return code type
     */
    public RCodeType getCodeType() {
        return codeType;
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
     * Method to get Dimensions
     *
     * @return dimensions object
     */
    public RBinDimensions getDimensions() {
        return dimensions;
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
     * get current Font Metrics
     *
     * @return metrics
     */
    public RFontMetrics getMetrics() {
        return metrics;
    }

    /**
     * Returns row wrapping mode.
     *
     * @return row wrapping mode
     */
    public RRowWrappingMode getRowWrapping() {
        return rowWrapping;
    }

    public RBinStructure getStructure() {
        return structure;
    }

    public RBinVisibility getVisibility(){
        return visibility;
    }

    /**
     * Returns size of the byte group.
     *
     * @return size of the byte group
     */
    public int getWrappingBytesGroupSize() {
        return wrappingBytesGroupSize;
    }
}
