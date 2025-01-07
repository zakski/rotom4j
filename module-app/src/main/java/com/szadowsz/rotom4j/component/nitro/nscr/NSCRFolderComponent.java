package com.szadowsz.rotom4j.component.nitro.nscr;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.nscr.NSCR;
import com.szadowsz.rotom4j.component.nitro.NitroCmpFolderComponent;
import com.szadowsz.rotom4j.component.nitro.NitroPreview;
import com.szadowsz.rotom4j.component.nitro.ncgr.NCGRFolderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;


public class NSCRFolderComponent extends NitroCmpFolderComponent<NSCR> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NSCRFolderComponent.class);

    public NSCRFolderComponent(RotomGui gui, String path, RFolder parent, NSCR nscr) {
        super(gui, path, parent, nscr, SELECT_NSCR_FILE);
        children.clear();
        children.add(new NitroPreview(gui, path + "/" + drawable.getFileName(), this, drawable));

        children.add(createZoom());

        children.add(new NCGRFolderComponent(gui, path + "/" + IMAGE_NODE_NAME, this, drawable.getNCGR()));
    }

    @Override
    public void recolorImage() throws NitroException {
        super.recolorImage();
        drawable.recolorImage();

        PImage pImage = resizeImage(drawable.getImage());
        ((NitroPreview) findChildByName(drawable.getFileName())).loadImage(pImage);

        this.window.resizeForContents(true);
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        if (drawable.getNCGR() != null) {
            return ((NitroPreview) children.get(0)).getImage().width;
        } else {
            return drawable.getWidth();
        }
    }
}
