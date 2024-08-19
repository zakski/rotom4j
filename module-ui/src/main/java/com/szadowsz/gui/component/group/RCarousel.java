package com.szadowsz.gui.component.group;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.layout.RLayoutConfig;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Similar to a Spinner, this component should give you the option to rotate through a list of other Components
 */
public class RCarousel extends RGroup {
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
    protected RCarousel(RotomGui gui, String path, RFolder parentFolder) {
        super(gui, path, parentFolder);
    }

    @Override
    protected PVector calcPreferredSize() {
        return null;
    }

    @Override
    public RLayoutConfig getLayoutConfig() {
        return null;
    }

    @Override
    protected void drawBackground(PGraphics pg) {

    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {

    }

    @Override
    public float suggestWidth() {
        return 0;
    }
}
