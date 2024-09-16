package com.szadowsz.gui.component.group;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.RComponentTree;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLayoutConfig;
import com.szadowsz.gui.layout.RLinearLayout;
import processing.core.PVector;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.szadowsz.gui.utils.RCoordinates.isPointInRect;

/**
 * Base class for complex pre-defined groupings of components
 */
public abstract class RGroup extends RComponent {

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
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parent       the parent component folder reference // TODO consider if needed
     */
    protected RGroup(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
        layout = new RLinearLayout(this); // Default to Linear Vertical
    }

    /**
     * Find a node by its name
     *
     * @param name the name of the node
     * @return the node, if found
     */
    protected RComponent findChildByName(String name) {
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
    protected RComponent findComponentAt(float x, float y) {
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

    public List<RComponent> getChildren() {
        return children;
    }

    public RLayoutConfig getCompLayoutConfig() {
        return layout.getLayoutConfig();
    }

    @Override
    public PVector getPreferredSize() {
        return layout.calcPreferredSize(getName(),children);
    }

    public RLayoutConfig getWinLayoutConfig() {
        return layoutConfig;
    }

    public boolean canChangeLayout() {
        return true;
    }

    public abstract void setLayout(RLayoutBase layout);


    public void insertChild(RComponent child){
        children.add(child);
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
