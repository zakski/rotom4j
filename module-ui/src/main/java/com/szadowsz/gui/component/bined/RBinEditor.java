package com.szadowsz.gui.component.bined;

import com.szadowsz.binary.io.reader.Buffer;
import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.bined.bounds.RBinDimensions;
import com.szadowsz.gui.component.bined.caret.RCaretPos;
import com.szadowsz.gui.component.bined.caret.RCaret;
import com.szadowsz.gui.component.bined.complex.scroll.RBinScrollPos;
import com.szadowsz.gui.component.bined.settings.*;
import com.szadowsz.gui.component.bined.sizing.RBinMetrics;
import com.szadowsz.gui.component.bined.sizing.RBinStructure;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.component.utils.RComponentScrollbar;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.nds4j.file.bin.core.BinaryData;
import com.szadowsz.nds4j.file.bin.core.ByteArrayData;
import com.szadowsz.nds4j.file.bin.core.EmptyBinaryData;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;

import java.nio.charset.Charset;

public class RBinEditor extends RGroupDrawable {

    protected static final String HEADER = "header";

    // The Data
    protected BinaryData contentData = EmptyBinaryData.INSTANCE;

    // How to Display
    protected CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;
    protected AntialiasingMode antialiasingMode = AntialiasingMode.AUTO;
    protected BackgroundPaintMode backgroundPaintMode = BackgroundPaintMode.STRIPED;

    // Character Config
    protected Charset charset = Charset.forName(RFontStore.DEFAULT_ENCODING);
    protected CodeCharactersCase codeCharactersCase = CodeCharactersCase.UPPER;
    protected CodeType codeType = CodeType.HEXADECIMAL;
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


    protected final RBinDimensions dimensions = new RBinDimensions();
    protected final RBinMetrics metrics = new RBinMetrics();
    protected final RBinStructure structure = new RBinStructure();
    protected final RBinVisibility visibility = new RBinVisibility();

    protected RComponentScrollbar horizontalScrollBar = new RComponentScrollbar(this,new PVector(),new PVector(),0,0);
    protected RComponentScrollbar verticalScrollBar = new RComponentScrollbar(this,new PVector(),new PVector(),0,0);
    protected final RBinScrollPos scrollPosition = new RBinScrollPos();
    protected ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    protected VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.ROW;
    protected ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    protected HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;

    protected RowDataCache rowDataCache = null;
    //protected CursorDataCache cursorDataCache = null;

    // Cursor Caret
    protected RCaret caret;
    protected boolean showMirrorCursor = true;

    protected final RBinSelection selection = new RBinSelection();

    protected volatile boolean initialized = false;
    protected volatile boolean layoutChanged = true;
    protected volatile boolean fontChanged = false;
    protected volatile boolean resetColors = true;
    protected volatile boolean caretChanged = true;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     * @param filePath
     */
    public RBinEditor(RotomGui gui, String path, RGroup parent, String filePath) {
        super(gui, path, parent);

        byte[] data = Buffer.readFile(filePath);
        contentData = new ByteArrayData(data);

        children.add(new RBinHeader(gui, path + "/" + HEADER,this));
    }

    protected float getHorizontalScrollBarHeight() {
        return horizontalScrollBar.isVisible() ? horizontalScrollBar.getHeight() : 0;
    }

    protected float getVerticalScrollBarWidth() {
        return verticalScrollBar.isVisible() ? verticalScrollBar.getWidth() : 0;
    }

