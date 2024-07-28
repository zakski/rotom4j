package com.szadowsz.nds4j.app.nodes.nitro;

import com.szadowsz.nds4j.file.ImageableWithPalette;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.impl.FolderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NitroImgFolderNode<I extends ImageableWithPalette> extends NitroFolderNode<I> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NitroImgFolderNode.class);

    protected static final String SELECT_NCGR_FILE = "Select NCGR";

    public NitroImgFolderNode(String path, String selectName, FolderNode parent, LayoutType layout, I imageable) {
        super(path, parent, layout, imageable, selectName);
    }
}
