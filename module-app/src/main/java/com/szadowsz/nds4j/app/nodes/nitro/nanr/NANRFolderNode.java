package com.szadowsz.nds4j.app.nodes.nitro.nanr;

import com.szadowsz.nds4j.app.nodes.nitro.NitroCmpFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.PreviewNode;
import com.szadowsz.nds4j.app.nodes.nitro.ncer.NCERFolderNode;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.nanr.NANR;
import com.old.ui.node.LayoutType;
import com.old.ui.node.impl.FolderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PImage;

import static com.old.ui.store.LayoutStore.cell;


public class NANRFolderNode extends NitroCmpFolderNode<NANR> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NANRFolderNode.class);
    private static final String SELECT_NANR_FILE = "Select NANR";

    protected static final String CELL_NODE_NAME = "cell";


    public NANRFolderNode(String path, FolderNode parent, NANR nanr) throws NitroException {
        super(path, SELECT_NANR_FILE, parent, LayoutType.VERTICAL_1_COL, nanr);
         children.clear();
         children.add(new PreviewNode(path + "/" + PREVIEW_NODE, this,nanr));

        children.add(createZoom());

        children.add(new NCERFolderNode(path + "/" + CELL_NODE_NAME, this,imageable.getNCER()));
    }

    public void recolorImage() throws NitroException {
        imageable.getNCGR().recolorImage();

        PImage pImage = resizeImage(imageable.getImage());
        ((PreviewNode) findChildByName(PREVIEW_NODE)).loadImage(pImage);

        this.window.resizeForContents();
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        float suggested = super.autosuggestWindowWidthForContents();
        if (imageable.getNCGR() != null) {
            return Math.max(suggested,((PreviewNode) children.get(0)).getImage().width);
        } else {
            return suggested;
        }
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        drawLeftText(pg, name);
        drawRightBackdrop(pg, cell);
   }
}
