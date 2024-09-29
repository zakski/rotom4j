package com.old.gui.layout;

import com.old.gui.component.RComponent;
import com.old.gui.component.group.RGroup;
import com.old.gui.window.internal.RWindowInt;
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
    public PVector calcPreferredSize(String title, List<RComponent> components) {
        return null;
    }

    @Override
    public RGroup getGroup() {
        return null;
    }

    @Override
    public RLayoutConfig getLayoutConfig() {
        return new RLayoutConfig() {
        };
    }

    @Override
    public void setCompLayout(PVector start, PVector area, List<RComponent> components) {

    }

    @Override
    public void setWinLayout(PVector area, List<RWindowInt> windows) {

    }

    @Override
    public void setGroup(RGroup group) {

    }
}