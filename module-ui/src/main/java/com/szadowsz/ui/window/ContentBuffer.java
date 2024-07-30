package com.szadowsz.ui.window;

import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.constants.theme.ThemeStore;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.store.FontStore;
import com.szadowsz.ui.store.LayoutStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.szadowsz.ui.constants.theme.ThemeColorType.WINDOW_BORDER;
import static com.szadowsz.ui.store.LayoutStore.cell;
import static processing.core.PConstants.*;

public class ContentBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentBuffer.class);

    private final Window win;

    private PGraphics buffer = null;

    private boolean isBufferInvalid = true;
    private boolean isReInitRequired = true;

    public ContentBuffer(Window win) {
        this.win = win;
    }

    private void createBuffer(float sizeX, float sizeY) {
        createBuffer((int) sizeX, (int) sizeY);
    }

    private void createBuffer(int sizeX, int sizeY) {
        buffer = GlobalReferences.app.createGraphics(sizeX, sizeY, PConstants.P2D);
    }

    private float calcColWidth(List<AbstractNode> children, int col) {
        float spaceForName = 0;
        float spaceForValue = 0;

        for (AbstractNode child : children) {
            if (col != child.getColumn())
                continue;

            float nameTextWidth = child.findNameTextWidthRoundedUpToWholeCells();
            float valueTextWidth = child.findValueTextWidthRoundedUpToWholeCells();

            spaceForName = Math.max(spaceForName, nameTextWidth);
            spaceForValue = Math.max(spaceForValue, valueTextWidth);
        }

        return spaceForName + spaceForValue;
    }

    /**
     * Draw Child Node
     *
     * @param node      child node
     * @param x         relative content x position
     * @param nodeWidth already processed width of the node
     */
    private void drawChildNodeHorizontally(AbstractNode node, float x, float nodeWidth) {
        float nodeHeight = node.getHeight();
        node.updateInlineNodeCoordinates(win.posX + x, win.posY, nodeWidth, nodeHeight);
        buffer.pushMatrix();
        buffer.pushStyle();
        node.updateDrawInlineNode(buffer);
        buffer.popStyle();
        buffer.popMatrix();
    }

    /**
     * Draw Child Node
     *
     * @param node       child node
     * @param y          relative content y position
     * @param nodeHeight already processed height of the node
     */
    private void drawChildNodeVertically(AbstractNode node, float x, float y, float nodeWidth, float nodeHeight) {
        node.updateInlineNodeCoordinates(win.posX + x, win.posY + y, nodeWidth, nodeHeight);
        buffer.pushMatrix();
        buffer.pushStyle();
        node.updateDrawInlineNode(buffer);
        buffer.popStyle();
        buffer.popMatrix();
    }

    /**
     * Draw A Horizontal separator between two nodes
     *
     * @param pg Processing Graphics Context
     */
    private void drawHorizontalSeparator(PGraphics pg) {
        drawHorizontalSeparator(pg, pg.width);
    }

    /**
     * Draw A Horizontal separator between two nodes
     *
     * @param pg    Processing Graphics Context
     * @param width separator width
     */
    private void drawHorizontalSeparator(PGraphics pg, int width) {
        boolean show = LayoutStore.isShowHorizontalSeparators();
        float weight = LayoutStore.getHorizontalSeparatorStrokeWeight();
        if (show) {
            pg.strokeCap(SQUARE);
            pg.strokeWeight(weight);
            pg.stroke(ThemeStore.getColor(WINDOW_BORDER));
            pg.line(0, 0, width, 0);
        }
    }

    /**
     * Draw A Horizontal separator between two nodes
     *
     * @param pg Processing Graphics Context
     */
    private void drawVerticalSeparator(PGraphics pg) {
        //boolean show = LayoutStore.isShowHorizontalSeparators();
        float weight = LayoutStore.getHorizontalSeparatorStrokeWeight();
        // if (show) {
        pg.strokeCap(SQUARE);
        pg.strokeWeight(weight);
        pg.stroke(ThemeStore.getColor(WINDOW_BORDER));
        pg.line(0, 0, 0, pg.height);
        // }
    }

    private void drawNodesVertically(List<AbstractNode> colChildren, float x, float width) {
        float y = cell; // Account for titlebar
        int index = 0;
        for (AbstractNode node : colChildren) {
            if (!node.isVisible()) {
                index++;
                continue;
            }
            float nodeHeight = node.getHeight();
            drawChildNodeVertically(node, x, y, width, nodeHeight);
            if (index > 0) {
                // separator
                buffer.pushStyle();
                drawHorizontalSeparator(buffer, (int) width);
                buffer.popStyle();
            }
            y += nodeHeight;
            buffer.translate(0, nodeHeight);
            index++;
        }
    }

    /**
     * Draw Child Nodes vertically
     *
     * @param folder Container of UI Nodes
     */
    void drawContentVertically(FolderNode folder) {
        buffer.beginDraw();
        buffer.clear();

        buffer.textFont(FontStore.getMainFont());
        buffer.textAlign(LEFT, CENTER);

        drawNodesVertically(folder.children, 0, buffer.width);

        buffer.endDraw();
    }

    /**
     * Draw Child Nodes vertically
     *
     * @param folder Container of UI Nodes
     */
    private void drawContentInVerticalCols(FolderNode folder) {

        int currentCol = 0;
        Map<Integer, List<AbstractNode>> childrenByCols = folder.getColChildren();
        List<AbstractNode> colChildren = childrenByCols.computeIfAbsent(currentCol, k -> new ArrayList<>());

        buffer.beginDraw();
        buffer.clear();

        buffer.textFont(FontStore.getMainFont());
        buffer.textAlign(LEFT, CENTER);

        float pos = 0.0f;

        while (!colChildren.isEmpty()) {
            float width = calcColWidth(colChildren, currentCol);

            buffer.pushMatrix();
            buffer.translate(pos, 0);

            drawNodesVertically(colChildren, pos, width);

            buffer.popMatrix();

            if (currentCol > 0) {
                buffer.pushMatrix();
                buffer.translate(pos, 0);
                drawVerticalSeparator(buffer);
                buffer.popMatrix();
            }
            colChildren = childrenByCols.computeIfAbsent(++currentCol, k -> new ArrayList<>());
            pos = pos + width;
        }
        buffer.endDraw();
    }


    /**
     * Draw Child Nodes horizontally
     *
     * @param folder Container of UI Nodes
     */
    private void drawContentHorizontally(FolderNode folder) {
        buffer.beginDraw();
        buffer.clear();

        buffer.textFont(FontStore.getMainFont());
        buffer.textAlign(LEFT, CENTER);

        float x = 0;
        for (AbstractNode node : folder.children) {
            if (!node.isVisible()) {
                continue;
            }
            float nodeWidth = node.getRequiredWidthForHorizontalLayout();
            drawChildNodeHorizontally(node, x, nodeWidth);
            // TODO consider Vertical Separator
            x += nodeWidth;
            buffer.translate(nodeWidth, 0);
        }

        buffer.endDraw();
    }

    /**
     * Draw The Content of The Window
     *
     * @param folder Container of UI Nodes
     */
    private void drawContent(FolderNode folder) {
        long time = System.currentTimeMillis();
        if (!folder.children.isEmpty()) {
            switch (folder.getLayout()) {
                case HORIZONAL -> drawContentHorizontally(folder);
                case VERTICAL_X_COL -> drawContentInVerticalCols(folder);
                default -> drawContentVertically(folder);
            }
        }
        LOGGER.info("Content Buffer Draw Duration {}", System.currentTimeMillis() - time);
    }

    private synchronized void redrawIfNecessary(){
        if (isReInitRequired){
            createBuffer(win.contentWidth(), win.contentHeight());
            isReInitRequired = false;
        }
        if (isBufferInvalid) {
            drawContent(win.folder);
            isBufferInvalid = false;
        }
    }

    public synchronized PGraphics draw(){
        redrawIfNecessary();
        return buffer;
    }

    public synchronized void invalidateBuffer() {
        isBufferInvalid = true;
    }

    public synchronized void resetBuffer() {
        isReInitRequired = true;
        isBufferInvalid = true;
    }
}
