package com.szadowsz.ui.window;

import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.input.InputWatcherBackend;
import com.szadowsz.ui.input.MouseHiding;
import com.szadowsz.ui.input.UserInputSubscriber;
import com.szadowsz.ui.input.keys.GuiKeyEvent;
import com.szadowsz.ui.input.mouse.GuiMouseEvent;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.NodePaths;
import com.szadowsz.ui.node.NodeTree;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.constants.theme.ThemeStore;
import com.szadowsz.ui.store.FontStore;
import com.szadowsz.ui.store.LayoutStore;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.*;

import static com.szadowsz.ui.constants.theme.ThemeColorType.*;
import static com.szadowsz.ui.store.LayoutStore.cell;
import static com.szadowsz.ui.utils.Coordinates.isPointInRect;
import static processing.core.PApplet.*;

/**
 * Gui Window Node Organisation and Drawing
 */
public class Window implements UserInputSubscriber {

    // Companion Folder Node
    final FolderNode folder;

    // Vertical Scrollbar
    private Optional<Scrollbar> vsb = Optional.empty();

    // Window Content Buffer
    protected boolean isBufferInvalid = true;
    protected PGraphics contentBuffer = null;

    // Window Position Info
    public float posX;
    public float posY;

    // Window Width Info
    public float windowSizeXForContents; // width of the content nodes
    public float windowSizeX; // total window width including scrollbar

    // Window Height Info
    public float windowSizeYUnconstrained; // total window height, based on individual content node heights
    public float windowSizeY; // actual window height, constrained by app window size

    // Window Status Info
    protected boolean isTitleHighlighted; // if the window title is visible, should it be highlighted
    protected boolean isScrollbarHighlighted;  // if the vertical scrollbar is visible, should it be highlighted
    protected boolean isBeingResized; // if the window shape is changing
    protected boolean isBeingDragged; // if the window is being moved
    protected boolean isVisible = true; // if the window is visible

    // Window Transition Status Info
    private boolean isCloseInProgress; // if close button press in progress


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

        if (LayoutType.isVertical(folder.getLayout())) {
            vsb = Optional.of(new Scrollbar(posX + windowSizeXForContents, posY, cell, windowSizeY, windowSizeYUnconstrained, 16));
            vsb.ifPresent(s -> s.setVisible(false));
        }
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
     * Constrain the Windows width/height when the layout is horizontal
     *
     * @param pg Processing Graphics Context
     */
    protected void constrainWidth(PGraphics pg) {
        windowSizeY = cell;
    }

