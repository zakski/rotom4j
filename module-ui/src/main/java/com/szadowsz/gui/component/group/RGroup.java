package com.szadowsz.gui.component.group;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLayoutConfig;
import com.szadowsz.gui.layout.RLinearLayout;
import com.szadowsz.gui.window.pane.RWindowPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.szadowsz.gui.utils.RCoordinates.isPointInRect;


/**
 * Base class for complex pre-defined groupings of components
 */
public abstract class RGroup extends RComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RGroup.class);

    protected RLayoutBase layout;


    /**
     * CopyOnWriteArrayList is needed to avoid concurrent modification
     * because the children get drawn by one thread and user input changes the list from another thread
     */
    protected final CopyOnWriteArrayList<RComponent> children = new CopyOnWriteArrayList<>(); // TODO LazyGui
    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected RGroup(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
    }

    /**
     * Get All Child Components
     *
     * @return list of child Components
     */
    public List<RComponent> getChildren() {
        return children;
    }

    public RLayoutBase getLayout() {
        return null;
    }


    public boolean isChildMouseOver(){
        return children.stream().anyMatch(RComponent::isMouseOver);
    }

    public boolean canChangeLayout() {
        return true;
    }

    public abstract void setLayout(RLayoutBase layout);

    /**
     * Find a node by its name
     *
     * @param name the name of the node
     * @return the node, if found
     */
    public RComponent findChildByName(String name) {
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        for (RComponent node : children) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Find A Component At The Given Coordinates
     *
     * @param x X-Coordinate
     * @param y Y-Coordinate
     * @return The Component at these Coordinates, if one exists, false otherwise
     */
    public RComponent findComponentAt(float x, float y) {
        for (RComponent node : children) {
            if (!node.isVisible()) {
                continue;
            }
            if (isPointInRect(x, y, node.getPosX(), node.getPosY(), node.getWidth(), node.getHeight())) {
                return node;
            }
        }
        return null;
    }

    /**
     *
     * @param keyEvent
     * @param mouseX
     * @param mouseY
     */
    public void keyPressed(RKeyEvent keyEvent, float mouseX, float mouseY) {
        if (!isVisible()){
            return;
        }
        RComponent underMouse = findComponentAt(mouseX, mouseY);
        switch (underMouse){
            case null -> {}// NOOP
            case RGroup g -> g.keyPressed(keyEvent,mouseX,mouseY);
            case RComponent c -> keyPressedOver(keyEvent, mouseX, mouseY);
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

    public RLayoutConfig getCompLayoutConfig() {
        return layout.getLayoutConfig();
    }

    public RLayoutConfig getWinLayoutConfig() {
        return layoutConfig;
    }

    public void sortChildren() {
        children.sort((o1, o2) -> switch (layout){
            case RLinearLayout linear -> {
                if (linear.getDirection() == RDirection.VERTICAL){
                    yield Float.compare(o1.getPosY(), o2.getPosY());
                } else {
                    yield Float.compare(o1.getPosX(), o2.getPosX());
                }
            }
            default -> Float.compare(o1.getPosY(), o2.getPosY());
        });
    }
}
