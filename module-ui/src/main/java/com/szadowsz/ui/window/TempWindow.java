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
    boolean isPointInsideResizeBorder(float x, float y) {
        return false;
    }

    public boolean isInParentWindow(float x, float y){
        return folder.parent != null && folder.parent.window.isPointInsideWindow(x,y);
    }

    public boolean isInChildWindow(float x, float y){
        return folder.children.stream().filter(n -> n instanceof FolderNode)
                .map(n -> (FolderNode) n)
                .anyMatch(n -> n.window.isPointInsideWindow(x,y) ||
                        ((n.window instanceof TempWindow) && ((TempWindow) n.window).isInChildWindow(x, y)));
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
