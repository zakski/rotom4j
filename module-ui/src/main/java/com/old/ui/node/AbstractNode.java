package com.old.ui.node;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.old.ui.constants.GlobalReferences;
import com.old.ui.input.MouseInteractable;
import com.old.ui.input.keys.GuiKeyEvent;
import com.old.ui.input.mouse.GuiMouseEvent;
import com.old.ui.constants.theme.ThemeColorType;
import com.old.ui.constants.theme.ThemeStore;
import com.old.ui.node.impl.FolderNode;
import com.old.ui.store.ChangeListener;
import com.old.ui.store.FontStore;
import com.old.ui.store.LayoutStore;
import processing.core.PGraphics;
import processing.core.PVector;

import static com.old.ui.store.LayoutStore.cell;
import static processing.core.PApplet.ceil;
import static processing.core.PConstants.*;

/**
 *
 * A node in the GUI Tree representing one or more of the following
 *  - a folder of other nodes
 *  - a transient preview of some value
 *  - a directly adjustable value that is returned to the user
 */
public abstract class AbstractNode implements MouseInteractable {

    @Expose
    public final String className = this.getClass().getSimpleName();

    @Expose
    public final String path;

    @Expose
    public final NodeType type;

    protected int col;
    protected float heightInCells = 1;


    protected final String name;
    public final FolderNode parent;
    public final PVector pos = new PVector();
    public final PVector size = new PVector();

     public boolean isInlineNodeDragged = false;
    public boolean isInlineNodeDraggable = true;
    public boolean isMouseOverNode = false;

    private boolean isVisible = true;


    /**
     *
     * @param type
     * @param path
     * @param parentFolder
     */
    protected AbstractNode(NodeType type, String path, FolderNode parentFolder) {
        this(type, path, parentFolder,0);
    }

    /**
     *
     * @param type
     * @param path
     * @param parentFolder
     */
    protected AbstractNode(NodeType type, String path, FolderNode parentFolder, int col) {
        this.path = path;
        this.name = extractNameFromPath(path);
        this.type = type;
        this.parent = parentFolder;
        this.col = (parent != null && parent.getLayout() == LayoutType.VERTICAL_X_COL)?Math.max(0,col):0;
    }

    private String extractNameFromPath(String path) {
        if ("".equals(path)) { // this is the root node
            String overridingSketchName = LayoutStore.getOverridingSketchName();
            if(overridingSketchName != null){
                return overridingSketchName;
            }
            return GlobalReferences.app.getClass().getSimpleName(); // not using lowercase separated class name after all because it breaks what users expect to see
        }
        String[] split = NodePaths.splitByUnescapesSlashesWithoutRemovingThem(path);
        if (split.length == 0) {
            return "";
        }
        String nameWithoutPrefixSlash = NodePaths.getNameWithoutPrefixSlash(split[split.length - 1]);
        return NodePaths.getDisplayStringWithoutEscapes(nameWithoutPrefixSlash);
    }

    /**
     * Sets the color used to fill the background of the node
     *
     * @param pg graphics reference to use
     */
    protected void fillBackgroundBasedOnMouseOver(PGraphics pg) {
        if(isMouseOverNode){
            pg.fill(ThemeStore.getColor(ThemeColorType.FOCUS_BACKGROUND));
        } else {
            pg.fill(ThemeStore.getColor(ThemeColorType.NORMAL_BACKGROUND));
        }
    }

    /**
     * Sets the color used to fill the foreground of the node
     *
     * @param pg graphics reference to use
     */
    protected void fillForegroundBasedOnMouseOver(PGraphics pg) {
        if(isMouseOverNode){
            pg.fill(ThemeStore.getColor(ThemeColorType.FOCUS_FOREGROUND));
        } else {
            pg.fill(ThemeStore.getColor(ThemeColorType.NORMAL_FOREGROUND));
        }
    }

    /**
     * Method to calculate the width of the text for the font size
     *
     * @param textToMeasure text to calculate the width of
     * @return width rounded up to whole cells
     */
    protected float findTextWidthRoundedUpToWholeCells(String textToMeasure) {
        PGraphics textWidthProvider = FontStore.getMainFontUtilsProvider();
        float leftTextWidth = textWidthProvider.textWidth(textToMeasure);
        return ceil(leftTextWidth / cell) * cell;
    }

