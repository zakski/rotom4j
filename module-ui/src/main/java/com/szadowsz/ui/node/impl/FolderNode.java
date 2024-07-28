package com.szadowsz.ui.node.impl;

import com.google.gson.annotations.Expose;
import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.constants.theme.ThemeColorType;
import com.szadowsz.ui.constants.theme.ThemeStore;
import com.szadowsz.ui.input.mouse.GuiMouseEvent;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.NodeType;
import com.szadowsz.ui.store.JsonSaveStore;
import com.szadowsz.ui.store.LayoutStore;
import com.szadowsz.ui.window.Window;
import com.szadowsz.ui.window.WindowManager;
import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.szadowsz.ui.store.LayoutStore.cell;
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

    /**
     * Construct a FolderNode with a Vertical Layout
     *
     * @param path   folder path
     * @param parent parent folder
     */
    public FolderNode(String path, FolderNode parent) {
        this(path, parent, LayoutType.VERTICAL_1_COL);
        JsonSaveStore.overwriteWithLoadedStateIfAny(this);
    }

    /**
     * Construct a FolderNode with a Specified Layout
     *
     * @param path   folder path
     * @param parent parent folder
     * @param layout the layout to use
     */
    public FolderNode(String path, FolderNode parent, LayoutType layout) {
        super(NodeType.FOLDER, path, parent);
        this.layout = layout;
        JsonSaveStore.overwriteWithLoadedStateIfAny(this);
    }


    private String getInlineDisplayNameOverridableByContents(String name) {
        String overridableName = name;
        return overridableName;
    }

    /**
     * Find a node by its name
     *
     * @param name the name of the node
     * @return the node, if found
     */
    protected AbstractNode findChildByName(String name) {
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        for (AbstractNode node : children) {
            if (node.getName().equals(name)) {
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
        if (name.startsWith("/")) {
            nameStartsWith = name.substring(1);
        }
        for (AbstractNode node : children) {
            if (node.getName().startsWith(nameStartsWith)) {
                return node;
            }
        }
        return null;
    }


    /**
     * @return
     */
    private boolean isFolderActiveJudgingByContents() {
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
        if (isFolderActiveJudgingByContents()) {
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

    private float autosuggestWindowWidthFor1Col() {
        float maximumSpaceTotal = GlobalReferences.appWindow.getWidth();//cell * LayoutStore.defaultWindowWidthInCells;
        if (!LayoutStore.getAutosuggestWindowWidth()) {
            return cell * LayoutStore.defaultWindowWidthInCells;
        }
        float spaceForName = cell * 2;
        float spaceForValue = cell * 2;
        float minimumSpaceTotal = spaceForName + spaceForValue;
        float titleTextWidth = findTextWidthRoundedUpToWholeCells(name);
        spaceForName = PApplet.max(spaceForName, titleTextWidth);
        for (AbstractNode child : children) {
            float nameTextWidth = child.findNameTextWidthRoundedUpToWholeCells();
            spaceForName = PApplet.max(spaceForName, nameTextWidth);
            float valueTextWidth = child.findValueTextWidthRoundedUpToWholeCells();
            spaceForValue = PApplet.max(spaceForValue, valueTextWidth);
        }
        return PApplet.constrain(spaceForName + spaceForValue, minimumSpaceTotal, maximumSpaceTotal);
    }

    private float autosuggestWindowWidthForXCol() {
        float maximumSpaceTotal = cell * LayoutStore.defaultWindowWidthInCells;
        if (!LayoutStore.getAutosuggestWindowWidth()) {
            return maximumSpaceTotal;
        }

        float spaceForName = cell * 2;
        float spaceForValue = cell * 2;
        float minimumSpaceTotal = spaceForName + spaceForValue;
        float titleTextWidth = findTextWidthRoundedUpToWholeCells(name);
        spaceForName = PApplet.max(spaceForName, titleTextWidth);

        Map<Integer,Float> columnNameWidth = new HashMap<>();
        Map<Integer,Float> columnValueWidth = new HashMap<>();
        int maxColumn = 0;

        for (AbstractNode child : children) {
            maxColumn = Math.max(child.getColumn(),maxColumn);
            float nameWidth = columnNameWidth.getOrDefault(child.getColumn(),0.0f);
            float valueWidth = columnValueWidth.getOrDefault(child.getColumn(),0.0f);

            float nameTextWidth = child.findNameTextWidthRoundedUpToWholeCells();;
            float valueTextWidth = child.findValueTextWidthRoundedUpToWholeCells();

            columnNameWidth.put(child.getColumn(),Math.max(nameWidth, nameTextWidth));
            columnValueWidth.put(child.getColumn(),Math.max(valueWidth, valueTextWidth));
        }
        float width = 0.0f;
        for (int c = 0; c <= maxColumn; c++){
            width = width + columnNameWidth.get(c) + columnValueWidth.get(c);
        }
        width = Math.max(spaceForName + spaceForValue,width);

        return PApplet.constrain(width, minimumSpaceTotal, maximumSpaceTotal);
    }

    public float autosuggestWindowWidthForContents() {
        return switch (layout) {
            case VERTICAL_X_COL -> autosuggestWindowWidthForXCol();
            case HORIZONAL -> GlobalReferences.app.width;
            default -> autosuggestWindowWidthFor1Col();
        };
    }


    /**
     * Method to tell the window whether to draw the title
     *
     * @return true if it should draw, false otherwise
     */
    public boolean shouldDrawTitle() {
        return true;
    }

    public LayoutType getLayout() {
        return layout;
    }

    @Override
    public float getRequiredWidthForHorizontalLayout() {
        return autosuggestWindowWidthForContents();
    }

    public List<AbstractNode> getColChildren(int col){
        return children.stream().filter(c -> c.getColumn()== col).toList();
    }

    public float getColWidth(int col){
        float spaceForName = 0;
        float spaceForValue = 0;


        for (AbstractNode child : children) {
            if (col != child.getColumn())
                continue;

            float nameTextWidth = child.findNameTextWidthRoundedUpToWholeCells();
            float valueTextWidth = child.findValueTextWidthRoundedUpToWholeCells();

            spaceForName = Math.max(spaceForName, nameTextWidth);
            spaceForValue = Math.max(spaceForValue, valueTextWidth);
        }

        return  spaceForName + spaceForValue;
    }
}
