package com.old.gui.component.group;

import com.old.gui.RotomGui;
import com.old.gui.component.RComponent;
import com.old.gui.component.RComponentTree;
import com.old.gui.input.keys.RKeyEvent;
import com.old.gui.input.mouse.RMouseEvent;
import com.old.gui.input.mouse.RMouseHiding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RGroupDrawable extends RGroup {
    private static final Logger LOGGER = LoggerFactory.getLogger(RGroupDrawable.class);

    protected RGroupDrawable(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
    }

    @Override
    public boolean isDragged(){
        return children.stream().anyMatch(RComponent::isDragged);
    }

    @Override
    public boolean isMouseOver(){
        return children.stream().anyMatch(RComponent::isMouseOver);
    }

    @Override
    public void setMouseOver(boolean mouseOver) {
        if (isMouseOver() != mouseOver && !mouseOver) {
            children.forEach(child -> child.setMouseOver(false));
        }
    }

    @Override
    public void setIsMouseOverThisNodeOnly(RComponentTree t, RMouseEvent e) {
        RComponent node = findComponentAt(e.getX(), e.getY()); // TODO I predict scrollbar issues here
        if (node != null && node.isParentWindowVisible()) {
            if (!node.isMouseOver()) {
                LOGGER.debug("Inside {} [NX {} NY {} Width {} Height {}]", node.getName(), node.getPosX(), node.getPosY(), node.getWidth(), node.getHeight());
                node.setIsMouseOverThisNodeOnly(gui.getComponentTree(), e);
                this.getParentFolder().getWindow().redrawBuffer();
            }
            e.consume();
        }
    }

    @Override
    public void keyPressedOverComponent(RKeyEvent keyEvent, float x, float y) { // TODO LazyGui
        RComponent nodeUnderMouse = findComponentAt(x, y);
        if (nodeUnderMouse != null && nodeUnderMouse.isParentWindowVisible() && this.isVisibleParentAware()) {
            nodeUnderMouse.keyPressedOverComponent(keyEvent, x, y);
        }
    }

    @Override
    public void mousePressed(RMouseEvent e) {
        if (!isVisible() || !this.isVisibleParentAware()) {
            return;
        }
        RComponent node = findComponentAt(e.getX(), e.getY());
        if (node != null && node.isParentWindowVisible()) {
            LOGGER.debug("Mouse Pressed for node {} [{}, {}, {}, {}, {}, {}]", node.getName(),e.getX(),e.getY(),node.getPosX(),node.getPosY(),node.getWidth(),node.getHeight());
            this.getParentFolder().getWindow().redrawBuffer();
            node.mousePressed(e);
        }
    }

    @Override
    public void mouseReleasedOverComponent(RMouseEvent e) {
        if (!isVisible() || !this.isVisibleParentAware()) {
            return;
        }
        RComponent clickedNode = findComponentAt(e.getX(), e.getY());
        for (RComponent node : children) {
            if (!e.isConsumed() && node.equals(clickedNode) && clickedNode.isParentWindowVisible() && clickedNode.isVisible()) {
                this.getParentFolder().getWindow().redrawBuffer();
                clickedNode.mouseReleasedOverComponent(e);
                e.consume();
            } else {
                node.mouseReleasedAnywhere(e);
            }
        }
    }

    @Override
    public void mouseDragContinues(RMouseEvent e) {
        if (!isVisible()) {
            return;
        }
        for (RComponent child : children) {
            if (child.isDragged() && child.isParentWindowVisible()) {
                child.mouseDragContinues(e);
                if (e.isConsumed() && child.isDraggable()) {
                    RMouseHiding.tryHideMouseForDragging(gui.getSketch());
                }
            }
        }
    }

    @Override
    public void updateCoordinates(float bX, float bY, float rX, float rY, float w, float h) {
        LOGGER.debug("Update Coordinates for Drawable Group [{}, {}, {}, {}, {}, {}]", bX,bY,rX,rY,w,h);
        super.updateCoordinates(bX, bY, rX, rY, w, h);
        layout.setCompLayout(pos,size,children);
    }
}