    /**
     * Method to calculate the width of the name text for the font size
     *
    * @return width rounded up to whole cells
     */
    public float findNameTextWidthRoundedUpToWholeCells() {
        PGraphics textWidthProvider = FontStore.getMainFontUtilsProvider();
        float leftTextWidth = textWidthProvider.textWidth(getVisibleName());
        return ceil(leftTextWidth / cell) * cell;
    }

    /**
     * Method to calculate the width of the value text for the font size
     *
     * @return width rounded up to whole cells
     */
    public float findValueTextWidthRoundedUpToWholeCells() {
        PGraphics textWidthProvider = FontStore.getMainFontUtilsProvider();
        float leftTextWidth = textWidthProvider.textWidth(getValueAsString());
        return ceil(leftTextWidth / cell) * cell;
    }


    /**
     * Draw a highlighted background of the node
     *
     * @param pg graphics reference to use
     */
    protected void highlightNodeBackground(PGraphics pg) {
        pg.noStroke();
        pg.fill(ThemeStore.getColor(ThemeColorType.FOCUS_BACKGROUND));
        pg.rect(0,0,size.x,size.y);
    }

    /**
     * Sets the color used to draw lines and borders around the node
     *
     * @param pg graphics reference
     */
    protected void strokeForegroundBasedOnMouseOver(PGraphics pg) {
        if (isMouseOverNode) {
            pg.stroke(ThemeStore.getColor(ThemeColorType.FOCUS_FOREGROUND));
        } else {
            pg.stroke(ThemeStore.getColor(ThemeColorType.NORMAL_FOREGROUND));
        }
    }

    /**
     * Draw the text to the left
     *
     * @param pg graphics reference to use
     * @param text the text to draw
     */
    protected void drawLeftText(PGraphics pg, String text){
        fillForegroundBasedOnMouseOver(pg);
        String trimmedText = FontStore.getSubstringFromStartToFit(pg, text, size.x - FontStore.textMarginX);
        pg.textAlign(LEFT, CENTER);
        pg.text(trimmedText, FontStore.textMarginX, cell - FontStore.textMarginY);
    }

    /**
     * Draw the text to the right
     *
     * @param pg graphics reference to use
     * @param text the text to draw
     * @param fillBackground whether to fill the backgrounf
     */
    protected void drawRightText(PGraphics pg, String text, boolean fillBackground) {
        if(fillBackground){
            float backdropBuffer = cell * 0.5f;
            float w = pg.textWidth(text) + FontStore.textMarginX + backdropBuffer;
            drawRightBackdrop(pg, w);
        }
        pg.textAlign(RIGHT, CENTER);
        pg.text(text,size.x - FontStore.textMarginX,size.y - FontStore.textMarginY);
    }

    protected void drawRightTextToNotOverflowLeftText(PGraphics pg, String rightText, String leftText, boolean fillBackground) {
        pg.textAlign(RIGHT, CENTER);
        String trimmedTextLeft = FontStore.getSubstringFromStartToFit(pg, leftText, size.x - FontStore.textMarginX);
        float leftOffset = pg.textWidth(trimmedTextLeft)+(FontStore.textMarginX*2);
        String trimmedRightText = FontStore.getSubstringFromStartToFit(pg, rightText, size.x - FontStore.textMarginX -leftOffset);
        if(fillBackground){
            float w = pg.textWidth(trimmedRightText) + FontStore.textMarginX * 2;
            drawRightBackdrop(pg, w);
        }
        pg.text(trimmedRightText,size.x - FontStore.textMarginX,size.y - FontStore.textMarginY);
    }

    /**
     * Draw the backdrop to the right
     *
     * @param pg graphics reference to use
     * @param backdropSize size of the background
     */
    protected void drawRightBackdrop(PGraphics pg, float backdropSize) {
        pg.pushStyle();
        fillBackgroundBasedOnMouseOver(pg);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(size.x-backdropSize, 0, backdropSize, size.y);
        pg.popStyle();
    }

    /**
     * Method to draw the background of the node
     *
     * @param pg graphics reference to use
     */
    protected abstract void drawNodeBackground(PGraphics pg);

    /**
     * Method to draw the foreground of the node
     *
     * @param pg graphics reference to use
     * @param name name of the node
     */
    protected abstract void drawNodeForeground(PGraphics pg, String name);

