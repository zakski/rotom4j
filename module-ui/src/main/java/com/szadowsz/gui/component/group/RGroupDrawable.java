package com.szadowsz.gui.component.group;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sub-Class for Groups that draw their children
 */
public abstract class RGroupDrawable extends RGroup {
    private static final Logger LOGGER = LoggerFactory.getLogger(RGroupDrawable.class);

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected RGroupDrawable(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
    }

    @Override
    public boolean isDragged(){
        return children.stream().anyMatch(RComponent::isDragged);
    }

    @Override
    public boolean isMouseOver(){
        return isChildMouseOver();
    }

    /**
     * TODO
     *
     * @return TODO
     */
    public boolean hasFocus() {
        return children.stream().anyMatch(RComponent::hasFocus);
    }

    @Override
    public void keyPressed(RKeyEvent keyEvent, float mouseX, float mouseY) {
        if (!isVisible()){
            return;
        }
        RComponent focused = findFocusedComponent();
        if (focused != null){
            switch (focused) {
                case null -> {
                }// NOOP
                case RGroup g -> g.keyPressed(keyEvent, mouseX, mouseY);
                case RComponent c -> keyPressedFocused(keyEvent);
            }
        } else {
            RComponent underMouse = findComponentAt(mouseX, mouseY);
            switch (underMouse) {
                case null -> {
                }// NOOP
                case RGroup g -> g.keyPressed(keyEvent, mouseX, mouseY);
                case RComponent c -> keyPressedOver(keyEvent, mouseX, mouseY);
            }
        }
    }
    @Override
    public void mouseOver(RMouseEvent mouseEvent, float adjustedMouseY){
        RComponent underMouse = findComponentAt(mouseEvent.getX(), adjustedMouseY);
        if (underMouse != null){
            if(!underMouse.isMouseOver()) {
                LOGGER.debug("Inside Component {} [NX {} NY {} Width {} Height {}]", underMouse.getName(), underMouse.getPosX(), underMouse.getPosY(), underMouse.getWidth(), underMouse.getHeight());
            }
            underMouse.mouseOver(mouseEvent,adjustedMouseY);
        }
        mouseEvent.consume();
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float adjustedMouseY){
        if (!isVisible() || !this.isVisibleParentAware()) {
            return;
        }
        RComponent node = findComponentAt(mouseEvent.getX(), adjustedMouseY);
        if (node != null) {
            LOGGER.debug("Mouse Pressed for node {} [{}, {}, {}, {}, {}, {}]", node.getName(),mouseEvent.getX(),adjustedMouseY,node.getPosX(),node.getPosY(),node.getWidth(),node.getHeight());
            this.getParentFolder().getWindow().redrawBuffer();
            node.mousePressed(mouseEvent,adjustedMouseY);
        }
    }
    @Override
    public void mouseDragged(RMouseEvent mouseEvent) {
        if (!isVisible()) {
            return;
        }
        for (RComponent child : children) {
            LOGGER.debug("Mouse Drag Check for Content {}", child.getName());
            if (child.isDragged()) {
                LOGGER.debug("Mouse Dragged for Content {}", child.getName());
                child.mouseDragged(mouseEvent);
                if (mouseEvent.isConsumed()) {
                    break;
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
