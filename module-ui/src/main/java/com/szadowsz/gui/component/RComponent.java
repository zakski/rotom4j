package com.szadowsz.gui.component;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.config.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.theme.RTheme;
import com.szadowsz.gui.config.theme.RThemeColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.RInputListener;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * RComponent provides default behaviour for all components in RotomGui.
 * <p>
 * Every GUI element extends from this class in some way.
 */
public abstract class RComponent implements PConstants, RInputListener {

    // Reference to the RotomGui instance that owns this component
    protected final RotomGui gui;

    // Link to the parent folder (if null then it is a root component) // TODO root handling
    protected final RGroup parent; // TODO LazyGui & G4P

    // TODO LazyGui
    public final String path;  // TODO protected?
    // TODO LazyGui
    protected final String name;

    // Top left position of component in pixels (absolute)
    protected final PVector pos = new PVector(); // TODO LazyGui & G4P
    // Width and height of component in pixels
    protected final PVector size = new PVector(); // TODO LazyGui & G4P
    // TODO LazyGui
    protected float heightInCells = 1;
// protected int col; TODO push this sort of config to layout

    protected int localTheme = RThemeStore.getGlobalSchemeNum(); // TODO G4P
    protected RTheme palette = null;

    // TODO LazyGui
    protected boolean isDraggable = true;
    // Set to true when mouse is dragging, set to false on mouse released
    protected boolean isDragged = false; // TODO LazyGui & G4P
    // TODO LazyGui
    protected boolean isMouseOver = false;
    // Is the component visible? (Not Parent Aware)
    protected boolean isVisible = true; // TODO LazyGui & G4P
    // TODO Difference between mouse over and has focus

    // TODO LazyGui
    public final String className = this.getClass().getSimpleName();

    // TODO LazyGui
    // public final NodeType type; TODO Unneeded?

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui the gui for the window that the component is drawn under
     * @param path the path in the component tree
     * @param parent the parent component reference
     */
    protected RComponent(RotomGui gui, String path, RGroup parent) {
        this.gui = gui;
        this.parent = parent;

        this.path = path;
        this.name = extractNameFromPath(path);

        this.palette = RThemeStore.getTheme(localTheme);
    }

    private String extractNameFromPath(String path) {
        if ("".equals(path)) { // this is the root component
            return gui.getSketch().getClass().getSimpleName(); // not using lowercase separated class name after all because it breaks what users expect to see
        }
        String[] split = RPaths.splitByUnescapesSlashesWithoutRemovingThem(path);
        if (split.length == 0) {
            return "";
        }
        String nameWithoutPrefixSlash = RPaths.getNameWithoutPrefixSlash(split[split.length - 1]);
        return RPaths.getDisplayStringWithoutEscapes(nameWithoutPrefixSlash);
    }

    /**
     * Method to calculate the width of the text for the font size
     *
     * @param textToMeasure text to calculate the width of
     * @return width rounded up to whole cells
     */ // TODO is this why cell size is necessary?
    protected float calcTextWidth(String textToMeasure) { // TODO LazyGui
        return RFontStore.calcMainTextWidth(textToMeasure, RLayoutStore.getCell()); // TODO consider Granularity
    }

    /**
     * Method to calculate the width of the name text for the font size
     *
     * @return width rounded up to whole cells
     */
    public float calcNameTextWidth() {
        return calcTextWidth(getVisibleName());
    }

    /**
     * Method to calculate the width of the value text for the font size
     *
     * @return width rounded up to whole cells
     */
    public float findValueTextWidthRoundedUpToWholeCells() {
        return calcTextWidth(getValueAsString());
    }


    /**
     * Sets the background fill color of the component, as part of the draw method
     *
     * @param pg graphics reference to use
     */
    protected void fillBackground(PGraphics pg) { // TODO LazyGui
        if(isMouseOver){
            pg.fill(RThemeStore.getRGBA(RThemeColorType.FOCUS_BACKGROUND));
        } else {
            pg.fill(RThemeStore.getRGBA(RThemeColorType.NORMAL_BACKGROUND));
        }
    }

    /**
     * Sets the foreground fill color of the component, as part of the draw method
     *
     * @param pg graphics reference to use
     */
    protected void fillForeground(PGraphics pg) { // TODO LazyGui
        if(isMouseOver){
            pg.fill(RThemeStore.getRGBA(RThemeColorType.FOCUS_FOREGROUND));
        } else {
            pg.fill(RThemeStore.getRGBA(RThemeColorType.NORMAL_FOREGROUND));
        }
    }

    /**
     * Draw highlighted background for component
     *
     * @param pg graphics reference to use
     */
    protected void highlightBackground(PGraphics pg) { // TODO LazyGui
        pg.noStroke();
        pg.fill(RThemeStore.getRGBA(RThemeColorType.FOCUS_BACKGROUND));
        pg.rect(0,0,size.x,size.y);
    }

