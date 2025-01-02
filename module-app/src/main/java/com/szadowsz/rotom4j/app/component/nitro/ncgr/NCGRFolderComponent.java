package com.szadowsz.rotom4j.app.component.nitro.ncgr;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;
import com.szadowsz.rotom4j.app.ProcessingRotom4J;
import com.szadowsz.rotom4j.app.component.nitro.NitroCmpFolderComponent;
import com.szadowsz.rotom4j.app.component.nitro.NitroImgFolderComponent;
import com.szadowsz.rotom4j.app.component.nitro.NitroPreview;
import com.szadowsz.rotom4j.app.component.nitro.nclr.NCLRFolderComponent;
import com.szadowsz.rotom4j.app.utils.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;

import java.io.File;
import java.io.IOException;

public class NCGRFolderComponent extends NitroImgFolderComponent<NCGR> {

    private static final String PALETTE_NODE_NAME = "palette";
    protected static final Logger LOGGER = LoggerFactory.getLogger(NCGRFolderComponent.class);

    private NitroCmpFolderComponent<?> cmpFolder;

    public NCGRFolderComponent(RotomGui gui, String path, RGroup parentFolder, NCGR ncgr) {
        super(gui, path, parentFolder, ncgr, SELECT_NCGR_FILE);
        if (parentFolder instanceof NitroCmpFolderComponent<?>) {
            LOGGER.debug("Attached to parent NCGR");
            cmpFolder = (NitroCmpFolderComponent<?>) parentFolder;
        }
        initNode();
    }

    protected void initNode() {
        if (imageable == null) {
            return;
        }
        children.clear();
        children.add(new NitroPreview(gui, path + "/" + PREVIEW_NODE, this, imageable));
        children.add(createZoom());
        children.add(new NCLRFolderComponent(gui, path + "/" + PALETTE_NODE_NAME, this, imageable.getNCLR()));
        if (cmpFolder != null) {
            RButton reset = new RButton(gui, path + "/" + RESET_NODE, this);
            reset.registerAction(RActivateByType.RELEASE, this::resetImage);
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
        String lastPath = ProcessingRotom4J.prefs.get("openNarcPath", System.getProperty("user.dir"));
        String ncgrPath = FileChooser.selectNcgrFile(gui.getSketch(), lastPath, SELECT_NCGR_FILE);
        if (ncgrPath != null) {
            ProcessingRotom4J.prefs.put("openNarcPath", new File(ncgrPath).getParentFile().getAbsolutePath());
            try {
                LOGGER.debug("Loading NCGR File: " + ncgrPath);
                imageable = NCGR.fromFile(ncgrPath);
                initNode();
                recolorImage();
                LOGGER.info("Loaded NCGR File: " + ncgrPath);
            } catch (IOException e) {
                LOGGER.error("NCGR Load Failed", e);
            }
        }
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (shouldDisplayName()) {
            super.mousePressed(mouseEvent, adjustedMouseY);
        } else {
            selectNcgr();
        }
    }

    @Override
    public void recolorImage() throws NitroException {
        imageable.setNCLR(((NCLRFolderComponent) findChildByName(PALETTE_NODE_NAME)).getImageable());
        imageable.recolorImage();

        PImage pImage = resizeImage(imageable.getImage());

        ((NitroPreview) findChildByName(imageable.getFileName())).loadImage(pImage);

        if (this.window != null) {
            this.window.resizeForContents(true);
        }
        if (cmpFolder != null) {
            cmpFolder.recolorImage();
        }
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        return ((NitroPreview) children.getFirst()).getImage().width;
    }
}
