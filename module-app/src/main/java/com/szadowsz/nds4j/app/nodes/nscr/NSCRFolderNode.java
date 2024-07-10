package com.szadowsz.nds4j.app.nodes.nscr;

import com.szadowsz.nds4j.app.Processing;
import com.szadowsz.nds4j.app.utils.FileChooser;
import com.szadowsz.nds4j.app.utils.ImageUtils;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.NCGR;
import com.szadowsz.nds4j.file.nitro.NCLR;
import com.szadowsz.nds4j.file.nitro.NSCR;
import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.node.impl.ButtonNode;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.node.impl.SliderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PImage;

import java.io.File;
import java.io.IOException;

import static com.szadowsz.ui.store.LayoutStore.cell;


public class NSCRFolderNode extends FolderNode {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NSCRFolderNode.class);

    private final NSCR nscr;

    private final String ZOOM_NODE = "Zoom";
    private final String SELECT_NCGR_FILE = "Select NCGR";
    private final String SELECT_NCLR_FILE = "Select NClR";

    public NSCRFolderNode(String path, FolderNode parent, NSCR nscr) {
        super(path, parent);
        this.nscr = nscr;
        children.clear();
        children.add(new NSCRPreviewNode(path + "/" + nscr.getFileName(), this,nscr));

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

        ButtonNode selectNcgr = new ButtonNode(path + "/" + SELECT_NCGR_FILE,this);
        selectNcgr.registerAction(ActivateByType.RELEASE, this::selectNcgr);
        children.add(selectNcgr);
        ButtonNode selectNcLr = new ButtonNode(path + "/" + SELECT_NCLR_FILE,this);
        selectNcLr.registerAction(ActivateByType.RELEASE, this::selectNclr);
        children.add(selectNcLr);
    }

    private void selectNcgr() {
        String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
        String ncgrPath = FileChooser.selectNcgrFile(GlobalReferences.gui.getGuiCanvas().parent, lastPath,SELECT_NCGR_FILE);
        if (ncgrPath != null) {
            Processing.prefs.put("openNarcPath", new File(ncgrPath).getParentFile().getAbsolutePath());
           try {
                LOGGER.info("Loading NCGR File: " + ncgrPath);
                nscr.setNCGR(NCGR.fromFile(ncgrPath));
                recolorImage();
                LOGGER.info("Loaded NCGR File: " + ncgrPath);
            } catch (IOException e) {
                LOGGER.error("NCGR Load Failed",e);
            }
        }
    }

    private void selectNclr() {
        String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
        String nclrPath = FileChooser.selectNclrFile(GlobalReferences.gui.getGuiCanvas().parent, lastPath,SELECT_NCLR_FILE);
        if (nclrPath != null) {
            Processing.prefs.put("openNarcPath", new File(nclrPath).getParentFile().getAbsolutePath());
            NCLR original = nscr.getNCGR().getNCLR();
            try {
                LOGGER.info("Loading NCLR File: " + nclrPath);
                nscr.setNCLR(NCLR.fromFile(nclrPath));
                recolorImage();
                LOGGER.info("Loaded NCLR File: " + nclrPath);
            } catch (IOException e) {
                LOGGER.error("NCLR Load Failed",e);
                nscr.setNCLR(original);
                try {
                    recolorImage();
                } catch (Throwable t){
                    throw new RuntimeException(t);
                }
            }
        }
    }

    public void recolorImage() throws NitroException {
        nscr.recolorImage();

        PImage pImage = ImageUtils.convertToPImage(nscr.getImage());
        float zoom = ((SliderNode) findChildByName(ZOOM_NODE)).valueFloat;
        pImage.resize(Math.round(pImage.width*zoom),0);

        ((NSCRPreviewNode) findChildByName(nscr.getFileName())).loadImage(pImage);
        this.window.windowSizeX = autosuggestWindowWidthForContents();
        this.window.windowSizeXForContents = autosuggestWindowWidthForContents();
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        if (nscr.getNCGR() != null) {
            return ((NSCRPreviewNode) children.get(0)).image.width;
        } else {
            return nscr.getWidth();
        }
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        drawLeftText(pg, name);
        drawRightBackdrop(pg, cell);
        //       drawRightTextToNotOverflowLeftText(pg, getValueAsString(), name, true); //we need to calculate how much space is left for value after the name is displayed
    }
}
