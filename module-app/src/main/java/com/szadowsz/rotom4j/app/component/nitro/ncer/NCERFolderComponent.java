package com.szadowsz.rotom4j.app.component.nitro.ncer;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.component.input.slider.RSliderInt;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.ncer.NCER;
import com.szadowsz.rotom4j.app.RotomGuiImpl;
import com.szadowsz.rotom4j.app.component.nitro.NitroCmpFolderComponent;
import com.szadowsz.rotom4j.app.component.nitro.NitroPreview;
import com.szadowsz.rotom4j.app.component.nitro.ncgr.NCGRFolderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;

public class NCERFolderComponent extends NitroCmpFolderComponent<NCER> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NCERFolderComponent.class);

    private final String CELL_NODE = "Cell";

    public NCERFolderComponent(RotomGui gui, String path, RFolder parent, NCER ncer) throws NitroException {
        super(gui, path, parent, ncer, SELECT_NCER_FILE);
        children.clear();
        children.add(new NitroPreview(gui, path + "/" + PREVIEW_NODE, this, ncer));
        RSliderInt cell = new RSliderInt(gui, path + "/" + CELL_NODE, this, 0, 0, ncer.getCellsCount() - 1, true) {
            @Override
            protected void onValueChange() {
                super.onValueChange();
                try {
                    recolorImage();
                } catch (NitroException e) {

                }
            }
        };
        children.add(cell);

        children.add(createZoom());
        children.add(new NCGRFolderComponent(gui, path + "/" + IMAGE_NODE_NAME, this, imageable.getNCGR()));
    }

    public void recolorImage() throws NitroException {
        super.recolorImage();
        RSliderInt cellNode = (RSliderInt) findChildByName(CELL_NODE);

        PImage pImage = resizeImage(imageable.getImage((int) cellNode.getValueAsInt()));
        ((NitroPreview) findChildByName(imageable.getFileName())).loadImage(pImage);

        this.window.resizeForContents(true);
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        float suggested = super.autosuggestWindowWidthForContents();
        if (imageable.getNCGR() != null) {
            return Math.max(suggested, ((NitroPreview) children.get(0)).getImage().width);
        } else {
            return suggested;
        }
    }
}
