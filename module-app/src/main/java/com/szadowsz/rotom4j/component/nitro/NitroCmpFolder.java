package com.szadowsz.rotom4j.component.nitro;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.rotom4j.component.R4JFolder;
import com.szadowsz.rotom4j.file.RotomFile;
import com.szadowsz.rotom4j.file.nitro.DrawableWithGraphic;

public abstract class NitroCmpFolder<C extends RotomFile & DrawableWithGraphic> extends R4JFolder<C> {
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
    public NitroCmpFolder(RotomGui gui, String path, RGroup parent, C data, String selectName) {
        super(gui, path, parent, data, selectName);
    }
}
