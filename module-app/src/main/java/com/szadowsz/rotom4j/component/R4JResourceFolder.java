package com.szadowsz.rotom4j.component;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.rotom4j.file.RotomFile;

public abstract class R4JResourceFolder<R extends RotomFile> extends R4JFolder<R> {

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui        the gui for the window that the component is drawn under
     * @param path       the path in the component tree
     * @param parent     the parent component reference
     * @param data
     * @param selectName
     */
    public R4JResourceFolder(RotomGui gui, String path, RGroup parent, R data, String selectName) {
        super(gui, path, parent, data, selectName);
    }
}
