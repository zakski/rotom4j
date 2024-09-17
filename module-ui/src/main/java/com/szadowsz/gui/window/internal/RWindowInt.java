package com.szadowsz.gui.window.internal;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.RPaths;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.component.group.RRoot;
import com.szadowsz.gui.config.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.input.mouse.RMouseHiding;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLayoutConfig;
import com.szadowsz.gui.layout.RLinearLayout;
import com.szadowsz.gui.input.RInputListener;
import com.szadowsz.gui.utils.RCoordinates;
import com.szadowsz.gui.utils.RSnapToGrid;
import com.szadowsz.gui.window.RWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.Arrays;
import java.util.Optional;

import static com.szadowsz.gui.config.theme.RThemeColorType.*;
import static com.szadowsz.gui.utils.RCoordinates.isPointInRect;
import static processing.core.PApplet.*;
import static processing.core.PApplet.dist;
import static processing.core.PConstants.CENTER;

/**
 * Base Class for Internal Windows
 */
public class RWindowInt implements RWindow, RInputListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RWindowInt.class);

    // PApplet to draw in
    protected final PApplet app;

    // Parent Gui that manages the internal window
    protected final RotomGui gui;

    //
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

    // Window Status Info
    protected RSizeMode sizing = RSizeMode.COMPONENT;
    protected boolean isTitleHighlighted; // if the window title is visible, should it be highlighted
    protected boolean isScrollbarHighlighted;  // if the vertical scrollbar is visible, should it be highlighted
    protected boolean isBeingResized; // if the window shape is changing
    protected boolean isBeingDragged; // if the window is being moved
    protected boolean isVisible = true; // if the window is visible
    protected boolean isResizeWidth; // if the window is visible

    // Window Transition Status Info
    protected boolean isCloseInProgress; // if close button press in progress

    /**
     * Constructor for Internal Window
     *
     * @param app   PApplet to render window inside
     * @param title title to give the window
     * @param pos   initial display location in PApplet
     * @param size  initial window dimensions
     */
    public RWindowInt(PApplet app, RotomGui gui, RFolder folder, String title, PVector pos, PVector size) {
        this.app = app;
        this.gui = gui;

        // Listen to input events
        gui.subscribe(this);

        // Link this window and its folder together
        this.folder = folder;
        folder.setWindow(this);
        this.title = title;

        LOGGER.debug("{} Window [{},{},{},{}] Init", title,pos.x,pos.y, size.x,size.y);
        this.pos = new PVector(pos.x, pos.y);
        this.size = new PVector(size.x, size.y);
        this.sizeUnconstrained = new PVector(size.x, size.y);
        this.contentSize = new PVector(size.x, size.y);
        initDimensions();
        LOGGER.debug("{} Window [{},{},{},{}] Post-Dimension Init", title,pos.x,pos.y, this.size.x,this.size.y);
        contentBuffer = new RContentBuffer(this);
    }

    /**
     * Constructor for Internal Window
     *
     * @param app    PApplet to render window inside
     * @param title  title to give the window
     * @param xPos   initial X display location in PApplet
     * @param yPos   initial Y display location in PApplet
     * @param width  initial window width
     * @param height initial window height
     */
    public RWindowInt(PApplet app, RotomGui gui, RFolder folder, String title, float xPos, float yPos, float width, float height) {
        this(app, gui, folder, title, new PVector(xPos, yPos), new PVector(width, height));
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
    public RWindowInt(PApplet app, RotomGui gui, RFolder folder, float xPos, float yPos, float width, float height) {
        this(app, gui, folder, folder.getName(), xPos, yPos, width, height);
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
                PVector preferredSize = layout.calcPreferredSize((folder.shouldDrawTitle())?folder.getName():"",folder.getChildren());
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
                    vsb = Optional.of(new RScrollbar(this, pos.x + contentSize.x, pos.y, RLayoutStore.getCell(), size.y, sizeUnconstrained.y, 16));
                }
            }
        }
    }

    protected RComponent findComponentAt(float x, float y) {
        for (RComponent node : folder.getChildren()) {
            if (!node.isVisible()) {
                continue;
            }
            if (isPointInRect(x, y, node.getPosX(), node.getPosY(), node.getWidth(), node.getHeight())) {
                return node;
            }
        }
        return null;
    }

    /**
     * Check if the point is inside the Window
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the point is inside the window, false otherwise
     */
    protected boolean isPointInsideWindow(float x, float y) {
        return RCoordinates.isPointInRect(x, y, pos.x, pos.y, size.x, size.y);
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
     * Check if the mouse is inside the content of the Window
     *
     * @param e mouse event
     * @return true if the mouse is inside the content, false otherwise
     */

    protected boolean isMouseInsideContent(RMouseEvent e) {
        return isPointInsideContent(e.getX(), e.getY()) && !isPointInsideResizeBorder(e.getX(), e.getY());
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
        if (vsb.isEmpty() || !vsb.get().visible) {
            return false;
        }
        PVector contentStart = getContentStart();
        return isPointInRect(x, y,
                contentStart.x + contentSize.x, contentStart.y , RLayoutStore.getCell(), contentSize.y);
    }


    /**
     * Check if the mouse is inside the scroll bar of the Window
     *
     * @param e mouse event
     * @return true if the mouse is inside the scroll bar, false otherwisec
     */
    protected boolean isMouseInsideScrollbar(RMouseEvent e) {
        return isVisible && folder.isVisibleParentAware() && isPointInsideScrollbar(e.getX(), e.getY());
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
     * Check if the mouse is inside the title bar of the Window
     *
     * @param e mouse event
     * @return true if the mouse is inside the title bar, false otherwise
     */
    protected boolean isMouseInsideTitlebar(RMouseEvent e) {
        return isVisible && folder.isVisibleParentAware() && isPointInsideTitleBar(e.getX(), e.getY());
    }

    protected synchronized boolean isResizeWidth() {
        return isResizeWidth;
    }

    /**
     * Check if this window is the root window
     *
     * @return true if root, false otherwise
     */
    protected boolean isRoot() {
        return folder.getParent() instanceof RRoot;
    }

    /**
     * Set Focus to this Window
     */
    protected void setFocusOnThis() { // TODO LazyGui
        gui.setFocus(this);
    }

    protected void setTitleHighlighted(boolean v) {
        isTitleHighlighted = v;
    }

    protected void setScrollbarHighlighted(boolean v) {
        isScrollbarHighlighted = v;
    }

    /**
     * Keep the window in bounds, by altering its position
     *
     * @param pg Processing Graphics Context
     */
    protected void constrainPosition(PGraphics pg) {
        if (!RLayoutStore.shouldKeepWindowsInBounds()) {
            return;
        }
        float rightEdge = pg.width - size.x - 1;
        float bottomEdge = pg.height - size.y - 1;
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
     * Constrain the Windows width/height when the layout is horizontal
     *
     * @param pg             Processing Graphics Context
     * @param preferredWidth what it isa calculated to need
     */
    protected void constrainWidth(PGraphics pg, float preferredWidth) {
        if (isResizeWidth()) {
            size.x  = preferredWidth;
            contentSize.x = size.x - ((vsb.isPresent()&&vsb.get().visible)?RLayoutStore.getCell():0);
            resizeForContents(false);
        } else {
            if (contentSize.x == size.x && vsb.isPresent() && vsb.get().visible) {
                size.x = size.x + RLayoutStore.getCell();
            }

            if (sizing == RSizeMode.LAYOUT && preferredWidth != size.x) {
                size.x  = preferredWidth;
                contentSize.x = size.x - ((vsb.isPresent()&&vsb.get().visible)?RLayoutStore.getCell():0);
            }

            if (!RLayoutStore.shouldKeepWindowsInBounds()) {
                return;
            }

            if (pos.x + size.x > pg.width) {
                size.x = pg.width - pos.x;
                contentSize.x = size.x - ((vsb.isPresent()&&vsb.get().visible)?RLayoutStore.getCell():0);
            }
        }
        // TODO window bounds constraint
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

    protected PVector calcPreferredSize() {
        PVector preferredContentSize = folder.getLayout().calcPreferredSize((folder.shouldDrawTitle())?folder.getName():"",folder.getChildren());
        return switch (sizing) {
            case COMPONENT -> new PVector(preferredContentSize.x + ((vsb.isPresent()&&vsb.get().visible)?RLayoutStore.getCell():0),
                    preferredContentSize.y + ((folder.shouldDrawTitle())?RLayoutStore.getCell():0));
            case USER -> new PVector(size.x,size.y);
            case LAYOUT -> new PVector(size.x,size.y);
        };
    }

    protected void constrainBounds(PGraphics pg) {
        float oldWindowSizeX = size.x;
        float oldWindowSizeXForContents = contentSize.x;
        float oldWindowSizeXUnconstrained = sizeUnconstrained.x;

        float oldWindowSizeY = size.y;
        float oldWindowSizeYForContents = contentSize.y;
        float oldWindowSizeYUnconstrained = sizeUnconstrained.y;

        constrainPosition(pg);
        PVector preferredSize = calcPreferredSize();

        constrainHeight(pg, preferredSize.y);
        constrainWidth(pg, preferredSize.x);
        if (oldWindowSizeX != size.x || oldWindowSizeY != size.y ||
                oldWindowSizeXForContents != contentSize.x || oldWindowSizeYForContents != contentSize.y ||
                oldWindowSizeXUnconstrained != sizeUnconstrained.x || oldWindowSizeYUnconstrained != sizeUnconstrained.y) {
            LOGGER.trace("Old Width: [{},{},{}], New Width: [{},{},{}]", oldWindowSizeX, oldWindowSizeXUnconstrained, oldWindowSizeXForContents, contentSize.x, sizeUnconstrained.x, contentSize.x);
            LOGGER.trace("Old Height: [{},{},{}], New Height: [{},{},{}]", oldWindowSizeY, oldWindowSizeYUnconstrained, oldWindowSizeYForContents, contentSize.y, sizeUnconstrained.y, contentSize.y);

            reinitialiseBuffer();
        }
    }

    protected void handleBeingResized(RMouseEvent e) {
        sizing = RSizeMode.USER;
        float minimumWindowSizeInCells = 4;
        float maximumWindowSize = app.width;
        float oldWindowSizeX = size.x;
        size.x += e.getX() - e.getPrevX();
        size.x = constrain(size.x, minimumWindowSizeInCells * RLayoutStore.getCell(), maximumWindowSize);
        if (vsb.map(sb -> !sb.visible).orElse(true)) {
            contentSize.x = size.x;
        } else {
            contentSize.x = size.x - RLayoutStore.getCell();
        }
        vsb.ifPresent(RScrollbar::invalidateBuffer);
        e.consume();
        reinitialiseBuffer();
        LOGGER.debug("oldX: " + e.getPrevX() + ", new:X " + e.getX());
        LOGGER.debug("old: " + oldWindowSizeX + ", new: " + size.x);
    }

    private void trySnapToGrid() {
        PVector snappedPos = RSnapToGrid.trySnapToGrid(pos.x, pos.y);
        pos.x = snappedPos.x;
        pos.y = snappedPos.y;
    }

    /**
     * Draw The Window Title Bar
     *
     * @param pg         Processing Graphics Context
     * @param shouldDraw if the title bar should be drawn
     */
    protected void drawTitleBar(PGraphics pg, boolean shouldDraw) {
        if (shouldDraw) {
            float availableWidthForText = size.x - RFontStore.getMarginX() + (isRoot() ? 0 : -RLayoutStore.getCell());
            String leftText = RFontStore.substringToFit(pg, folder.getName(), availableWidthForText);
            pg.pushMatrix();
            pg.pushStyle();
            pg.translate(pos.x, pos.y);
            pg.fill(isTitleHighlighted() ? RThemeStore.getRGBA(FOCUS_BACKGROUND) : RThemeStore.getRGBA(NORMAL_BACKGROUND));
            if (!app.focused && isRoot()) {
                pg.fill(RThemeStore.getRGBA(FOCUS_BACKGROUND));
                leftText = "not in focus";
                setTitleHighlighted(true);
            }
            float titleBarWidth = size.x;
            pg.strokeWeight(1);
            pg.stroke(RThemeStore.getRGBA(WINDOW_BORDER));
            pg.rect(0, 0, titleBarWidth, RLayoutStore.getCell());
            pg.fill(isTitleHighlighted() ? RThemeStore.getRGBA(FOCUS_FOREGROUND) : RThemeStore.getRGBA(NORMAL_FOREGROUND));
            pg.textAlign(LEFT, CENTER);
            pg.text(leftText, RFontStore.getMarginX(), RLayoutStore.getCell() - RFontStore.getMarginY());
            pg.popStyle();
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
        if (isPointInsideCloseButton(app.mouseX, app.mouseY) || isCloseInProgress) {
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
     * Draw The Window resize indicator
     *
     * @param pg Processing Graphics Context
     */
    protected void drawResizeIndicator(PGraphics pg) {
        if (isPointInsideResizeBorder(app.mouseX, app.mouseY) && RLayoutStore.shouldDrawResizeIndicator()) {
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
     * Draw The Window tooltip
     *
     * @param pg Processing Graphics Context
     */
    protected void drawPathTooltipOnHighlight(PGraphics pg) { // TODO make use of this
        if (!isPointInsideTitleBar(app.mouseX, app.mouseY) || !RLayoutStore.shouldShowPathTooltips()) {
            return;
        }
        pg.pushMatrix();
        pg.pushStyle();
        pg.translate(pos.x, pos.y);
        String[] pathSplit = RPaths.splitFullPathWithoutEndAndRoot(folder.path);
        int lineCount = pathSplit.length;
        float tooltipXOffset = RLayoutStore.getCell() * 0.5f;
        float tooltipWidthMinimum = size.x - tooltipXOffset - RLayoutStore.getCell();
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.textAlign(LEFT, CENTER);
        for (int i = 0; i < lineCount; i++) {
            String line = pathSplit[lineCount - 1 - i];
            float tooltipWidth = max(tooltipWidthMinimum, pg.textWidth(line) + RFontStore.getMarginX() * 2);
            pg.fill(RThemeStore.getRGBA(NORMAL_BACKGROUND));
            pg.rect(tooltipXOffset, -i * RLayoutStore.getCell() - RLayoutStore.getCell(), tooltipWidth, RLayoutStore.getCell());
            pg.fill(RThemeStore.getRGBA(NORMAL_FOREGROUND));
            pg.text(line, RFontStore.getMarginX() + tooltipXOffset, -i * RLayoutStore.getCell() - RFontStore.getMarginY());
        }
        pg.popMatrix();
        pg.popStyle();
    }


    /**
     * Draw The Window background
     *
     * @param pg                 Processing Graphics Context
     * @param drawBackgroundOnly true if drawing the background, false if drawing the background's outline
     */
    protected void drawBackgroundWithWindowBorder(PGraphics pg, boolean drawBackgroundOnly) {
        pg.pushMatrix();
        pg.translate(pos.x, pos.y);
        pg.stroke(RThemeStore.getRGBA(WINDOW_BORDER));
        pg.strokeWeight(1);
        pg.fill(RThemeStore.getRGBA(NORMAL_BACKGROUND));
        if (drawBackgroundOnly) {
            pg.noStroke();
        } else {
            pg.noFill();
        }
        pg.rect(0, 0, size.x, size.y);
        pg.popMatrix();
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
            if (vsb.isPresent() && vsb.get().visible) {
                int yDiff = round((sizeUnconstrained.y - size.y) * vsb.map(s -> s.value).orElse(0.0f));
                pg.image(contentBuffer.draw().get(0, yDiff, (int) contentSize.x, (int) (size.y - RLayoutStore.getCell())), 0, 0);
            } else {
                pg.image(contentBuffer.draw(), 0, 0);
            }
            pg.popMatrix();
        }
    }

    /**
     * Draw The Window
     *
     * @param pg Processing Graphics Context
     */
    public void drawWindow(PGraphics pg) {
        pg.textFont(RFontStore.getMainFont());
        setTitleHighlighted(isVisible && (isPointInsideTitleBar(app.mouseX, app.mouseY) && isBeingDragged) || folder.isMouseOver());
        setScrollbarHighlighted(isVisible && (isPointInsideScrollbar(app.mouseX, app.mouseY) && !isBeingDragged) || folder.isMouseOver());
        if (!isVisible || !folder.isVisibleParentAware()) {
            return;
        }
        constrainBounds(pg);
        pg.pushMatrix();
        drawBackgroundWithWindowBorder(pg, true);
        drawPathTooltipOnHighlight(pg);
        PVector contentStart = getContentStart();
        vsb.ifPresent(s ->
                s.draw(
                        pg,
                        contentStart.x,
                        contentStart.y,
                        contentSize.x,
                        contentSize.y,
                        sizeUnconstrained.y - ((folder.shouldDrawTitle())?RLayoutStore.getCell():0) // TODO Sizing Check
                )
        );
        if (!folder.getChildren().isEmpty()) {
            drawContent(pg);
        }
        drawBackgroundWithWindowBorder(pg, false);
        drawTitleBar(pg, folder.shouldDrawTitle());
        if (!isRoot()) {
            drawCloseButton(pg);
        }
        drawResizeIndicator(pg);
        pg.popMatrix();
    }


    public void drawContextLineFromTitleBarToInlineNode(PGraphics pg, float endRectSize, boolean pickShortestLine) {
        RComponent firstOpenParent = gui.getComponentTree().findFirstOpenParentNodeRecursively(folder);
        if (firstOpenParent == null || !firstOpenParent.isParentWindowVisible()) {
            return;
        }
        float xOffset = RLayoutStore.getCell() / 2f;
        float y0 = pos.y + RLayoutStore.getCell() / 2f;
        float y1 = firstOpenParent.getPosY() + firstOpenParent.getHeight() / 2f;
        float x0a = pos.x - xOffset;
        float x0b = pos.x + size.x + xOffset;
        float x1a = firstOpenParent.getPosX() - xOffset;
        float x1b = firstOpenParent.getPosX() + firstOpenParent.getWidth() + xOffset;
        float x0 = x0a;
        float x1 = x1b;
        if (pickShortestLine) {
            class PointDist {
                final float x0, x1, d;

                public PointDist(float x0, float x1, float d) {
                    this.x0 = x0;
                    this.x1 = x1;
                    this.d = d;
                }
            }
            PointDist[] pointsWithDistances = new PointDist[]{
                    new PointDist(x0a, x1a, dist(x0a, y0, x1a, y1)),
                    new PointDist(x0a, x1b, dist(x0a, y0, x1b, y1)),
                    new PointDist(x0b, x1b, dist(x0b, y0, x1b, y1)),
                    new PointDist(x0b, x1a, dist(x0b, y0, x1a, y1)),
            };
            Arrays.sort(pointsWithDistances, (p1, p2) -> Float.compare(p1.d, p2.d));
            x0 = pointsWithDistances[0].x0;
            x1 = pointsWithDistances[0].x1;
        }
        pg.line(x0, y0, x1, y1);
        pg.rectMode(CENTER);
        pg.rect(x0, y0, endRectSize, endRectSize);
        pg.rect(x1, y1, endRectSize, endRectSize);
    }

    public PVector getContentStart(){
        return new PVector(pos.x, pos.y + ((folder.shouldDrawTitle())?RLayoutStore.getCell():0));
    }

    public int getContentWidth() {
        switch (folder.getLayout()) {
            case RLinearLayout linear ->  {
                return (int) contentSize.x;
            }
            default -> throw new IllegalStateException("Unexpected value: " + folder.getLayout());
        }
    }

    public float getContentHeight() {
        switch (folder.getLayout()) {
            case RLinearLayout linear -> {return (int) contentSize.y;
            }
            default -> throw new IllegalStateException("Unexpected value: " + folder.getLayout());
        }
    }

    public RFolder getFolder() {
        return folder;
    }

    @Override
    public int getHeight() {
        return (int) size.y;
    }


    public RLayoutConfig getLayoutConfig() {
        return folder.getCompLayoutConfig();
    }

    @Override
    public PVector getPos() {
        return new PVector(pos.x, pos.y);
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
    public PVector getSize() {
        return new PVector(size.x, size.y);
    }

    @Override
    public PApplet getSketch() {
        return app;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return (int) size.x;
    }


    public boolean isVisible() {
        return isVisible;
    }

    public boolean isDragged() {
        return false;
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
     * Check if the title bar is highlighted
     *
     * @return true if the title is highlighted, false otherwise
     */
    public boolean isTitleHighlighted() { // TODO LazyGui
        return isTitleHighlighted;
    }

    public void setCoordinates(float x, float y) {
        pos.x = x;
        pos.y = y;
    }

    public void setBounds(float x, float y, float sizeX, float sizeY, RSizeMode mode) {
        LOGGER.trace("Setting Bounds [{},{},{},{}] for {} using {}",x,y,sizeX,sizeY,folder.getName(), mode);
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
        LOGGER.trace("Adjusted Bounds [{},{},{},{}] for {} using {}",x,y,size.x,size.y,folder.getName(), mode);
        reinitialiseBuffer();
        folder.sortChildren();
    }

//    public void setDimensions(float x, float y) {
//        setBounds(pos.x, pos.y, x, y);
//    }

    public void setWidth(Float nullableSizeX) {
        size.x = nullableSizeX;
    }

    public void redrawBuffer() { // TODO LazyGui
        contentBuffer.invalidateBuffer();
    }

    public void reinitialiseBuffer() { // TODO LazyGui
        contentBuffer.resetBuffer();
    }

    /**
     * Method to open the Window
     *
     * @param setFocus true if should be focused on, false otherwise
     */
    public void open(boolean setFocus) { // TODO LazyGui
        isVisible = true;
        if (setFocus) {
             setFocusOnThis();
        }
        reinitialiseBuffer();
    }

    public void close() { // TODO LazyGui
        isVisible = false;
        isBeingDragged = false;
    }

    public synchronized void resizeForContents(boolean shouldResize) {
        isResizeWidth = shouldResize;
        if (isResizeWidth){
            sizing = RSizeMode.COMPONENT;
        }
    }

    @Override
    public void keyPressed(RKeyEvent keyEvent) {
        float x = app.mouseX;
        float y = app.mouseY;
        if (isPointInsideTitleBar(x, y)) {
            folder.keyPressedOverComponent(keyEvent, x, y);
            return;
        }
        RComponent nodeUnderMouse = findComponentAt(x, y);
        if (nodeUnderMouse != null && nodeUnderMouse.isParentWindowVisible() && folder.isVisibleParentAware()) {
            if (isPointInsideContent(x, y)) {
                nodeUnderMouse.keyPressedOverComponent(keyEvent, x, y);
            }
        }
    }

    @Override
    public void mousePressed(RMouseEvent e) {
        if (!isVisible() || !folder.isVisibleParentAware()) {
            return;
        }
        if (isPointInsideWindow(e.getX(), e.getY()) && e.isLeft()) {
            if (!isFocused()) {
                setFocusOnThis();
            }
            e.consume();
        }
        if (isPointInsideTitleBar(e.getX(), e.getY()) && e.isLeft()) {
            isBeingDragged = true;
            e.consume();
            setFocusOnThis();
            return;
        }
        if (isPointInsideScrollbar(e.getX(), e.getY()) && e.isLeft()) {
            vsb.ifPresent(s -> s.mousePressed(e));
            e.consume();
            setFocusOnThis();
            return;
        }
        if (!isRoot() &&
                ((isPointInsideCloseButton(e.getX(), e.getY()) && e.isLeft()) ||
                        (isPointInsideWindow(e.getX(), e.getY()) && e.isRight()))) {
            isCloseInProgress = true;
            e.consume();
            return;
        }
        if (isPointInsideResizeBorder(e.getX(), e.getY()) && RLayoutStore.isWindowResizeEnabled()) {
            isBeingResized = true;
            e.consume();
        } else if (isPointInsideContent(e.getX(), e.getY())) {
            RComponent node = findComponentAt(e.getX(), e.getY());
            if (node != null && node.isParentWindowVisible()) {
                contentBuffer.invalidateBuffer();
                node.mousePressed(e);
            }
        }
    }

    @Override
    public void mouseReleased(RMouseEvent e) {
        RMouseHiding.tryRevealMouseAfterDragging(app);
        if (!isVisible() || !folder.isVisibleParentAware()) {
            return;
        }
        if (!isRoot() && isCloseInProgress &&
                ((isPointInsideCloseButton(e.getX(), e.getY()) && e.isLeft()) ||
                        (isPointInsideWindow(e.getX(), e.getY()) && e.isRight()))) {
            close();
            e.consume();
        } else if (isBeingDragged) {
            trySnapToGrid();
            e.consume();
        } else if (isBeingResized && RSnapToGrid.snapToGridEnabled) {
            size.x = RSnapToGrid.trySnapToGrid(size.x, 0).x;
            contentSize.x = size.x;
            sizeUnconstrained.x = size.x;
            reinitialiseBuffer();
            e.consume();
        }
        isCloseInProgress = false;
        isBeingDragged = false;
        isBeingResized = false;

        if (e.isConsumed()) {
            return;
        }

        if (isPointInsideContent(e.getX(), e.getY())) {
            RComponent clickedNode = findComponentAt(e.getX(), e.getY());
            for (RComponent node : folder.getChildren()) {
                if (!e.isConsumed() && node.equals(clickedNode) && clickedNode.isParentWindowVisible() && clickedNode.isVisible()) {
                    contentBuffer.invalidateBuffer();
                    clickedNode.mouseReleasedOverComponent(e);
                    e.consume();
                } else {
                    node.mouseReleasedAnywhere(e);
                }
            }
        }
    }

    @Override
    public void mouseMoved(RMouseEvent e) {
        if (isMouseInsideTitlebar(e)) {
            if (folder.getChildren().stream().anyMatch(RComponent::isMouseOver)) {
                contentBuffer.invalidateBuffer();
            }
            e.consume();
            folder.setIsMouseOverThisNodeOnly(gui.getComponentTree(),e);
        } else if (isMouseInsideScrollbar(e)) {
            if (folder.getChildren().stream().anyMatch(RComponent::isMouseOver)) {
                contentBuffer.invalidateBuffer();
            }
            e.consume();
            vsb.ifPresent(s -> s.mouseMoved(e));
            folder.setIsMouseOverThisNodeOnly(gui.getComponentTree(),e);
        } else if (isMouseInsideContent(e)) {
            PVector contentStart = getContentStart();
            // LOGGER.debug("Mouse Inside Content: X {} Y {} WinX {} WinY {} Width {} Height {}", e.getX(), e.getY(), contentStart.x, contentStart.y, size.x, size.y);
            float yDiff = sizeUnconstrained.y - size.y;
            RComponent node = findComponentAt(e.getX(), e.getY() + yDiff * vsb.map(s -> s.value).orElse(0.0f));
            if (node != null && !node.isMouseOver()) {
                LOGGER.debug("Inside {} [NX {} NY {} Width {} Height {}]", node.getName(), node.getPosX(), node.getPosY(), node.getWidth(), node.getHeight());
                contentBuffer.invalidateBuffer();
            }
            if (node != null && node.isParentWindowVisible()) {
                node.setIsMouseOverThisNodeOnly(gui.getComponentTree(),e);
                e.consume();
            }
        } else {
            if (folder.getChildren().stream().anyMatch(RComponent::isMouseOver)) {
                contentBuffer.invalidateBuffer();
            }
            gui.setAllMouseOverToFalse(this.folder);
        }
    }

    @Override
    public void mouseDragged(RMouseEvent e) {
        if (!isVisible()) {
            return;
        }
        if (isBeingDragged) {
            pos.x += e.getX() - e.getPrevX();
            pos.y += e.getY() - e.getPrevY();
            vsb.ifPresent(RScrollbar::invalidateBuffer);
            e.consume();
        } else if (isBeingResized) {
            handleBeingResized(e);
        } else if (vsb.map(s -> s.dragging).orElse(false)) {
            vsb.ifPresent(s -> s.mouseDragged(e));
        }
        for (RComponent child : folder.getChildren()) {
            if (child.isDragged() && child.isParentWindowVisible()) {
                child.mouseDragContinues(e);
                if (e.isConsumed() && child.isDraggable()) {
                    RMouseHiding.tryHideMouseForDragging(app);
                }
            }
        }
    }

    @Override
    public void mouseWheel(RMouseEvent e) {
        if (isPointInsideWindow(e.getX(), e.getY())) {
            vsb.ifPresent(s -> s.mouseWheel(e));
            e.consume();
        }
    }
}
