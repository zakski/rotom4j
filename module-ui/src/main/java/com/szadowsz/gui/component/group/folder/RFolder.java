package com.szadowsz.gui.component.group.folder;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.config.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.window.pane.RWindowPane;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import static com.old.ui.store.LayoutStore.cell;
import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.CORNER;

/**
 * Folder Component that controls the visibility of its child components in a separate internal window
 *
 * It opens the window with child components when clicked.
 */
public class RFolder extends RGroup {

    protected RWindowPane window; // TODO LazyGui

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    public RFolder(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
    }

    protected String getInlineDisplayNameOverridableByContents(String name) { // TODO LazyGui
        return name;
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
    protected void drawForeground(PGraphics pg, String name) {
        String displayName = getInlineDisplayNameOverridableByContents(name);
        drawTextLeft(pg, displayName);
        drawBackdropRight(pg, RLayoutStore.getCell());
        drawMiniatureWindowIcon(pg);
    }

    /**
     *
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     *
     * @return
     */
    public RWindowPane getWindow() {
        return window;
    }

    /**
     *
     * @return
     */
    public boolean isWindowVisible() {
        return false;
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
    public void setLayout(RLayoutBase layout) {
        this.layout = layout;
        layout.setGroup(this);
        window.reinitialiseBuffer();
    }

    /**
     *
     * @param pane
     */
    public void setWindow(RWindowPane pane) {
        window = pane;
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

        float titleTextWidth = RFontStore.calcMainTextWidth(name,RLayoutStore.getCell());
        spaceForName = PApplet.max(spaceForName, titleTextWidth);
        PVector preferredSize = layout.calcPreferredSize(getName(),children);
        spaceTotal = PApplet.max(spaceTotal, preferredSize.x);
        return PApplet.constrain(spaceTotal, minimumSpaceTotal, maximumSpaceTotal);
    }

    public float suggestWindowWidthInCells() {
        return RLayoutStore.getWindowWidthInCells();
    }

    @Override
    public float suggestWidth() {
        return autosuggestWindowWidthForContents();
    }


    @Override
    public void keyPressed(RKeyEvent keyEvent, float mouseX, float mouseY) {
        if (!isWindowVisible()){
            return;
        }
        RComponent underMouse = findComponentAt(mouseX, mouseY);
        switch (underMouse){
            case null -> {}// NOOP
            case RGroup g -> g.keyPressed(keyEvent,mouseX,mouseY);
            case RComponent c -> keyPressedOver(keyEvent, mouseX, mouseY);
        }
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float adjustedMouseY){
        super.mousePressed(mouseEvent,adjustedMouseY);
        gui.getWinManager().uncoverOrCreateWindow(this);
        gui.getWinManager().setFocus(window);
        this.isDragged = false;
    }

    @Override
    public void insertChild(RComponent child) {
        children.add(child);
        if (getWindow() != null) {
            getWindow().resizeForContents(true);
            getWindow().reinitialiseBuffer();
        }
    }
}
