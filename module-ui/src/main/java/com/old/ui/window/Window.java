package com.old.ui.window;

import com.old.ui.constants.GlobalReferences;
import com.old.ui.constants.theme.ThemeStore;
import com.old.ui.input.InputWatcherBackend;
import com.old.ui.input.MouseHiding;
import com.old.ui.input.UserInputSubscriber;
import com.old.ui.input.keys.GuiKeyEvent;
import com.old.ui.input.mouse.GuiMouseEvent;
import com.old.ui.node.AbstractNode;
import com.old.ui.node.LayoutType;
import com.old.ui.node.NodePaths;
import com.old.ui.node.NodeTree;
import com.old.ui.node.impl.FolderNode;
import com.old.ui.store.FontStore;
import com.old.ui.store.LayoutStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.*;

import static com.old.ui.constants.theme.ThemeColorType.*;
import static com.old.ui.store.LayoutStore.cell;
import static com.old.ui.utils.Coordinates.isPointInRect;
import static processing.core.PApplet.*;

/**
 * Gui Window Node Organisation and Drawing
 */
public class Window implements UserInputSubscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(Window.class);

    // Companion Folder Node
    protected final FolderNode folder;

    // Vertical Scrollbar
    protected Optional<Scrollbar> vsb = Optional.empty();

    // Window Content Buffer
    protected final ContentBuffer contentBuffer;

    // Window Position Info
    protected float posX;
    protected float posY;

    // Window Width Info
    protected float windowSizeXForContents; // width of the content nodes
    protected float windowSizeX; // total window width including scrollbar

    // Window Height Info
    protected float windowSizeYUnconstrained; // total window height, based on individual content node heights
    protected float windowSizeY; // actual window height, constrained by app window size

    // Window Status Info
    protected boolean isTitleHighlighted; // if the window title is visible, should it be highlighted
    protected boolean isScrollbarHighlighted;  // if the vertical scrollbar is visible, should it be highlighted
    protected boolean isBeingResized; // if the window shape is changing
    protected boolean isBeingDragged; // if the window is being moved
    protected boolean isVisible = true; // if the window is visible
    protected boolean isResizeWidth; // if the window is visible

    // Window Transition Status Info
    protected boolean isCloseInProgress; // if close button press in progress

    static String[] splitFullPathWithoutEndAndRoot(String fullPath) {
        String[] pathWithEnd = NodePaths.splitByUnescapedSlashes(fullPath);
        return Arrays.copyOf(pathWithEnd, pathWithEnd.length - 1);
    }

    /**
     * Make a window for a folder node
     *
     * @param folder        its companion folder
     * @param posX          initial window x position
     * @param posY          initial window y position
     * @param nullableWidth null if no expected width, otherwise a value
     */
    public Window(FolderNode folder, float posX, float posY, Float nullableWidth) {
        // Set Initial Position
        this.posX = posX;
        this.posY = posY;

        // Listen to input events
        InputWatcherBackend.subscribe(this);

        // Link this window and its folder together
        this.folder = folder;
        folder.window = this;

        initWindowWidth(nullableWidth);
        initScrollbar();
        contentBuffer = new ContentBuffer(this);
    }

    /**
     * Calculate the width of the Window
     *
     * @param nullableWidth null if no expected width, otherwise a value
     */
    protected void initWindowWidth(Float nullableWidth) {
        if (nullableWidth == null) {
            if (LayoutStore.getAutosuggestWindowWidth() && folder.idealWindowWidthInCells == LayoutStore.defaultWindowWidthInCells) {
                windowSizeX = folder.autosuggestWindowWidthForContents();
            } else {
                windowSizeX = cell * folder.idealWindowWidthInCells;
            }
        } else {
            windowSizeX = nullableWidth;
        }
        windowSizeXForContents = windowSizeX;
    }

    /**
     * Initialise Scrollbar for Vertical Layouts
     */
    protected void initScrollbar() {
        if (LayoutType.isVertical(folder.getLayout())) {
            vsb = Optional.of(new Scrollbar(posX + windowSizeXForContents, posY, cell, windowSizeY, windowSizeYUnconstrained, 16));
        }
    }

    /**
     * Calculate the max height of all columns
     *
     * @return max height of columns
     */
    protected float heightOfColumns() {
        Map<Integer, Float> columnHeights = new HashMap<>();
        for (AbstractNode child : folder.children) {
            if (!child.isVisible()) {
                continue;
            }
            float height = columnHeights.getOrDefault(child.getColumn(), 0.0f);
            columnHeights.put(child.getColumn(), height + child.getHeight());
        }
        return columnHeights.values().stream().max(Float::compareTo).orElse(0.0f);
    }

    /**
     * Calculate the height of all visible Child Nodes
     *
     * @return total height of Child Nodes
     */
    protected float heightSumOfChildNodes() {
        float sum = 0;
        for (AbstractNode child : folder.children) {
            if (!child.isVisible()) {
                continue;
            }
            sum += child.getHeight();
        }
        return sum;
    }

    /**
     * Keep the window in bounds, by altering its position
     *
     * @param pg Processing Graphics Context
     */
    protected void constrainPosition(PGraphics pg) {
        if (!LayoutStore.getShouldKeepWindowsInBounds()) {
            return;
        }
        float rightEdge = pg.width - windowSizeX - 1;
        float bottomEdge = pg.height - windowSizeY - 1;
        float lerpAmt = 0.3f;
        if (posX < 0) {
            posX = lerp(posX, 0, lerpAmt);
        }
        if (posY < 0) {
            posY = lerp(posY, 0, lerpAmt);
        }
        if (posX > rightEdge) {
            posX = lerp(posX, rightEdge, lerpAmt);
        }
        if (posY > bottomEdge) {
            posY = lerp(posY, bottomEdge, lerpAmt);
        }
    }

    /**
     * Constrain the Windows width/height when the layout is vertical
     *
     * @param pg Processing Graphics Context
     */
    protected void constrainColumnedHeight(PGraphics pg) {
        if (isResizeWidth()) {
            windowSizeX = windowSizeXForContents = folder.autosuggestWindowWidthForContents();
            resizeForContents(false);
        }
        windowSizeY = cell + heightOfColumns();
        windowSizeYUnconstrained = windowSizeY;
        if (!LayoutStore.getShouldKeepWindowsInBounds()) {
            return;
        }
        if (posY + windowSizeY > pg.height * 0.9) {
            float sum = cell;
            int index = 0;
            while (index < folder.children.size()) {
                AbstractNode child = folder.children.get(index);
                if (!child.isVisible()) {
                    continue;
                }
                if (posY + sum + child.getHeight() > pg.height * 0.9) {
                    index -= 1;
                    break;
                }
                sum += child.getHeight();
                index++;
            }
            if (windowSizeXForContents == windowSizeX && !isBeingResized) {
                windowSizeXForContents = windowSizeX;
                windowSizeX = windowSizeX + cell;
            }
            windowSizeY = sum;
            vsb.ifPresent(s -> s.setVisible(true));
        }
    }


    /**
     * Constrain the Windows width/height when the layout is vertical
     *
     * @param pg Processing Graphics Context
     */
    protected void constrainHeight(PGraphics pg) {
        if (isResizeWidth()) {
            windowSizeX = windowSizeXForContents = folder.autosuggestWindowWidthForContents();
            resizeForContents(false);
        }
        windowSizeY = cell + heightSumOfChildNodes();
        windowSizeYUnconstrained = windowSizeY;
        if (!LayoutStore.getShouldKeepWindowsInBounds()) {
            return;
        }
        if (posY + windowSizeY > pg.height * 0.9) {
            float sum = cell;
            int index = 0;
            while (index < folder.children.size()) {
                AbstractNode child = folder.children.get(index);
                if (!child.isVisible()) {
                    continue;
                }
                if (posY + sum + child.getHeight() > pg.height * 0.9) {
                   break;
                }
                sum += child.getHeight();
                index++;
            }
            if (windowSizeXForContents == windowSizeX && !isBeingResized) {
                windowSizeXForContents = windowSizeX;
                windowSizeX = windowSizeX + cell;
            }
            windowSizeY = sum;
            vsb.ifPresent(s -> s.setVisible(true));
        }
    }

    /**
     * Constrain the Windows width/height when the layout is horizontal
     *
     * @param pg Processing Graphics Context
     */
    protected void constrainWidth(PGraphics pg) {
        if (isResizeWidth()) {
            windowSizeX = windowSizeXForContents = folder.autosuggestWindowWidthForContents();
            resizeForContents(false);
        }
        windowSizeY = cell;
    }

    protected void constrainBounds(PGraphics pg) {
        float oldWindowSizeX = windowSizeX;
        float oldWindowSizeXForContents = windowSizeXForContents;

        float oldWindowSizeY = windowSizeY;
        float oldWindowSizeYUnconstrained = windowSizeYUnconstrained;

        constrainPosition(pg);
        switch (folder.getLayout()) {
            case HORIZONAL -> constrainWidth(pg);
            case VERTICAL_X_COL -> constrainColumnedHeight(pg);
            default -> constrainHeight(pg);
        }
        switch (folder.getLayout()) {
            case HORIZONAL -> {
                if (oldWindowSizeX != windowSizeX || oldWindowSizeY != windowSizeY){
                    contentBuffer.resetBuffer();
                }
            }
            default -> {
                if (oldWindowSizeXForContents != windowSizeXForContents || oldWindowSizeYUnconstrained != windowSizeYUnconstrained){
                    contentBuffer.resetBuffer();
                }
            }
        }
    }


    protected void handleBeingResized(GuiMouseEvent e) {
        float minimumWindowSizeInCells = 4;
        float maximumWindowSize = GlobalReferences.app.width;
        float oldWindowSizeX = windowSizeX;
        windowSizeX += e.getX() - e.getPrevX();
        windowSizeX = constrain(windowSizeX, minimumWindowSizeInCells * cell, maximumWindowSize);
        if (vsb.map(sb -> !sb.visible).orElse(true)) {
            windowSizeXForContents = windowSizeX;
        } else {
            windowSizeXForContents = windowSizeX;
            windowSizeX = windowSizeXForContents + cell;
        }
        vsb.ifPresent(Scrollbar::invalidateBuffer);
        e.setConsumed(true);
        contentBuffer.resetBuffer();
        LOGGER.debug("oldX: " + e.getPrevX() + ", new:X " + e.getX());
        LOGGER.debug("old: " + oldWindowSizeX + ", new: " + windowSizeX);
    }

    /**
     * Find a Child Node that the point is inside
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return Child Node that the point is inside, null if not inside any of the children
     */
    protected AbstractNode tryFindChildNodeAt(float x, float y) {
        for (AbstractNode node : folder.children) {
            if (!node.isVisible()) {
                continue;
            }
            if (isPointInRect(x, y, node.pos.x, node.pos.y, node.size.x, node.size.y)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Check if the point is inside the close button of the Window
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the point is inside the close button, false otherwise
     */
    protected boolean isPointInsideCloseButton(float x, float y) {
        return isPointInRect(x, y, posX + windowSizeX - cell - 1, posY, cell + 1, cell - 1);
    }

    /**
     * Check if the point is inside the content of the Window
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the point is inside the child nodes, false otherwise
     */
    protected boolean isPointInsideContent(float x, float y) {
        if (folder.shouldDrawTitle()) {
            return isPointInRect(x, y,
                    posX, posY + cell,
                    windowSizeXForContents, windowSizeY - cell);
        } else {
            return isPointInsideWindow(x, y);
        }
    }

    /**
     * Check if the mouse is inside the content of the Window
     *
     * @param e mouse event
     * @return true if the mouse is inside the content, false otherwise
     */

    protected boolean isMouseInsideContent(GuiMouseEvent e) {
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
        if (!LayoutStore.getWindowResizeEnabled()) {
            return false;
        }
        float w = LayoutStore.getResizeRectangleSize();
        return isPointInRect(x, y, posX + windowSizeX - w / 2f, posY, w, windowSizeY);
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
        return isPointInRect(x, y, posX + windowSizeXForContents, posY + cell, cell, windowSizeY - cell);
    }


    /**
     * Check if the mouse is inside the scroll bar of the Window
     *
     * @param e mouse event
     * @return true if the mouse is inside the scroll bar, false otherwise
     */
    protected boolean isMouseInsideScrollbar(GuiMouseEvent e) {
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
            if (isRoot()) {
                return isPointInRect(x, y, posX, posY, windowSizeX, cell);
            }
            return isPointInRect(x, y, posX, posY, windowSizeX - cell, cell);
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
    protected boolean isMouseInsideTitlebar(GuiMouseEvent e) {
        return isVisible && folder.isVisibleParentAware() && isPointInsideTitleBar(e.getX(), e.getY());
    }

    /**
     * Check if the point is inside the Window
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the point is inside the window, false otherwise
     */
    protected boolean isPointInsideWindow(float x, float y) {
        return isPointInRect(x, y, posX, posY, windowSizeX, windowSizeY);
    }

    protected synchronized boolean isResizeWidth(){
        return isResizeWidth;
    }

    protected boolean isScrollbarHighlighted() {
        return isScrollbarHighlighted;
    }

    /**
     * Check if the window is focused upon
     *
     * @return true if the window is the focus, false otherwise
     */
    protected boolean isFocused() {
        return WindowManager.isFocused(this);
    }

    /**
     * Check if this window is the root window
     *
     * @return true if root, false otherwise
     */
    boolean isRoot() {
        return folder.equals(NodeTree.getRoot());
    }

    public boolean isBeingDragged() {
        return isBeingDragged;
    }

    /**
     * Check if the title bar is highlighted
     *
     * @return true if the title is highlighted, false otherwise
     */
    public boolean isTitleHighlighted() {
        return isTitleHighlighted;
    }

    /**
     * Check if the window is visible
     *
     * @return true if the window is visible, false otherwise
     */
    public boolean isVisible() {
        return isVisible || LayoutStore.isGuiHidden();
    }

    protected void setTitleHighlighted(boolean v) {
        isTitleHighlighted = v;
    }

    protected void setScrollbarHighlighted(boolean v) {
        isScrollbarHighlighted = v;
    }

    /**
     * Set Focus to this Window
     */
    void setFocusOnThis() {
        WindowManager.setFocus(this);
        InputWatcherBackend.setFocus(this);
    }

    private void trySnapToGrid() {
        PVector snappedPos = SnapToGrid.trySnapToGrid(posX, posY);
        posX = snappedPos.x;
        posY = snappedPos.y;
    }

    /**
     * Method to open the Window
     *
     * @param startDragging true if being dragged, false otherwise
     */
    void open(boolean startDragging) {
        isVisible = true;
        if (startDragging) {
            isBeingDragged = true;
            setFocusOnThis();
        }
        contentBuffer.resetBuffer();
    }

    /**
     * Method to close the Window
     */
    public void close() {
        isVisible = false;
        isBeingDragged = false;
    }

    /**
     * Draw The Window Title Bar
     *
     * @param pg         Processing Graphics Context
     * @param shouldDraw if the title bar should be drawn
     */
    private void drawTitleBar(PGraphics pg, boolean shouldDraw) {
        if (shouldDraw) {
            float availableWidthForText = windowSizeX - FontStore.textMarginX + (isRoot() ? 0 : -cell);
            String leftText = FontStore.getSubstringFromStartToFit(pg, folder.getName(), availableWidthForText);
            pg.pushMatrix();
            pg.pushStyle();
            pg.translate(posX, posY);
            pg.fill(isTitleHighlighted() ? ThemeStore.getColor(FOCUS_BACKGROUND) : ThemeStore.getColor(NORMAL_BACKGROUND));
            if (!GlobalReferences.app.focused && isRoot()) {
                pg.fill(ThemeStore.getColor(FOCUS_BACKGROUND));
                leftText = "not in focus";
                setTitleHighlighted(true);
            }
            float titleBarWidth = windowSizeX;
            pg.strokeWeight(1);
            pg.stroke(ThemeStore.getColor(WINDOW_BORDER));
            pg.rect(0, 0, titleBarWidth, cell);
            pg.fill(isTitleHighlighted() ? ThemeStore.getColor(FOCUS_FOREGROUND) : ThemeStore.getColor(NORMAL_FOREGROUND));
            pg.textAlign(LEFT, CENTER);
            pg.text(leftText, FontStore.textMarginX, cell - FontStore.textMarginY);
            pg.popStyle();
            pg.popMatrix();
        }
    }

    /**
     * Draw The Window close button
     *
     * @param pg Processing Graphics Context
     */
    private void drawCloseButton(PGraphics pg) {
        pg.pushMatrix();
        pg.translate(posX, posY);
        pg.stroke(ThemeStore.getColor(WINDOW_BORDER));
        pg.strokeWeight(1);
        pg.line(windowSizeX - cell, 0, windowSizeX - cell, cell - 1);
        if (isPointInsideCloseButton(GlobalReferences.app.mouseX, GlobalReferences.app.mouseY) || isCloseInProgress) {
            pg.fill(ThemeStore.getColor(FOCUS_BACKGROUND));
            pg.noStroke();
            pg.rectMode(CORNER);
            pg.rect(windowSizeX - cell + 0.5f, 1, cell - 1, cell - 1);
            pg.stroke(ThemeStore.getColor(FOCUS_FOREGROUND));
            pg.strokeWeight(1.99f);
            pg.pushMatrix();
            pg.translate(windowSizeX - cell * 0.5f + 0.5f, cell * 0.5f);
            float n = cell * 0.2f;
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
    private void drawResizeIndicator(PGraphics pg) {
        if (isPointInsideResizeBorder(GlobalReferences.app.mouseX, GlobalReferences.app.mouseY) && LayoutStore.getShouldDrawResizeIndicator()) {
            float w = LayoutStore.getResizeRectangleSize();
            pg.pushMatrix();
            pg.translate(posX, posY);
            pg.noStroke();
            pg.fill(ThemeStore.getColor(WINDOW_BORDER));
            pg.rect(windowSizeX - w / 2f, 0, w, windowSizeY);
            pg.popMatrix();
        }
    }

    /**
     * Draw The Window tooltip
     *
     * @param pg Processing Graphics Context
     */
    private void drawPathTooltipOnHighlight(PGraphics pg) { // TODO make use of this
        if (!isPointInsideTitleBar(GlobalReferences.app.mouseX, GlobalReferences.app.mouseY) || !LayoutStore.getShowPathTooltips()) {
            return;
        }
        pg.pushMatrix();
        pg.pushStyle();
        pg.translate(posX, posY);
        String[] pathSplit = splitFullPathWithoutEndAndRoot(folder.path);
        int lineCount = pathSplit.length;
        float tooltipXOffset = cell * 0.5f;
        float tooltipWidthMinimum = windowSizeX - tooltipXOffset - cell;
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.textAlign(LEFT, CENTER);
        for (int i = 0; i < lineCount; i++) {
            String line = pathSplit[lineCount - 1 - i];
            float tooltipWidth = max(tooltipWidthMinimum, pg.textWidth(line) + FontStore.textMarginX * 2);
            pg.fill(ThemeStore.getColor(NORMAL_BACKGROUND));
            pg.rect(tooltipXOffset, -i * cell - cell, tooltipWidth, cell);
            pg.fill(ThemeStore.getColor(NORMAL_FOREGROUND));
            pg.text(line, FontStore.textMarginX + tooltipXOffset, -i * cell - FontStore.textMarginY);
        }
        pg.popMatrix();
        pg.popStyle();
    }

    /**
     * Draw Child Nodes horizontally
     *
     * @param pg Processing Graphics Context
     */
    private void drawInlineFolderChildrenHorizontally(PGraphics pg) {
        pg.pushMatrix();
        pg.translate(posX, posY);
        pg.image(contentBuffer.draw(), 0, 0);
        pg.popMatrix();
    }

    /**
     * Draw Child Nodes vertically
     *
     * @param pg Processing Graphics Context
     */
    protected void drawInlineFolderChildrenVertically(PGraphics pg) {
        pg.pushMatrix();
        pg.translate(posX, posY);
        pg.translate(0, cell);
        if (vsb.isPresent() && vsb.get().visible) {
            int yDiff = round((windowSizeYUnconstrained - windowSizeY) * vsb.map(s -> s.value).orElse(0.0f));
            pg.image(contentBuffer.draw().get(0, yDiff, (int) windowSizeXForContents, (int) (windowSizeY - cell)), 0, 0);
        } else {
            pg.image(contentBuffer.draw(), 0, 0);
        }
        pg.popMatrix();
    }

    /**
     * Draw Child Nodes vertically
     *
     * @param pg Processing Graphics Context
     */
    protected void drawInlineFolderChildrenVerticalCols(PGraphics pg) {
        pg.pushMatrix();
        pg.translate(posX, posY);
        pg.translate(0, cell);
        if (vsb.isPresent() && vsb.get().visible) {
            int yDiff = round((windowSizeYUnconstrained - windowSizeY) * vsb.map(s -> s.value).orElse(0.0f));
            pg.image(contentBuffer.draw().get(0, yDiff, (int) windowSizeXForContents, (int) (windowSizeY - cell)), 0, 0);
        } else {
            pg.image(contentBuffer.draw(), 0, 0);
        }
        pg.popMatrix();
    }

    /**
     * Draw The Window background
     *
     * @param pg                 Processing Graphics Context
     * @param drawBackgroundOnly true if drawing the background, false if drawing the background's outline
     */
    protected void drawBackgroundWithWindowBorder(PGraphics pg, boolean drawBackgroundOnly) {
        pg.pushMatrix();
        pg.translate(posX, posY);
        pg.stroke(ThemeStore.getColor(WINDOW_BORDER));
        pg.strokeWeight(1);
        pg.fill(ThemeStore.getColor(NORMAL_BACKGROUND));
        if (drawBackgroundOnly) {
            pg.noStroke();
        } else {
            pg.noFill();
        }
        pg.rect(0, 0, windowSizeX, windowSizeY);
        pg.popMatrix();
    }

    /**
     * Draw The Content of The Window
     *
     * @param pg Processing Graphics Context
     */
    protected void drawContent(PGraphics pg) {
        if (!folder.children.isEmpty()) {
            switch (folder.getLayout()) {
                case HORIZONAL -> drawInlineFolderChildrenHorizontally(pg);
                case VERTICAL_X_COL -> drawInlineFolderChildrenVerticalCols(pg);
                default -> drawInlineFolderChildrenVertically(pg);
            }
        }
    }

    /**
     * Draw The Window
     *
     * @param pg Processing Graphics Context
     */
    void drawWindow(PGraphics pg) {
        pg.textFont(FontStore.getMainFont());
        setTitleHighlighted(isVisible && (isPointInsideTitleBar(GlobalReferences.app.mouseX, GlobalReferences.app.mouseY) && isBeingDragged) || folder.isMouseOverNode);
        setScrollbarHighlighted(isVisible && (isPointInsideScrollbar(GlobalReferences.app.mouseX, GlobalReferences.app.mouseY) && !isBeingDragged) || folder.isMouseOverNode);
        if (!isVisible || !folder.isVisibleParentAware()) {
            return;
        }
        constrainBounds(pg);
        pg.pushMatrix();
        drawBackgroundWithWindowBorder(pg, true);
        drawPathTooltipOnHighlight(pg);
        vsb.ifPresent(s ->
                s.draw(
                        pg,
                        posX,
                        posY + cell,
                        windowSizeXForContents,
                        windowSizeY - cell,
                        windowSizeYUnconstrained - cell
                )
        );
        drawContent(pg);
        drawBackgroundWithWindowBorder(pg, false);
        drawTitleBar(pg, folder.shouldDrawTitle());
        if (!isRoot()) {
            drawCloseButton(pg);
        }
        drawResizeIndicator(pg);
        pg.popMatrix();
    }


    public void drawContextLineFromTitleBarToInlineNode(PGraphics pg, float endRectSize, boolean pickShortestLine) {
        AbstractNode firstOpenParent = NodeTree.findFirstOpenParentNodeRecursively(folder);
        if (firstOpenParent == null || !firstOpenParent.isParentWindowVisible()) {
            return;
        }
        float xOffset = cell / 2f;
        float y0 = posY + cell / 2f;
        float y1 = firstOpenParent.pos.y + firstOpenParent.size.y / 2f;
        float x0a = posX - xOffset;
        float x0b = posX + windowSizeX + xOffset;
        float x1a = firstOpenParent.pos.x - xOffset;
        float x1b = firstOpenParent.pos.x + firstOpenParent.size.x + xOffset;
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

    public synchronized void resizeForContents(boolean shouldResize){
        isResizeWidth=shouldResize;
    }

    public void resizeForContents(){
        resizeForContents(true);
    }

    public void reinitialiseBuffer(){
        contentBuffer.resetBuffer();
    }

    public float contentWidth(){
        return (folder.getLayout()==LayoutType.HORIZONAL)?windowSizeX:windowSizeXForContents;
    }

    public float contentHeight(){
        return (folder.getLayout()==LayoutType.HORIZONAL)?windowSizeY:(windowSizeYUnconstrained - cell);
    }

    @Override
    public void keyPressed(GuiKeyEvent keyEvent) {
        float x = GlobalReferences.app.mouseX;
        float y = GlobalReferences.app.mouseY;
        if (isPointInsideTitleBar(x, y)) {
            folder.keyPressedOverNode(keyEvent, x, y);
            return;
        }
        AbstractNode nodeUnderMouse = tryFindChildNodeAt(x, y);
        if (nodeUnderMouse != null && nodeUnderMouse.isParentWindowVisible() && folder.isVisibleParentAware()) {
            if (isPointInsideContent(x, y)) {
                nodeUnderMouse.keyPressedOverNode(keyEvent, x, y);
            }
        }
    }

    @Override
    public void mousePressed(GuiMouseEvent e) {
        if (!isVisible() || !folder.isVisibleParentAware()) {
            return;
        }
        if (isPointInsideWindow(e.getX(), e.getY()) && e.isLeft()) {
            if (!isFocused()) {
                setFocusOnThis();
            }
            e.setConsumed(true);
        }
        if (isPointInsideTitleBar(e.getX(), e.getY()) && e.isLeft()) {
            isBeingDragged = true;
            e.setConsumed(true);
            setFocusOnThis();
            return;
        }
        if (isPointInsideScrollbar(e.getX(), e.getY()) && e.isLeft()) {
            vsb.ifPresent(s -> s.mousePressed(e));
            e.setConsumed(true);
            setFocusOnThis();
            return;
        }
        if (!isRoot() &&
                ((isPointInsideCloseButton(e.getX(), e.getY()) && e.isLeft()) ||
                        (isPointInsideWindow(e.getX(), e.getY()) && e.isRight()))) {
            isCloseInProgress = true;
            e.setConsumed(true);
            return;
        }
        if (isPointInsideResizeBorder(e.getX(), e.getY()) && LayoutStore.getWindowResizeEnabled()) {
            isBeingResized = true;
            e.setConsumed(true);
        } else if (isPointInsideContent(e.getX(), e.getY())) {
            AbstractNode node = tryFindChildNodeAt(e.getX(), e.getY());
            if (node != null && node.isParentWindowVisible()) {
                contentBuffer.invalidateBuffer();
                node.mousePressedEvent(e);
            }
        }
    }

    @Override
    public void mouseReleased(GuiMouseEvent e) {
        MouseHiding.tryRevealMouseAfterDragging();
        if (!isVisible() || !folder.isVisibleParentAware()) {
            return;
        }
        if (!isRoot() && isCloseInProgress &&
                ((isPointInsideCloseButton(e.getX(), e.getY()) && e.isLeft()) ||
                        (isPointInsideWindow(e.getX(), e.getY()) && e.isRight()))) {
            close();
            e.setConsumed(true);
        } else if (isBeingDragged) {
            trySnapToGrid();
            e.setConsumed(true);
        } else if (isBeingResized && SnapToGrid.snapToGridEnabled) {
            windowSizeX = SnapToGrid.trySnapToGrid(windowSizeX, 0).x;
            windowSizeXForContents = windowSizeX;
            contentBuffer.resetBuffer();
            e.setConsumed(true);
        }
        isCloseInProgress = false;
        isBeingDragged = false;
        isBeingResized = false;

        if (e.isConsumed()) {
            return;
        }
        for (AbstractNode node : folder.children) {
            node.mouseReleasedAnywhereEvent(e);
        }
        if (isPointInsideContent(e.getX(), e.getY())) {
            AbstractNode clickedNode = tryFindChildNodeAt(e.getX(), e.getY());
            if (clickedNode != null && clickedNode.isParentWindowVisible() && clickedNode.isVisible()) {
                contentBuffer.invalidateBuffer();
                clickedNode.mouseReleasedOverNodeEvent(e);
                e.setConsumed(true);
            }
        }
    }

    @Override
    public void mouseMoved(GuiMouseEvent e) {
        if (isMouseInsideTitlebar(e)) {
            if (folder.children.stream().anyMatch(c -> c.isMouseOverNode)){
                contentBuffer.invalidateBuffer();
            }
            e.setConsumed(true);
            folder.setIsMouseOverThisNodeOnly();
        } else if (isMouseInsideScrollbar(e)) {
            if (folder.children.stream().anyMatch(c -> c.isMouseOverNode)){
                contentBuffer.invalidateBuffer();
            }
            e.setConsumed(true);
            vsb.ifPresent(s -> s.mouseMoved(e));
            folder.setIsMouseOverThisNodeOnly();
        } else if (isMouseInsideContent(e)) {
            LOGGER.debug("Mouse Inside Content: X {} Y {} WinX {} WinY {} Width {} Height {}", e.getX(), e.getY(), posX, posY, windowSizeX, windowSizeY);
            float yDiff = windowSizeYUnconstrained - windowSizeY;
            AbstractNode node = tryFindChildNodeAt(e.getX(), e.getY() + yDiff * vsb.map(s -> s.value).orElse(0.0f));
            if (node != null && !node.isMouseOverNode) {
                LOGGER.debug("Inside {} [NX {} NY {} Width {} Height {}]", node.getName(), node.pos.x, node.pos.y, node.size.x, node.getHeight());
                contentBuffer.invalidateBuffer();
            }
            if (node != null && node.isParentWindowVisible()) {
                node.setIsMouseOverThisNodeOnly();
                e.setConsumed(true);
            }
        } else {
          if (folder.children.stream().anyMatch(c -> c.isMouseOverNode)){
              contentBuffer.invalidateBuffer();
          }
          NodeTree.setAllChildNodesMouseOverToFalse(this.folder);
        }
    }

    @Override
    public void mouseDragged(GuiMouseEvent e) {
        if (!isVisible()) {
            return;
        }
        if (isBeingDragged) {
            posX += e.getX() - e.getPrevX();
            posY += e.getY() - e.getPrevY();
            vsb.ifPresent(Scrollbar::invalidateBuffer);
            e.setConsumed(true);
        } else if (isBeingResized) {
            handleBeingResized(e);
        } else if (vsb.map(s -> s.dragging).orElse(false)) {
            vsb.ifPresent(s -> s.mouseDragged(e));
        }
        for (AbstractNode child : folder.children) {
            if (child.isInlineNodeDragged && child.isParentWindowVisible()) {
                child.mouseDragNodeContinueEvent(e);
                if (e.isConsumed() && child.isInlineNodeDraggable) {
                    MouseHiding.tryHideMouseForDragging();
                }
            }
        }
    }

    @Override
    public void mouseWheelMoved(GuiMouseEvent e) {
        if (isPointInsideWindow(e.getX(), e.getY())) {
            vsb.ifPresent(s -> s.mouseWheelMoved(e));
            e.setConsumed(true);
        }
    }
}
