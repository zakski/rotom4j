package com.szadowsz.rotom4j.component.nitro.n2d.narc.options;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.folder.RDropdownMenu;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.rotom4j.file.nitro.n2d.narc.NARC;
import com.szadowsz.rotom4j.app.ProcessingRotom4J;
import com.szadowsz.rotom4j.app.utils.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NARCApplyFolder extends RDropdownMenu {
    private static final Logger LOGGER = LoggerFactory.getLogger(NARCApplyFolder.class);

    private static final String APPLY_H = "Apply H";
    private static final String APPLY_LST = "Apply LST";
    private static final String APPLY_NAIX = "Apply NAIX";
    private static final String APPLY_SCR = "Apply SCR";
    private static final String REINDEX = "Reindex";

    private final static String selectLstFile = "Select lst File";
    private final static String selectHeaderFile = "Select .h File";

    private final NARCOptions narcOptions;
    private final NARC narc;

    public NARCApplyFolder(RotomGui gui, String path, NARCOptions parent) {
        super(gui, path, parent);
        narcOptions = parent;
        narc = narcOptions.getData();
        initNodes();
    }

    private void initNodes() {
        if (!children.isEmpty()) {
            return;
        }
        RButton applyH = new RButton(gui,path + "/" + APPLY_H, this);
        applyH.registerAction(RActivateByType.RELEASE, () -> {
            String lastPath = ProcessingRotom4J.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String defPath = FileChooser.selectDefFile(gui.getSketch(), lastPath, selectHeaderFile);
            try {
                narc.applyDef(defPath);
                narcOptions.getParentFolder().refreshBuffer();
            } catch (IOException e) {
                LOGGER.error("Failed to Apply .h file {}", defPath, e);
            }
            gui.getWinManager().uncoverOrCreateWindow(this);
        });
        children.add(applyH);

        RButton applyLST = new RButton(gui,path + "/" + APPLY_LST, this);
        narcOptions.getParentFolder().refreshBuffer();
        applyLST.registerAction(RActivateByType.RELEASE, () -> {
            String lastPath = ProcessingRotom4J.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String lstPath = FileChooser.selectLstFile(gui.getSketch(), lastPath, selectLstFile);
            try {
                narc.applyLst(lstPath);
            } catch (IOException e) {
                LOGGER.error("Failed to Apply .lst file {}", lstPath, e);
            }
            gui.getWinManager().uncoverOrCreateWindow(this);
        });
        children.add(applyLST);

        RButton applyNaix = new RButton(gui, path + "/" + APPLY_NAIX, this);
        applyNaix.registerAction(RActivateByType.RELEASE, () -> {
            String lastPath = ProcessingRotom4J.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String naixPath = FileChooser.selectNaixFile(gui.getSketch(), lastPath, selectHeaderFile);
            try {
                narc.applyNaix(naixPath);
                narcOptions.getParentFolder().refreshBuffer();
             } catch (IOException e) {
                LOGGER.error("Failed to Apply .naix file {}", naixPath, e);
            }
            gui.getWinManager().uncoverOrCreateWindow(this);
        });
        children.add(applyNaix);

        RButton applyScr = new RButton(gui,path + "/" + APPLY_SCR, this);
        applyScr.registerAction(RActivateByType.RELEASE, () -> {
            String lastPath = ProcessingRotom4J.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String scrPath = FileChooser.selectScrFile(gui.getSketch(), lastPath, selectHeaderFile);
            try {
                narc.applyScr(scrPath);
                narcOptions.getParentFolder().refreshBuffer();
            } catch (IOException e) {
                LOGGER.error("Failed to Apply .scr file {}", scrPath, e);
            }
            gui.getWinManager().uncoverOrCreateWindow(this);
        });
        children.add(applyScr);
    }
}
