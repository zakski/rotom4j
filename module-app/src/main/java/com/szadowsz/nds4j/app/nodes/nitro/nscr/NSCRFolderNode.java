package com.szadowsz.nds4j.app.nodes.nitro.nscr;

import com.szadowsz.nds4j.app.nodes.nitro.NitroCmpFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.PreviewNode;
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

        ButtonNode selectNcgr = new ButtonNode(path + "/" + SELECT_NCGR_FILE,this);
        selectNcgr.registerAction(ActivateByType.RELEASE, this::selectNcgr);
        children.add(selectNcgr);

        ButtonNode selectNcLr = new ButtonNode(path + "/" + SELECT_NCLR_FILE,this);
        selectNcLr.registerAction(ActivateByType.RELEASE, this::selectPalette);
        children.add(selectNcLr);
    }

    @Override
    public void recolorImage() throws NitroException {
        imageable.recolorImage();

        PImage pImage = resizeImage(imageable.getImage());

        ((PreviewNode) findChildByName(imageable.getFileName())).loadImage(pImage);

        this.window.windowSizeX = autosuggestWindowWidthForContents();
        this.window.windowSizeXForContents = autosuggestWindowWidthForContents();
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
