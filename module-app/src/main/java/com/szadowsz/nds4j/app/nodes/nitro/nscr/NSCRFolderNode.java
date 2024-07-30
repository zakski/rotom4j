package com.szadowsz.nds4j.app.nodes.nitro.nscr;

import com.szadowsz.nds4j.app.nodes.nitro.NitroCmpFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.PreviewNode;
import com.szadowsz.nds4j.app.nodes.nitro.ncgr.NCGRFolderNode;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.nscr.NSCR;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.impl.ButtonNode;
import com.szadowsz.ui.node.impl.FolderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;


public class NSCRFolderNode extends NitroCmpFolderNode<NSCR> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NSCRFolderNode.class);

    public NSCRFolderNode(String path, FolderNode parent, NSCR nscr) {
        super(path, SELECT_NSCR_FILE, parent, LayoutType.VERTICAL_1_COL,nscr);
       children.clear();
        children.add(new PreviewNode(path + "/" + imageable.getFileName(), this,imageable));

        children.add(createZoom());

        children.add(new NCGRFolderNode(path + "/" + IMAGE_NODE_NAME, this,imageable.getNCGR()));
    }

    @Override
    public void recolorImage() throws NitroException {
        super.recolorImage();
        imageable.recolorImage();

        PImage pImage = resizeImage(imageable.getImage());
        ((PreviewNode) findChildByName(imageable.getFileName())).loadImage(pImage);

        this.window.resizeForContents();
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        if (imageable.getNCGR() != null) {
            return ((PreviewNode) children.get(0)).getImage().width;
        } else {
            return imageable.getWidth();
        }
    }
}
