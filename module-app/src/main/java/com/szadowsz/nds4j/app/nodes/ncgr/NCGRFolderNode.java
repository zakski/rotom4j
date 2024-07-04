package com.szadowsz.nds4j.app.nodes.ncgr;

import com.szadowsz.nds4j.app.Processing;
import com.szadowsz.nds4j.app.nodes.nclr.NCLRFolderNode;
import com.szadowsz.nds4j.app.utils.FileChooser;
import com.szadowsz.nds4j.app.utils.ImageUtils;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.NCGR;
import com.szadowsz.nds4j.file.nitro.NCLR;
import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.node.impl.ButtonNode;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.node.impl.SliderNode;
import com.szadowsz.ui.store.JsonSaveStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PImage;

import java.io.File;
import java.io.IOException;

import static com.szadowsz.ui.store.LayoutStore.cell;


public class NCGRFolderNode extends FolderNode {

    private final String PALETTE_NODE_NAME = "palette";
    private final String ZOOM_NODE = "Zoom";
    private final String SELECT_NCLR_FILE = "Select NClR";
    protected static final Logger LOGGER = LoggerFactory.getLogger(NCGRFolderNode.class);

    private NCGR ncgr;

    public NCGRFolderNode(String path, FolderNode parent, NCGR ncgr) {
        super(path, parent);
        setImage(ncgr);
        JsonSaveStore.overwriteWithLoadedStateIfAny(this);
    }

    public void setImage(NCGR ncgr) {
        if (ncgr == null) {
            return;
        }
        this.ncgr = ncgr;
        children.clear();
        children.add(new NCGRPreviewNode(path + "/" + ncgr.getFileName(), this,ncgr));
        SliderNode zoom = new SliderNode(path + "/" + ZOOM_NODE, this, 1.0f, 1.0f, 4.0f, true){
            @Override
            protected void onValueFloatChanged() {
                super.onValueFloatChanged();
                try {
                    recolorImage();
                } catch (NitroException e) {
                    throw new RuntimeException(e);
                }
            }

        };
        zoom.increasePrecision();
        children.add(zoom);

        ButtonNode selectNcLr = new ButtonNode(path + "/" + SELECT_NCLR_FILE,this);
        selectNcLr.registerAction(ActivateByType.RELEASE, this::selectNclr);
        children.add(selectNcLr);
        children.add(new NCLRFolderNode(path + "/" + PALETTE_NODE_NAME, this,ncgr.getNCLR()));
    }

    public void recolorImage() throws NitroException {
        ((NCLRFolderNode)findChildByName(PALETTE_NODE_NAME)).setPalette(ncgr.getNCLR());
        ncgr.recolorImage();

        PImage pImage = ImageUtils.convertToPImage(ncgr.getImage());
        float zoom = ((SliderNode) findChildByName(ZOOM_NODE)).valueFloat;
        pImage.resize(Math.round(pImage.width*zoom),0);

        ((NCGRPreviewNode) findChildByName(ncgr.getFileName())).loadImage(pImage);
        this.window.windowSizeX = autosuggestWindowWidthForContents();
        this.window.windowSizeXForContents = autosuggestWindowWidthForContents();
    }
    private void selectNclr() {
        String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
        String nclrPath = FileChooser.selectNclrFile(GlobalReferences.gui.getGuiCanvas().parent, lastPath,SELECT_NCLR_FILE);
        if (nclrPath != null) {
            Processing.prefs.put("openNarcPath", new File(nclrPath).getParentFile().getAbsolutePath());
            try {
                LOGGER.info("Loading NCLR File: " + nclrPath);
                ncgr.setPalette(NCLR.fromFile(nclrPath));
                recolorImage();
                LOGGER.info("Loaded NCLR File: " + nclrPath);
            } catch (IOException e) {
                LOGGER.error("NCLR Load Failed",e);
            }
        }
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        return ((NCGRPreviewNode)children.get(0)).image.width;
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        drawLeftText(pg, name);
        drawRightBackdrop(pg, cell);
     //       drawRightTextToNotOverflowLeftText(pg, getValueAsString(), name, true); //we need to calculate how much space is left for value after the name is displayed
    }
}
