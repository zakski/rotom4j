package com.szadowsz.gui.component.input.dropdown;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.folder.RFolder;
import processing.core.PGraphics;

/**
 * A dropdown list component, single selection
 * The number of items in the list is not restricted but the user can define the
 * maximum number of items to be displayed in the drop list. If there are too
 * many items to display a vertical scroll bar is provide to scroll through all
 * the items.
 *
 * The vertical size of an individual item is calculated from the overall height
 * specified when creating the control.
 */
public class RDropdown extends RComponent {
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
    protected RDropdown(RotomGui gui, String path, RFolder parentFolder) {
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