    /**
     * Sets the color used to draw lines and borders
     *
     * @param pg graphics reference
     */
    protected void strokeForeground(PGraphics pg) { // TODO LazyGui
        if (isMouseOver) {
            pg.stroke(RThemeStore.getRGBA(RThemeColorType.FOCUS_FOREGROUND));
        } else {
            pg.stroke(RThemeStore.getRGBA(RThemeColorType.NORMAL_FOREGROUND));
        }
    }

    /**
     * Draw the text to the left
     *
     * @param pg graphics reference to use
     * @param text the text to draw
     */
    protected void drawTextLeft(PGraphics pg, String text){ // TODO LazyGui
        fillForeground(pg);
        String trimmedText = RFontStore.substringToFit(pg, text, size.x,true);
        pg.textAlign(LEFT, CENTER);
        pg.text(trimmedText, RFontStore.getMarginX(), RLayoutStore.getCell() - RFontStore.getMarginY());
    }

    /**
     * Draw the text to the right
     *
     * @param pg graphics reference to use
     * @param text the text to draw
     * @param fillBackground whether to fill the background
     */
    protected void drawTextRight(PGraphics pg, String text, boolean fillBackground) { // TODO LazyGui
        if(fillBackground){
            float backdropBuffer = RLayoutStore.getCell() * 0.5f;
            float w = pg.textWidth(text) + RFontStore.getMarginX() + backdropBuffer;
            drawBackdropRight(pg, w);
        }
        pg.textAlign(RIGHT, CENTER);
        pg.text(text,size.x - RFontStore.getMarginX(),size.y - RFontStore.getMarginY());
    }

    /**
     * Draw the text to the right, with no overlap
     *
     * @param pg graphics reference to use
     * @param leftText left-centered text to draw
     * @param rightText right-centered text to draw
     * @param fillBackground whether to fill the background
     */
    protected void drawTextRightNoOverflow(PGraphics pg, String leftText, String rightText, boolean fillBackground) { // TODO LazyGui
        pg.textAlign(RIGHT, CENTER);
        String trimmedTextLeft = RFontStore.substringToFit(pg, leftText, size.x,true);
        float leftOffset = pg.textWidth(trimmedTextLeft)+(RFontStore.getMarginX()*2);
        String trimmedRightText = RFontStore.substringToFit(pg, leftText, size.x - leftOffset,true);
        if(fillBackground){
            float w = pg.textWidth(trimmedRightText) + RFontStore.getMarginX() * 2;
            drawBackdropRight(pg, w);
        }
        pg.text(trimmedRightText,size.x - RFontStore.getMarginX(), size.y - RFontStore.getMarginY());
    }

    /**
     * Draw the backdrop to the right
     *
     * @param pg graphics reference to use
     * @param backdropSize size of the background
     */
    protected void drawBackdropRight(PGraphics pg, float backdropSize) { // TODO LazyGui
        pg.pushStyle();
        fillBackground(pg);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(size.x-backdropSize, 0, backdropSize, size.y);
        pg.popStyle();
    }

    /**
     * Method to draw the background of the component
     *
     * @param pg graphics reference to use
     */
    protected abstract void drawBackground(PGraphics pg); // TODO LazyGui

    /**
     * Method to draw the foreground of the component
     *
     * @param pg graphics reference to use
     * @param name name of the component
     */
    protected abstract void drawForeground(PGraphics pg, String name); // TODO LazyGui

    /**
     * Main update function, only called when the parent window containing this component is open.
     * @see RComponent#drawBackground(PGraphics)
     * @param pg main PGraphics of the gui of the same size as the main PApplet canvas to draw on
     */
    public final void draw(PGraphics pg) { // TODO LazyGui
        // the component knows its absolute position but here the current matrix is already translated to it
        if(isMouseOver){
            highlightBackground(pg);
        }

        pg.pushMatrix();
        pg.pushStyle();
        drawBackground(pg);
        pg.popStyle();
        pg.popMatrix();

        pg.pushMatrix();
        pg.pushStyle();
        drawForeground(pg, name);
        pg.popStyle();
        pg.popMatrix();
    }

    protected void onValueChangeEnd() { // TODO LazyGui
        if(parent != null){
            // go up the parent chain recursively and keep notifying of a change until the root is reached
            parent.onValueChangeEnd();
        }
    }

    /**
     * Handle a pressed key while over the component
     *
     * @param e the pressed key
     * @param x x position
     * @param y y position
     */
    public void keyPressedOverComponent(RKeyEvent e, float x, float y) { // TODO LazyGui
        // TODO Needed?
        // TODO Difference between has focus and just mouse over
    }

    /**
     * Method to handle the component's reaction to the mouse continuing to be dragged.
     *
     * @param e the change made by the mouse
     */
    public void mouseDragContinues(RMouseEvent e) {
        isMouseOver = true;
    }

