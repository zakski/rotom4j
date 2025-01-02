package com.szadowsz.rotom4j.app.component.nitro;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.rotom4j.file.ImageableWithPalette;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NitroImgFolderComponent<I extends ImageableWithPalette> extends NitroFolderComponent<I> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NitroImgFolderComponent.class);

    protected static final String SELECT_NCGR_FILE = "Select NCGR";

    public NitroImgFolderComponent(RotomGui gui, String path, RGroup parent, I imageable, String selectName) {
        super(gui, path, parent, imageable, selectName);
    }
}
