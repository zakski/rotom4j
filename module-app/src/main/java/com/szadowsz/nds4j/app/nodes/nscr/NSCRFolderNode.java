package com.szadowsz.nds4j.app.nodes.nscr;

import com.szadowsz.nds4j.app.Processing;
import com.szadowsz.nds4j.app.nodes.util.NitroFolderNode;
import com.szadowsz.nds4j.app.nodes.util.PreviewNode;
import com.szadowsz.nds4j.app.utils.FileChooser;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.NCGR;
import com.szadowsz.nds4j.file.nitro.NSCR;
import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.impl.ButtonNode;
import com.szadowsz.ui.node.impl.FolderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;

import java.io.File;
import java.io.IOException;


public class NSCRFolderNode extends NitroFolderNode {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NSCRFolderNode.class);

    private final NSCR nscr;

    public NSCRFolderNode(String path, FolderNode parent, NSCR nscr) {
        super(path, parent, LayoutType.VERTICAL_1_COL,nscr);
        this.nscr = nscr;
        children.clear();
        children.add(new PreviewNode(path + "/" + nscr.getFileName(), this,nscr));

        children.add(createZoom());

        ButtonNode selectNcgr = new ButtonNode(path + "/" + SELECT_NCGR_FILE,this);
        selectNcgr.registerAction(ActivateByType.RELEASE, this::selectNcgr);
        children.add(selectNcgr);

        ButtonNode selectNcLr = new ButtonNode(path + "/" + SELECT_NCLR_FILE,this);
        selectNcLr.registerAction(ActivateByType.RELEASE, this::selectPalette);
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

    @Override
    public void recolorImage() throws NitroException {
        nscr.recolorImage();

        PImage pImage = resizeImage(nscr.getImage());

        ((PreviewNode) findChildByName(nscr.getFileName())).loadImage(pImage);

        this.window.windowSizeX = autosuggestWindowWidthForContents();
        this.window.windowSizeXForContents = autosuggestWindowWidthForContents();
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        if (nscr.getNCGR() != null) {
            return ((PreviewNode) children.get(0)).getImage().width;
        } else {
            return nscr.getWidth();
        }
    }
}