    @Override
    public void mousePressed(RMouseEvent e) { // TODO LazyGui
        isDragged = true;
        isMouseOver = true;
    }

    /**
     * Method to handle the component's reaction to the mouse being released outside of itself
     *
     * @param e the change made by the mouse
     */
    public void mouseReleasedAnywhere(RMouseEvent e) { // TODO LazyGui
        if(isDragged){
            e.consume();
        }
        isDragged = false;
    }

    /**
     * Method to handle the component's reaction to the mouse being released over it
     *
     * @param e the change made by the mouse
     */
    public void mouseReleasedOverComponent(RMouseEvent e) { // TODO LazyGui
        // NOOP
    }

    /**
     * The components must know its absolute position and size, so it can respond to user input events
     *
     * @param x absolute screen x
     * @param y absolute screen y
     * @param w absolute screen width
     * @param h absolute screen height
     */
    public void updateCoordinates(float x, float y, float w, float h) { // TODO LazyGui
        pos.x = x;
        pos.y = y;
        size.x = w;
        size.y = h;
    }

    /**
     * Secondary update function, called for all components every frame, regardless of their parent window's closed state.
     */
    public void updateValues() { // TODO LazyGui
        // NOOP
    }

    /**
     * Method to set hide the component if not root
     */
    public void hide() { // TODO LazyGui
//        if(this.equals(NodeTree.getRoot())){ // TODO Root component Concept?
//            return;
//        }
        isVisible = false;
    }

    /**
     * Method to set the component to visible
     */
    public void show() {
        isVisible = true;
    }

    /**
     * Method to suggest the width
     *
     * @return the width of the component
     */
    public abstract float suggestWidth();

    /**
     * Method to calculate the height
     *
     * @return the height of the component
     */
    public float getHeight(){ // TODO LazyGui
        //return heightInCells * RLayoutStore.getCell();
        return size.y;
    }

    public String getName(){ // TODO LazyGui
        return name;
    }

    /**
     * Get the parent component. If null then this is a top-level component
     *
     * @return return parent, null if top level
     */
    public RGroup getParent() { // TODO LazyGui & G4P
        return parent;
    }

    /**
     * Get the parent component. If null then this is a top-level component
     *
     * @return return parent, null if top level
     */
    public RFolder getParentFolder() { // TODO LazyGui & G4P
        RGroup p = parent;
        while(p != null || !(p instanceof RFolder)){
            p = p.getParent();
        }
        return (RFolder) p;
    }

    public float getPosX() {
        return pos.x;
    }

    public float getPosY() {
        return pos.y;
    }

    public PVector getPosition() {
        return new PVector(pos.x,pos.y);
    }

    public PVector getPreferredSize(){
        return new PVector(suggestWidth(),getHeight());
    }



    public String getValueAsString(){
        return "";
    }

    public String getVisibleName(){ // TODO LazyGui
        return name;
    }

    /**
     * Method to calculate the width
     *
     * @return the width of the component
     */
    public float getWidth(){ // TODO LazyGui
        //return heightInCells * RLayoutStore.getCell();
        return size.x;
    }


    public boolean isMouseOver() {
        return isMouseOver;
    }

    /**
     * Method to check if this component is visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isVisible(){ // TODO LazyGui
        return isVisible;
    }

    /**
     * Method to check if this window of this component is visible, and if all parent nodes are visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isVisibleParentAware() { // TODO LazyGui
        boolean visible = isVisible();
        if (parent != null) {
            return visible && parent.isVisible();
        }
        return visible;
    }

    public void setMouseOver(boolean mouseOver) {
        isMouseOver = mouseOver;
    }

    /**
     * Method to check if the parent window of this component is visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isParentWindowVisible(){ // TODO LazyGui TODO Needed?
        RFolder pFolder = getParentFolder();
        if(pFolder == null || pFolder.getWindow() == null){
            return !RLayoutStore.isGuiHidden();
        }
        return pFolder.isWindowVisible();
    }

    /**
     * Method to check if the parent window of this component is open
     *
     * @return true if open, false otherwise
     */
    public boolean isParentWindowOpen(){ // TODO LazyGui TODO Needed?
        if(parent == null || !(parent instanceof RFolder) || ((RFolder) parent).getWindow() == null){
            return false;
        }
        return ((RFolder) parent).isWindowVisible();
    }

    public void setIsMouseOverThisNodeOnly() { // TODO LazyGui
        isMouseOver = true;
//        NodeTree.setAllOtherNodesMouseOverToFalse(this);
    }

    /**
     * Used by value nodes to load state from json
     *
     * @param loadedNode Json state of loaded component
     */
//    public void overwriteState(JsonElement loadedNode){ // TODO Jackson
//        // NOOP
//    }
}
