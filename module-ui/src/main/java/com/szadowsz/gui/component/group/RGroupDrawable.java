package com.szadowsz.gui.component.group;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLinearLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

import static com.szadowsz.gui.config.theme.RColorType.WINDOW_BORDER;
import static processing.core.PConstants.*;

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
        buffer = new RGroupBuffer(this);
    }

    /**
     * Sets the background fill color of the component, as part of the draw method
     *
     * @param pg graphics reference to use
     */
    @Override
    protected void fillBackground(PGraphics pg) {
        pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND));
    }

    /**
     * Draw Child Component
     *
     * @param child
     */
    protected void drawChildComponent(PGraphics pg, RComponent child) {
        pg.pushMatrix();
        pg.pushStyle();
        child.draw(pg);
        pg.popStyle();
        pg.popMatrix();
    }

    /**
     * Draw A Horizontal separator between two nodes
     *
     * @param pg Processing Graphics Context
     */
    protected void drawHorizontalSeparator(PGraphics pg) {
        boolean show = RLayoutStore.isShowHorizontalSeparators();
        float weight = RLayoutStore.getHorizontalSeparatorStrokeWeight();
        if (show) {
            pg.strokeCap(SQUARE);
            pg.strokeWeight(weight);
            pg.stroke(RThemeStore.getRGBA(WINDOW_BORDER));
            pg.line(0, 0, getWidth(), 0);
        }
    }

    /**
     * Draw A Horizontal separator between two nodes
     *
     * @param pg Processing Graphics Context
     */
    protected void drawVerticalSeparator(PGraphics pg) {
        //boolean show = LayoutStore.isShowHorizontalSeparators();
        float weight = RLayoutStore.getHorizontalSeparatorStrokeWeight();
        // if (show) {
        pg.strokeCap(SQUARE);
        pg.strokeWeight(weight);
        pg.stroke(RThemeStore.getRGBA(WINDOW_BORDER));
        pg.line(0, 0, 0, getHeight());
        // }
    }

    protected void drawChildren(PGraphics pg) {

        int index = 0;

        for (RComponent component : children) {
            if (!component.isVisible()) {
                index++;
                continue;
            }
            pg.pushMatrix();
            pg.translate(component.getRelPosX(), component.getRelPosY());
            drawChildComponent(pg, component);
            if (index > 0) { // TODO if as to kind of separator to draw
                // separator
                if (layout instanceof RLinearLayout linear) {
                    pg.pushStyle();
                    if (linear.getDirection() == RDirection.VERTICAL) {
                        drawHorizontalSeparator(pg);
                    } else {
                        drawVerticalSeparator(pg);
                    }
                    pg.popStyle();
                }
            }
            index++;
            pg.popMatrix();
        }
    }

    /**
     * Draw The Content of The Window
     */
    @Override
    protected void drawForeground(PGraphics pg, String name) {
        long time = System.currentTimeMillis();
        if (!children.isEmpty()) {
            pg.textFont(RFontStore.getMainFont());
            pg.textAlign(LEFT, CENTER);
            LOGGER.debug("{} Layout [{},{}]", getName(), getWidth(), getHeight());
            drawChildren(pg);
        }
        LOGGER.debug("{} Group [{},{}] Draw Duration {}", getName(), pg.width, pg.height, System.currentTimeMillis() - time);
    }

    @Override
    protected void drawContent(PGraphics pg) {
        super.drawContent(pg);
    }

    @Override
    public void drawToBuffer() {
        children.forEach(RComponent::drawToBuffer);
        buffer.redraw();
    }

    /**
     * Get the preferred size characteristics
     *
     * @return width and height in a PVector
     */
    public PVector getPreferredSize() {
        return layout.calcPreferredSize(getParentFolder().getName(), children);
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
    public boolean isDragged() {
        return children.stream().anyMatch(RComponent::isDragged);
    }

    @Override
    public boolean isMouseOver() {
        return isChildMouseOver();
    }

    @Override
    public void keyPressed(RKeyEvent keyEvent, float mouseX, float mouseY) {
        if (!this.isVisibleParentAware(null)) {
            return;
        }
        RComponent focused = findFocusedComponent();
        if (focused != null) {
            switch (focused) {
                case null -> {
                }// NOOP
                case RGroup g -> g.keyPressed(keyEvent, mouseX, mouseY);
                case RComponent c -> keyPressedFocused(keyEvent);
            }
        } else {
            RComponent underMouse = findVisibleComponentAt(mouseX, mouseY);
            switch (underMouse) {
                case null -> {
                }// NOOP
                case RGroup g -> g.keyPressed(keyEvent, mouseX, mouseY);
                case RComponent c -> keyPressedOver(keyEvent, mouseX, mouseY);
            }
        }
    }

    @Override
    public void mouseOver(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (!this.isVisibleParentAware(null)) {
            return;
        }
        RComponent underMouse = findVisibleComponentAt(mouseEvent.getX(), adjustedMouseY);
        if (underMouse != null) {
            if (!underMouse.isMouseOver()) {
                LOGGER.debug("Inside Child Component {} [NX {} NY {} Width {} Height {}]", underMouse.getName(), underMouse.getPosX(), underMouse.getPosY(), underMouse.getWidth(), underMouse.getHeight());
                redrawBuffers(); // REDRAW-VALID: we should redraw the group buffer if the mouse is over one of the children
            }
            underMouse.mouseOver(mouseEvent, adjustedMouseY);
        }
        mouseEvent.consume();
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (!this.isVisibleParentAware(null)) {
            return;
        }

        RComponent child = findVisibleComponentAt(mouseEvent.getX(), adjustedMouseY);
        if (child != null) {
            LOGGER.debug("Mouse Pressed for Child Component {} [{}, {}, {}, {}, {}, {}]", child.getName(), mouseEvent.getX(), adjustedMouseY, child.getPosX(), child.getPosY(), child.getWidth(), child.getHeight());
            child.mousePressed(mouseEvent, adjustedMouseY);
            redrawBuffers(); // REDRAW-VALID: we should redraw the group buffer if the user pressed the mouse over a child
        }
    }

    /**
     * Method to handle the component's reaction to the mouse being released outside of itself
     *
     * @param mouseEvent     the change made by the mouse
     * @param adjustedMouseY adjust for scrollbar
     */
    @Override
    public void mouseReleasedAnywhere(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (!this.isVisibleParentAware(null)) {
            return;
        }

        if (isDragged()) {
            redrawBuffers(); // REDRAW-VALID: we should redraw the group buffer if the user was dragging a child and released the mouse anywhere
        }

        for (RComponent component : children) {
            component.mouseReleased(mouseEvent, adjustedMouseY, false);
        }
    }

    /**
     * Method to handle the component's reaction to the mouse being released over it
     *
     * @param mouseEvent     the change made by the mouse
     * @param adjustedMouseY adjust for scrollbar
     */
    @Override
    public void mouseReleasedOverComponent(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (!this.isVisibleParentAware(null)) {
            return;
        }
        RComponent child = findVisibleComponentAt(mouseEvent.getX(), adjustedMouseY);
        if (child != null) {
            child.mouseReleased(mouseEvent, adjustedMouseY, true);
            redrawBuffers(); // REDRAW-VALID: we should redraw the group buffer if the user released the mouse over a child
        }
    }


    @Override
    public void mouseDragged(RMouseEvent mouseEvent) {
        if (!this.isVisibleParentAware(null)) {
            return;
        }
        for (RComponent child : children) {
            LOGGER.debug("Mouse Drag Check for Content {}", child.getName());
            if (child.isDragged()) {
                LOGGER.debug("Mouse Dragged for Content {}", child.getName());
                child.mouseDragged(mouseEvent);
                if (mouseEvent.isConsumed()) {
                    redrawBuffers(); // REDRAW-VALID: we should redraw the group buffer if the user is dragging a child
                    break;
                }
            }
        }
    }

    @Override
    public void refreshBuffer() {
        if (isVisibleParentAware(null)) {
            buffer.resetBuffer();
            children.forEach(RComponent::refreshBuffer);
        }
    }
    @Override
    public float suggestWidth() {
        return getPreferredSize().x;
    }

    @Override
    public void updateCoordinates(float bX, float bY, float rX, float rY, float w, float h) {
        LOGGER.debug("Update Coordinates for Drawable Group [{}, {}, {}, {}, {}, {}]", bX, bY, rX, rY, w, h);
        super.updateCoordinates(bX, bY, rX, rY, w, h);
        updateChildrenCoordinates();
    }

    public void updateChildrenCoordinates() {
        LOGGER.debug("{} Layout [{},{}]",getName(),getWidth(),getHeight());
        layout.setCompLayout(pos, size, children);
    }

    public void updateComponentCoordinates(float bX, float bY, float rX, float rY, float w, float h) {
        LOGGER.debug("Update Coordinates for Just Drawable Group [{}, {}, {}, {}, {}, {}]", bX, bY, rX, rY, w, h);
        super.updateCoordinates(bX, bY, rX, rY, w, h);
    }
}
