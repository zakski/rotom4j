package com.szadowsz.gui.component.group.folder;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.RComponentBuffer;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.window.internal.RWindowImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.CORNER;

/**
 * Folder Component that controls the visibility of its child components in a separate internal window
 * <p>
 * It opens the window with child components when clicked.
 */
public class RFolder extends RGroup {
    private static final Logger LOGGER = LoggerFactory.getLogger(RFolder.class);

    protected RWindowImpl window; // reference to the companion window

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
        buffer = new RComponentBuffer(this);
    }

    /**
     * Method to allow override of display name
     *
     * @param name default name
     * @return finalised display name
     */
    protected String getDisplayName(String name) { // TODO is this needed?
        return name;
    }

    /**
     * Draw Miniature Window to the right of the component
     *
     * @param pg graphics context to draw onto
     */
    protected void drawMiniatureWindowIcon(PGraphics pg) {
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
        String displayName = getDisplayName(name);
        drawTextLeft(pg, displayName);
        drawBackdropRight(pg, RLayoutStore.getCell());
        drawMiniatureWindowIcon(pg);
    }

    /**
     * Get the Component Tree path
     *
     * @return path to the folder in the component tree
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the Window that the Folder's Children are displayed in
     *
     * @return companion window
     */
    public RWindowImpl getWindow() {
        return window;
    }

    /**
     * Check if the companion window is visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isWindowVisible() {
        return window != null && window.isVisible();
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
     * Method to set the companion window
     *
     * @param pane internal window to set as companion
     */
    public void setWindow(RWindowImpl pane) {
        window = pane;
    }

    @Override
    public void drawToBuffer() {
        buffer.redraw();
    }
    /**
     * Method to auto calculate a good width for the window based on its contents
     *
     * @return the width
     */
    public float autosuggestWindowWidthForContents() {
        if (!RLayoutStore.shouldSuggestWindowWidth()) {
            return RLayoutStore.getCell() * RLayoutStore.getWindowWidthInCells();
        }
        float maximumSpaceTotal = gui.getGLWindow().getWidth();//cell * LayoutStore.defaultWindowWidthInCells;

        float spaceForName = RLayoutStore.getCell() * 2;
        float spaceForValue = RLayoutStore.getCell() * 2;
        float spaceTotal = spaceForName + spaceForValue;

        float minimumSpaceTotal = spaceForName + spaceForValue;

        float titleTextWidth = RFontStore.calcMainTextWidth(name, RLayoutStore.getCell());
        spaceForName = PApplet.max(spaceForName, titleTextWidth);
        PVector preferredSize = layout.calcPreferredSize(getName(), children);
        spaceTotal = PApplet.max(spaceTotal, preferredSize.x);
        return PApplet.constrain(spaceTotal, minimumSpaceTotal, maximumSpaceTotal);
    }

    /**
     * Method to suggest window width in cells
     *
     * @return window width in cells
     */
    public float suggestWindowWidthInCells() {
        // TODO Better?
        // float width = autosuggestWindowWidthForContents();
        // return width / RLayoutStore.getCell() + ((width % RLayoutStore.getCell() == 0)?0:1);
        return RLayoutStore.getWindowWidthInCells();
    }

    @Override
    public float suggestWidth() {
        return autosuggestWindowWidthForContents();
    }

    @Override
    public void keyPressed(RKeyEvent keyEvent, float mouseX, float mouseY) {
        if (!isWindowVisible()) {
            return;
        }
        RComponent underMouse = findVisibleComponentAt(mouseX, mouseY);
        switch (underMouse) {
            case null -> {
            }// NOOP
            case RGroup g -> g.keyPressed(keyEvent, mouseX, mouseY);
            case RComponent c -> keyPressedOver(keyEvent, mouseX, mouseY);
        }
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float adjustedMouseY) {
        super.mousePressed(mouseEvent, adjustedMouseY);
        gui.getWinManager().uncoverOrCreateWindow(this);
        gui.getWinManager().setFocus(window);
        this.isDragged = false;
    }

    @Override
    public void insertChild(RComponent child) {
        children.add(child);
        resetWinBuffer();
    }

    public void resetWinBuffer(){
        RWindowImpl window = getWindow();
        if (window != null) {
            window.reinitialiseBuffer();
        }
    }
}
