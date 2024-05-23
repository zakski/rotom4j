package com.szadowsz.ui.node.impl;

import com.google.gson.annotations.Expose;
import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.constants.theme.ThemeColorType;
import com.szadowsz.ui.constants.theme.ThemeStore;
import com.szadowsz.ui.input.mouse.GuiMouseEvent;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.NodeType;
import com.szadowsz.ui.store.FontStore;
import com.szadowsz.ui.store.JsonSaveStore;
import com.szadowsz.ui.store.LayoutStore;
import com.szadowsz.ui.window.Window;
import com.szadowsz.ui.window.WindowManager;
import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.concurrent.CopyOnWriteArrayList;

import static com.szadowsz.ui.store.LayoutStore.cell;
import static processing.core.PApplet.ceil;
import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.CORNER;

/**
 * A node that opens a new window with child nodes when clicked.
 */
public class FolderNode extends AbstractNode {

    /**
     * CopyOnWriteArrayList is needed to avoid concurrent modification
     * because the children get drawn by one thread and user input changes the list from another thread
     */
    @Expose
    public final CopyOnWriteArrayList<AbstractNode> children = new CopyOnWriteArrayList<>();

    private final LayoutType layout;

    @Expose
    public
    Window window;

    public float idealWindowWidthInCells = LayoutStore.defaultWindowWidthInCells;

    public FolderNode(String path, FolderNode parent) {
        this(path, parent, LayoutType.VERTICAL);
        JsonSaveStore.overwriteWithLoadedStateIfAny(this);
    }

    public FolderNode(String path, FolderNode parent, LayoutType layout) {
        super(NodeType.FOLDER, path, parent);
        this.layout = layout;
        JsonSaveStore.overwriteWithLoadedStateIfAny(this);
    }



    private String getInlineDisplayNameOverridableByContents(String name) {
        String overridableName = name;
//        String desiredClassName = TextNode.class.getSimpleName();
//        AbstractNode renamingNode = findChildByName("");
//        if(renamingNode == null || !renamingNode.className.contains(desiredClassName)){
//            renamingNode = findChildByNameStartsWith("label");
//        }
//        if(renamingNode == null || !renamingNode.className.contains(desiredClassName)){
//            renamingNode = findChildByNameStartsWith("name");
//        }
//        if(renamingNode != null && renamingNode.className.contains(desiredClassName) && ((TextNode) renamingNode).stringValue.length() > 0){
//            overridableName = ((TextNode) renamingNode).stringValue;
//        }
        return overridableName;
    }

