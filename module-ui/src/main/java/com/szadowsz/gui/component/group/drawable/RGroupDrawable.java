package com.szadowsz.gui.component.group.drawable;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLinearLayout;
import com.szadowsz.gui.window.pane.RWindowPane;
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
            layout.setCompLayout(getParentWindow().getContentStart(),pos, getSize(), getChildren());
            drawChildren(pg);
        }
        LOGGER.debug("{} Group [{},{}] Draw Duration {}", getName(), pg.width, pg.height, System.currentTimeMillis() - time);
    }

    /**
     * Get the preferred size characteristics
     *
     * @return width and height in a PVector
     */
    public PVector getPreferredSize() {
        return layout.calcPreferredSize(getParentFolder().getName(), children);
    }

    public PVector getBufferSize() {
        return getSize();
    }

    @Override
    public boolean isDragged() {
        return children.stream().anyMatch(RComponent::isDragged);
    }

    @Override
    public boolean isMouseOver() {
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
        if (!isVisible()) {
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
    public void mouseOver(RMouseEvent mouseEvent, float adjustedMouseY) {
        RComponent underMouse = findComponentAt(mouseEvent.getX(), adjustedMouseY);
        if (underMouse != null) {
            if (!underMouse.isMouseOver()) {
                LOGGER.info("Inside Component {} [NX {} NY {} Width {} Height {}]", underMouse.getName(), underMouse.getPosX(), underMouse.getPosY(), underMouse.getWidth(), underMouse.getHeight());
                redrawWinBuffer();
            }
            underMouse.mouseOver(mouseEvent, adjustedMouseY);
        }
        mouseEvent.consume();
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (!isVisible() || !this.isVisibleParentAware()) {
            return;
        }
        RComponent node = findComponentAt(mouseEvent.getX(), adjustedMouseY);
        if (node != null) {
            LOGGER.debug("Mouse Pressed for component {} [{}, {}, {}, {}, {}, {}]", node.getName(), mouseEvent.getX(), adjustedMouseY, node.getPosX(), node.getPosY(), node.getWidth(), node.getHeight());
            node.mousePressed(mouseEvent, adjustedMouseY);
            redrawWinBuffer();
        }
    }

    /**
     * Method to handle the component's reaction to the mouse being released outside of itself
     *
     * @param mouseEvent the change made by the mouse
     * @param adjustedMouseY adjust for scrollbar
     */
    @Override
    public void mouseReleasedAnywhere(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (!isVisible() || !this.isVisibleParentAware()) {
            return;
        }
        if (isDragged() && mouseEvent.isConsumed()){
            redrawWinBuffer();
        }
        for (RComponent component : children) {
            component.mouseReleased(mouseEvent, adjustedMouseY,false);
        }
    }

    /**
     * Method to handle the component's reaction to the mouse being released over it
     *
     * @param mouseEvent the change made by the mouse
     * @param adjustedMouseY adjust for scrollbar
     */
    @Override
    public void mouseReleasedOverComponent(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (!isVisible() || !this.isVisibleParentAware()) {
            return;
        }
        RComponent node = findComponentAt(mouseEvent.getX(), adjustedMouseY);
        if (node != null) {
             node.mouseReleased(mouseEvent, adjustedMouseY,true);
            redrawWinBuffer();
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
                    redrawWinBuffer();
                    break;
                }
            }
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
        layout.setCompLayout(pos, size, children);
    }

    public void redrawWinBuffer() {
        RWindowPane win = getParentWindow();
        if (win != null) {
            win.redrawBuffer();
        }
    }
}
