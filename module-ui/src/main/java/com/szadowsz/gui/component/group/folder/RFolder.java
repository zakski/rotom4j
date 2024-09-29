package com.szadowsz.gui.component.group.folder;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.window.pane.RWindowPane;

public class RFolder extends RGroup {

    protected RWindowPane window; // TODO LazyGui

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected RFolder(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
    }

    public RWindowPane getWindow() {
        return window;
    }

    public void setWindow(RWindowPane pane) {
        window = pane;
    }

    public boolean isWindowVisible() {
        return false;
    }

    public boolean shouldDrawTitle() {
        return false;
    }
}