    /**
     * Main update function, only called when the parent window containing this node is open.
     * @see AbstractNode#drawNodeBackground(PGraphics)
     * @param pg main PGraphics of the gui of the same size as the main PApplet canvas to draw on
     */
    public final void updateDrawInlineNode(PGraphics pg) {
        // the node knows its absolute position but here the current matrix is already translated to it
        if(isMouseOverNode){
            highlightNodeBackground(pg);
        }
        pg.pushMatrix();
        pg.pushStyle();
        drawNodeBackground(pg);
        pg.popMatrix();
        pg.popStyle();
        pg.pushMatrix();
        pg.pushStyle();
        drawNodeForeground(pg, name);
        pg.popMatrix();
        pg.popStyle();
    }

    /**
     * Handle a pressed key while over the node
     *
     * @param e the pressed key
     * @param x x position
     * @param y y position
     */
    public void keyPressedOverNode(GuiKeyEvent e, float x, float y) {

    }

    @Override
    public void mousePressedEvent(GuiMouseEvent e) {
        isInlineNodeDragged = true;
        isMouseOverNode = true;
    }

    @Override
    public void mouseReleasedAnywhereEvent(GuiMouseEvent e) {
        if(isInlineNodeDragged){
            e.setConsumed(true);
        }
        isInlineNodeDragged = false;
    }

    @Override
    public void mouseDragNodeContinueEvent(GuiMouseEvent e) {
        isMouseOverNode = true;
    }

    public void onValueChangingActionEnded() {
        ChangeListener.onValueChangingActionEnded(path);
        if(parent != null){
            // go up the parent chain recursively and keep notifying of a change until the root node is reached
            parent.onValueChangingActionEnded();
        }
    }

    /**
     * Used by value nodes to load state from json
     *
     * @param loadedNode Json state of loaded node
     */
    public void overwriteState(JsonElement loadedNode){
        // NOOP
    }

    /**
     * The node must know its absolute position and size, so it can respond to user input events
     *
     * @param x absolute screen x
     * @param y absolute screen y
     * @param w absolute screen width
     * @param h absolute screen height
     */
    public void updateInlineNodeCoordinates(float x, float y, float w, float h) {
        pos.x = x;
        pos.y = y;
        size.x = w;
        size.y = h;
    }

    /**
     * Secondary update function, called for all nodes every frame, regardless of their parent window's closed state.
     */
    public void updateValuesRegardlessOfParentWindowOpenness(){

    }

    /**
     * Method to set hide the node if not root
     */
    public void hideInlineNode() {
        if(this.equals(NodeTree.getRoot())){
            return;
        }
        isVisible = false;
    }

    /**
     * Method to set the node to visible
      */
    public void showInlineNode() {
        isVisible = true;
    }

    public String getName(){
        return name;
    }

    public String getVisibleName(){
        return name;
    }

    public int getColumn(){
        return col;
    }

    public abstract float getRequiredWidthForHorizontalLayout();

    public String getValueAsString(){
        return "";
    }

    /**
     * Method to calculate the height
     *
     * @return the height of the node
     */
    public float getHeight(){
       return heightInCells * cell;
    }

    /**
     * Method to check if this node is visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isVisible(){
        return isVisible;
    }

    /**
     * Method to check if this window of this node is visible, and if all parent nodes are visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isVisibleParentAware(){
        return NodeTree.areAllParentsInlineVisible(this) && isVisible();
    }

    /**
     * Method to check if the parent window of this node is visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isParentWindowVisible(){
        if(parent == null || parent.window == null){
            return !LayoutStore.isGuiHidden();
        }
        return parent.isWindowVisible();
    }

    /**
     * Method to check if the parent window of this node is open
     *
     * @return true if open, false otherwise
     */
    public boolean isParentWindowOpen(){
        if(parent == null || parent.window == null){
            return false;
        }
        return parent.isWindowVisible();
    }

    public void setColumn(int col){
        this.col = (parent != null && parent.getLayout() == LayoutType.VERTICAL_X_COL)?Math.max(0,col):0;
    }

    public void setIsMouseOverThisNodeOnly(){
        isMouseOverNode = true;
        NodeTree.setAllOtherNodesMouseOverToFalse(this);
    }
}
