package com.szadowsz.gui.layout;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.folder.RFolder;
import processing.core.PVector;

import java.util.List;

/**
 * Layout that renders all components in an arbitrarily sized grid
 */
public class RGridLayout extends RLayoutBase {
    // TODO Component Stub : WIP

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parentFolder the parent component folder reference // TODO consider if needed
     */
    protected RGridLayout() {
        super();
    }

    @Override
    public PVector calcPreferredSize(List<RComponent> components) {
        return null;
    }

    @Override
    public RLayoutConfig getLayoutConfig() {
        return null;
    }
}