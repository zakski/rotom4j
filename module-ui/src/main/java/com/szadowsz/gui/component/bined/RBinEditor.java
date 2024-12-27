package com.szadowsz.gui.component.bined;

import com.szadowsz.binary.io.reader.Buffer;
import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.bined.bounds.RBinDimensions;
import com.szadowsz.gui.component.bined.bounds.RBinRect;
import com.szadowsz.gui.component.bined.bounds.RBinSelection;
import com.szadowsz.gui.component.bined.sizing.CharsetStreamTranslator;
import com.szadowsz.gui.component.bined.sizing.RBinVisibility;
import com.szadowsz.gui.component.bined.caret.CursorShape;
import com.szadowsz.gui.component.bined.caret.RCaretPos;
import com.szadowsz.gui.component.bined.caret.RCaret;
import com.szadowsz.gui.component.bined.scroll.RBinScrollPos;
import com.szadowsz.gui.component.bined.settings.*;
import com.szadowsz.gui.component.bined.sizing.RBinMetrics;
import com.szadowsz.gui.component.bined.sizing.RBinStructure;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.component.oldbinary.CodeAreaCaretPosition;
import com.szadowsz.gui.component.oldbinary.basic.BasicCodeAreaSection;
import com.szadowsz.gui.component.oldbined.basic.CodeAreaScrollPosition;
import com.szadowsz.gui.component.utils.RComponentScrollbar;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.clip.BinaryDataClipboardData;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.nds4j.file.bin.core.BinaryData;
import com.szadowsz.nds4j.file.bin.core.ByteArrayData;
import com.szadowsz.nds4j.file.bin.core.EditableBinaryData;
import com.szadowsz.nds4j.file.bin.core.EmptyBinaryData;
import com.szadowsz.nds4j.file.bin.core.paged.PagedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * Editor Level Logic
 */
public class RBinEditor extends RGroupDrawable {

    private static Logger LOGGER = LoggerFactory.getLogger(RBinEditor.class);

    protected static final String MIME_CHARSET = "charset";

    protected static final String MAIN = "main";

    protected RBinColorAssessor colorAssessor = new RBinColorAssessor();
    protected RBinCharAssessor charAssessor = new RBinCharAssessor();

    // The Data
    protected BinaryData contentData = EmptyBinaryData.INSTANCE;

    // How to Display
    protected CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;
    protected AntialiasingMode antialiasingMode = AntialiasingMode.AUTO;

    protected BackgroundPaintMode backgroundPaintMode = BackgroundPaintMode.STRIPED;

    // Character Config
    protected Charset charset = Charset.forName(RFontStore.DEFAULT_ENCODING);
    protected CodeCharactersCase codeCharactersCase = CodeCharactersCase.UPPER;
    protected CodeType codeType = CodeType.DECIMAL;
    protected PFont codeFont;

    // Row Layout Config
    protected RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    protected int rowPositionLength;
    protected int minRowPositionLength;
    protected int maxRowPositionLength;
    protected int maxBytesPerRow = 16;
    protected int wrappingBytesGroupSize = 0;

    // Edit Op
    protected EditMode editMode = EditMode.EXPANDING;
    protected EditOperation editOperation = EditOperation.OVERWRITE;

    protected EnterKeyHandlingMode enterKeyHandlingMode = EnterKeyHandlingMode.PLATFORM_SPECIFIC;
    protected TabKeyHandlingMode tabKeyHandlingMode = TabKeyHandlingMode.PLATFORM_SPECIFIC;

    protected final RBinDimensions dimensions = new RBinDimensions();
    protected final RBinMetrics metrics = new RBinMetrics();
    protected final RBinStructure structure = new RBinStructure();
    protected final RBinVisibility visibility = new RBinVisibility();

