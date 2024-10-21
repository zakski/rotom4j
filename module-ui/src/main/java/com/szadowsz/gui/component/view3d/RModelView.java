package com.szadowsz.gui.component.view3d;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.folder.RFolder;
import processing.core.PGraphics;

/**
 * Component for display of 3D model
 */
public class RModelView extends RComponent {
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
    protected RModelView(RotomGui gui, String path, RFolder parentFolder) {
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