    /**
     * Find a node by its name
     *
     * @param name the name of the node
     * @return the node, if found
     */
    protected AbstractNode findChildByName(String name) {
        if(name.startsWith("/")){
            name = name.substring(1);
        }
        for (AbstractNode node : children) {
            if (node.name.equals(name)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Find a node by a partial name
     *
     * @param nameStartsWith what the name of the node starts with
     * @return the node, if found
     */
    private AbstractNode findChildByNameStartsWith(String nameStartsWith) {
        if(name.startsWith("/")){
            nameStartsWith = name.substring(1);
        }
        for (AbstractNode node : children) {
            if (node.name.startsWith(nameStartsWith)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Method to calculate the width of the text for the font size
     *
     * @param textToMeasure text to calculate the width of
     * @return width rounded up to whole cells
     */
    private float findTextWidthRoundedUpToWholeCells(String textToMeasure) {
        PGraphics textWidthProvider = FontStore.getMainFontUtilsProvider();
        float leftTextWidth = textWidthProvider.textWidth(textToMeasure);
        return ceil(leftTextWidth / cell) * cell;
    }

    /**
     *
     * @return
     */
    private boolean isFolderActiveJudgingByContents(){
//        String desiredClassName = ToggleNode.class.getSimpleName();
//        AbstractNode enabledNode = findChildByName("");
//        if(enabledNode == null || !enabledNode.className.contains(desiredClassName)){
//            enabledNode = findChildByNameStartsWith("active");
//        }
//        if(enabledNode == null || !enabledNode.className.contains(desiredClassName)){
//            enabledNode = findChildByNameStartsWith("enabled");
//        }
//        if(enabledNode == null || !enabledNode.className.contains(desiredClassName)){
//            enabledNode = findChildByNameStartsWith("visible");
//        }
//        return enabledNode != null &&
//                enabledNode.className.contains(desiredClassName) &&
//                ((ToggleNode) enabledNode).valueBoolean;
        return false;
    }

    private void drawMiniatureWindowIcon(PGraphics pg) {
        strokeForegroundBasedOnMouseOver(pg);
        fillBackgroundBasedOnMouseOver(pg);
        float previewRectSize = cell * 0.6f;
        float miniCell = cell * 0.18f;
        pg.translate(size.x - cell * 0.5f, size.y * 0.5f);
        pg.rectMode(CENTER);
        pg.rect(0, 0, previewRectSize, previewRectSize); // window border
        pg.rectMode(CORNER);
        pg.translate(-previewRectSize * 0.5f, -previewRectSize * 0.5f);
        pg.pushStyle();
        if(isFolderActiveJudgingByContents()){
            pg.fill(ThemeStore.getColor(ThemeColorType.FOCUS_FOREGROUND));
        }
        pg.rect(0, 0, previewRectSize, miniCell); // handle
        pg.popStyle();
        pg.rect(previewRectSize - miniCell, 0, miniCell, miniCell); // close button
    }

    @Override
    protected void drawNodeBackground(PGraphics pg) {

    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        String displayName = getInlineDisplayNameOverridableByContents(name);
        drawLeftText(pg, displayName);
        drawRightBackdrop(pg, cell);
        drawMiniatureWindowIcon(pg);
    }

    @Override
    public void mousePressedEvent(GuiMouseEvent e) {
        super.mousePressedEvent(e);
        WindowManager.setFocus(parent.window);
        WindowManager.uncoverOrCreateWindow(this);
        this.isInlineNodeDragged = false;
    }

//
//    protected AbstractNode findChildByName(String name) {
//        if(name.startsWith("/")){
//            name = name.substring(1);
//        }
//        for (AbstractNode node : children) {
//            if (node.name.equals(name)) {
//                return node;
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public void keyPressedOverNode(LazyKeyEvent e, float x, float y) {
//        // copy + paste whole folders of controls
//        if ((e.isControlDown() && e.getKeyCode() == KeyCodes.C)) {
//            ClipboardUtils.setClipboardString(JsonSaveStore.getFolderAsJsonString(this));
//            e.consume();
//        }
//        if (e.isControlDown() && e.getKeyCode() == KeyCodes.V) {
//            String toPaste = ClipboardUtils.getClipboardString();
//            JsonSaveStore.loadStateFromJsonString(toPaste, path);
//            e.consume();
//        }
//    }
//
    public float autosuggestWindowWidthForContents() {
        if (layout == LayoutType.VERTICAL) {
            float maximumSpaceTotal = cell * LayoutStore.defaultWindowWidthInCells;
            if (!LayoutStore.getAutosuggestWindowWidth()) {
                return maximumSpaceTotal;
            }
            float spaceForName = cell * 2;
            float spaceForValue = cell * 2;
            float minimumSpaceTotal = spaceForName + spaceForValue;
            float titleTextWidth = findTextWidthRoundedUpToWholeCells(name);
            spaceForName = PApplet.max(spaceForName, titleTextWidth);
            for (AbstractNode child : children) {
                float nameTextWidth = findTextWidthRoundedUpToWholeCells(child.name);
                spaceForName = PApplet.max(spaceForName, nameTextWidth);
                float valueTextWidth = 0;//findTextWidthRoundedUpToWholeCells(child.getValueAsString());
                spaceForValue = PApplet.max(spaceForValue, valueTextWidth);
            }
            return PApplet.constrain(spaceForName + spaceForValue, minimumSpaceTotal, maximumSpaceTotal);
        } else {
            return GlobalReferences.app.width;
        }
    }

    /**
     * Method to tell the window whether to draw the title
     *
     * @return true if it should draw, false otherwise
     */
    public boolean shouldDrawTitle() {
        return true;
    }
}
