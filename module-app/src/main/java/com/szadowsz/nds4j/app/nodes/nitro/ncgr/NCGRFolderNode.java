package com.szadowsz.nds4j.app.nodes.nitro.ncgr;

import com.szadowsz.nds4j.app.Processing;
import com.szadowsz.nds4j.app.nodes.nitro.NitroCmpFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.nclr.NCLRFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.NitroImgFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.PreviewNode;
import com.szadowsz.nds4j.app.utils.FileChooser;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.ncgr.NCGR;
import com.old.ui.constants.GlobalReferences;
import com.old.ui.input.ActivateByType;
import com.old.ui.input.mouse.GuiMouseEvent;
import com.old.ui.node.LayoutType;
import com.old.ui.node.impl.ButtonNode;
import com.old.ui.node.impl.FolderNode;
import com.old.ui.store.JsonSaveStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;

import java.io.File;
import java.io.IOException;

public class NCGRFolderNode extends NitroImgFolderNode<NCGR> {

    private static final String PALETTE_NODE_NAME = "palette";
    protected static final Logger LOGGER = LoggerFactory.getLogger(NCGRFolderNode.class);

    private NitroCmpFolderNode<?> cmpFolder;

    public NCGRFolderNode(String path, FolderNode parentFolder, NCGR ncgr) {
        super(path, SELECT_NCGR_FILE, parentFolder, LayoutType.VERTICAL_1_COL,ncgr);
        if (parentFolder instanceof NitroCmpFolderNode){
            LOGGER.debug("Attached to parent NCGR");
            cmpFolder = (NitroCmpFolderNode<?>) parentFolder;
        }
        initNode();
        JsonSaveStore.overwriteWithLoadedStateIfAny(this);
    }

    protected void initNode() {
        if (imageable == null) {
            return;
        }
        children.clear();
        children.add(new PreviewNode(path + "/" + PREVIEW_NODE, this,imageable));
        children.add(createZoom());
        children.add(new NCLRFolderNode(path + "/" + PALETTE_NODE_NAME, this,imageable.getNCLR()));
        if (cmpFolder != null) {
            ButtonNode reset = new ButtonNode(path + "/" + RESET_NODE, this);
            reset.registerAction(ActivateByType.RELEASE, this::resetImage);
            children.add(reset);
        }
    }

    protected void resetImage() {
        NCGR original = imageable;
        try {
            LOGGER.debug("Resetting NCGR File: " + original.getFileName());
            imageable = null;
            LOGGER.info("Reset NCGR File to Default: " + original.getFileName());
            //recolorImage();
            if (cmpFolder != null) {
                cmpFolder.recolorImage();
            }
            this.window.close();
        } catch (IOException e) {
            LOGGER.error("NCLR Load Failed", e);
            try {
                imageable = original;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }
    protected void selectNcgr() {
        String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
        String ncgrPath = FileChooser.selectNcgrFile(GlobalReferences.gui.getGuiCanvas().parent, lastPath,SELECT_NCGR_FILE);
        if (ncgrPath != null) {
            Processing.prefs.put("openNarcPath", new File(ncgrPath).getParentFile().getAbsolutePath());
            try {
                LOGGER.debug("Loading NCGR File: " + ncgrPath);
                imageable = NCGR.fromFile(ncgrPath);
                initNode();
                recolorImage();
                LOGGER.info("Loaded NCGR File: " + ncgrPath);
            } catch (IOException e) {
                LOGGER.error("NCGR Load Failed",e);
            }
        }
    }

    @Override
    public void mousePressedEvent(GuiMouseEvent e) {
        if (shouldDisplayName()){
            super.mousePressedEvent(e);
        } else {
            selectNcgr();
        }
    }

    @Override
    public void recolorImage() throws NitroException {
        imageable.setNCLR(((NCLRFolderNode)findChildByName(PALETTE_NODE_NAME)).getImageable());
        imageable.recolorImage();

        PImage pImage = resizeImage(imageable.getImage());

        ((PreviewNode) findChildByName(imageable.getFileName())).loadImage(pImage);

        if (this.window!=null) {
            this.window.resizeForContents();
        }
        if (cmpFolder != null) {
            cmpFolder.recolorImage();
        }
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        return ((PreviewNode)children.getFirst()).getImage().width;
    }
}
