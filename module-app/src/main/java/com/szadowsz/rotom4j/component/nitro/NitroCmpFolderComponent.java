package com.szadowsz.rotom4j.component.nitro;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.DrawableWithGraphic;
import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;
import com.szadowsz.rotom4j.component.nitro.ncgr.NCGRFolderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NitroCmpFolderComponent<I extends DrawableWithGraphic> extends NitroImgFolderComponent<I> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NitroCmpFolderComponent.class);

    protected static final String SELECT_NSCR_FILE = "Select NSCR";
    protected static final String SELECT_NCER_FILE = "Select NCER";
    protected static final String IMAGE_NODE_NAME = "image";

    public NitroCmpFolderComponent(RotomGui gui, String path, RGroup parent, I drawable, String selectName) {
            super(gui,path, parent, drawable, selectName);
    }

    @Override
    public void recolorImage() throws NitroException {
        NCGR ncgr = ((NCGRFolderComponent) findChildByName(IMAGE_NODE_NAME)).getDrawable();
        drawable.setNCGR(ncgr);
    }
}
