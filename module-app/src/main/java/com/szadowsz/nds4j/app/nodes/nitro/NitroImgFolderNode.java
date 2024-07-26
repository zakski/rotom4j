package com.szadowsz.nds4j.app.nodes.nitro;

import com.szadowsz.nds4j.app.Processing;
import com.szadowsz.nds4j.app.utils.FileChooser;
import com.szadowsz.nds4j.app.utils.ImageUtils;
import com.szadowsz.nds4j.file.Imageable;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.ImageableWithPalette;
import com.szadowsz.nds4j.file.nitro.nclr.NCLR;
import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.node.impl.SliderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.szadowsz.ui.store.LayoutStore.cell;

public abstract class NitroImgFolderNode<I extends ImageableWithPalette> extends NitroFolderNode<I> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NitroImgFolderNode.class);

    protected static final String SELECT_NCGR_FILE = "Select NCGR";

    public NitroImgFolderNode(String path, String selectName, FolderNode parent, LayoutType layout, I imageable) {
        super(path, parent, layout, imageable, selectName);
    }

    protected void selectPalette() {
        String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
        String nclrPath = FileChooser.selectNclrFile(GlobalReferences.gui.getGuiCanvas().parent, lastPath,SELECT_NCLR_FILE);
        if (nclrPath != null) {
            Processing.prefs.put("openNarcPath", new File(nclrPath).getParentFile().getAbsolutePath());
            NCLR original = imageable.getNCLR();
            try {
                LOGGER.debug("Loading NCLR File: " + nclrPath);
                imageable.setNCLR(NCLR.fromFile(nclrPath));
                LOGGER.info("Loaded NCLR File: " + nclrPath);
            } catch (IOException e) {
                LOGGER.error("NCLR Load Failed",e);
                try {
                    imageable.setNCLR(original);
                } catch (Throwable t){
                    throw new RuntimeException(t);
                }
            }
        }
    }
}
