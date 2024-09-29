package com.szadowsz.gui.component.group;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.layout.RLayoutBase;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base class for complex pre-defined groupings of components
 */
public class RGroup extends RComponent {

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
     *
     *
     * @return list of child Components
     */
    public List<RComponent> getChildren() {
        return children;
    }

    public RLayoutBase getLayout() {
        return null;
    }
}
