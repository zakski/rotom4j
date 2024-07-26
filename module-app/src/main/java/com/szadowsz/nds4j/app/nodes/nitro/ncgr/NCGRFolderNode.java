package com.szadowsz.nds4j.app.nodes.nitro.ncgr;

import com.szadowsz.nds4j.app.nodes.nitro.nclr.NCLRFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.NitroImgFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.PreviewNode;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.ncgr.NCGR;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.impl.ButtonNode;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.store.JsonSaveStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;


public class NCGRFolderNode extends NitroImgFolderNode<NCGR> {

    private final String PALETTE_NODE_NAME = "palette";
    protected static final Logger LOGGER = LoggerFactory.getLogger(NCGRFolderNode.class);

    public NCGRFolderNode(String path, FolderNode parent, NCGR ncgr) {
        super(path, SELECT_NCGR_FILE, parent, LayoutType.VERTICAL_1_COL,ncgr);
        initNode();
        JsonSaveStore.overwriteWithLoadedStateIfAny(this);
    }

    protected void initNode() {
        if (imageable == null) {
            return;
        }
        children.clear();
        children.add(new PreviewNode(path + "/" + imageable.getFileName(), this,imageable));
        children.add(createZoom());
        children.add(new NCLRFolderNode(path + "/" + PALETTE_NODE_NAME, this,imageable.getNCLR()));
    }

    @Override
    public void recolorImage() throws NitroException {
        imageable.setNCLR(((NCLRFolderNode)findChildByName(PALETTE_NODE_NAME)).getImageable());
        imageable.recolorImage();

        PImage pImage = resizeImage(imageable.getImage());

        ((PreviewNode) findChildByName(imageable.getFileName())).loadImage(pImage);

        this.window.windowSizeX = autosuggestWindowWidthForContents();
        this.window.windowSizeXForContents = autosuggestWindowWidthForContents();
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        return ((PreviewNode)children.getFirst()).getImage().width;
    }
}
