package com.szadowsz.rotom4j.app.component.nitro.narc;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.nds4j.file.nitro.narc.NARC;
import com.szadowsz.rotom4j.app.ProcessingRotom4J;
import com.szadowsz.rotom4j.app.utils.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NarcExtractFolderComponent extends RFolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(NarcExtractFolderComponent.class);

    private static final String EXTRACT = "Extract";
    private static final String COMPRESSION = "Extract with compression";
    private static final String LST_CREATION = "Create .lst";

    private final static String selectNarcFile = "Select NARC File";
    private final static String selectLstFile = "Select lst File";

    private final NarcFolderComponent narcFolder;
    private final NARC narc;

    NarcExtractFolderComponent(RotomGui gui, String path, NarcFolderComponent parent) {
        super(gui, path, parent);
        narcFolder = parent;
        narc = narcFolder.narc;
        initNodes();
    }

    private void initNodes() {
        if (!children.isEmpty()) {
            return;
        }
        RButton extract = new RButton(gui,path + "/" + EXTRACT, this);
        extract.registerAction(RActivateByType.RELEASE, () -> {
            String lastPath = ProcessingRotom4J.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String folderPath = FileChooser.selectFolder(gui.getSketch(), lastPath, selectNarcFile);
            try {
                narc.unpack(folderPath);
            } catch (IOException e) {
                LOGGER.error("Failed to Extract", e);
            }
            gui.getWinManager().uncoverOrCreateWindow(extract.getParentFolder());
        });
        children.add(extract);

        RButton extractCompression = new RButton(gui,path + "/" + COMPRESSION, this);
        extractCompression.registerAction(RActivateByType.RELEASE, () -> {
            String lastPath = ProcessingRotom4J.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String folderPath = FileChooser.selectFolder(gui.getSketch(), lastPath, selectNarcFile);
            try {
                narc.unpackWithCompression(folderPath);
            } catch (IOException e) {
                LOGGER.error("Failed to Extract With Compression", e);
            }
            gui.getWinManager().uncoverOrCreateWindow(extractCompression.getParentFolder());
        });
        children.add(extractCompression);

        RButton createLst = new RButton(gui,path + "/" + LST_CREATION, this);
        createLst.registerAction(RActivateByType.RELEASE, () -> {
            String lastPath = ProcessingRotom4J.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String lstPath = FileChooser.saveLstFile(gui.getSketch(), lastPath, selectLstFile, narc.createLst());
            gui.getWinManager().uncoverOrCreateWindow(createLst.getParentFolder());
        });
        children.add(createLst);
    }
}
