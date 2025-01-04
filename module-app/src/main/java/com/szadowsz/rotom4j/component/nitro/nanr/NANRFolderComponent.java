package com.szadowsz.rotom4j.component.nitro.nanr;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.nanr.NANR;
import com.szadowsz.rotom4j.component.nitro.NitroCmpFolderComponent;
import com.szadowsz.rotom4j.component.nitro.NitroPreview;
import com.szadowsz.rotom4j.component.nitro.ncer.NCERFolderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PImage;

import static com.old.ui.store.LayoutStore.cell;


public class NANRFolderComponent extends NitroCmpFolderComponent<NANR> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NANRFolderComponent.class);
    private static final String SELECT_NANR_FILE = "Select NANR";

    protected static final String CELL_NODE_NAME = "cell";


    public NANRFolderComponent(RotomGui gui, String path, RFolder parent, NANR nanr) throws NitroException {
        super(gui, path, parent, nanr,SELECT_NANR_FILE);
         children.clear();
         children.add(new NitroPreview(gui,path + "/" + PREVIEW_NODE, this,nanr));

        children.add(createZoom());

        children.add(new NCERFolderComponent(gui, path + "/" + CELL_NODE_NAME, this,imageable.getNCER()));
    }

    public void recolorImage() throws NitroException {
        imageable.getNCGR().recolorImage();

        PImage pImage = resizeImage(imageable.getImage());
        ((NitroPreview) findChildByName(PREVIEW_NODE)).loadImage(pImage);

        this.window.resizeForContents(true);
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        float suggested = super.autosuggestWindowWidthForContents();
        if (imageable.getNCGR() != null) {
            return Math.max(suggested,((NitroPreview) children.get(0)).getImage().width);
        } else {
            return suggested;
        }
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        drawTextLeft(pg, name);
        drawBackdropRight(pg, cell);
   }
}
