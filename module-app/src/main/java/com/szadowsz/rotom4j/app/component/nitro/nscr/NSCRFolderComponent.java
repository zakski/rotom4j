package com.szadowsz.rotom4j.app.component.nitro.nscr;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.nscr.NSCR;
import com.szadowsz.rotom4j.app.RotomGuiImpl;
import com.szadowsz.rotom4j.app.component.nitro.NitroCmpFolderComponent;
import com.szadowsz.rotom4j.app.component.nitro.NitroPreview;
import com.szadowsz.rotom4j.app.component.nitro.ncgr.NCGRFolderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;


public class NSCRFolderComponent extends NitroCmpFolderComponent<NSCR> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NSCRFolderComponent.class);

    public NSCRFolderComponent(RotomGui gui, String path, RFolder parent, NSCR nscr) {
        super(gui, path, parent, nscr, SELECT_NSCR_FILE);
        children.clear();
        children.add(new NitroPreview(gui, path + "/" + imageable.getFileName(), this, imageable));

        children.add(createZoom());

        children.add(new NCGRFolderComponent(gui, path + "/" + IMAGE_NODE_NAME, this, imageable.getNCGR()));
    }

    @Override
    public void recolorImage() throws NitroException {
        super.recolorImage();
        imageable.recolorImage();

        PImage pImage = resizeImage(imageable.getImage());
        ((NitroPreview) findChildByName(imageable.getFileName())).loadImage(pImage);

        this.window.resizeForContents(true);
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        if (imageable.getNCGR() != null) {
            return ((NitroPreview) children.get(0)).getImage().width;
        } else {
            return imageable.getWidth();
        }
    }
}