    protected void computeLayout() {
        int charactersPerPage = dimensions.getCharactersPerPage();
        structure.updateCache(this, charactersPerPage);
//        int rowsPerPage = dimensions.getRowsPerPage();
//        long rowsPerDocument = structure.getRowsPerDocument();
//        int charactersPerRow = structure.getCharactersPerRow();

        updateScrollBars();

        layoutChanged = false;
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

    protected long recomputeRows(){
        return contentData.getDataSize()/maxBytesPerRow + ((contentData.getDataSize()%maxBytesPerRow>0)?1:0);
    }

    protected void recomputeDimensions() {
        float verticalScrollBarSize = getVerticalScrollBarWidth();
        float horizontalScrollBarSize = getHorizontalScrollBarHeight();
        float componentWidth = metrics.getCharacterWidth()*maxBytesPerRow*PositionCodeType.HEXADECIMAL.getMaxDigitsForByte() + verticalScrollBarSize; // TODO
        float componentHeight = metrics.getRowHeight()*recomputeRows() + horizontalScrollBarSize; // TODO
        dimensions.recomputeSizes(metrics, 0, 0, componentWidth, componentHeight, rowPositionLength, verticalScrollBarSize, horizontalScrollBarSize);
    }

    protected void recomputeLayout() {
        rowPositionLength = recomputeRowPositionLength();
        recomputeDimensions();

        int charactersPerPage = dimensions.getCharactersPerPage();
        structure.updateCache(this, charactersPerPage);

//        int rowsPerPage = dimensions.getRowsPerPage();
//        long rowsPerDocument = structure.getRowsPerDocument();
//        int charactersPerRow = structure.getCharactersPerRow();

//        if (metrics.isInitialized()) {
//            scrolling.updateMaximumScrollPosition(rowsPerDocument, rowsPerPage, charactersPerRow, charactersPerPage, dimensions.getLastCharOffset(), dimensions.getLastRowOffset());
//        }

        updateScrollBars();

        layoutChanged = false;
    }



    protected void init(PGraphics pg) {
        PGraphics fontGraphics = gui.getSketch().createGraphics(800, 600, PConstants.JAVA2D);
        fontGraphics.beginDraw();
        fontGraphics.endDraw();
        metrics.recomputeMetrics(fontGraphics, RFontStore.getMainFont(),charset); // get Font Character sizes

        // we have the maximum of bytes per row, so at this stage we should work out the width we ideally should have to play with
        // contentData.getDataSize()
        // structure.getBytesPerRow() vs maxBytesPerRow
        // long numRows = contentData.getDataSize() / maxBytesPerRow + (contentData.getDataSize() % maxBytesPerRow>0?1:0);
        int characterWidth = metrics.getCharacterWidth(); // Get the width of a single character
        int digitsForByte = codeType.getMaxDigitsForByte(); // Get the number of characters for a byte
        float width = digitsForByte * characterWidth * maxBytesPerRow; // Get the ideal width of a row based on the max byte width

        long rowsInData = contentData.getDataSize() / maxBytesPerRow + (contentData.getDataSize() % maxBytesPerRow > 0 ? 1 : 0);
        float height = metrics.getRowHeight()*rowsInData;

        float verticalScrollBarWidth = getVerticalScrollBarWidth();
        float horizontalScrollBarHeight = getHorizontalScrollBarHeight();

        dimensions.recomputeSizes(metrics, 0, 0, width, height, rowPositionLength, verticalScrollBarWidth, horizontalScrollBarHeight);

        int charactersPerPage = dimensions.getCharactersPerPage();
        structure.updateCache(this, charactersPerPage);

        rowPositionLength = recomputeRowPositionLength();
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

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        if (!isInitialized()){
            init(pg);
        }
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
    public PFont getCodeFont() {
        return codeFont == null ? RFontStore.getMainFont() : codeFont;
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

    public CodeCharactersCase getCodeCharacterCase(){
        return codeCharactersCase;
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


    RBinMetrics getMetrics() {
        return metrics;
    }

    public RowDataCache getRowDataCache(){
        return rowDataCache;
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

    public boolean isInitialized(){
        return initialized;
    }

    @Override
    public void setLayout(RLayoutBase layout) {

    }

    @Override
    public float suggestWidth() {
        int characterWidth = metrics.getCharacterWidth(); // Get the width of a single character
        int digitsForByte = codeType.getMaxDigitsForByte(); // Get the number of characters for a byte
        return digitsForByte * characterWidth * maxBytesPerRow; // Get the ideal width of a row based on the max byte width
    }

    public void updateScrollBars(){
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


    public static class RowDataCache {

        char[] headerChars;
        byte[] rowData;
        char[] rowPositionCode;
        char[] rowCharacters;
    }
}
