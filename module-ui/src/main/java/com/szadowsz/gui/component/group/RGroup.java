package com.szadowsz.gui.component.group;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLayoutConfig;
import com.szadowsz.gui.layout.RLinearLayout;
import processing.core.PVector;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
        layout = new RLinearLayout(); // Default to Linear Vertical
    }


    /**
     * Calculate the size characteristics based on the layout
     *
     * @return width and height in a PVector
     */
    protected PVector calcPreferredSize() {
        return layout.calcPreferredSize(children);
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

    public List<RComponent> getChildren() {
        return children;
    }

    public RLayoutConfig getLayoutConfig() {
        return layout.getLayoutConfig();
    }

    public boolean canChangeLayout() {
        return true;
    }

    public abstract void setLayout(RLayoutBase layout);


    public void insertChild(RComponent child){
        children.add(child);
    }
}
