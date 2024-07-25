package com.szadowsz.nds4j.app.nodes.nitro;

import com.szadowsz.nds4j.app.Processing;
import com.szadowsz.nds4j.app.utils.FileChooser;
import com.szadowsz.nds4j.file.ImageableWithGraphic;
import com.szadowsz.nds4j.file.nitro.ncgr.NCGR;
import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.impl.FolderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public abstract class NitroCmpFolderNode extends NitroImgFolderNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(NitroCmpFolderNode.class);

    protected ImageableWithGraphic complex;

    public NitroCmpFolderNode(String path, FolderNode parent, LayoutType layout, ImageableWithGraphic imageable) {
        super(path, parent, layout, imageable);
        this.complex = imageable;
    }

    protected void selectNcgr() {
        String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
        String ncgrPath = FileChooser.selectNcgrFile(GlobalReferences.gui.getGuiCanvas().parent, lastPath,SELECT_NCGR_FILE);
        if (ncgrPath != null) {
            Processing.prefs.put("openNarcPath", new File(ncgrPath).getParentFile().getAbsolutePath());
            try {
                LOGGER.debug("Loading NCGR File: " + ncgrPath);
                complex.setNCGR(NCGR.fromFile(ncgrPath));
                recolorImage();
                LOGGER.info("Loaded NCGR File: " + ncgrPath);
            } catch (IOException e) {
                LOGGER.error("NCGR Load Failed",e);
            }
        }
    }


}
