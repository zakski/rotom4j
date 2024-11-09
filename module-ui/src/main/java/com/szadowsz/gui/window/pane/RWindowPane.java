package com.szadowsz.gui.window.pane;

import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RPaths;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.input.mouse.RMouseHiding;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLayoutConfig;
import com.szadowsz.gui.layout.RLinearLayout;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.input.RInputListener;
import com.szadowsz.gui.window.RWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.Optional;

import static com.szadowsz.gui.config.theme.RColorType.*;
import static com.szadowsz.gui.utils.RCoordinates.isPointInRect;
import static processing.core.PApplet.*;

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

    protected boolean isVisible = true; // if the window is visible

    // Window Transition Status Info
    protected boolean isBeingDragged; // if the window is being moved
    protected boolean isBeingResized; // if the window shape is changing
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

        LOGGER.debug("{} Window [{},{},{},{}] Init", title, xPos, yPos, width, height);
        this.pos = new PVector(xPos, yPos);
        this.size = new PVector(width, height);
        this.sizeUnconstrained = new PVector(size.x, size.y);
        this.contentSize = new PVector(size.x, size.y);
        initDimensions();
        LOGGER.debug("{} Window [{},{},{},{}] Post-Dimension Init", title, pos.x, pos.y, this.size.x, this.size.y);
        contentBuffer = new RContentBuffer(this);
    }

    /**
     * Constructor for Internal Window
     *
     * @param app    PApplet to render window inside
     * @param xPos   initial X display location in PApplet
     * @param yPos   initial Y display location in PApplet
     * @param width  initial window width
     * @param height initial window height
     */
    public RWindowPane(PApplet app, RotomGui gui, RFolder folder, float xPos, float yPos, float width, float height) {
        this(app, gui, folder, folder.getName(), (int) xPos, (int) yPos, (int) width, (int) height);
    }

    /**
     * Constructor for Internal Window
     *
     * @param app   PApplet to render window inside
     * @param title title to give the window
     * @param pos   initial X,Y display location in PApplet
     * @param dim   initial window dimensions
     */
    public RWindowPane(PApplet app, RotomGui gui, RFolder folder, String title, PVector pos, PVector dim) {
        this(app, gui, folder, title, (int) pos.x, (int) pos.y, (int) dim.x, (int) dim.y);
    }


    /**
     * Helper Method to work out the upper left most point of where the content should be drawn from
     *
     * @return content starting coordinates
     */
    protected PVector getContentStart() {
        return new PVector(pos.x, pos.y + ((folder.shouldDrawTitle()) ? RLayoutStore.getCell() : 0));
    }

    /**
     * Helper Method to work out the drawable size of the Scrollbar
     *
     * @return Scrollbar dimensions
     */
    protected PVector getScrollbarBounds() {
        return new PVector(RLayoutStore.getCell(), size.y);
    }

    /**
     * Helper Method to work out the upper left most point of where the Scrollbar should be drawn from
     *
     * @return Scrollbar starting coordinates
     */
    protected PVector getScrollbarStart() {
        return new PVector(pos.x + contentSize.x, pos.y);
    }

    protected float getScrollbarValue() {
        return vsb.map(RScrollbar::getValue).orElse(0.0f);
    }

    protected float getScrolledY() {
        float yDiff = sizeUnconstrained.y - size.y;
        return yDiff * vsb.map(RScrollbar::getValue).orElse(0.0f);
    }

    /**
     * Check if the window is focused upon
     *
     * @return true if the window is the focus, false otherwise
     */
    protected boolean isFocused() {
        return gui.getWinManager().isFocused(this);
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
     * Check if the point is inside the content of the Window
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the point is inside the child nodes, false otherwise
     */
    protected boolean isPointInsideContent(float x, float y) {
        PVector contentStart = getContentStart();
        return isPointInRect(x, y, contentStart.x, contentStart.y, contentSize.x, contentSize.y);
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
     * Check if the point is inside the scroll bar of the Window
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the point is inside the scroll bar, false otherwise
     */
    protected boolean isPointInsideScrollbar(float x, float y) {
        if (vsb.isEmpty() || !vsb.get().isVisible()) {
            return false;
        }
        PVector contentStart = getContentStart();
        return isPointInRect(x, y,
                contentStart.x + contentSize.x, contentStart.y, RLayoutStore.getCell(), contentSize.y);
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
            return isPointInRect(x, y, pos.x, pos.y, size.x - RLayoutStore.getCell(), RLayoutStore.getCell());
        } else {
            return false;
        }
    }

    /**
     * Check if the point is inside the Window
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the point is inside the window, false otherwise
     */
    protected boolean isPointInsideWindow(float x, float y) {
        return isPointInRect(x, y, pos.x, pos.y, size.x, size.y);
    }

    /**
     * Check if the mouse is inside the close button of the Window
     *
     * @param e mouse event
     * @return true if the mouse is inside the close button, false otherwise
     */
    protected boolean isMouseInsideCloseButton(RMouseEvent e) {
        return isPointInsideCloseButton(e.getX(), e.getY());
    }

    /**
     * Check if the c is inside the content of the Window
     *
     * @param e mouse event
     * @return true if the mouse is inside the content, false otherwise
     */
    protected boolean isMouseInsideContent(RMouseEvent e) {
        return isPointInsideContent(e.getX(), e.getY()) && !isPointInsideResizeBorder(e.getX(), e.getY());
    }

    /**
     * Check if the mouse is inside the border resizer of the Window
     *
     * @param e mouse event
     * @return true if the mouse is inside the border resizer, false otherwise
     */
    protected boolean isMouseInsideResizeBorder(RMouseEvent e) {
        return isPointInsideResizeBorder(e.getX(), e.getY());
    }

    /**
     * Check if the mouse is inside the title bar of the Window
     *
     * @param e mouse event
     * @return true if the mouse is inside the title bar, false otherwise
     */
    protected boolean isMouseInsideTitlebar(RMouseEvent e) {
        return isVisible() && isPointInsideTitleBar(e.getX(), e.getY());
    }

    /**
     * Check if the mouse is inside the scroll bar of the Window
     *
     * @param e mouse event
     * @return true if the mouse is inside the scroll bar, false otherwise
     */
    protected boolean isMouseInsideScrollbar(RMouseEvent e) {
        return isVisible && isPointInsideScrollbar(e.getX(), e.getY());
    }

    /**
     * Check if the mouse is inside the Window
     *
     * @param e mouse event
     * @return true if the mouse is inside the Window, false otherwise
     */
    protected boolean isMouseInsideWindow(RMouseEvent e) {
        return isPointInsideWindow(e.getX(), e.getY());
    }

    protected boolean isRoot() {
        return false;
    }

    /**
     * Check to determine whether the title should be highlighted based on the current conditions
     *
     * @return true if it should be highlighted, false otherwise
     */
    protected boolean shouldHighlightTitle() {
        return isVisible() &&
                (isPointInsideTitleBar(sketch.mouseX, sketch.mouseY) && isBeingDragged) ||
                folder.isMouseOver();
    }

    /**
     * Check to determine whether the scrollbar should be highlighted based on the current conditions
     *
     * @return true if it should be highlighted, false otherwise
     */
    protected boolean shouldHighlightScrollbar() {
        return isVisible() &&
                (isPointInsideScrollbar(sketch.mouseX, sketch.mouseY) && !isBeingDragged) ||
                folder.isMouseOver();
    }

    protected void setFocusOnThis() {
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
     * Initialise the dimensions of the Window
     */
    protected void initDimensions() {
        if (folder.getChildren().isEmpty()) {
            // We only have what's provided to go on as at this stage, as we would have no child components
            if (size.x == 0 || size.y == 0) {
                if (size.x == 0) {
                    if (RLayoutStore.shouldSuggestWindowWidth() && folder.suggestWindowWidthInCells() == RLayoutStore.getWindowWidthInCells()) {
                        size.x = folder.autosuggestWindowWidthForContents();
                    } else {
                        size.x = RLayoutStore.getCell() * folder.suggestWindowWidthInCells();
                    }
                    contentSize.x = size.x;
                    sizeUnconstrained.x = size.x;
                }
                if (size.y == 0) {
                    contentSize.y = RLayoutStore.getCell();
                    sizeUnconstrained.y = contentSize.y;
                    if (folder.shouldDrawTitle()) {
                        size.y = contentSize.y + RLayoutStore.getCell();
                    } else {
                        size.y = contentSize.y;
                    }
                }
            }
        } else {
            if (size.x == 0 || size.y == 0) {
                RLayoutBase layout = folder.getLayout();
                PVector preferredSize = layout.calcPreferredSize((folder.shouldDrawTitle()) ? folder.getName() : "", folder.getChildren());
                if (size.y == 0) {
                    contentSize.y = preferredSize.y;
                    sizeUnconstrained.y = preferredSize.y;
                    if (folder.shouldDrawTitle()) {
                        size.y = preferredSize.y + RLayoutStore.getCell();
                    } else {
                        size.y = preferredSize.y;
                    }
                }
                if (size.x == 0) {
                    contentSize.x = preferredSize.x;
                    sizeUnconstrained.x = preferredSize.x;
                    if (vsb.isPresent()) {
                        size.x = preferredSize.x + RLayoutStore.getCell();
                    } else {
                        size.x = preferredSize.x;
                    }
                }

            }
        }
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
        PVector preferredContentSize = folder.getLayout().calcPreferredSize((folder.shouldDrawTitle()) ? folder.getName() : "", folder.getChildren());
        return switch (sizing) {
            case COMPONENT -> {
                float width = preferredContentSize.x + ((vsb.isPresent() && vsb.get().isVisible()) ? RLayoutStore.getCell() : 0);
                float height = preferredContentSize.y + ((folder.shouldDrawTitle()) ? RLayoutStore.getCell() : 0);
                yield new PVector(width, height);
            }
            case USER, LAYOUT -> size.copy();
        };
    }

    protected void handleBeingResized(RMouseEvent mouseEvent) {
        sizing = RSizeMode.USER;
        float minimumWindowSizeInCells = 4;
        float maximumWindowSize = sketch.width;
        float oldWindowSizeX = size.x;
        size.x += mouseEvent.getX() - mouseEvent.getPrevX();
        size.x = constrain(size.x, minimumWindowSizeInCells * RLayoutStore.getCell(), maximumWindowSize);
        if (vsb.map(sb -> !sb.isVisible()).orElse(true)) {
            contentSize.x = size.x;
        } else {
            contentSize.x = size.x - RLayoutStore.getCell();
        }
        vsb.ifPresent(RScrollbar::invalidateBuffer);
        mouseEvent.consume();
        reinitialiseBuffer();
        LOGGER.debug("oldX: " + mouseEvent.getPrevX() + ", new:X " + mouseEvent.getX());
        LOGGER.debug("old: " + oldWindowSizeX + ", new: " + size.x);
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
            size.x = preferredWidth;
            contentSize.x = size.x - ((isScrollbarVisible()) ? RLayoutStore.getCell() : 0);
        }

        if (!RLayoutStore.shouldKeepWindowsInBounds()) {
            return;
        }

        if (pos.x + size.x > pg.width) {
            size.x = pg.width - pos.x;
            contentSize.x = size.x - ((isScrollbarVisible()) ? RLayoutStore.getCell() : 0);
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
     * @param canvas     Processing Graphics Context
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
        if (!isPointInsideTitleBar(sketch.mouseX, sketch.mouseY) || !RLayoutStore.shouldShowPathTooltips()) {
            return;
        }
        canvas.pushMatrix();
        canvas.pushStyle();
        canvas.translate(pos.x, pos.y);
        String[] pathSplit = RPaths.splitFullPathWithoutEndAndRoot(folder.getPath());
        int lineCount = pathSplit.length;
        float tooltipXOffset = RLayoutStore.getCell() * 0.5f;
        float tooltipWidthMinimum = size.x - tooltipXOffset - RLayoutStore.getCell();
        canvas.noStroke();
        canvas.rectMode(CORNER);
        canvas.textAlign(LEFT, CENTER);
        for (int i = 0; i < lineCount; i++) {
            String line = pathSplit[lineCount - 1 - i];
            float tooltipWidth = max(tooltipWidthMinimum, canvas.textWidth(line) + RFontStore.getMarginX() * 2);
            canvas.fill(RThemeStore.getRGBA(NORMAL_BACKGROUND));
            canvas.rect(tooltipXOffset, -i * RLayoutStore.getCell() - RLayoutStore.getCell(), tooltipWidth, RLayoutStore.getCell());
            canvas.fill(RThemeStore.getRGBA(NORMAL_FOREGROUND));
            canvas.text(line, RFontStore.getMarginX() + tooltipXOffset, -i * RLayoutStore.getCell() - RFontStore.getMarginY());
        }
        canvas.popMatrix();
        canvas.popStyle();
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
                        sizeUnconstrained.y - ((folder.shouldDrawTitle()) ? RLayoutStore.getCell() : 0) // TODO Sizing Check
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

    public float getContentHeight() {
        switch (folder.getLayout()) {
            case RLinearLayout linear -> {
                return (int) contentSize.y;
            }
            default -> throw new IllegalStateException("Unexpected value: " + folder.getLayout());
        }
    }

    @Override
    public PApplet getSketch() {
        return sketch;
    }

    /**
     * Get Internal Window's Companion Folder
     *
     * @return the folder
     */
    public RFolder getFolder() {
        return folder;
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

    public int getContentWidth() {
        switch (folder.getLayout()) {
            case RLinearLayout linear -> {
                return (int) contentSize.x;
            }
            default -> throw new IllegalStateException("Unexpected value: " + folder.getLayout());
        }
    }

    public RLayoutConfig getLayoutConfig() {
        return folder.getCompLayoutConfig();
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
        return vsb.isPresent() && vsb.get().isVisible();
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
    public boolean isVisible() {
        return isVisible;
    }


    public void setBounds(float x, float y, float sizeX, float sizeY, RSizeMode mode) {
        LOGGER.trace("Setting Bounds [{},{},{},{}] for {} using {}", x, y, sizeX, sizeY, folder.getName(), mode);
        sizing = mode;
        pos.x = x;
        pos.y = y;

        size.y = sizeY;
        if (folder.shouldDrawTitle()) {
            contentSize.y = size.y - RLayoutStore.getCell();
        } else {
            contentSize.y = size.y;
        }
        sizeUnconstrained.y = contentSize.y;

        contentSize.x = sizeX;
        sizeUnconstrained.x = sizeX;
        if (vsb.isPresent()) {
            size.x = sizeX + RLayoutStore.getCell();
        } else {
            size.x = sizeX;
        }
        LOGGER.trace("Adjusted Bounds [{},{},{},{}] for {} using {}", x, y, size.x, size.y, folder.getName(), mode);
        reinitialiseBuffer();
        folder.sortChildren();
    }

    /**
     * Method to open the Window
     *
     * @param setFocus true if should be focused on, false otherwise
     */
    public void open(boolean setFocus) {
        isVisible = true;
        if (setFocus) {
            setFocusOnThis();
        }
        reinitialiseBuffer();
    }

    public void close() {
        isVisible = false;
        isBeingDragged = false;
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

    protected RComponent findComponentAt(float x, float y) {
        for (RComponent child : folder.getChildren()) {
            if (!child.isVisible()) {
                continue;
            }
            if (isPointInRect(x, y, child.getPosX(), child.getPosY(), child.getWidth(), child.getHeight())) {
                return child;
            }
        }
        return null;
    }


    @Override
    public void keyPressed(RKeyEvent keyEvent) {
        if (!isVisible()) {
            return;
        }

        float mouseX = sketch.mouseX;
        float mouseY = sketch.mouseY;

        if (isPointInsideTitleBar(mouseX, mouseY)) {
            folder.keyPressedOver(keyEvent, mouseX, mouseY);
            return;
        }

        folder.keyPressed(keyEvent, mouseX, mouseY);
    }

    @Override
    public void keyChordPressed(RKeyEvent keyEvent) {
        if (!isVisible()) {
            return;
        }

        float mouseX = sketch.mouseX;
        float mouseY = sketch.mouseY;

        if (isPointInsideTitleBar(mouseX, mouseY)) {
            folder.keyChordPressedOver(keyEvent, mouseX, mouseY);
            return;
        }

        folder.keyChordPressed(keyEvent, mouseX, mouseY);
    }

    @Override
    public void mouseMoved(RMouseEvent mouseEvent) {
        if (!isVisible()) {
            return;
        }

        if (isMouseInsideContent(mouseEvent)) {
            PVector contentStart = getContentStart();
            float yShift = getScrolledY();
            float mouseY = mouseEvent.getY() + yShift;
            LOGGER.trace("Mouse Inside Content: X {} Y {} WinX {} WinY {} Width {} Height {}", mouseEvent.getX(), mouseY, contentStart.x, contentStart.y, size.x, size.y);
            RComponent child = findComponentAt(mouseEvent.getX(), mouseY);
            if (child != null) {
                if (!child.isMouseOver()) {
                    LOGGER.debug("Inside Component {} [NX {} NY {} Width {} Height {}]", child.getName(), child.getPosX(), child.getPosY(), child.getWidth(), child.getHeight());
                    contentBuffer.invalidateBuffer();
                }
                child.mouseOver(mouseEvent, mouseY);
            }
        } else if (isMouseInsideTitlebar(mouseEvent)) {
            if (folder.isChildMouseOver()) {
                contentBuffer.invalidateBuffer();
            }
            folder.setMouseOverThisOnly(gui.getComponentTree(), mouseEvent);
            mouseEvent.consume();
        } else if (isMouseInsideScrollbar(mouseEvent)) {
            if (folder.isChildMouseOver()) {
                contentBuffer.invalidateBuffer();
            }
            vsb.ifPresent(s -> s.mouseMoved(mouseEvent));
            folder.setMouseOverThisOnly(gui.getComponentTree(), mouseEvent);
            mouseEvent.consume();
        } else {
            if (folder.isChildMouseOver()) {
                LOGGER.info("Child Was Over {} Window",title);
                contentBuffer.invalidateBuffer();
            }
            gui.setAllMouseOverToFalse(this.folder);
        }
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent) {
        if (!isVisible()) {
            return;
        }
        // Make sure Window Grabs focus
        if (isMouseInsideWindow(mouseEvent)) {
            if (!isFocused()) {
                setFocusOnThis();
            }
        }
        // Reset Values
        isCloseInProgress = false;
        isBeingDragged = false;
        isBeingResized = false;


        // Then Check Window Parts
        if (!isRoot() && ((isMouseInsideCloseButton(mouseEvent) && mouseEvent.isLeft()) || (isMouseInsideWindow(mouseEvent) && mouseEvent.isRight()))) {
            isCloseInProgress = true;
            mouseEvent.consume();
        } else if (isPointInsideContent(mouseEvent.getX(), mouseEvent.getY())) {
            RComponent child = findComponentAt(mouseEvent.getX(), mouseEvent.getY());
            if (child != null) {
                float yShift = getScrolledY();
                float mouseY = mouseEvent.getY() + yShift;
                contentBuffer.invalidateBuffer();
                child.mousePressed(mouseEvent, mouseY);
            }
        } else if (isMouseInsideTitlebar(mouseEvent) && mouseEvent.isLeft()) {
            isBeingDragged = true;
            mouseEvent.consume();
        } else if (isMouseInsideScrollbar(mouseEvent) && mouseEvent.isLeft()) {
            vsb.ifPresent(s -> s.mousePressed(mouseEvent));
            mouseEvent.consume();
        } else if (isMouseInsideResizeBorder(mouseEvent) && RLayoutStore.isWindowResizeEnabled()) {
            isBeingResized = true;
            mouseEvent.consume();
        }
    }

    @Override
    public void mouseReleased(RMouseEvent mouseEvent) {
        if (!isVisible()) {
            return;
        }

        // Check Window Parts
        if (isCloseInProgress && ((isMouseInsideCloseButton(mouseEvent) && mouseEvent.isLeft()) || (isMouseInsideWindow(mouseEvent) && mouseEvent.isRight()))) {
            close();
            mouseEvent.consume();
        } else if (isMouseInsideTitlebar(mouseEvent) && mouseEvent.isLeft()) {
            isBeingDragged = true;
            mouseEvent.consume();
        } else if (isMouseInsideScrollbar(mouseEvent) && mouseEvent.isLeft()) {
            vsb.ifPresent(s -> s.mousePressed(mouseEvent));
            mouseEvent.consume();
        } else if (isMouseInsideResizeBorder(mouseEvent) && RLayoutStore.isWindowResizeEnabled()) {
            LOGGER.info("{} isBeingResized", title);
            isBeingResized = true;
            mouseEvent.consume();
        } else {
            RComponent released = findComponentAt(mouseEvent.getX(), mouseEvent.getY());
            float yShift = getScrolledY();
            float mouseY = mouseEvent.getY() + yShift;
            for (RComponent child : folder.getChildren()) {
                boolean isReleased = child.equals(released);
                if (isReleased) {
                    contentBuffer.invalidateBuffer();
                }
                child.mouseReleased(mouseEvent, mouseY, isReleased);
                if (mouseEvent.isConsumed()){
                    RMouseHiding.tryRevealMouseAfterDragging(sketch);
                    break;
                }
            }
        }
    }

    @Override
    public void mouseDragged(RMouseEvent mouseEvent) {
        if (!isVisible()) {
            return;
        }
        if (isBeingDragged) {
            pos.x += mouseEvent.getX() - mouseEvent.getPrevX();
            pos.y += mouseEvent.getY() - mouseEvent.getPrevY();
            vsb.ifPresent(RScrollbar::invalidateBuffer);
            mouseEvent.consume();
        } else if (isBeingResized) {
            handleBeingResized(mouseEvent);
        } else if (vsb.map(RScrollbar::isDragging).orElse(false)) {
            vsb.ifPresent(s -> s.mouseDragged(mouseEvent));
        }
        for (RComponent child : folder.getChildren()) {
            LOGGER.debug("Mouse Drag Check for Content {}", child.getName());
            if (child.isDragged()) {
                LOGGER.debug("Mouse Dragged for Content {}", child.getName());
                child.mouseDragged(mouseEvent);
                if (mouseEvent.isConsumed() && child.isDraggable()) {
                    RMouseHiding.tryHideMouseForDragging(sketch);
                }
                if (mouseEvent.isConsumed()) {
                    break;
                }
            }
        }
    }

    @Override
    public void mouseWheel(RMouseEvent mouseEvent) {
        if (isMouseInsideWindow(mouseEvent)) {
            vsb.ifPresent(s -> s.mouseWheel(mouseEvent));
            mouseEvent.consume();
        }
    }

    public void reinitialiseBuffer() {
        contentBuffer.resetBuffer();
    }

    public void redrawBuffer() {
        contentBuffer.invalidateBuffer();
    }

    public void resizeForContents(boolean shouldResize) {
        isBeingResized = shouldResize;
        if (isBeingResized) {
            sizing = RSizeMode.COMPONENT;
        }
    }

    public void setCoordinates(float x, float y) {
    }

    public void setWidth(Float nullableSizeX) {
    }
}