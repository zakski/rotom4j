package com.old.ui.window;

import com.old.ui.input.mouse.GuiMouseEvent;
import com.old.ui.node.AbstractNode;
import com.old.ui.node.LayoutType;
import com.old.ui.node.NodeTree;
import com.old.ui.node.impl.FolderNode;
import com.old.ui.store.FontStore;
import com.old.ui.store.LayoutStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

import static com.old.ui.store.LayoutStore.cell;
import static com.old.ui.utils.Coordinates.isPointInRect;

/**
 * Gui Temporary Window Node Organisation and Drawing
 */
public class TempWindow extends Window {
    private static final Logger LOGGER = LoggerFactory.getLogger(TempWindow.class);

    public TempWindow(FolderNode folder, float posX, float posY, Float nullableSizeX) {
        super(folder, posX, posY, nullableSizeX);
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
        }
    }

    @Override
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
     * Check if the mouse is inside the content of the Window
     *
     * @param e mouse event
     * @return true if the mouse is inside the content, false otherwise
     */
    @Override
    protected boolean isMouseInsideContent(GuiMouseEvent e) {
        return isPointInsideWindow(e.getX(), e.getY()) && !isPointInsideResizeBorder(e.getX(), e.getY());
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
    protected boolean isPointInsideResizeBorder(float x, float y) {
        return false;
    }

    /**
     * Method to Check if the mouse is inside the parent window
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true if it is inside the parent window, false otherwise
     */
    public boolean isInParentWindow(float x, float y){
        return folder.parent != null && folder.parent.window.isPointInsideWindow(x,y);
    }

    /**
     * Method to Check if the mouse is inside a child window
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true if it is inside a child window, false otherwise
     */
    public boolean isInChildWindow(float x, float y){
        return folder.children.stream().filter(n -> n instanceof FolderNode)
                .map(n -> (FolderNode) n)
                .anyMatch(n -> n.window != null && (n.window.isPointInsideWindow(x,y) ||
                        ((n.window instanceof TempWindow) && ((TempWindow) n.window).isInChildWindow(x, y))));
    }

    @Override
    protected void drawInlineFolderChildrenVertically(PGraphics pg) {
        pg.pushMatrix();
        pg.translate(posX, posY);
        pg.image(contentBuffer.draw(), 0, 0);
        pg.popMatrix();
    }

    @Override
    void drawWindow(PGraphics pg) {
        pg.textFont(FontStore.getMainFont());
        if (!isVisible || !folder.isVisibleParentAware()) {
            return;
        }
        constrainBounds(pg);
        pg.pushMatrix();
        drawBackgroundWithWindowBorder(pg, true);
        drawContent(pg);
        drawBackgroundWithWindowBorder(pg, false);
        pg.popMatrix();
    }


    @Override
    public void mouseMoved(GuiMouseEvent e) {
        if (isMouseInsideContent(e)) {
            LOGGER.debug("Mouse Inside Content: X {} Y {} WinX {} WinY {} Width {} Height {}", e.getX(), e.getY(), posX, posY, windowSizeX, windowSizeY);
            AbstractNode node = tryFindChildNodeAt(e.getX(), e.getY());
            if (node != null && !node.isMouseOverNode) {
                LOGGER.debug("Inside {} [NX {} NY {} Width {} Height {}]", node.getName(), node.pos.x, node.pos.y, node.size.x, node.getHeight());
                contentBuffer.invalidateBuffer();
            }
            if (node != null && node.isParentWindowVisible()) {
                node.setIsMouseOverThisNodeOnly();
                e.setConsumed(true);
            }
        } else {
            if (!isPointInRect(e.getX(),e.getY(),posX-5,posY-5,windowSizeX+10,windowSizeY+10)) {
                NodeTree.setAllChildNodesMouseOverToFalse(this.folder);
                if (!isInParentWindow(e.getX(), e.getY()) && !isInChildWindow(e.getX(), e.getY())) {
                    close();
                }
            }
        }
    }

    @Override
    public void mouseReleased(GuiMouseEvent e) {
        super.mouseReleased(e);
    }

    @Override
    public float contentHeight(){
        return (folder.getLayout()==LayoutType.HORIZONAL)?windowSizeY:(windowSizeYUnconstrained);
    }
}
