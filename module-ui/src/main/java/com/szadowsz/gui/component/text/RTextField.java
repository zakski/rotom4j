package com.szadowsz.gui.component.text;


import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;

/**
 * Standard text field component.
 *
 * It allows the user to enter and edit a single line of text.
 */
public class RTextField extends RTextEditable {
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
    protected RTextField(RotomGui gui, String path, RFolder parentFolder) {
        super(gui, path, parentFolder);
    }
}
