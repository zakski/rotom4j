package com.szadowsz.gui.window.pane;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLinearLayout;
import com.szadowsz.gui.utils.RCoordinates;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.config.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.input.RInputListener;
import com.szadowsz.gui.window.RWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.Optional;

import static com.old.gui.utils.RCoordinates.isPointInRect;
import static com.szadowsz.gui.config.theme.RColorType.*;
import static processing.core.PApplet.lerp;
import static processing.core.PApplet.round;
import static processing.core.PConstants.*;

/**
 * Base Class for Internal Windows
 */
public class RWindowPane implements RWindow, RInputListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RWindowPane.class);

    // PApplet to draw in
    protected final PApplet sketch;

    // Parent Gui that manages the internal window
    protected final RotomGui gui;

    // Companion Folder
    protected final RFolder folder;

    // Window Title
    protected final String title;

    // Window Content Buffer
    protected final RContentBuffer contentBuffer;

    // Vertical Scrollbar
    protected Optional<RScrollbar> vsb = Optional.empty();

    // Window Position Info
    protected final PVector pos;

    // Window Dimensions
    protected final PVector size;
    protected final PVector sizeUnconstrained;
    protected final PVector contentSize;

    // How Dimensions where Set
    protected RSizeMode sizing = RSizeMode.COMPONENT;

    // Window Status Info

    // Draw Info
    protected boolean isTitleHighlighted; // if the window title is visible, should it be highlighted
    protected boolean isScrollbarHighlighted;  // if the vertical scrollbar is visible, should it be highlighted

    protected boolean isBeingDragged; // if the window is being moved
    protected boolean isVisible = true; // if the window is visible

    // Window Transition Status Info
    protected boolean isCloseInProgress; // if close button press in progress

    public RWindowPane(PApplet sketch, RotomGui gui, RFolder folder, String title, int xPos, int yPos, int width, int height) {
        this.sketch = sketch;
        this.gui = gui;

        // Listen to input events
        gui.subscribe(this);

        // Link this window and its folder together
        this.folder = folder;
        folder.setWindow(this);
        this.title = title;

        LOGGER.debug("{} Window [{},{},{},{}] Init", title,xPos,yPos, width,height);
        this.pos = new PVector(xPos, yPos);
        this.size = new PVector(width, height);
        this.sizeUnconstrained = new PVector(size.x, size.y);
        this.contentSize = new PVector(size.x, size.y);
        //initDimensions();
        LOGGER.debug("{} Window [{},{},{},{}] Post-Dimension Init", title,pos.x,pos.y, this.size.x,this.size.y);
        contentBuffer = new RContentBuffer(this);
    }

    /**
     * Helper Method to work out the upper left most point of where the content should be drawn from
     *
     * @return content starting coordinates
     */
    public PVector getContentStart(){
        return new PVector(pos.x, pos.y + ((folder.shouldDrawTitle())? RLayoutStore.getCell():0));
    }


    /**
     * Helper Method to work out the drawable size of the Scrollbar
     *
     * @return Scrollbar dimensions
     */
    public PVector getScrollbarBounds(){
        return new PVector(RLayoutStore.getCell(), size.y);
    }
    
    /**
     * Helper Method to work out the upper left most point of where the Scrollbar should be drawn from
     *
     * @return Scrollbar starting coordinates
     */
    public PVector getScrollbarStart(){
        return new PVector(pos.x + contentSize.x, pos.y);
    }

    private float getScrollbarValue() {
        return vsb.map(RScrollbar::getValue).orElse(0.0f);
    }

    /**
     * Check if the point is inside the close button of the Window
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the point is inside the close button, false otherwise
     */
    protected boolean isPointInsideCloseButton(float x, float y) {
        return isPointInRect(x, y, pos.x + size.x - RLayoutStore.getCell() - 1, pos.y, RLayoutStore.getCell() + 1, RLayoutStore.getCell() - 1);
    }

    /**
     * Check if the point is inside the scroll bar of the Window
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the point is inside the scroll bar, false otherwise
     */
    protected boolean isPointInsideScrollbar(float x, float y) {
        if (vsb.isEmpty() || !vsb.get().getVisible()) {
            return false;
        }
        PVector contentStart = getContentStart();
        return RCoordinates.isPointInRect(x, y,
                contentStart.x + contentSize.x, contentStart.y , RLayoutStore.getCell(), contentSize.y);
    }

    /**
     * Check if the point is inside the border resizer of the Window
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the point is inside the border resizer, false otherwise
     */
    protected boolean isPointInsideResizeBorder(float x, float y) {
        if (!RLayoutStore.isWindowResizeEnabled()) {
            return false;
        }
        float w = RLayoutStore.getResizeRectangleSize();
        return isPointInRect(x, y, pos.x + size.x - w / 2f, pos.y, w, size.y);
    }

    /**
     * Check if the point is inside the title bar of the Window
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the point is inside the title bar, false otherwise
     */
    protected boolean isPointInsideTitleBar(float x, float y) {
        if (folder.shouldDrawTitle()) {
            return RCoordinates.isPointInRect(x, y, pos.x, pos.y, size.x - RLayoutStore.getCell(), RLayoutStore.getCell());
        } else {
            return false;
        }
    }

    protected boolean isRoot() {
        return false;
    }

    /**
     * Check to determine whether the title should be highlighted based on the current conditions
     *
     * @return true if it should be highlighted, false otherwise
     */
    protected boolean shouldHighlightTitle(){
        return isVisible() &&
                (isPointInsideTitleBar(sketch.mouseX, sketch.mouseY) && isBeingDragged) ||
                folder.isMouseOver();
    }

    /**
     * Check to determine whether the scrollbar should be highlighted based on the current conditions
     *
     * @return true if it should be highlighted, false otherwise
     */
    protected boolean shouldHighlightScrollbar(){
        return isVisible() &&
                (isPointInsideScrollbar(sketch.mouseX, sketch.mouseY) && !isBeingDragged) ||
                folder.isMouseOver();
    }

    /**
     * Helper Method to control titlebar Highlighting
     *
     * @param v true if it is to be highlighted, false otherwise
     */
    protected void setTitleHighlighted(boolean v) {
        isTitleHighlighted = v;
    }

    /**
     * Helper Method to control scrollbar Highlighting
     *
     * @param v true if it is to be highlighted, false otherwise
     */
    protected void setScrollbarHighlighted(boolean v) {
        isScrollbarHighlighted = v;
    }

    /**
     * Initialise Scrollbar for Vertical Layouts
     */
    protected void initScrollbar() {
        if (vsb.isEmpty()) {
            RLayoutBase layout = folder.getLayout();
            if (layout instanceof RLinearLayout) {
                if (((RLinearLayout) layout).getDirection() == RDirection.VERTICAL) {
                    vsb = Optional.of(new RScrollbar(this, getScrollbarStart(), getScrollbarBounds(), sizeUnconstrained.y, 16));
                }
            }
        }
    }

    protected PVector calcPreferredSize() {
        PVector preferredContentSize = folder.getLayout().calcPreferredSize((folder.shouldDrawTitle())?folder.getName():"",folder.getChildren());
        return switch (sizing) {
            case COMPONENT -> {
                float width = preferredContentSize.x + ((vsb.isPresent()&&vsb.get().getVisible())? RLayoutStore.getCell():0);
                float height = preferredContentSize.y + ((folder.shouldDrawTitle())? RLayoutStore.getCell():0);
                yield new PVector(width,height);
            }
            case USER,LAYOUT -> size.copy();
        };
    }

    protected void reinitialiseBuffer() {
    }

    /**
     * Keep the window in bounds, by altering its position
     *
     * @param canvas the PGraphics context the Window will be drawn onto
     */
    protected void constrainPosition(PGraphics canvas) {
        if (!RLayoutStore.shouldKeepWindowsInBounds()) {
            return;
        }
        float rightEdge = canvas.width - size.x - 1;
        float bottomEdge = canvas.height - size.y - 1;
        float lerpAmt = 0.3f;
        if (pos.x < 0) {
            pos.x = lerp(pos.x, 0, lerpAmt);
        }
        if (pos.y < 0) {
            pos.y = lerp(pos.y, 0, lerpAmt);
        }
        if (pos.x > rightEdge) {
            pos.x = lerp(pos.x, rightEdge, lerpAmt);
        }
        if (pos.y > bottomEdge) {
            pos.y = lerp(pos.y, bottomEdge, lerpAmt);
        }
    }

    /**
     * Constrain the Windows width/height when the layout is vertical
     *
     * @param pg              Processing Graphics Context
     * @param preferredHeight what it isa calculated to need
     */
    protected void constrainHeight(PGraphics pg, float preferredHeight) {
        if (!RLayoutStore.shouldKeepWindowsInBounds()) {
            return;
        }
        size.y = preferredHeight;
        sizeUnconstrained.y = size.y;
        contentSize.y = sizeUnconstrained.y;

        if (pos.y + size.y > pg.height) {
            size.y = pg.height - pos.y;
            initScrollbar();
            vsb.ifPresent(s -> s.setVisible(true));
        }
    }

    /**
     * Constrain the Windows width/height when the layout is horizontal
     *
     * @param pg             Processing Graphics Context
     * @param preferredWidth what it isa calculated to need
     */
    protected void constrainWidth(PGraphics pg, float preferredWidth) {
             if (contentSize.x == size.x && isScrollbarVisible()) {
                size.x = size.x + RLayoutStore.getCell();
            }

            if (sizing == RSizeMode.LAYOUT && preferredWidth != size.x) {
                size.x  = preferredWidth;
                contentSize.x = size.x - ((isScrollbarVisible())? RLayoutStore.getCell():0);
            }

            if (!RLayoutStore.shouldKeepWindowsInBounds()) {
                return;
            }

            if (pos.x + size.x > pg.width) {
                size.x = pg.width - pos.x;
                contentSize.x = size.x - ((isScrollbarVisible())? RLayoutStore.getCell():0);
            }
    }

    /**
     * Fix the Bounds of the Window
     *
     * @param canvas the PGraphics context the Window will be drawn onto
     */
    protected void constrainBounds(PGraphics canvas) {
        float oldWindowSizeX = size.x;
        float oldWindowSizeXForContents = contentSize.x;
        float oldWindowSizeXUnconstrained = sizeUnconstrained.x;

        float oldWindowSizeY = size.y;
        float oldWindowSizeYForContents = contentSize.y;
        float oldWindowSizeYUnconstrained = sizeUnconstrained.y;

        // Make sure the position of the Window is in bounds, then work out the Preferred Size
        constrainPosition(canvas);
        PVector preferredSize = calcPreferredSize();

        // Then fix the width and height
        constrainHeight(canvas, preferredSize.y);
        constrainWidth(canvas, preferredSize.x);

        // If the size changes, we need to redraw and resize the Window Buffer
        if (oldWindowSizeX != size.x || oldWindowSizeY != size.y ||
                oldWindowSizeXForContents != contentSize.x || oldWindowSizeYForContents != contentSize.y ||
                oldWindowSizeXUnconstrained != sizeUnconstrained.x || oldWindowSizeYUnconstrained != sizeUnconstrained.y) {
            LOGGER.trace("Old Width: [{},{},{}], New Width: [{},{},{}]", oldWindowSizeX, oldWindowSizeXUnconstrained, oldWindowSizeXForContents, contentSize.x, sizeUnconstrained.x, contentSize.x);
            LOGGER.trace("Old Height: [{},{},{}], New Height: [{},{},{}]", oldWindowSizeY, oldWindowSizeYUnconstrained, oldWindowSizeYForContents, contentSize.y, sizeUnconstrained.y, contentSize.y);

            reinitialiseBuffer();
        }
    }

    /**
     * Draw The Window background
     *
     * @param canvas             Processing Graphics Context
     * @param drawBackgroundOnly true if drawing the background, false if drawing the background's outline
     */
    protected void drawBackgroundWithWindowBorder(PGraphics canvas, boolean drawBackgroundOnly) {
        canvas.pushMatrix();
        canvas.translate(pos.x, pos.y);
        canvas.stroke(RThemeStore.getRGBA(WINDOW_BORDER));
        canvas.strokeWeight(1);
        canvas.fill(RThemeStore.getRGBA(NORMAL_BACKGROUND));
        if (drawBackgroundOnly) {
            canvas.noStroke();
        } else {
            canvas.noFill();
        }
        canvas.rect(0, 0, size.x, size.y);
        canvas.popMatrix();
    }

    /**
     * Draw The Window resize indicator
     *
     * @param pg Processing Graphics Context
     */
    protected void drawResizeIndicator(PGraphics pg) {
        if (isPointInsideResizeBorder(sketch.mouseX, sketch.mouseY) && RLayoutStore.shouldDrawResizeIndicator()) {
            float w = RLayoutStore.getResizeRectangleSize();
            pg.pushMatrix();
            pg.translate(pos.x, pos.y);
            pg.noStroke();
            pg.fill(RThemeStore.getRGBA(WINDOW_BORDER));
            pg.rect(size.x - w / 2f, 0, w, size.y);
            pg.popMatrix();
        }
    }

    /**
     * Draw The Window Title Bar
     *
     * @param canvas         Processing Graphics Context
     * @param shouldDraw if the title bar should be drawn
     */
    protected void drawTitleBar(PGraphics canvas, boolean shouldDraw) {
        if (shouldDraw) {
            float availableWidthForText = size.x - RFontStore.getMarginX() + (isRoot() ? 0 : -RLayoutStore.getCell());
            String leftText = RFontStore.substringToFit(canvas, folder.getName(), availableWidthForText);
            canvas.pushMatrix();
            canvas.pushStyle();
            canvas.translate(pos.x, pos.y);
            canvas.fill(isTitleHighlighted() ? RThemeStore.getRGBA(FOCUS_BACKGROUND) : RThemeStore.getRGBA(NORMAL_BACKGROUND));
            if (!sketch.focused && isRoot()) {
                canvas.fill(RThemeStore.getRGBA(FOCUS_BACKGROUND));
                leftText = "not in focus";
                setTitleHighlighted(true);
            }
            float titleBarWidth = size.x;
            canvas.strokeWeight(1);
            canvas.stroke(RThemeStore.getRGBA(WINDOW_BORDER));
            canvas.rect(0, 0, titleBarWidth, RLayoutStore.getCell());
            canvas.fill(isTitleHighlighted() ? RThemeStore.getRGBA(FOCUS_FOREGROUND) : RThemeStore.getRGBA(NORMAL_FOREGROUND));
            canvas.textAlign(LEFT, CENTER);
            canvas.text(leftText, RFontStore.getMarginX(), RLayoutStore.getCell() - RFontStore.getMarginY());
            canvas.popStyle();
            canvas.popMatrix();
        }
    }

    /**
     * Draw The Content of The Window
     *
     * @param pg Processing Graphics Context
     */
    protected void drawContent(PGraphics pg) {
        if (!folder.getChildren().isEmpty()) {
            pg.pushMatrix();
            PVector contentStart = getContentStart();
            pg.translate(contentStart.x, contentStart.y);
            if (isScrollbarVisible()) {
                int yDiff = round((sizeUnconstrained.y - size.y) * getScrollbarValue());
                pg.image(contentBuffer.draw().get(0, yDiff, (int) contentSize.x, (int) (size.y - RLayoutStore.getCell())), 0, 0);
            } else {
                pg.image(contentBuffer.draw(), 0, 0);
            }
            pg.popMatrix();
        }
    }

    /**
     * Draw The Window close button
     *
     * @param pg Processing Graphics Context
     */
    protected void drawCloseButton(PGraphics pg) {
        pg.pushMatrix();
        pg.translate(pos.x, pos.y);
        pg.stroke(RThemeStore.getRGBA(WINDOW_BORDER));
        pg.strokeWeight(1);
        pg.line(size.x - RLayoutStore.getCell(), 0, size.x - RLayoutStore.getCell(), RLayoutStore.getCell() - 1);
        if (isPointInsideCloseButton(sketch.mouseX, sketch.mouseY) || isCloseInProgress) {
            pg.fill(RThemeStore.getRGBA(FOCUS_BACKGROUND));
            pg.noStroke();
            pg.rectMode(CORNER);
            pg.rect(size.x - RLayoutStore.getCell() + 0.5f, 1, RLayoutStore.getCell() - 1, RLayoutStore.getCell() - 1);
            pg.stroke(RThemeStore.getRGBA(FOCUS_FOREGROUND));
            pg.strokeWeight(1.99f);
            pg.pushMatrix();
            pg.translate(size.x - RLayoutStore.getCell() * 0.5f + 0.5f, RLayoutStore.getCell() * 0.5f);
            float n = RLayoutStore.getCell() * 0.2f;
            pg.line(-n, -n, n, n);
            pg.line(-n, n, n, -n);
            pg.popMatrix();
        }
        pg.popMatrix();
    }

    /**
     * Draw The Window tooltip
     *
     * @param canvas Processing Graphics Context
     */
    protected void drawPathTooltipOnHighlight(PGraphics canvas) { 
        // TODO NOOP
    }

    protected void drawPane(PGraphics canvas) {
        drawBackgroundWithWindowBorder(canvas, true);
        drawPathTooltipOnHighlight(canvas);
        PVector contentStart = getContentStart();
        vsb.ifPresent(s ->
                s.draw(
                        canvas,
                        contentStart.x,
                        contentStart.y,
                        contentSize.x,
                        contentSize.y,
                        sizeUnconstrained.y - ((folder.shouldDrawTitle())? RLayoutStore.getCell():0) // TODO Sizing Check
                )
        );
        if (!folder.getChildren().isEmpty()) {
            drawContent(canvas);
        }
        drawBackgroundWithWindowBorder(canvas, false);
        drawTitleBar(canvas, folder.shouldDrawTitle());
        if (!isRoot()) {
            drawCloseButton(canvas);
        }
        drawResizeIndicator(canvas);
    }

    @Override
    public PApplet getSketch() {
        return sketch;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public float getPosX() {
        return pos.x;
    }

    @Override
    public float getPosY() {
        return pos.y;
    }

    @Override
    public PVector getPos() {
        return pos.copy();
    }

    @Override
    public int getWidth() {
        return (int) size.x;
    }

    @Override
    public int getHeight() {
        return (int) size.y;
    }

    @Override
    public PVector getSize() {
        return size.copy();
    }

    /**
     * Check if Window is being Dragged by The User
     *
     * @return true if dragged, false otherwise
     */
    public boolean isDragged() {
        return false;
    }

    /**
     * Check if the Scrollbar is currently in use/visible
     *
     * @return if Scrollbar visible, false otherwise
     */
    public boolean isScrollbarVisible() {
        return vsb.isPresent() && vsb.get().getVisible();
    }

    /**
     * Check if the Titlebar is currently highlighted
     *
     * @return true if highlighted, false otherwise
     */
    public boolean isTitleHighlighted() {
        return isTitleHighlighted;
    }

    /**
     * Check if the Window is currently open/visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isVisible(){
        return isVisible;
    }

    public void drawContextLine(PGraphics canvas, float endpointRectSize, boolean shouldPickShortestLine) {
    }

    /**
     * Main Drawing Method For the Window, Updates status before drawing
     *
     * @param canvas the PGraphics context to draw the Window onto
     */
    public void drawWindow(PGraphics canvas) {
        canvas.textFont(RFontStore.getMainFont());

        // Check The Need for Highlights
        setTitleHighlighted(shouldHighlightTitle());
        setScrollbarHighlighted(shouldHighlightScrollbar());

        if (!isVisible || !folder.isVisibleParentAware()) {
            return;
        }

        // Check if we need to resize to fit the screen
        constrainBounds(canvas);

        // Draw Window Pane
        canvas.pushMatrix();
        drawPane(canvas);
        canvas.popMatrix();
    }
}
