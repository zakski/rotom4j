package com.szadowsz.gui.component.input.color;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.component.group.RColorPicker;
import processing.core.PGraphics;

import java.awt.*;

/**
 * Node to give a graphical preview of a given Color
 */
public class RColorPreview extends RComponent {
    // TODO Component Stub : WIP

    private final RColorPicker group;
    private Color currentValue;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parentFolder the parent component folder reference // TODO consider if needed
     */
    public RColorPreview(RotomGui gui, String path, RColorPicker group, Color currentValue) {
        super(gui, path, group);
        this.group = group;
        this.currentValue = currentValue;
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
