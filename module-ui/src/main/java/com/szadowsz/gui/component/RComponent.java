package com.szadowsz.gui.component;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RTheme;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RLayoutConfig;
import com.szadowsz.gui.layout.RRect;
import com.szadowsz.gui.window.internal.RWindowImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PConstants.*;

/**
 * RComponent provides default behaviour for all components in RotomGui.
 * <p>
 * Every GUI element extends from this class in some way.
 */
public abstract class RComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RComponent.class);

    // Reference to the RotomGui instance that owns this component
    protected final RotomGui gui;

    // Link to the parent folder (if null then it is a root component) // TODO root handling
    protected final RGroup parent; // TODO LazyGui & G4P

    protected final String path;  // TODO LazyGui
    protected final String name; // TODO LazyGui

    // TODO LazyGui
    public final String className = this.getClass().getSimpleName();

    protected int localTheme = RThemeStore.getGlobalSchemeNum(); // TODO G4P
    protected RTheme palette = null;

    // Top left position of component in pixels (absolute)
    protected final PVector pos = new PVector(); // TODO LazyGui & G4P
    // Top left position of component in pixels (relative)
    protected final PVector relPos = new PVector();
    // Width and height of component in pixels
    protected final PVector size = new PVector(); // TODO LazyGui & G4P

    protected RLayoutConfig layoutConfig = new RLayoutConfig() {};

    protected RComponentBuffer buffer;

    // Component Config
    protected boolean isDraggable = true; // Is the component able to be dragged
    protected boolean isVisible = true; // Is the component visible? (Not Parent Aware)

    // Component State
    protected boolean isDragged = false; // Set to true when mouse is dragging, set to false on mouse released
    //protected boolean isFocused = false; // TODO Difference between mouse over and has focus
    protected boolean isMouseOver = false; // TODO Difference between mouse over and has focus

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected RComponent(RotomGui gui, String path, RGroup parent) {
        this.gui = gui;
        this.parent = parent;

        this.path = path;
        this.name = extractNameFromPath(path);

        this.palette = RThemeStore.getTheme(localTheme);

        // If overridden in Subclasses, both size.y and heightInCells should be changed
        size.y = RLayoutStore.getCell();
    }

    protected String extractNameFromPath(String path) {
        if ("".equals(path)) { // this is the root component
            return gui.getSketch().getClass().getSimpleName(); // not using lowercase separated class name after all because it breaks what users expect to see
        }
        return RPaths.getNameFromPath(path);
    }

    protected void onValueChange() {
        if (parent != null) {
            // go up the parent chain recursively and keep notifying of a change until the root is reached
            parent.onValueChange();
        }
    }

    protected void onValueChangeEnd() { // TODO LazyGui
        if (parent != null) {
            // go up the parent chain recursively and keep notifying of a change until the root is reached
            parent.onValueChangeEnd();
        }
    }

    /**
     * Sets the background fill color of the component, as part of the draw method
     *
     * @param pg graphics reference to use
     */
    protected void fillBackground(PGraphics pg) { // TODO LazyGui
        if (isMouseOver) {
            LOGGER.trace("Doing Background Focus Fill {} Component", getName());
            pg.fill(RThemeStore.getRGBA(RColorType.FOCUS_BACKGROUND));
        } else {
            pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND));
        }
    }

    /**
     * Sets the foreground fill color of the component, as part of the draw method
     *
     * @param pg graphics reference to use
     */
    protected void fillForeground(PGraphics pg) { // TODO LazyGui
        if (isMouseOver) {
            LOGGER.debug("Doing Foreground Focus Fill {} Component", getName());
            pg.fill(RThemeStore.getRGBA(RColorType.FOCUS_FOREGROUND));
        } else {
            LOGGER.debug("Doing Foreground Normal Fill {} Component", getName());
            pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND));
        }
    }

    /**
     * Draw highlighted background for component
     *
     * @param pg graphics reference to use
     */
    protected void highlightBackground(PGraphics pg) { // TODO LazyGui
        pg.noStroke();
        pg.fill(RThemeStore.getRGBA(RColorType.FOCUS_BACKGROUND));
        pg.rect(0, 0, size.x, size.y);
    }

    /**
     * Sets the color used to draw lines and borders
     *
     * @param pg graphics reference
     */
    protected void strokeForeground(PGraphics pg) { // TODO LazyGui
        if (isMouseOver) {
            pg.stroke(RThemeStore.getRGBA(RColorType.FOCUS_FOREGROUND));
        } else {
            pg.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND));
        }
    }

    /**
     * Draw the text to the left
     *
     * @param pg   graphics reference to use
     * @param text the text to draw
     */
    protected void drawTextLeft(PGraphics pg, String text) { // TODO LazyGui
        fillForeground(pg);
        String trimmedText = RFontStore.substringToFit(pg, text, size.x, true);
        pg.textAlign(LEFT, CENTER);
        pg.text(trimmedText, RFontStore.getMarginX(), RLayoutStore.getCell() - RFontStore.getMarginY());
    }

    /**
     * Draw the text to the right
     *
     * @param pg             graphics reference to use
     * @param text           the text to draw
     * @param fillBackground whether to fill the background
     */
    protected void drawTextRight(PGraphics pg, String text, boolean fillBackground) { // TODO LazyGui
        if (fillBackground) {
            float backdropBuffer = RLayoutStore.getCell() * 0.5f;
            float w = pg.textWidth(text) + RFontStore.getMarginX() + backdropBuffer;
            drawBackdropRight(pg, w);
        }
        pg.textAlign(RIGHT, CENTER);
        pg.text(text, size.x - RFontStore.getMarginX(), size.y - RFontStore.getMarginY());
    }

    /**
     * Draw the text to the right, with no overlap
     *
     * @param pg             graphics reference to use
     * @param leftText       left-centered text to draw
     * @param rightText      right-centered text to draw
     * @param fillBackground whether to fill the background
     */
    protected void drawTextRightNoOverflow(PGraphics pg, String leftText, String rightText, boolean fillBackground) { // TODO LazyGui
        pg.textAlign(RIGHT, CENTER);
        String trimmedTextLeft = RFontStore.substringToFit(pg, leftText, size.x, true);
        float leftOffset = pg.textWidth(trimmedTextLeft) + (RFontStore.getMarginX() * 2);
        String trimmedRightText = RFontStore.substringToFit(pg, rightText, size.x - leftOffset, true);
        if (fillBackground) {
            float w = pg.textWidth(trimmedRightText) + RFontStore.getMarginX() * 2;
            drawBackdropRight(pg, w);
        }
        pg.text(trimmedRightText, size.x - RFontStore.getMarginX(), size.y - RFontStore.getMarginY());
    }

    /**
     * Draw the backdrop to the right
     *
     * @param pg           graphics reference to use
     * @param backdropSize size of the background
     */
    protected void drawBackdropRight(PGraphics pg, float backdropSize) { // TODO LazyGui
        pg.pushStyle();
        fillBackground(pg);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(size.x - backdropSize, 0, backdropSize, size.y);
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
     * @param pg   graphics reference to use
     * @param name name of the component
     */
    protected abstract void drawForeground(PGraphics pg, String name); // TODO LazyGui


    /**
     * Method to draw the foreground of the component
     *
     * @param pg graphics reference to use
     */
    protected void drawContent(PGraphics pg) {
        // the component knows its absolute position but here the current matrix is already translated to it
        if (isMouseOver) {
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

    /**
     * Make a call to flag a redraw of the component buffer due to a change in state, most likely by user input.
     *
     * This should not need to call redraw on parent buffers, because the parent should flag it needs to redraw after
     * seeing the mouse/key event consumed.
     */
    protected void redrawBuffers() {
        if (isVisibleParentAware(null)) {
            buffer.invalidateBuffer();
            if (parent != null && !(parent instanceof RFolder)) {
                parent.redrawBuffers();
            }
            RWindowImpl win = getParentWindow(); // TODO check if needed
            if (win != null) {
                win.redrawBuffer();
            }
        }
    }

    public void resetBuffer() {
        if (isVisibleParentAware(null)) {
            buffer.resetBuffer();
            RGroup parent = getParent();
            if (parent != null && !(parent instanceof RFolder)) {
                parent.resetBuffer();
            }
            RWindowImpl win = getParentWindow(); // TODO check if needed
            if (win != null) {
                win.redrawBuffer();
            }
        }
    }

    /**
     * Secondary update function, called for all components every frame, regardless of their parent window's closed state.
     */
    public void updateValues() { // TODO LazyGui
        // NOOP
    }

    /**
     * Get Component Name for ID
     *
     * @return Component name
     */
    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public RRect getBounds(){
        return new RRect(pos.x, pos.y, size.x, size.y);
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
        while (p != null && !(p instanceof RFolder)) {
            p = p.getParent();
        }
        return (RFolder) p;
    }

    public RWindowImpl getParentWindow() {
        RFolder folder =  getParentFolder();
        if (folder != null){
            return folder.getWindow();
        } else {
            return null;
        }
    }

    /**
     * Get X Coordinate of the Component Start Position
     *
     * @return Top Left X Coordinate
     */
    public float getPosX() {
        return pos.x;
    }

    /**
     * Get Y Coordinate of the Component Start Position
     *
     * @return Top Left Y Coordinate
     */
    public float getPosY() {
        return pos.y;
    }

    public float getRelPosX() {
        return relPos.x;
    }

    public float getRelPosY() {
        return relPos.y;
    }


    /**
     * Get the Component Width (Left to Right)
     *
     * @return The Width of the Component
     */
    public float getWidth() {
        return size.x;
    }

    /**
     * Get the Component Height (Top to Bottom)
     *
     * @return The Height of the Component
     */
    public float getHeight() {
        return size.y;
    }

    public RotomGui getGui() {
        return gui;
    }


    public String getPath() {
        return path;
    }


    public PVector getPosition() {
        return pos.copy();
    }

    public PVector getRelPosition() {
        return new PVector(relPos.x, relPos.y);
    }

    public PVector getRelPosTo(RGroupDrawable group) {
        return new PVector(pos.x - group.getPosX(), pos.y - group.getPosY());
    }

    /**
     * Get the preferred size characteristics
     *
     * @return width and height in a PVector
     */
    public PVector getPreferredSize() {
        return new PVector(suggestWidth(), getHeight());
    }

    /**
     * Get the size characteristics
     *
     * @return width and height in a PVector
     */
    public PVector getSize() {
        return new PVector(getWidth(), getHeight());
    }

    public PVector getBufferSize() {
        return new PVector(getWidth(), getHeight());
    }

    public RLayoutConfig getCompLayoutConfig() {
        return layoutConfig;
    }

    public String getValueAsString() {
        return "";
    }

    public boolean isDraggable() {
        return isDraggable;
    }

    public boolean isDragged() {
        return isDragged;
    }

    /**
     * Method to check if this component is covered by the mouse
     *
     * @return true if covered, false otherwise
     */
    public boolean isMouseOver() {
        return isMouseOver;
    }

    /**
     * Method to check if this component is visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Method to check if this window of this component is visible, and if all parent nodes are visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isVisibleParentAware(RComponent child) {
        boolean visible = isVisible();
        if (parent != null) {
            if (parent instanceof RFolder) {
                return visible && parent.isVisible();
            } else {
                return visible && parent.isVisibleParentAware(this);
            }
        }
        return visible;
    }

    /**
     * Method to check if the parent window of this component is visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isParentWindowVisible() { // TODO LazyGui TODO Needed?
        RFolder folder = getParentFolder();
        if (folder == null || folder.getWindow() == null) {
            return !RLayoutStore.isGuiHidden();
        }
        return folder.isWindowVisible();
    }

    /**
     * TODO
     *
     * @return TODO
     */
    public boolean hasFocus() {
        return gui.hasFocus(this);
    }

    /**
     * TODO
     *
     * @param focused TODO
     */
    public void setFocus(boolean focused) {
        if (focused) {
            gui.takeFocus(this);
        } else if (gui.hasFocus(this)) {
            gui.takeFocus(null);
            loseFocus(null);
        }
    }

    public void setMouseOver(boolean b) {
        if (isMouseOver != b) {
            isMouseOver = b;
            redrawBuffers(); // REDRAW-VALID: we should redraw the buffer when the mouse over state changes
        }
    }

    public void setLayoutConfig(RLayoutConfig config) {
        this.layoutConfig = config;
    }

    /**
     * @param componentTree
     * @param mouseEvent
     */
    public void setMouseOverThisOnly(RComponentTree componentTree, RMouseEvent mouseEvent) {
        if (!isMouseOver()){
            buffer.invalidateBuffer();
        }
        setMouseOver(true);
        componentTree.setAllOtherMouseOversToFalse(this);
    }


    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    /**
     * Handle a pressed key while the component focused upon
     *
     * @param keyEvent the pressed key
     */
    public void keyPressedFocused(RKeyEvent keyEvent) {
        // NOOP
    }

    /**
     * Handle a pressed key while over the component
     *
     * @param keyEvent the pressed key
     * @param mouseX   x position
     * @param mouseY   y position
     */
    public void keyPressedOver(RKeyEvent keyEvent, float mouseX, float mouseY) {
        // NOOP
    }

    /**
     * Handle a pressed key chord while over the component
     *
     * @param keyEvent the pressed key
     * @param mouseX   x position
     * @param mouseY   y position
     */
    public void keyChordPressedOver(RKeyEvent keyEvent, float mouseX, float mouseY) {
        // NOOP
    }


    public void keyTypedOver(RKeyEvent keyEvent, float mouseX, float mouseY) {
        // NOOP
    }

    /**
     * @param mouseEvent
     * @param adjustedMouseY
     */
    public void mouseOver(RMouseEvent mouseEvent, float adjustedMouseY) {
        setMouseOverThisOnly(gui.getComponentTree(), mouseEvent);
        mouseEvent.consume();
    }

    /**
     * Method to handle the component's reaction to the mouse being pressed.
     *
     * @param mouseEvent the change made by the mouse
     * @param mouseY     adjust for scrollbar
     */
    public void mousePressed(RMouseEvent mouseEvent, float mouseY) {
        // We should mark these as true, but we don't need to do reprocessing of the buffers based on their prior state
        isDragged = true;
        isMouseOver = true;

        redrawBuffers(); // REDRAW-VALID: we should redraw the buffer solely on the basis that the user pressed the mouse
        mouseEvent.consume();
     }

    /**
     * Method to handle the component's reaction to the mouse being released outside of itself
     *
     * @param mouseEvent the change made by the mouse
     * @param mouseY     adjust for scrollbar
     */
    public void mouseReleasedAnywhere(RMouseEvent mouseEvent, float mouseY) {
        if (isDragged) {
            setFocus(false);
            mouseEvent.consume();
            redrawBuffers(); // REDRAW-VALID: we should redraw the buffer solely on the basis that the user released the mouse
        }
        isDragged = false;
    }

    /**
     * Method to handle the component's reaction to the mouse being released over it
     *
     * @param mouseEvent the change made by the mouse
     * @param mouseY     adjust for scrollbar
     */
    public void mouseReleasedOverComponent(RMouseEvent mouseEvent, float mouseY) {
        if (isDragged) {
            setFocus(true);
            mouseEvent.consume();
        }
        isDragged = false;
        redrawBuffers(); // REDRAW-VALID: we should redraw the buffer solely on the basis that the user released the mouse
    }

    /**
     * Method to handle the component's reaction to the mouse being released
     *
     * @param mouseEvent the change made by the mouse
     * @param mouseY     adjust for scrollbar
     * @param isOver     was released over the component
     */
    public void mouseReleased(RMouseEvent mouseEvent, float mouseY, boolean isOver) {
        if (isOver) {
            mouseReleasedOverComponent(mouseEvent, mouseY);
        } else {
            mouseReleasedAnywhere(mouseEvent, mouseY);
        }
    }

    /**
     * Method to handle the component's reaction to the mouse continuing to be dragged.
     *
     * @param mouseEvent the change made by the mouse
     */
    public void mouseDragged(RMouseEvent mouseEvent) {
        isMouseOver = true;
    }

    /**
     * Main update function, only called when the parent window containing this component is open.
     *
     * @param pg main PGraphics of the gui of the same size as the main PApplet canvas to draw on
     * @see RComponent#drawBackground(PGraphics)
     */
    public void draw(PGraphics pg) {
        // the component knows its absolute position but here the current matrix is already translated to it
        pg.image(buffer.draw(),0,0);
    }


    public abstract void drawToBuffer();

    /**
     * For most controls there is nothing to do when they loose focus. Override this
     * method in classes that need to do something when they loose focus eg
     * TextField
     */
    public void loseFocus(RComponent grabber) {
        if (isMouseOver) {
            isMouseOver = false;
        }
        redrawBuffers(); // REDRAW-VALID: we should redraw the buffer when the component loses focus
    }

    public abstract float suggestWidth();

    /**
     * The components must know its absolute position and size, so it can respond to user input events
     *
     * @param rX relative screen x
     * @param rX relative screen y
     * @param w  absolute screen width
     * @param h  absolute screen height
     */
    public void updateCoordinates(float bX, float bY, float rX, float rY, float w, float h) { // TODO LazyGui
        pos.x = bX + rX;
        pos.y = bY + rY;
        relPos.x = rX;
        relPos.y = rY;

        if (size.x != w){
            resetBuffer(); // RESET-VALID: we should resize the buffer if the size changes
        }

        size.x = w;

        if (size.y != h){
            resetBuffer(); // RESET-VALID: we should resize the buffer if the size changes
        }

        size.y = h;
    }

    /**
     * The components must know its absolute position and size, so it can respond to user input events
     *
     * @param basePos absolute base position in window
     * @param relPos  relative position from base
     * @param dim     allowed width and height
     */
    public final void updateCoordinates(PVector basePos, PVector relPos, PVector dim) { // TODO LazyGui
        updateCoordinates(basePos.x, basePos.y, relPos.x, relPos.y, dim.x, dim.y);
    }
}
