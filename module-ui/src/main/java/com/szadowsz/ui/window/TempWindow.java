package com.szadowsz.ui.window;

import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.input.mouse.GuiMouseEvent;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.NodeTree;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.store.FontStore;
import com.szadowsz.ui.store.LayoutStore;
import processing.core.PApplet;
import processing.core.PGraphics;

import static com.szadowsz.ui.store.LayoutStore.cell;
import static com.szadowsz.ui.utils.Coordinates.isPointInRect;
import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.LEFT;

public class TempWindow extends Window {

    public TempWindow(FolderNode folder, float posX, float posY, Float nullableSizeX) {
        super(folder, posX, posY, nullableSizeX);
    }

    @Override
    protected boolean isPointInsideTitleBar(float x, float y) {
        return false;
    }

    @Override
    protected boolean isPointInsideCloseButton(float x, float y) {
        return false;
    }

    @Override
    protected void constrainHeight(PGraphics pg) {
        windowSizeY = heightSumOfChildNodes();
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
        }
    }

    @Override
    protected boolean isPointInsideResizeBorder(float x, float y) {
        return false;
    }

    public boolean isInParentWindow(float x, float y){
        return folder.parent != null && folder.parent.window.isPointInsideWindow(x,y);
    }

    public boolean isInChildWindow(float x, float y){
        return folder.children.stream().filter(n -> n instanceof FolderNode)
                .map(n -> (FolderNode) n)
                .anyMatch(n -> n.window != null && (n.window.isPointInsideWindow(x,y) ||
                        ((n.window instanceof TempWindow) && ((TempWindow) n.window).isInChildWindow(x, y))));
    }

    @Override
    public void mouseMoved(GuiMouseEvent e) {
        if (isVisible && folder.isInlineNodeVisibleParentAware() && isPointInsideTitleBar(e.getX(), e.getY())) {
            e.setConsumed(true);
            folder.setIsMouseOverThisNodeOnly();
        } else if (isPointInsideContent(e.getX(), e.getY()) && !isPointInsideResizeBorder(e.getX(), e.getY())) {
            AbstractNode node = tryFindChildNodeAt(e.getX(), e.getY());
            if (node != null && node.isParentWindowVisible()) {
                node.setIsMouseOverThisNodeOnly();
                e.setConsumed(true);
            }
        } else {
            if (!isPointInRect(e.getX(),e.getY(),posX-5,posY-5,windowSizeX+10,windowSizeY+10)) {
                NodeTree.setAllNodesMouseOverToFalse();
                if (!isInParentWindow(e.getX(), e.getY()) && !isInChildWindow(e.getX(), e.getY())) {
                    close();
                }
            }
        }
    }

    @Override
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

    @Override
    protected void drawInlineFolderChildrenVertically(PGraphics pg) {
        contentBuffer = GlobalReferences.app.createGraphics((int) windowSizeXForContents, (int) (windowSizeYUnconstrained), PApplet.JAVA2D);
        float y = 0;
        contentBuffer.beginDraw();
        contentBuffer.textFont(FontStore.getMainFont());
        contentBuffer.textAlign(LEFT, CENTER);
        for (int i = 0; i < folder.children.size(); i++) {
            AbstractNode node = folder.children.get(i);
            if (!node.isInlineNodeVisible()) {
                continue;
            }
            float nodeHeight = cell * node.masterInlineNodeHeightInCells;
            drawChildNodeVertically(node, y, nodeHeight);
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

        pg.pushMatrix();
        pg.translate(posX, posY);
        pg.image(contentBuffer, 0, 0);
        pg.popMatrix();
    }

    @Override
    void drawWindow(PGraphics pg) {
        pg.textFont(FontStore.getMainFont());
        if (!isVisible || !folder.isInlineNodeVisibleParentAware()) {
            return;
        }
        constrainPosition(pg);
        if (folder.getLayout() == LayoutType.VERTICAL) {
            constrainHeight(pg);
        } else if (folder.getLayout() == LayoutType.HORIZONAL) {
            constrainWidth(pg);
        }
        pg.pushMatrix();
        drawBackgroundWithWindowBorder(pg, true);
        drawContent(pg);
        drawBackgroundWithWindowBorder(pg, false);
        pg.popMatrix();
    }
}
