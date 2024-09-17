package com.szadowsz.gui.component.folder;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.config.RFontStore;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.theme.RThemeColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.window.internal.RWindowInt;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import static com.szadowsz.ui.store.LayoutStore.cell;


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
    public PVector getPreferredSize() {
        return new PVector(RFontStore.calcMainTextWidth(name, RLayoutStore.getCell()) + RLayoutStore.getCell() * 2,RLayoutStore.getCell());
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

    @Override
    public void mousePressed(RMouseEvent e) {
        super.mousePressed(e);
        gui.getWinManager().uncoverOrCreateWindow(this);
        gui.getWinManager().setFocus(window);
        this.isDragged = false;
    }


    public float autosuggestWindowWidthForContents() {
        if (!RLayoutStore.shouldSuggestWindowWidth()) {
            return cell * RLayoutStore.getWindowWidthInCells();
        }
        float maximumSpaceTotal = gui.getGLWindow().getWidth();//cell * LayoutStore.defaultWindowWidthInCells;

        float spaceForName = RLayoutStore.getCell() * 2;
        float spaceForValue = RLayoutStore.getCell() * 2;
        float spaceTotal = spaceForName + spaceForValue;

        float minimumSpaceTotal = spaceForName + spaceForValue;

        float titleTextWidth = calcNameTextWidth();
        spaceForName = PApplet.max(spaceForName, titleTextWidth);
        PVector preferredSize = layout.calcPreferredSize(getName(),children);
        spaceTotal = PApplet.max(spaceTotal, preferredSize.x);
        return PApplet.constrain(spaceTotal, minimumSpaceTotal, maximumSpaceTotal);
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
        layout.setGroup(this);
        window.reinitialiseBuffer();
    }

    public void setWindow(RWindowInt win) {
        window = win;
    }

    public float suggestWindowWidthInCells() {
        return RLayoutStore.getWindowWidthInCells();
    }
}
