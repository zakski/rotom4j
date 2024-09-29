package com.szadowsz.nds4j.app.nodes.nitro;

import com.szadowsz.nds4j.app.nodes.nitro.ncgr.NCGRFolderNode;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.ImageableWithGraphic;
import com.szadowsz.nds4j.file.nitro.ncgr.NCGR;
import com.old.ui.node.LayoutType;
import com.old.ui.node.impl.FolderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NitroCmpFolderNode<I extends ImageableWithGraphic> extends NitroImgFolderNode<I> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NitroCmpFolderNode.class);

    protected static final String SELECT_NSCR_FILE = "Select NSCR";
    protected static final String SELECT_NCER_FILE = "Select NCER";
    protected static final String IMAGE_NODE_NAME = "image";

    public NitroCmpFolderNode(String path, String selectName, FolderNode parent, LayoutType layout, I imageable) {
        super(path, selectName, parent, layout, imageable);
    }

    @Override
    public void recolorImage() throws NitroException {
        NCGR ncgr = ((NCGRFolderNode) findChildByName(IMAGE_NODE_NAME)).getImageable();
        imageable.setNCGR(ncgr);
    }
}
