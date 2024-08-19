package com.szadowsz.gui.component.input.toggle;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.folder.RFolder;

/**
 *  Variant component for binary user input controls, that is also known as an Option, or a Switch.
 */
public class RToggle extends RToggleBase {
    // TODO Component Stub : WIP

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui           the gui for the window that the component is drawn under
     * @param path          the path in the component tree
     * @param parentFolder  the parent component folder reference // TODO consider if needed
     * @param startingValue
     */
    public RToggle(RotomGui gui, String path, RFolder parentFolder, boolean startingValue) {
        super(gui, path, parentFolder);
    }
}
