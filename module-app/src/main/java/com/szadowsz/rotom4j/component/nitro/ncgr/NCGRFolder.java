package com.szadowsz.rotom4j.component.nitro.ncgr;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.drawable.tab.RTab;
import com.szadowsz.gui.component.group.drawable.tab.RTabFunction;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.rotom4j.app.ProcessingRotom4J;
import com.szadowsz.rotom4j.app.utils.FileChooser;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.R4JFolder;
import com.szadowsz.rotom4j.component.R4JResourceFolder;
import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class NCGRFolder extends R4JFolder<NCGR> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NCGRFolder.class);

    private R4JResourceFolder<?> cmpFolder;

    /**
     * Default Constructor
     *
     * @param gui        the gui for the window that the component is drawn under
     * @param path       the path in the component tree
     * @param parent     the parent component reference
     * @param ncgr
     */
    public NCGRFolder(RotomGui gui, String path, RGroup parent, NCGR ncgr) {
        super(gui, path, parent,ncgr,SELECT_NCGR_FILE);
        if (parent instanceof R4JResourceFolder<?>){
            cmpFolder = (R4JResourceFolder<?>)parent;
        }
    }

    @Override
    protected RTabFunction<R4JComponent<NCGR>> createDisplay() {
        return (RTab tab) -> new NCGRComponent(gui, tab.getPath() + "/Image" ,tab, data);
    }

    protected void selectNcgr() {
        String lastPath = ProcessingRotom4J.prefs.get("openNarcPath", System.getProperty("user.dir"));
        String ncgrPath = FileChooser.selectNcgrFile(gui.getSketch(), lastPath, SELECT_NCGR_FILE);
        if (ncgrPath != null) {
            ProcessingRotom4J.prefs.put("openNarcPath", new File(ncgrPath).getParentFile().getAbsolutePath());
            try {
                LOGGER.debug("Loading NCGR File: " + ncgrPath);
                data = NCGR.fromFile(ncgrPath);
                if (data != null) {
                    createTabs();
                };
                display.recolorImage();
                LOGGER.info("Loaded NCGR File: " + ncgrPath);
                if (cmpFolder != null){
                    cmpFolder.recolorImage();
                }
            } catch (IOException e) {
                LOGGER.error("NCGR Load Failed", e);
            }
        }
    }

//    NitroCmpFolder<?> getCmpFolder() {
//        return cmpFolder;
//    }

    R4JResourceFolder<?> getCmpFolder() {
        return cmpFolder;
    }

    public void setDisplay(NCGRComponent ncgrComponent) {
        this.display = ncgrComponent;
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (shouldDisplayName()) {
            super.mousePressed(mouseEvent, adjustedMouseY);
        } else {
            selectNcgr();
        }
    }
}
