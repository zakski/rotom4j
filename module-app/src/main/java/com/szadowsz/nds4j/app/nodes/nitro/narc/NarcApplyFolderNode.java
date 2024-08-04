package com.szadowsz.nds4j.app.nodes.nitro.narc;

import com.szadowsz.nds4j.app.Processing;
import com.szadowsz.nds4j.app.utils.FileChooser;
import com.szadowsz.nds4j.file.nitro.narc.NARC;
import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.node.impl.ButtonNode;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.node.impl.TextNode;
import com.szadowsz.ui.window.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NarcApplyFolderNode extends FolderNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(NarcApplyFolderNode.class);

    private static final String APPLY_H = "Apply H";
    private static final String APPLY_LST = "Apply LST";
    private static final String APPLY_NAIX = "Apply NAIX";
    private static final String APPLY_SCR = "Apply SCR";
    private static final String REINDEX = "Reindex";

    private final static String selectLstFile = "Select lst File";
    private final static String selectHeaderFile = "Select .h File";

    private final NarcFolderNode narcFolder;
    private final NARC narc;

    NarcApplyFolderNode(String path, NarcFolderNode parent) {
        super(path, parent);
        narcFolder = parent;
        narc = narcFolder.narc;
        initNodes();
    }

    private void initNodes() {
        if (!children.isEmpty()) {
            return;
        }
        ButtonNode applyH = new ButtonNode(path + "/" + APPLY_H, this);
        applyH.registerAction(ActivateByType.RELEASE, () -> {
            String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String defPath = FileChooser.selectDefFile(GlobalReferences.app, lastPath, selectHeaderFile);
            try {
                narc.applyDef(defPath);
                //filesRadio.setOptions(narc.getFilenames().toArray(new String[0]), narc.getFilenames().getFirst());
            } catch (IOException e) {
                LOGGER.error("Failed to Apply .h file {}", defPath, e);
            }
            WindowManager.uncoverOrCreateWindow(this);
        });
        children.add(applyH);

        ButtonNode applyLST = new ButtonNode(path + "/" + APPLY_LST, this);
        applyLST.registerAction(ActivateByType.RELEASE, () -> {
            String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String lstPath = FileChooser.selectLstFile(GlobalReferences.app, lastPath, selectLstFile);
            try {
                narc.applyLst(lstPath);
                //filesRadio.setOptions(narc.getFilenames().toArray(new String[0]), narc.getFilenames().getFirst());
            } catch (IOException e) {
                LOGGER.error("Failed to Apply .lst file {}", lstPath, e);
            }
            WindowManager.uncoverOrCreateWindow(this);
        });
        children.add(applyLST);

        ButtonNode applyNaix = new ButtonNode(path + "/" + APPLY_NAIX, this);
        applyNaix.registerAction(ActivateByType.RELEASE, () -> {
            String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String naixPath = FileChooser.selectNaixFile(GlobalReferences.app, lastPath, selectHeaderFile);
            try {
                narc.applyNaix(naixPath);
                //filesRadio.setOptions(narc.getFilenames().toArray(new String[0]), narc.getFilenames().getFirst());
            } catch (IOException e) {
                LOGGER.error("Failed to Apply .naix file {}", naixPath, e);
            }
            WindowManager.uncoverOrCreateWindow(this);
        });
        children.add(applyNaix);

        ButtonNode applyScr = new ButtonNode(path + "/" + APPLY_SCR, this);
        applyScr.registerAction(ActivateByType.RELEASE, () -> {
            String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String scrPath = FileChooser.selectScrFile(GlobalReferences.app, lastPath, selectHeaderFile);
            try {
                narc.applyScr(scrPath);
                //filesRadio.setOptions(narc.getFilenames().toArray(new String[0]), narc.getFilenames().getFirst());
            } catch (IOException e) {
                LOGGER.error("Failed to Apply .scr file {}", scrPath, e);
            }
            WindowManager.uncoverOrCreateWindow(this);
        });
        children.add(applyScr);
    }
}
