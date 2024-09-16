package com.szadowsz.gui.component.group;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLayoutConfig;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Component to group Checkboxes, or Toggles, only one of which can be active at a time
 */
public class RRadioGroup extends RGroup{
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
    protected RRadioGroup(RotomGui gui, String path, RFolder parentFolder) {
        super(gui, path, parentFolder);
    }

    @Override
    public PVector getPreferredSize() {
        return null;
    }

    @Override
    protected void drawBackground(PGraphics pg) {

    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {

    }

    @Override
    public RLayoutConfig getCompLayoutConfig() {
        return null;
    }

    @Override
    public void setLayout(RLayoutBase layout) {

    }

    @Override
    public float suggestWidth() {
        return 0;
    }
}