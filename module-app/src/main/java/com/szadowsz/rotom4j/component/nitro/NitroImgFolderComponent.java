package com.szadowsz.rotom4j.component.nitro;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.rotom4j.file.nitro.DrawableWithPalette;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NitroImgFolderComponent<I extends DrawableWithPalette> extends NitroFolderComponent<I> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NitroImgFolderComponent.class);

    protected static final String SELECT_NCGR_FILE = "Select NCGR";

    public NitroImgFolderComponent(RotomGui gui, String path, RGroup parent, I drawable, String selectName) {
        super(gui, path, parent, drawable, selectName);
    }
}