    protected RComponentScrollbar horizontalScrollBar = new RComponentScrollbar(this, new PVector(), new PVector(), 0, 0);
    protected RComponentScrollbar verticalScrollBar = new RComponentScrollbar(this, new PVector(), new PVector(), 0, 0);
    protected final RBinScrollPos scrollPosition = new RBinScrollPos();
    protected ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    protected VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.ROW;
    protected ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    protected HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;

    protected RowDataCache rowDataCache = null;
    protected CursorDataCache cursorDataCache = null;

    // Cursor Caret
    protected RCaret caret;
    protected Cursor currentCursor;
    protected final Cursor defaultCursor = Cursor.getDefaultCursor();
    protected final Cursor textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
    protected boolean showMirrorCursor = true;

    protected final RBinSelection selection = new RBinSelection();

    protected volatile boolean initialized = false;
    protected volatile boolean layoutChanged = true;
    protected volatile boolean fontChanged = false;
    protected volatile boolean resetColors = true;
    protected volatile boolean caretChanged = true;

    protected Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    protected DataFlavor binedDataFlavor;
    protected DataFlavor binaryDataFlavor;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui      the gui for the window that the component is drawn under
     * @param path     the path in the component tree
     * @param parent   the parent component reference
     * @param filePath
     */
    public RBinEditor(RotomGui gui, String path, RGroup parent, String filePath) {
        super(gui, path, parent);

        LOGGER.info("Loading File \"{}\" for \"{}\" Binary Editor", filePath,name);
        byte[] data = Buffer.readFile(filePath);
        contentData = new ByteArrayData(data);
        caret = new RCaret(this);

       // children.add(new RBinHeader(gui, path + "/" + HEADER,this));
      //  children.add(new RBinRowPosition(gui, path + "/" + ROW,this));

        init();
        children.add(new RBinMain(gui, path + "/" + MAIN,this));
    }

    protected void notifyDataChanged() {
        getParentFolder().getWindow().reinitialiseBuffer();
    }

    protected void notifyCaretMoved() { // TODO consider if needed
        getParentFolder().getWindow().redrawBuffer();
    }

    protected void notifyCaretChanged() { // TODO consider if needed
        getParentFolder().getWindow().redrawBuffer();
    }

    protected SelectingMode isSelectingMode(RKeyEvent keyEvent) {
        return keyEvent.isShiftDown() ? SelectingMode.SELECTING : SelectingMode.NONE;
    }

    public boolean isValidChar(char value) {
        return getCharset().canEncode();
    }

    protected float getHorizontalScrollBarHeight() {
        return horizontalScrollBar.isVisible() ? horizontalScrollBar.getHeight() : 0;
    }

    protected float getVerticalScrollBarWidth() {
        return verticalScrollBar.isVisible() ? verticalScrollBar.getWidth() : 0;
    }

    protected int getMouseCursorShape(float positionX, float positionY) {
        float dataViewX = dimensions.getMainAreaRectangle().getX();
        float dataViewY = dimensions.getMainAreaRectangle().getY();
        float scrollPanelWidth = dimensions.getMainAreaRectangle().getWidth();
        float scrollPanelHeight = dimensions.getMainAreaRectangle().getHeight();
        if (positionX >= dataViewX && positionX < dataViewX + scrollPanelWidth
                && positionY >= dataViewY && positionY < dataViewY + scrollPanelHeight) {
            return Cursor.TEXT_CURSOR;
        }

        return Cursor.DEFAULT_CURSOR;
    }
    /**
     * Returns true if there is active selection for clipboard handling.
     *
     * @return true if non-empty selection is active
     */
    protected boolean hasSelection() {
        return !selection.isEmpty();
    }

