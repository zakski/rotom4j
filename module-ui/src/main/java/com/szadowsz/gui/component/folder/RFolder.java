package com.szadowsz.gui.component.folder;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.theme.RThemeColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RLayoutConfig;
import com.szadowsz.gui.window.internal.RWindowInt;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Folder Node that controls the visibility of its child components in a separate internal window
 * 
 * It opens the window with child components when clicked.
 */
public class RFolder extends RGroup { // TODO do we want this as RGroup

    protected RWindowInt window; // TODO LazyGui


    /**
     * Construct a RFolder with a Specified Layout
     *
     * @param path   folder path
     * @param parent parent folder
     */
    public RFolder(RotomGui gui, String path, RGroup parent) { // TODO LazyGui
        super(gui,path, parent);
    }

    /**
     * Find a node by its name
     *
     * @param name the name of the node
     * @return the node, if found
     */
    protected RComponent findChildByName(String name) { // TODO LazyGui
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        for (RComponent node : children) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    protected String getInlineDisplayNameOverridableByContents(String name) { // TODO LazyGui
        return name;
    }

    @Override
    protected PVector calcPreferredSize() {
        return null;
    }

    protected void drawMiniatureWindowIcon(PGraphics pg) { // TODO LazyGui
        strokeForeground(pg);
        fillBackground(pg);
        float previewRectSize = RLayoutStore.getCell() * 0.6f;
        float miniCell = RLayoutStore.getCell() * 0.18f;
        pg.translate(size.x - RLayoutStore.getCell() * 0.5f, size.y * 0.5f);
        pg.rectMode(CENTER);
        pg.rect(0, 0, previewRectSize, previewRectSize); // window border
        pg.rectMode(CORNER);
        pg.translate(-previewRectSize * 0.5f, -previewRectSize * 0.5f);
        pg.pushStyle();
//        if (isFolderActiveJudgingByContents()) { // TODO Better Focus handling
//            pg.fill(RThemeStore.getRGBA(RThemeColorType.FOCUS_FOREGROUND));
//        }
        pg.rect(0, 0, previewRectSize, miniCell); // handle
        pg.popStyle();
        pg.rect(previewRectSize - miniCell, 0, miniCell, miniCell); // close button
    }

    protected float autosuggestWindowWidthFor1Col() { // TODO LazyGui
        float maximumSpaceTotal = gui.getGLWindow().getWidth();//cell * LayoutStore.defaultWindowWidthInCells;
        float spaceForName = RLayoutStore.getCell() * 2;
        float spaceForValue = RLayoutStore.getCell() * 2;
        float minimumSpaceTotal = spaceForName + spaceForValue;
        float titleTextWidth = calcNameTextWidth();
        spaceForName = PApplet.max(spaceForName, titleTextWidth);
        for (RComponent child : children) {
            float nameTextWidth = child.calcNameTextWidth();
            spaceForName = PApplet.max(spaceForName, nameTextWidth);
            float valueTextWidth = child.findValueTextWidthRoundedUpToWholeCells();
            spaceForValue = PApplet.max(spaceForValue, valueTextWidth);
        }
        return PApplet.constrain(spaceForName + spaceForValue, minimumSpaceTotal, maximumSpaceTotal);
    }

    @Override
    protected void drawBackground(PGraphics pg) {
        // NOOP
    }

    /**
     * Sets the color used to fill the foreground of the node
     *
     * @param pg graphics reference to use
     */
    @Override
    protected void fillForeground(PGraphics pg) {
        if(isMouseOver){
            System.out.println("FOCUS " + name);
            pg.fill(RThemeStore.getRGBA(RThemeColorType.FOCUS_FOREGROUND));
        } else {
            pg.fill(RThemeStore.getRGBA(RThemeColorType.NORMAL_FOREGROUND));
        }
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        String displayName = getInlineDisplayNameOverridableByContents(name);
        drawTextLeft(pg, displayName);
        drawBackdropRight(pg, RLayoutStore.getCell());
        drawMiniatureWindowIcon(pg);
    }


    public RWindowInt getWindow() {
        return window;
    }

    public void insertChild(RComponent child){
        if (window != null) {
            window.reinitialiseBuffer();
        }
        children.add(child);
    }

    @Override
    public void mousePressed(RMouseEvent e) {
        super.mousePressed(e);
        gui.getWinManager().uncoverOrCreateWindow(this);
        gui.getWinManager().setFocus(window);
        this.isDragged = false;
    }


    public float autosuggestWindowWidthForContents() {
       return autosuggestWindowWidthFor1Col();
    }

    /**
     * Method to tell the window whether to draw the title
     *
     * @return true if it should draw, false otherwise
     */
    public boolean shouldDrawTitle() {
        return true;
    }

    @Override
    public float suggestWidth() {
        return autosuggestWindowWidthForContents();
    }


    public RLayoutBase getLayout(){
        return layout;
    }

    public boolean isWindowVisible() {
        return window.isVisible();
    }

    @Override
    public void setLayout(RLayoutBase layout) {
        this.layout = layout;
        window.reinitialiseBuffer();
    }

    public void setWindow(RWindowInt win) {
        window = win;
    }

    public void addChild(RComponent n) {
        children.add(n);
    }

    public float suggestWindowWidthInCells() {
        return 0;
    }
}
