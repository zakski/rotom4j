package com.szadowsz.gui.component.bined;


import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.component.text.RTextBase;

/**
 * Binary data viewer/editor component.
 */
public class RBinedArea extends RTextBase { // TODO Determine Exact Relationship
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
    protected RBinedArea(RotomGui gui, String path, RFolder parentFolder) {
        super(gui, path, parentFolder);
    }
}
