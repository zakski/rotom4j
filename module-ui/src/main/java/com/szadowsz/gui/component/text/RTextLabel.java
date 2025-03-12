package com.szadowsz.gui.component.text;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;

/**
 * Non-Editable Text label component.
 *
 */
public class RTextLabel extends RTextBase {
    // TODO Component Stub : WIP

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parentFolder the parent component folder reference // TODO consider if needed
     * @param content the value of the label
     */
    public RTextLabel(RotomGui gui, String path, RFolder parentFolder, String content) {
        super(gui, path, parentFolder);
        stext.setText(content);
    }
}