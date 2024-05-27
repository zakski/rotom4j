package com.szadowsz.ui.window;

import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.input.mouse.GuiMouseEvent;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.NodeTree;
import com.szadowsz.ui.node.impl.DropdownMenuNode;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.store.FontStore;
import com.szadowsz.ui.store.LayoutStore;
import processing.core.PGraphics;

public class TempWindow extends Window {

    public TempWindow(FolderNode folder, float posX, float posY, Float nullableSizeX) {
        super(folder, posX, posY, nullableSizeX);
    }

    @Override
    boolean isPointInsideTitleBar(float x, float y) {
        return false;
    }

    @Override
    protected boolean isPointInsideCloseButton(float x, float y) {
        return false;
    }

    @Override
    protected void constrainHeight(PGraphics pg) {
        windowSizeY = heightSumOfChildNodes();
        if (!LayoutStore.getShouldKeepWindowsInBounds()) {
            return;
        }
        if (posY + windowSizeY > pg.height) {
            float sum = 0;
            int index = Math.max(startIndex, 0);
            while (index < folder.children.size()) {
                AbstractNode child = folder.children.get(index);
                if (!child.isInlineNodeVisible()) {
                    continue;
                }
                if (sum + child.masterInlineNodeHeightInCells * LayoutStore.cell > pg.height - posY) {
                    index -= 1;
                    break;
                }
                sum += child.masterInlineNodeHeightInCells * LayoutStore.cell;
                index++;
            }
            if (startIndex < 0) {
                startIndex = 0;
            }
            windowSizeY = sum;
            endIndex = index;
        }
    }

    @Override
    boolean isPointInsideResizeBorder(float x, float y) {
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
        if (!closed && folder.isInlineNodeVisibleParentAware() && isPointInsideTitleBar(e.getX(), e.getY())) {
            e.setConsumed(true);
            folder.setIsMouseOverThisNodeOnly();
        } else if (isPointInsideContent(e.getX(), e.getY()) && !isPointInsideResizeBorder(e.getX(), e.getY())) {
            AbstractNode node = tryFindChildNodeAt(e.getX(), e.getY());
            if (node != null && node.isParentWindowVisible()) {
                node.setIsMouseOverThisNodeOnly();
                e.setConsumed(true);
            }
        } else {
            NodeTree.setAllNodesMouseOverToFalse();
            if (!isInParentWindow(e.getX(),e.getY()) && !isInChildWindow(e.getX(),e.getY())) {
                close();
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
                System.out.println(node.name + " X: " + x + ", Y: " + y + ", nodeX: " + node.pos.x + ", nodeY: " + node.pos.y + ", sizeX: " + node.size.x + ", sizeY: " + node.size.y);
                return node;
            }
        }
        return null;
    }

    protected void drawInlineFolderChildrenVertically(PGraphics pg) {
        pg.pushMatrix();
        pg.translate(posX, posY);
        float y = 0;
        for (int i = Math.max(startIndex, 0); (endIndex < 0 || i < endIndex) && i < folder.children.size(); i++) {
            AbstractNode node = folder.children.get(i);
            if (!node.isInlineNodeVisible()) {
                continue;
            }
            float nodeHeight = LayoutStore.cell * node.masterInlineNodeHeightInCells;
            node.updateInlineNodeCoordinates(posX, posY + y, windowSizeX, nodeHeight);
            pg.pushMatrix();
            pg.pushStyle();
            node.updateDrawInlineNode(pg);
            pg.popStyle();
            pg.popMatrix();

            if (i > 0) {
                // separator
                pg.pushStyle();
                drawHorizontalSeparator(pg);
                pg.popStyle();
            }

            y += nodeHeight;
            pg.translate(0, nodeHeight);
        }
        pg.popMatrix();
    }

    @Override
    void drawWindow(PGraphics pg) {
        pg.textFont(FontStore.getMainFont());
        if (closed || !folder.isInlineNodeVisibleParentAware()) {
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
