package com.szadowsz.gui.component.group;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.szadowsz.gui.utils.RCoordinates.isPointInRect;


/**
 * Base class for complex pre-defined groupings of components
 */
public abstract class RGroup extends RComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RGroup.class);

    /**
     * Ordering For Node Group
     */
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
        layout = new RLinearLayout(this); // Default to Linear Vertical
    }

    @Override
    protected void drawBackground(PGraphics pg) {
        // NOOP
    }

    /**
     * Get All Child Components
     *
     * @return list of child Components
     */
    public List<RComponent> getChildren() {
        return children;
    }

    /**
     * Get Layout to arrange the Children by
     *
     * @return the Layout
     */
    public RLayoutBase getLayout() {
        return layout;
    }

    /**
     * Method to check if a Child is being hovered over by the mouse
     *
     * @return true if the mouse is over a child, false otherwise
     */
    public boolean isChildMouseOver(){
        return children.stream().anyMatch(RComponent::isMouseOver);
    }

    /**
     * Method to check if the layout can be changed
     *
     * @return true if it can be, false otherwise
     */
    public boolean canChangeLayout() {
        return true;
    }

    /**
     * Method to change the layout, if able
     *
     * @param layout the layout to set
     */
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
     * Find A Component At The Given Coordinates
     *
     * @return The Component at these Coordinates, if one exists, false otherwise
     */
    public RComponent findFocusedComponent() {
        for (RComponent node : children) {
            if (!node.isVisible()) {
                continue;
            }
            if (node.hasFocus()) {
                return node;
            }
        }
        return null;
    }

    /**
     * Method to handle key presses
     *
     * @param keyEvent the key event info
     * @param mouseX Mouse X-Coordinate
     * @param mouseY Mouse Y-Coordinate
     */
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
                case RComponent c -> c.keyPressedFocused(keyEvent);
            }
        } else {
            RComponent underMouse = findComponentAt(mouseX, mouseY);
            switch (underMouse) {
                case null -> {
                }// NOOP
                case RGroup g -> g.keyPressed(keyEvent, mouseX, mouseY);
                case RComponent c -> c.keyPressedOver(keyEvent, mouseX, mouseY);
            }
        }
    }

    /**
     * Method to handle key chord presses
     *
     * @param keyEvent the key event info
     * @param mouseX Mouse X-Coordinate
     * @param mouseY Mouse Y-Coordinate
     */
    public void keyChordPressed(RKeyEvent keyEvent, float mouseX, float mouseY) {
        if (!isVisible()) {
            return;
        }
        RComponent underMouse = findComponentAt(mouseX, mouseY);
        switch (underMouse){
            case null -> {}// NOOP
            case RGroup g -> g.keyChordPressed(keyEvent,mouseX,mouseY);
            case RComponent c -> c.keyChordPressedOver(keyEvent, mouseX, mouseY);
        }
    }


    public void keyTyped(RKeyEvent keyEvent, float mouseX, float mouseY) {
        if (!isVisible()) {
            return;
        }
        RComponent underMouse = findComponentAt(mouseX, mouseY);
        switch (underMouse){
            case null -> {}// NOOP
            case RGroup g -> g.keyTyped(keyEvent,mouseX,mouseY);
            case RComponent c -> c.keyTypedOver(keyEvent, mouseX, mouseY);
        }
    }

    @Override
    public RLayoutConfig getCompLayoutConfig() {
        return layout.getLayoutConfig();
    }

    /**
     * Get the Window Layout Config
     *
     * @return layout config
     */
    public RLayoutConfig getWinLayoutConfig() {
        return layoutConfig;
    }

    /**
     * Method to add a new child
     *
     * @param child the child to add
     */
    public void insertChild(RComponent child) {
        children.add(child);
        RFolder folder = getParentFolder();
        if (folder.getWindow() != null) {
            folder.getWindow().resizeForContents(true);
            folder.getWindow().reinitialiseBuffer();
        }
    }

    /**
     * Method to sort the children according to their layout position
     */
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
