package com.szadowsz.gui.component.group;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.component.layout.RLayoutConfig;
import processing.core.PVector;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base class for complex pre-defined groupings of components
 */
public abstract class RGroup extends RComponent {

    protected final CopyOnWriteArrayList<RComponent> children = new CopyOnWriteArrayList<>();

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parentFolder the parent component folder reference // TODO consider if needed
     */
    protected RGroup(RotomGui gui, String path, RFolder parentFolder) {
        super(gui, path, parentFolder);
    }

    protected abstract PVector calcPreferredSize(List<RComponent> components);

    public abstract RLayoutConfig getLayoutConfig();
}