    protected boolean changeEditOperation() {
        if (editMode == EditMode.EXPANDING || editMode == EditMode.CAPPED) {
            switch (editOperation) {
                case INSERT: {
                    setEditOperation(EditOperation.OVERWRITE);
                    break;
                }
                case OVERWRITE: {
                    setEditOperation(EditOperation.INSERT);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    protected void setActiveCaretPosition(RCaretPos caretPosition) {
        caret.setCaretPosition(caretPosition);
        notifyCaretMoved();
    }

    protected void setActiveCaretPosition(long dataPosition) {
        caret.setCaretPosition(dataPosition);
        notifyCaretMoved();
    }

    protected void setEditOperation(EditOperation editOperation) {
        EditOperation previousOperation = this.editOperation;
        this.editOperation = editOperation;
        EditOperation currentOperation = this.editOperation;
        boolean changed = previousOperation != currentOperation;
        if (changed) {
//            editModeChangedListeners.forEach((listener) -> {
//                listener.editModeChanged(editMode, currentOperation);
//            });
            caret.resetBlink();
            notifyCaretChanged();
            getParentFolder().getWindow().redrawBuffer();
        }
    }

    protected void setScrollPosition(RBinScrollPos scrollPosition) {
        if (!scrollPosition.equals(this.scrollPosition)) {
            this.scrollPosition.setScrollPosition(scrollPosition);
            scrollPositionModified();
            updateScrollBars();
            notifyScrolled();
        }
    }

    protected void clearSelection() {
        selection.clearSelection();
    }

    protected void updateSelection(SelectingMode selectingMode, RCaretPos caretPosition) {
        long dataPosition = getDataPosition();
        if (selectingMode == SelectingMode.SELECTING) {
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
        if (editMode == EditMode.INPLACE) {
            ((EditableBinaryData) contentData).fillData(first, length);
        } else {
            ((EditableBinaryData) contentData).remove(first, length);
        }
        setActiveCaretPosition(first);
        clearSelection();
        revealCursor();
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

    protected void computeLayout() {
        int charactersPerPage = dimensions.getCharactersPerPage();
        structure.updateCache(this, charactersPerPage);
//        int rowsPerPage = dimensions.getRowsPerPage();
//        long rowsPerDocument = structure.getRowsPerDocument();
//        int charactersPerRow = structure.getCharactersPerRow();
        visibility.recomputeCharPositions(metrics,structure,dimensions);

        updateScrollBars();

        layoutChanged = false;
    }

    protected long computeRowsCount() {
        return contentData.getDataSize() / maxBytesPerRow + ((contentData.getDataSize() % maxBytesPerRow > 0) ? 1 : 0);
    }

    protected RCaretPos computeMovePosition(RCaretPos position, MovementDirection direction) {
        return structure.computeMovePosition(position, direction, dimensions.getRowsPerPage());
    }

    protected RBinScrollPos computeScrolling(RBinScrollPos startPosition, ScrollingDirection direction) {
        int rowsPerPage = dimensions.getRowsPerPage();
        long rowsPerDocument = structure.getRowsPerDocument();
        return scrolling.computeScrolling(startPosition, direction, rowsPerPage, rowsPerDocument);
    }

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


    protected int recomputeRowPositionLength() {
        if (minRowPositionLength > 0 && minRowPositionLength == maxRowPositionLength) {
            return minRowPositionLength;
        }

        long dataSize = getDataSize();
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

    public byte[] charToBytes(char value) {
        ByteBuffer buffer = getCharset().encode(Character.toString(value));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }


    protected void init() {
        LOGGER.info("Initialising \"{}\" Binary Editor", name);

        caret.setSection(CodeAreaSection.CODE_MATRIX);
        PGraphics fontGraphics = gui.getSketch().createGraphics(800, 600, PConstants.JAVA2D);
        fontGraphics.beginDraw();
        fontGraphics.endDraw();
        metrics.recomputeMetrics(fontGraphics, RFontStore.getMainFont(), charset); // get Font Character sizes
        LOGGER.info("Font Metrics Loaded for \"{}\" Binary Editor", name);

        long rowsCount = computeRowsCount();
        LOGGER.info("\"{}\" Binary Editor Data Rows Count: {}", name,rowsCount);
        rowPositionLength = recomputeRowPositionLength();

        dimensions.computeRowDimensions(metrics,rowPositionLength, rowsCount);

        dimensions.computeHeaderAndDataDimensions(metrics,codeType,maxBytesPerRow,rowsCount);

        // Relay the size to the proper place // TODO Bodge job
        size.x = dimensions.getComponentRectangle().getWidth();
        size.y = dimensions.getComponentRectangle().getHeight();

        dimensions.computeOtherMetrics(metrics);

        int charactersPerPage = dimensions.getCharactersPerPage();
        structure.updateCache(this, charactersPerPage);

        computeLayout(); // use the sizes to figure out the width
        updateRowDataCache();
    }

    /**
     * Draw Child Component
     *
     * @param pg    Processing Graphics Context
     * @param child draw
     */
    protected void drawChildComponent(PGraphics pg, RComponent child) {
        pg.pushMatrix();
        pg.pushStyle();
        child.draw(pg);
        pg.popStyle();
        pg.popMatrix();
    }

    protected void paintOutsideArea(PGraphics g) {
        float headerAreaHeight = dimensions.getHeaderAreaHeight();
        float rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        RBinRect componentRect = dimensions.getComponentRectangle();
        int characterWidth = metrics.getCharacterWidth();
        g.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND)); // g.setColor(colorsProfile.getTextBackground());
        g.rect(componentRect.getX(), componentRect.getY(), componentRect.getWidth(), headerAreaHeight);

        // Decoration lines
        g.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); // g.setColor(colorsProfile.getDecorationLine());
        g.line(componentRect.getX(), componentRect.getY() + headerAreaHeight - 1, componentRect.getX() + rowPositionAreaWidth, componentRect.getY() + headerAreaHeight - 1);

        float lineX = componentRect.getX() + rowPositionAreaWidth - ((float) characterWidth / 2);
        if (lineX >= componentRect.getX()) {
            g.line(lineX, componentRect.getY(), lineX, componentRect.getY() + headerAreaHeight);
        }
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        //paintOutsideArea(pg);
        for (RComponent component : children) {
            if (!component.isVisible()) {
                continue;
            }
            pg.pushMatrix();
            pg.translate(component.getRelPosX(), component.getRelPosY());
            drawChildComponent(pg, component);
            pg.popMatrix();
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
            if (editMode == EditMode.EXPANDING) {
                deleteSelection();
                notifyDataChanged();
            }
        }
    }

    protected void move(SelectingMode selectingMode, MovementDirection direction) {
        RCaretPos caretPosition = getActiveCaretPosition();
        RCaretPos movePosition = computeMovePosition(caretPosition, direction);
        if (!caretPosition.equals(movePosition)) {
            setActiveCaretPosition(movePosition);
            updateSelection(selectingMode, movePosition);
        } else if (selectingMode == SelectingMode.NONE) {
            clearSelection();
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
        if (editMode == EditMode.INPLACE) {
            if (dataPosition + toReplace > getDataSize()) {
                toReplace = getDataSize() - dataPosition;
            }
            ((EditableBinaryData) contentData).replace(dataPosition, pastedData, 0, toReplace);
        } else {
            if (editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) {
                if (dataPosition + toReplace > getDataSize()) {
                    toReplace = getDataSize() - dataPosition;
                }
                ((EditableBinaryData) contentData).remove(dataPosition, toReplace);
            }

            ((EditableBinaryData) contentData).insert(dataPosition, pastedData);
            caret.setCaretPosition(caret.getDataPosition() + clipDataSize);
            updateSelection(SelectingMode.NONE, caret.getCaretPosition());
        }

        caret.setCodeOffset(0);
        setActiveCaretPosition(caret.getCaretPosition());
        notifyDataChanged();
        revealCursor();
        clearSelection();
    }

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

    protected void revealPosition(RCaretPos caretPosition) {
        if (!isInitialized()) {
            // Silently ignore if painter is not yet initialized
            return;
        }

        Optional<RBinScrollPos> revealScrollPosition = computeRevealScrollPosition(caretPosition);
        revealScrollPosition.ifPresent(this::setScrollPosition);
    }

    protected void revealCursor() {
        revealPosition(getActiveCaretPosition());
        getParentFolder().getWindow().redrawBuffer();
    }

    protected void scroll(ScrollingDirection direction) {
        RBinScrollPos sourcePosition = this.scrollPosition;
        RBinScrollPos scrollPosition = computeScrolling(sourcePosition, direction);
        if (!sourcePosition.equals(scrollPosition)) {
            setScrollPosition(scrollPosition);
            getParentFolder().getWindow().redrawBuffer();
        }
    }

    protected void selectAll() {
        long dataSize = getDataSize();
        if (dataSize > 0) {
            selection.setSelection(0, dataSize);
        }
    }

    protected void enterPressed() {
        if (!checkEditAllowed()) {
            return;
        }

        if (getActiveSection() == CodeAreaSection.TEXT_PREVIEW) {
            String sequence = enterKeyHandlingMode.getSequence();
            if (!sequence.isEmpty()) {
                pressedCharInPreview(sequence.charAt(0));
                if (sequence.length() == 2) {
                    pressedCharInPreview(sequence.charAt(1));
                }
            }
        }
    }

    protected void tabPressed(SelectingMode selectingMode) {
        if (!checkEditAllowed()) {
            return;
        }

        if (tabKeyHandlingMode == TabKeyHandlingMode.PLATFORM_SPECIFIC || tabKeyHandlingMode == TabKeyHandlingMode.CYCLE_TO_NEXT_SECTION || tabKeyHandlingMode == TabKeyHandlingMode.CYCLE_TO_PREVIOUS_SECTION) {
            if (getViewMode() == CodeAreaViewMode.DUAL) {
                move(selectingMode, MovementDirection.SWITCH_SECTION);
                revealCursor();
            }
        } else if (getActiveSection() == CodeAreaSection.TEXT_PREVIEW) {
            String sequence = tabKeyHandlingMode == TabKeyHandlingMode.INSERT_TAB ? "\t" : "  ";
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
            long dataPosition = getDataPosition();
            if (dataPosition == 0 || dataPosition > getDataSize()) {
                return;
            }

            caret.setCodeOffset(0);
            move(SelectingMode.NONE, MovementDirection.LEFT);
            caret.setCodeOffset(0);
            ((EditableBinaryData) contentData).remove(dataPosition - 1, 1);
            notifyDataChanged();
            setActiveCaretPosition(caret.getCaretPosition());
            revealCursor();
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
            revealCursor();
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
            revealCursor();
        }
    }

    protected void pressedCharInPreview(char keyChar) {
        if (isValidChar(keyChar)) {
            RCaretPos caretPosition = getActiveCaretPosition();

            long dataPosition = caretPosition.getDataPosition();
            byte[] bytes = charToBytes(keyChar);
            if (editMode == EditMode.INPLACE) {
                int length = bytes.length;
                if (dataPosition + length > getDataSize()) {
                    return;
                }
            }
            if (hasSelection() && editMode != EditMode.INPLACE) {
                deleteSelection();
            }

            if ((editMode == EditMode.EXPANDING && editOperation == EditOperation.OVERWRITE) || editMode == EditMode.INPLACE) {
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
            move(SelectingMode.NONE, MovementDirection.RIGHT);
            revealCursor();
        }
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

    public BackgroundPaintMode getBackgroundPaintMode() {
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
     * Returns currently used charset.
     *
     * @return charset
     */
    public Charset getCharset() {
        return charset;
    }

    public RBinCharAssessor getCharAssessor() {
        return charAssessor;
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
    public PFont getCodeFont() {
        return codeFont == null ? RFontStore.getMainFont() : codeFont;
    }

    public int getCodeLastCharPos() {
        return visibility.getCodeLastCharPos();
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
     * Returns data or null.
     *
     * @return binary data
     */
    public BinaryData getContentData() {
        return contentData;
    }

    /**
     * Returns current code type.
     *
     * @return code type
     */
    public CodeType getCodeType() {
        return codeType;
    }

    public RBinColorAssessor getColorAssessor() {
        return colorAssessor;
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
    protected RBinRect getCursorPositionRect(long dataPosition, int codeOffset, CodeAreaSection section) {
        RBinRect rect = new RBinRect();
        updateRectToCursorPosition(rect, dataPosition, codeOffset, section);
        return rect;
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

    RBinDimensions getDimensions() {
        return dimensions;
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
     * Returns maximum number of bytes per row.
     *
     * @return bytes per row
     */
    public int getMaxBytesPerRow() {
        return maxBytesPerRow;
    }

    public RBinMetrics getMetrics() {
        return metrics;
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
    public PVector getPositionPoint(long dataPosition, int codeOffset, CodeAreaSection section) {
        int bytesPerRow = structure.getBytesPerRow();
        int rowsPerRect = dimensions.getRowsPerRect();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();

        long row = dataPosition / bytesPerRow - scrollPosition.getRowPosition();
        if (row < -1 || row > rowsPerRect) {
            return null;
        }

        int byteOffset = (int) (dataPosition % bytesPerRow);

        RBinRect dataViewRect = dimensions.getDataViewRectangle();
        float caretY = (dataViewRect.getY() + row * rowHeight) - scrollPosition.getRowOffset();
        float caretX;
        if (section == CodeAreaSection.TEXT_PREVIEW) {
            caretX = dataViewRect.getX() + visibility.getPreviewRelativeX() + characterWidth * byteOffset;
        } else {
            caretX = dataViewRect.getX() + characterWidth * (structure.computeFirstCodeCharacterPos(byteOffset) + codeOffset);
        }
        caretX -= scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();

        return new PVector(caretX, caretY);
    }

    public RowDataCache getRowDataCache() {
        return rowDataCache;
    }

    public int getRowPositionLength() {
        return rowPositionLength;
    }

    /**
     * Returns row wrapping mode.
     *
     * @return row wrapping mode
     */
    public RowWrappingMode getRowWrapping() {
        return rowWrapping;
    }

    public RBinScrollPos getScrollPos() {
        return scrollPosition;
    }

    public RBinSelection getSelectionHandler() {
        return selection;
    }

    public RBinStructure getStructure() {
        return structure;
    }

    /**
     * Returns current view mode.
     *
     * @return view mode
     */
    public CodeAreaViewMode getViewMode() {
        return viewMode;
    }

    public RBinVisibility getVisibility() {
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

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isMirrorCursorShowing() {
        return showMirrorCursor;
    }

    public boolean checkEditAllowed() {
        return contentData instanceof EditableBinaryData;
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

    public void updateScrollBars() {
        //verticalScrollBar
        //horizontalScrollBar
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

    protected void updateMirrorCursorRect(long dataPosition, CodeAreaSection section) {
        CodeType codeType = structure.getCodeType();
        PVector mirrorCursorPoint = getPositionPoint(dataPosition, 0, section == CodeAreaSection.CODE_MATRIX ? CodeAreaSection.TEXT_PREVIEW : CodeAreaSection.CODE_MATRIX);
        if (mirrorCursorPoint == null) {
            cursorDataCache.mirrorCursorRect.setSize(0, 0);
        } else {
            cursorDataCache.mirrorCursorRect.setBounds(mirrorCursorPoint.x, mirrorCursorPoint.y, metrics.getCharacterWidth() * (section == CodeAreaSection.TEXT_PREVIEW ? codeType.getMaxDigitsForByte() : 1), metrics.getRowHeight());
        }
    }

    public void updateAssessors() {
        colorAssessor.update(this);
        charAssessor.update(this);
    }

    @Override
    public void keyPressedFocused(RKeyEvent keyEvent) {
        if (!gui.hasFocus(this)) {
            return;
        }

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_LEFT: {
                move(isSelectingMode(keyEvent), MovementDirection.LEFT);
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_RIGHT: {
                move(isSelectingMode(keyEvent), MovementDirection.RIGHT);
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_UP: {
                move(isSelectingMode(keyEvent), MovementDirection.UP);
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_DOWN: {
                move(isSelectingMode(keyEvent), MovementDirection.DOWN);
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_HOME: {
                if (keyEvent.isControlDown()) {
                    move(isSelectingMode(keyEvent), MovementDirection.DOC_START);
                } else {
                    move(isSelectingMode(keyEvent), MovementDirection.ROW_START);
                }
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_END: {
                if (keyEvent.isControlDown()) {
                    move(isSelectingMode(keyEvent), MovementDirection.DOC_END);
                } else {
                    move(isSelectingMode(keyEvent), MovementDirection.ROW_END);
                }
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_PAGE_UP: {
                scroll(ScrollingDirection.PAGE_UP);
                move(isSelectingMode(keyEvent), MovementDirection.PAGE_UP);
                revealCursor();
                keyEvent.consume();
                break;
            }
            case KeyEvent.VK_PAGE_DOWN: {
                scroll(ScrollingDirection.PAGE_DOWN);
                move(isSelectingMode(keyEvent), MovementDirection.PAGE_DOWN);
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
                if (tabKeyHandlingMode != TabKeyHandlingMode.IGNORE) {
                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.VK_ENTER: {
                enterPressed();
                if (enterKeyHandlingMode != EnterKeyHandlingMode.IGNORE) {
                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.VK_DELETE: {
                if (editMode == EditMode.EXPANDING) {
                    deletePressed();
                    keyEvent.consume();
                }
                break;
            }
            case KeyEvent.VK_BACK_SPACE: {
                if (editMode == EditMode.EXPANDING) {
                    backSpacePressed();
                    keyEvent.consume();
                }
                break;
            }
            default: {
                if (getClipboardHandlingMode() == ClipboardHandlingMode.PROCESS) {
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
    public void mousePressed(RMouseEvent mouseEvent, float mouseY) {
        if (!gui.hasFocus(this)) {
            gui.takeFocus(this);
        }
        if (mouseEvent.isLeft()) {
            moveCaret(mouseEvent);
            super.mousePressed(mouseEvent, mouseY);
        }
    }

    public RCaretPos mousePositionToClosestCaretPosition(float positionX, float positionY, CaretOverlapMode overflowMode) {
        RCaretPos caret = new RCaretPos();
        CodeAreaScrollPosition scrollPosition = scrolling.getScrollPosition();
        int characterWidth = metrics.getCharacterWidth();
        int rowHeight = metrics.getRowHeight();
        float rowPositionAreaWidth = dimensions.getRowPositionAreaWidth();
        float headerAreaHeight = dimensions.getHeaderAreaHeight();

        int diffX = 0;
        if (positionX < rowPositionAreaWidth) {
            if (overflowMode == CaretOverlapMode.PARTIAL_OVERLAP) {
                diffX = 1;
            }
            positionX = rowPositionAreaWidth;
        }
        int cursorCharX = (positionX - rowPositionAreaWidth + scrollPosition.getCharOffset()) / characterWidth + scrollPosition.getCharPosition() - diffX;
        if (cursorCharX < 0) {
            cursorCharX = 0;
        }

        int diffY = 0;
        if (positionY < headerAreaHeight) {
            if (overflowMode == CaretOverlapMode.PARTIAL_OVERLAP) {
                diffY = 1;
            }
            positionY = headerAreaHeight;
        }
        long cursorRowY = (positionY - headerAreaHeight + scrollPosition.getRowOffset()) / rowHeight + scrollPosition.getRowPosition() - diffY;
        if (cursorRowY < 0) {
            cursorRowY = 0;
        }

        CodeAreaViewMode viewMode = structure.getViewMode();
        int previewCharPos = visibility.getPreviewCharPos();
        int bytesPerRow = structure.getBytesPerRow();
        CodeType codeType = structure.getCodeType();
        long dataSize = getDataSize();
        long dataPosition;
        int codeOffset = 0;
        int byteOnRow;
        if ((viewMode == CodeAreaViewMode.DUAL && cursorCharX < previewCharPos) || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            caret.setSection(CodeAreaSection.CODE_MATRIX);
            byteOnRow = structure.computePositionByte(cursorCharX);
            if (byteOnRow >= bytesPerRow) {
                codeOffset = 0;
            } else {
                codeOffset = cursorCharX - structure.computeFirstCodeCharacterPos(byteOnRow);
                if (codeOffset >= codeType.getMaxDigitsForByte()) {
                    codeOffset = codeType.getMaxDigitsForByte() - 1;
                }
            }
        } else {
            caret.setSection(CodeAreaSection.TEXT_PREVIEW);
            byteOnRow = cursorCharX;
            if (viewMode == CodeAreaViewMode.DUAL) {
                byteOnRow -= previewCharPos;
            }
        }

        if (byteOnRow >= bytesPerRow) {
            byteOnRow = bytesPerRow - 1;
        }

        dataPosition = byteOnRow + (cursorRowY * bytesPerRow);
        if (dataPosition < 0) {
            dataPosition = 0;
            codeOffset = 0;
        }

        if (dataPosition >= dataSize) {
            dataPosition = dataSize;
            codeOffset = 0;
        }

        caret.setDataPosition(dataPosition);
        caret.setCodeOffset(codeOffset);
        return caret;
    }

    protected void moveCaret(float positionX, float positionY, SelectingMode selecting) {
        RCaretPos caretPosition = mousePositionToClosestCaretPosition(positionX, positionY, CaretOverlapMode.PARTIAL_OVERLAP);
        setActiveCaretPosition(caretPosition);
        updateSelection(selecting, caretPosition);

        getParentFolder().getWindow().redrawBuffer();
    }

    protected void moveCaret(RMouseEvent me) {
        SelectingMode selecting = me.isShiftDown() ? SelectingMode.SELECTING : SelectingMode.NONE;
        moveCaret(me.getX(), me.getY(), selecting);
        revealCursor();
    }

    @Override
    public void mouseOver(RMouseEvent mouseEvent, float adjustedMouseY){
        super.mouseOver(mouseEvent,adjustedMouseY);
        updateMouseCursor(mouseEvent);
    }

    private void updateMouseCursor(RMouseEvent me) {
//        int cursorShape = getMouseCursorShape(me.getX(), me.getY());
//
//        // Reuse current cursor if unchanged
//        Cursor newCursor = cursorShape == 0 ? defaultCursor : textCursor;
//        if (newCursor != currentCursor) {
//            currentCursor = newCursor;
//            setCursor(newCursor);
//        }
    }

    @Override
    public void mouseDragged(RMouseEvent me) {
        updateMouseCursor(me);
        if (isMouseOver) {
            super.mouseDragged(me);
            moveCaret(me.getX(), me.getY(), SelectingMode.SELECTING);
            revealCursor();
        }
    }

    public static class RowDataCache {

        char[] headerChars;
        byte[] rowData;
        char[] rowPositionCode;
        char[] rowCharacters;
    }

    public static class CursorDataCache {

        RBinRect caretRect = new RBinRect();
        RBinRect mirrorCursorRect = new RBinRect();
        final BasicStroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
        int cursorCharsLength;
        char[] cursorChars;
        int cursorDataLength;
        byte[] cursorData;
    }
}
