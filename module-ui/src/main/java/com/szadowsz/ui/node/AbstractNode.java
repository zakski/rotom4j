package com.szadowsz.ui.node;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.input.keys.GuiKeyEvent;
import com.szadowsz.ui.input.mouse.GuiMouseEvent;
import com.szadowsz.ui.constants.theme.ThemeColorType;
import com.szadowsz.ui.constants.theme.ThemeStore;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.store.LayoutStore;
import processing.core.PGraphics;
import processing.core.PVector;

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
    public
    String path;
    @Expose
    public
    NodeType type;

    public final FolderNode parent;
    public final PVector pos = new PVector();
    public final PVector size = new PVector();
    public final String name;

    public float masterInlineNodeHeightInCells = 1;
    public boolean isInlineNodeDragged = false;
    public boolean isInlineNodeDraggable = true;
    public boolean isMouseOverNode = false;

    private boolean isInlineNodeVisible = true;

    public void setIsMouseOverThisNodeOnly(){
        isMouseOverNode = true;
        NodeTree.setAllOtherNodesMouseOverToFalse(this);
    }

    /**
     *
     * @param type
     * @param path
     * @param parentFolder
     */
    protected AbstractNode(NodeType type, String path, FolderNode parentFolder) {
        this.path = path;
        this.name = extractNameFromPath(path);
        this.type = type;
        this.parent = parentFolder;
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
     *
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


    /**
     * Used by value nodes to load state from json
     *
     * @param loadedNode Json state of loaded node
     */
    public void overwriteState(JsonElement loadedNode){

    }

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
        isInlineNodeVisible = false;
    }

    /**
     * Method to set the node to visible
      */
    public void showInlineNode() {
        isInlineNodeVisible = true;
    }

    /**
     * Method to check if this node is visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isInlineNodeVisible(){
        return isInlineNodeVisible;
    }

    /**
     * Method to check if this window of this node is visible, and if all parent nodes are visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isInlineNodeVisibleParentAware(){
        return NodeTree.areAllParentsInlineVisible(this) && isInlineNodeVisible;
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
        return !parent.window.closed;
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
        return !parent.window.closed;
    }
}
