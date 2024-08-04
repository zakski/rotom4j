package com.szadowsz.nds4j.app.nodes.nitro.narc;

import com.szadowsz.nds4j.app.Processing;
import com.szadowsz.nds4j.app.utils.FileChooser;
import com.szadowsz.nds4j.file.nitro.narc.NARC;
import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.impl.ButtonNode;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.window.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NarcExtractFolderNode extends FolderNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(NarcExtractFolderNode.class);

    private static final String EXTRACT = "Extract";
    private static final String COMPRESSION = "Extract with compression";
    private static final String LST_CREATION = "Create .lst";

    private final static String selectNarcFile = "Select NARC File";
    private final static String selectLstFile = "Select lst File";

    private final NarcFolderNode narcFolder;
    private final NARC narc;

    NarcExtractFolderNode(String path, NarcFolderNode parent) {
        super(path, parent);
        narcFolder = parent;
        narc = narcFolder.narc;
        initNodes();
    }

    private void initNodes() {
        if (!children.isEmpty()) {
            return;
        }
        ButtonNode extract = new ButtonNode(path + "/" + EXTRACT, this);
        extract.registerAction(ActivateByType.RELEASE, () -> {
            String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String folderPath = FileChooser.selectFolder(GlobalReferences.app, lastPath, selectNarcFile);
            try {
                narc.unpack(folderPath);
            } catch (IOException e) {
                LOGGER.error("Failed to Extract", e);
            }
            WindowManager.uncoverOrCreateWindow(extract.parent);
        });
        children.add(extract);

        ButtonNode extractCompression = new ButtonNode(path + "/" + COMPRESSION, this);
        extractCompression.registerAction(ActivateByType.RELEASE, () -> {
            String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String folderPath = FileChooser.selectFolder(GlobalReferences.app, lastPath, selectNarcFile);
            try {
                narc.unpackWithCompression(folderPath);
            } catch (IOException e) {
                LOGGER.error("Failed to Extract With Compression", e);
            }
            WindowManager.uncoverOrCreateWindow(extractCompression.parent);
        });
        children.add(extractCompression);

        ButtonNode createLst = new ButtonNode(path + "/" + LST_CREATION, this);
        createLst.registerAction(ActivateByType.RELEASE, () -> {
            String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String lstPath = FileChooser.saveLstFile(GlobalReferences.app, lastPath, selectLstFile, narc.createLst());
            WindowManager.uncoverOrCreateWindow(createLst.parent);
        });
        children.add(createLst);
    }
}
