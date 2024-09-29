package com.old.gui.component.input;

import com.old.gui.RotomGui;
import com.old.gui.component.RComponent;
import com.old.gui.component.folder.RFolder;
import processing.core.PGraphics;

/**
 * Configurable knob value component.
 */
public class RKnob extends RComponent {
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
    protected RKnob(RotomGui gui, String path, RFolder parentFolder) {
        super(gui, path, parentFolder);
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