    /**
     * Constrain the Windows width/height when the layout is vertical
     *
     * @param pg Processing Graphics Context
     */
    protected void constrainHeight(PGraphics pg) {
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
                if (!child.isInlineNodeVisible()) {
                    continue;
                }
                if (posY + sum + child.masterInlineNodeHeightInCells * cell > pg.height * 0.9) {
                    index -= 1;
                    break;
                }
                sum += child.masterInlineNodeHeightInCells * cell;
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

    protected void constrainColumnedHeight(PGraphics pg) {

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
                if (!child.isInlineNodeVisible()) {
                    continue;
                }
                if (posY + sum + child.masterInlineNodeHeightInCells * cell > pg.height * 0.9) {
                    index -= 1;
                    break;
                }
                sum += child.masterInlineNodeHeightInCells * cell;
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
     * Calculate the height of all visible Child Nodes
     *
     * @return total height of Child Nodes
     */
    protected float heightSumOfChildNodes() {
        float sum = 0;
        for (AbstractNode child : folder.children) {
            if (!child.isInlineNodeVisible()) {
                continue;
            }
            sum += child.masterInlineNodeHeightInCells * cell;
        }
        return sum;
    }

    /**
     * Calculate the height of all visible Child Nodes
     *
     * @return total height of Child Nodes
     */
    protected float heightOfColumns() {
        Map<Integer, Float> columnHeights = new HashMap<>();
        for (AbstractNode child : folder.children) {
            if (!child.isInlineNodeVisible()) {
                continue;
            }
            float height = columnHeights.getOrDefault(child.getColumn(), 0.0f);
            columnHeights.put(child.getColumn(), height + child.masterInlineNodeHeightInCells * cell);
        }
        return columnHeights.values().stream().max(Float::compareTo).orElse(0.0f);
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
            if (!node.isInlineNodeVisible()) {
                continue;
            }
            if (isPointInRect(x, y, node.pos.x, node.pos.y, node.size.x, node.size.y)) {
                return node;
            }
        }
        return null;
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
     * Check if the point is inside the Window
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the point is inside the window, false otherwise
     */
    protected boolean isPointInsideWindow(float x, float y) {
        return isPointInRect(x, y, posX, posY, windowSizeX, windowSizeY);
    }

    /**
     * Check if this window is the root window
     *
     * @return true if root, false otherwise
     */
    boolean isRoot() {
        return folder.equals(NodeTree.getRoot());
    }

    /**
     * Check if the title bar is highlighted
     *
     * @return true if the title is highlighted, false otherwise
     */
    public synchronized boolean isTitleHighlighted() {
        return isTitleHighlighted;
    }

    public synchronized boolean isScrollbarHighlighted() {
        return isScrollbarHighlighted;
    }

    /**
     * Check if the window is visible
     *
     * @return true if the window is visible, false otherwise
     */
    public boolean isVisible() {
        return isVisible || LayoutStore.isGuiHidden();
    }

    /**
     * Check if the window is focused upon
     *
     * @return true if the window is the focus, false otherwise
     */
    public boolean isFocused() {
        return WindowManager.isFocused(this);
    }


    public boolean isBeingDragged() {
        return isBeingDragged;
    }

    protected synchronized void setTitleHighlighted(boolean v) {
        isTitleHighlighted = v;
    }

    protected synchronized void setScrollbarHighlighted(boolean v) {
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
     * Draw Child Node
     *
     * @param node      child node
     * @param x         relative content x position
     * @param nodeWidth already processed width of the node
     */
    private void drawChildNodeHorizontally(AbstractNode node, float x, float nodeWidth) {
        float nodeHeight = cell * node.masterInlineNodeHeightInCells;
        node.updateInlineNodeCoordinates(posX + x, posY, nodeWidth, nodeHeight);
        contentBuffer.pushMatrix();
        contentBuffer.pushStyle();
        node.updateDrawInlineNode(contentBuffer);
        contentBuffer.popStyle();
        contentBuffer.popMatrix();
    }

    /**
     * Draw Child Node
     *
     * @param node       child node
     * @param y          relative content y position
     * @param nodeHeight already processed height of the node
     */
    protected void drawChildNodeVertically(AbstractNode node, float x, float y, float nodeWidth, float nodeHeight) {
        node.updateInlineNodeCoordinates(posX + x, posY + y, nodeWidth, nodeHeight);
        contentBuffer.pushMatrix();
        contentBuffer.pushStyle();
        node.updateDrawInlineNode(contentBuffer);
        contentBuffer.popStyle();
        contentBuffer.popMatrix();
    }

    /**
     * Draw Child Nodes horizontally
     *
     * @param pg Processing Graphics Context
     */
    private void drawInlineFolderChildrenHorizontally(PGraphics pg) {
        if (isBufferInvalid) {
            contentBuffer = GlobalReferences.app.createGraphics((int) windowSizeX, (int) windowSizeY, PConstants.P2D);
            contentBuffer.beginDraw();
            contentBuffer.textFont(FontStore.getMainFont());
            contentBuffer.textAlign(LEFT, CENTER);
            float x = 0;
            for (int i = 0; i < folder.children.size(); i++) {
                AbstractNode node = folder.children.get(i);
                if (!node.isInlineNodeVisible()) {
                    continue;
                }
                float nodeWidth = node.getRequiredWidthForHorizontalLayout();
                drawChildNodeHorizontally(node, x, nodeWidth);
                // TODO consider Vertical Separator
                x += nodeWidth;
                contentBuffer.translate(nodeWidth, 0);
            }
            contentBuffer.endDraw();
            isBufferInvalid = false;
        }
        pg.pushMatrix();
        pg.translate(posX, posY);
        pg.image(contentBuffer, 0, 0);
        pg.popMatrix();
    }

    /**
     * Draw A Horizontal separator between two nodes
     *
     * @param pg Processing Graphics Context
     */
    protected void drawHorizontalSeparator(PGraphics pg) {
        boolean show = LayoutStore.isShowHorizontalSeparators();
        float weight = LayoutStore.getHorizontalSeparatorStrokeWeight();
        if (show) {
            pg.strokeCap(SQUARE);
            pg.strokeWeight(weight);
            pg.stroke(ThemeStore.getColor(WINDOW_BORDER));
            pg.line(0, 0, windowSizeX, 0);
        }
    }

    /**
     * Draw A Horizontal separator between two nodes
     *
     * @param pg Processing Graphics Context
     */
    protected void drawVerticalSeparator(PGraphics pg) {
        //boolean show = LayoutStore.isShowHorizontalSeparators();
        float weight = LayoutStore.getHorizontalSeparatorStrokeWeight();
        // if (show) {
        pg.strokeCap(SQUARE);
        pg.strokeWeight(weight);
        pg.stroke(ThemeStore.getColor(WINDOW_BORDER));
        pg.line(0, 0, 0, windowSizeY);
        // }
    }

    /**
     * Draw Child Nodes vertically
     *
     * @param pg Processing Graphics Context
     */
    protected void drawInlineFolderChildrenVertically(PGraphics pg) {
        if (isBufferInvalid) {
            long time = 0;
            if (folder.children.size()>400){
                time = System.currentTimeMillis();
            }
            contentBuffer = GlobalReferences.app.createGraphics((int) windowSizeXForContents, (int) (windowSizeYUnconstrained - cell), PConstants.P2D);
            float y = cell;
            contentBuffer.beginDraw();
            contentBuffer.textFont(FontStore.getMainFont());
            contentBuffer.textAlign(LEFT, CENTER);
             for (int i = 0; i < folder.children.size(); i++) {
                AbstractNode node = folder.children.get(i);
                if (!node.isInlineNodeVisible()) {
                    continue;
                }
                float nodeHeight = cell * node.masterInlineNodeHeightInCells;
                drawChildNodeVertically(node, 0, y, windowSizeXForContents, nodeHeight);
                if (i > 0) {
                    // separator
                    contentBuffer.pushStyle();
                    drawHorizontalSeparator(contentBuffer);
                    contentBuffer.popStyle();
                }
                y += nodeHeight;
                contentBuffer.translate(0, nodeHeight);
            }
            contentBuffer.endDraw();
            isBufferInvalid = false;
            if (folder.children.size()>400){
                System.out.println("Duration " + (System.currentTimeMillis()-time));
            }
        }
        pg.pushMatrix();
        pg.translate(posX, posY);
        pg.translate(0, cell);
        if (vsb.isPresent() && vsb.get().visible) {
            int yDiff = round((windowSizeYUnconstrained - windowSizeY) * vsb.map(s -> s.value).orElse(0.0f));
            pg.image(contentBuffer.get(0, yDiff, (int) windowSizeXForContents, (int) (windowSizeY - cell)), 0, 0);
        } else {
            pg.image(contentBuffer, 0, 0);
        }
        pg.popMatrix();
    }

    /**
     * Draw Child Nodes vertically
     *
     * @param pg Processing Graphics Context
     */
    protected void drawInlineFolderChildrenVerticalCols(PGraphics pg) {
        if (isBufferInvalid) {
            contentBuffer = GlobalReferences.app.createGraphics((int) windowSizeXForContents, (int) (windowSizeYUnconstrained - cell), PConstants.P2D);

            float y = cell;

            int currentCol = 0;
            List<AbstractNode> colChildren = folder.getColChildren(currentCol);

            contentBuffer.beginDraw();
            contentBuffer.textFont(FontStore.getMainFont());
            contentBuffer.textAlign(LEFT, CENTER);
            float pos = 0.0f;
            while (!colChildren.isEmpty()) {
                float width = folder.getColWidth(currentCol);
                contentBuffer.pushMatrix();
                contentBuffer.translate(pos, 0);

                for (int i = 0; i < colChildren.size(); i++) {
                    AbstractNode node = colChildren.get(i);
                    if (!node.isInlineNodeVisible()) {
                        continue;
                    }
                    float nodeHeight = cell * node.masterInlineNodeHeightInCells;
                    drawChildNodeVertically(node, pos, y, width, nodeHeight);
                    if (i > 0) {
                        // separator
                        contentBuffer.pushStyle();
                        drawHorizontalSeparator(contentBuffer);
                        contentBuffer.popStyle();
                    }
                    y += nodeHeight;
                    contentBuffer.translate(0, nodeHeight);
                }
                contentBuffer.popMatrix();
                if (currentCol > 0) {
                    contentBuffer.pushMatrix();
                    contentBuffer.translate(pos, 0);
                    drawVerticalSeparator(contentBuffer);
                    contentBuffer.popMatrix();
                }
                colChildren = folder.getColChildren(++currentCol);
                y = cell;
                pos = pos + width;
            }
            contentBuffer.endDraw();
            isBufferInvalid = false;
        }
        pg.pushMatrix();
        pg.translate(posX, posY);
        pg.translate(0, cell);
        if (vsb.isPresent() && vsb.get().visible) {
            int yDiff = round((windowSizeYUnconstrained - windowSizeY) * vsb.map(s -> s.value).orElse(0.0f));
            pg.image(contentBuffer.get(0, yDiff, (int) windowSizeXForContents, (int) (windowSizeY - cell)), 0, 0);
        } else {
            pg.image(contentBuffer, 0, 0);
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
        if (!isVisible || !folder.isInlineNodeVisibleParentAware()) {
            return;
        }
        constrainPosition(pg);
        switch (folder.getLayout()) {
            case HORIZONAL -> constrainWidth(pg);
            case VERTICAL_X_COL -> constrainColumnedHeight(pg);
            default -> constrainHeight(pg);
        }
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

    static String[] splitFullPathWithoutEndAndRoot(String fullPath) {
        String[] pathWithEnd = NodePaths.splitByUnescapedSlashes(fullPath);
        return Arrays.copyOf(pathWithEnd, pathWithEnd.length - 1);
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
        if (nodeUnderMouse != null && nodeUnderMouse.isParentWindowVisible() && folder.isInlineNodeVisibleParentAware()) {
            if (isPointInsideContent(x, y)) {
                nodeUnderMouse.keyPressedOverNode(keyEvent, x, y);
            }
        }
    }

    @Override
    public void mousePressed(GuiMouseEvent e) {
        if (!isVisible() || !folder.isInlineNodeVisibleParentAware()) {
            return;
        }
        if (isPointInsideWindow(e.getX(), e.getY()) && e.getButton() == PConstants.LEFT) {
            if (!isFocused()) {
                setFocusOnThis();
            }
            e.setConsumed(true);
        }
        if (isPointInsideTitleBar(e.getX(), e.getY()) && e.getButton() == PConstants.LEFT) {
            isBeingDragged = true;
            e.setConsumed(true);
            setFocusOnThis();
            return;
        }
        if (isPointInsideScrollbar(e.getX(), e.getY()) && e.getButton() == PConstants.LEFT) {
            vsb.ifPresent(s -> s.mousePressed(e));
            e.setConsumed(true);
            setFocusOnThis();
            return;
        }
        if (!isRoot() &&
                ((isPointInsideCloseButton(e.getX(), e.getY()) && e.getButton() == PConstants.LEFT) ||
                        (isPointInsideWindow(e.getX(), e.getY()) && e.getButton() == PConstants.RIGHT))) {
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
                node.mousePressedEvent(e);
            }
        }
    }

    @Override
    public void mouseReleased(GuiMouseEvent e) {
        MouseHiding.tryRevealMouseAfterDragging();
        if (!isVisible() || !folder.isInlineNodeVisibleParentAware()) {
            return;
        }
        if (!isRoot() && isCloseInProgress &&
                ((isPointInsideCloseButton(e.getX(), e.getY()) && e.getButton() == PConstants.LEFT) ||
                        (isPointInsideWindow(e.getX(), e.getY()) && e.getButton() == PConstants.RIGHT))) {
            close();
            e.setConsumed(true);
        } else if (isBeingDragged) {
            trySnapToGrid();
            e.setConsumed(true);
        } else if (isBeingResized && SnapToGrid.snapToGridEnabled) {
            windowSizeX = SnapToGrid.trySnapToGrid(windowSizeX, 0).x;
            windowSizeXForContents = windowSizeX;
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
            if (clickedNode != null && clickedNode.isParentWindowVisible() && clickedNode.isInlineNodeVisible()) {
                clickedNode.mouseReleasedOverNodeEvent(e);
                e.setConsumed(true);
            }
        }
    }

    @Override
    public void mouseMoved(GuiMouseEvent e) {
        if (isVisible && folder.isInlineNodeVisibleParentAware() && isPointInsideTitleBar(e.getX(), e.getY())) {
            if (!isTitleHighlighted()) {
                isBufferInvalid = true;
            }
            e.setConsumed(true);
            folder.setIsMouseOverThisNodeOnly();
        } else if (isVisible && folder.isInlineNodeVisibleParentAware() && isPointInsideScrollbar(e.getX(), e.getY())) {
            if (!isScrollbarHighlighted()) {
                isBufferInvalid = true;
            }
            e.setConsumed(true);
            vsb.ifPresent(s -> s.mouseMoved(e));
            folder.setIsMouseOverThisNodeOnly();
        } else if (isPointInsideContent(e.getX(), e.getY()) && !isPointInsideResizeBorder(e.getX(), e.getY())) {
            float yDiff = windowSizeYUnconstrained - windowSizeY;
            AbstractNode node = tryFindChildNodeAt(e.getX(), e.getY() + yDiff * vsb.map(s -> s.value).orElse(0.0f));
            if (node != null && !node.isMouseOverNode) {
                isBufferInvalid = true;
            }
            if (node != null && node.isParentWindowVisible()) {
                node.setIsMouseOverThisNodeOnly();
                e.setConsumed(true);
            }
        } else {
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
            System.out.println("oldX: " + e.getPrevX() + ", new:X " + e.getX());
            System.out.println("old: " + oldWindowSizeX + ", new: " + windowSizeX);
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
            e.setConsumed(true);
            vsb.stream().forEach(s -> s.mouseWheelMoved(e));
        }
    }

}
