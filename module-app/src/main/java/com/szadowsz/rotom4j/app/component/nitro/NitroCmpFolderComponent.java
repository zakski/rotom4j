package com.szadowsz.rotom4j.app.component.nitro;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.ImageableWithGraphic;
import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;
import com.szadowsz.rotom4j.app.component.nitro.ncgr.NCGRFolderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NitroCmpFolderComponent<I extends ImageableWithGraphic> extends NitroImgFolderComponent<I> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NitroCmpFolderComponent.class);

    protected static final String SELECT_NSCR_FILE = "Select NSCR";
    protected static final String SELECT_NCER_FILE = "Select NCER";
    protected static final String IMAGE_NODE_NAME = "image";

    public NitroCmpFolderComponent(RotomGui gui, String path, RGroup parent, I imageable, String selectName) {
            super(gui,path, parent, imageable, selectName);
    }

    @Override
    public void recolorImage() throws NitroException {
        NCGR ncgr = ((NCGRFolderComponent) findChildByName(IMAGE_NODE_NAME)).getImageable();
        imageable.setNCGR(ncgr);
    }
}
