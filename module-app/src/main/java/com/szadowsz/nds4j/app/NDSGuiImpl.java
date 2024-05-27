package com.szadowsz.nds4j.app;

import com.szadowsz.nds4j.app.nodes.bin.evo.EvoFolderNode;
import com.szadowsz.nds4j.app.nodes.ncgr.NCGRFolderNode;
import com.szadowsz.nds4j.app.nodes.nclr.NCLRFolderNode;
import com.szadowsz.nds4j.app.nodes.nscr.NSCRFolderNode;
import com.szadowsz.nds4j.app.utils.FileChooser;
import com.szadowsz.nds4j.file.bin.EvolutionNFSFile;
import com.szadowsz.nds4j.file.nitro.NCGR;
import com.szadowsz.nds4j.file.nitro.NCLR;
import com.szadowsz.nds4j.file.nitro.NSCR;
import com.szadowsz.nds4j.file.nitro.Narc;
import com.szadowsz.ui.NDSGui;
import com.szadowsz.ui.NDSGuiSettings;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.NodeTree;
import com.szadowsz.ui.node.impl.ButtonNode;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.node.impl.RadioFolderNode;
import com.szadowsz.ui.node.impl.TextNode;
import com.szadowsz.ui.window.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.szadowsz.ui.node.NodeTree.*;

public class NDSGuiImpl extends NDSGui {
    final static Logger LOGGER = LoggerFactory.getLogger(NDSGuiImpl.class);
    final static String selectNarcFile = "Select Narc File";
    final static String selectLstFile = "Select lst File";
    final static String selectHeaderFile = "Select .h File";

    public NDSGuiImpl(PApplet sketch, NDSGuiSettings settings) {
        super(sketch, settings);
    }

    public NSCRFolderNode scrRes(String path, NSCR nscr) {
        String fullPath = getFolder() + path;
        if(isPathTakenByUnexpectedType(fullPath, NCGRFolderNode.class)){
            return null;//defaultOption == null ? options[0] : defaultOption;
        }
        NSCRFolderNode node = (NSCRFolderNode) findNode(fullPath);
        if (node == null) {
            FolderNode parentFolder = NodeTree.findParentFolderLazyInitPath(fullPath);
            node = new NSCRFolderNode(fullPath, parentFolder, nscr);
            insertNodeAtItsPath(node);
        }
        return node;
    }

    public NCGRFolderNode image(String path, NCGR ncgr) {
        String fullPath = getFolder() + path;
        if(isPathTakenByUnexpectedType(fullPath, NCGRFolderNode.class)){
            return null;//defaultOption == null ? options[0] : defaultOption;
        }
        NCGRFolderNode node = (NCGRFolderNode) findNode(fullPath);
        if (node == null) {
            FolderNode parentFolder = NodeTree.findParentFolderLazyInitPath(fullPath);
            node = new NCGRFolderNode(fullPath, parentFolder, ncgr);
            insertNodeAtItsPath(node);
        }
        return node;
    }
    public NCLRFolderNode palette(NCLR nclr) {
        String fullPath = getFolder() + nclr.getFileName();
        if(isPathTakenByUnexpectedType(fullPath, NCLRFolderNode.class)){
            return null;
        }
        NCLRFolderNode node = (NCLRFolderNode) findNode(fullPath);
        if (node == null) {
            FolderNode folder = NodeTree.findParentFolderLazyInitPath(fullPath);
            node = new NCLRFolderNode(fullPath, folder, nclr);
            insertNodeAtItsPath(node);
        }
        return node;
    }

    public EvoFolderNode evolution(EvolutionNFSFile evo) {
        String fullPath = getFolder() + evo.getFileName();
        if(isPathTakenByUnexpectedType(fullPath, EvoFolderNode.class)){
            return null;
        }
        EvoFolderNode node = (EvoFolderNode) findNode(fullPath);
        if (node == null) {
            FolderNode folder = NodeTree.findParentFolderLazyInitPath(fullPath);
            node = new EvoFolderNode(fullPath, folder, evo);
            insertNodeAtItsPath(node);
        }
        return node;
    }

    private List<AbstractNode> registerNarcApplyNodes(Narc narc) {
        List<AbstractNode> results = new ArrayList<>();
        pushFolder("Apply")/*)*/;
        results.add(button("Apply .h"));
        results.add(button("Apply .lst"));
        results.add(button("Apply .naix"));
        results.add(button("Apply .scr"));
        popFolder();
        pushFolder("Reindex");
        results.add(text("name"));
        results.add(button("Reindex"));
        popFolder();
        return results;
    }

    private List<AbstractNode> registerExtractionButtons(Narc narc) {
        List<AbstractNode> results = new ArrayList<>();
        pushFolder("Extract")/*)*/;

        ButtonNode extract = button("Extract");
        extract.registerAction(ActivateByType.RELEASE, () -> {
            String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String folderPath = FileChooser.selectFolder(getGuiCanvas().parent, lastPath, selectNarcFile);
            try {
                narc.unpack(folderPath);
            } catch (IOException e) {
                LOGGER.error("Failed to Extract",e);
            }
            WindowManager.uncoverOrCreateWindow(extract.parent);
        });
        results.add(extract);

        ButtonNode extractCompression = button("Extract with compression");
        extractCompression.registerAction(ActivateByType.RELEASE, () -> {
            String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String folderPath = FileChooser.selectFolder(getGuiCanvas().parent, lastPath, selectNarcFile);
            try {
                narc.unpackWithCompression(folderPath);
            } catch (IOException e) {
                LOGGER.error("Failed to Extract With Compression",e);
            }
            WindowManager.uncoverOrCreateWindow(extractCompression.parent);
        });
        results.add(extractCompression);

        ButtonNode createLst = button("Create .lst");
        createLst.registerAction(ActivateByType.RELEASE, () -> {
            String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String lstPath = FileChooser.saveLstFile(getGuiCanvas().parent, lastPath, selectLstFile, narc.createLst());
            WindowManager.uncoverOrCreateWindow(createLst.parent);
        });
        results.add(createLst);

        popFolder();

        return results;
    }

    private void registerActions(Narc narc, List<AbstractNode> applyNodes,  List<AbstractNode> extractNodes) {
        List<String> narcFilenames = narc.getFilenames();
        RadioFolderNode filesRadio = radio("files", narcFilenames);
        ((ButtonNode)applyNodes.get(0)).registerAction(ActivateByType.RELEASE, () -> {
            String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String defPath = FileChooser.selectDefFile(getGuiCanvas().parent, lastPath, selectHeaderFile);
            try {
                narc.applyDef(defPath);
                filesRadio.setOptions(narc.getFilenames().toArray(new String[0]), narc.getFilenames().get(0));
            } catch (IOException e) {
                LOGGER.error("Failed to Apply .h file " + defPath,e);
            }
            WindowManager.uncoverOrCreateWindow(applyNodes.get(0).parent);
        });
        ((ButtonNode)applyNodes.get(1)).registerAction(ActivateByType.RELEASE, () -> {
            String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String lstPath = FileChooser.selectLstFile(getGuiCanvas().parent, lastPath, selectLstFile);
            try {
                narc.applyLst(lstPath);
                filesRadio.setOptions(narc.getFilenames().toArray(new String[0]), narc.getFilenames().get(0));
            } catch (IOException e) {
                LOGGER.error("Failed to Apply .lst file " + lstPath,e);
            }
            WindowManager.uncoverOrCreateWindow(applyNodes.get(1).parent);
        });
        ((ButtonNode)applyNodes.get(2)).registerAction(ActivateByType.RELEASE, () -> {
            String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String naixPath = FileChooser.selectNaixFile(getGuiCanvas().parent, lastPath, selectHeaderFile);
            try {
                narc.applyNaix(naixPath);
                filesRadio.setOptions(narc.getFilenames().toArray(new String[0]), narc.getFilenames().get(0));
            } catch (IOException e) {
                LOGGER.error("Failed to Apply .naix file " + naixPath,e);
            }
            WindowManager.uncoverOrCreateWindow(applyNodes.get(2).parent);
        });
        ((ButtonNode)applyNodes.get(3)).registerAction(ActivateByType.RELEASE, () -> {
            String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
            String scrPath = FileChooser.selectScrFile(getGuiCanvas().parent, lastPath, selectHeaderFile);
            try {
                narc.applyScr(scrPath);
                filesRadio.setOptions(narc.getFilenames().toArray(new String[0]), narc.getFilenames().get(0));
            } catch (IOException e) {
                LOGGER.error("Failed to Apply .scr file " + scrPath,e);
            }
            WindowManager.uncoverOrCreateWindow(applyNodes.get(3).parent);
        });
        ((ButtonNode)applyNodes.get(5)).registerAction(ActivateByType.RELEASE, () -> {
            TextNode text = ((TextNode)applyNodes.get(4));
            String reindexValue = text.getValueAsString();
            text.setStringValue("");
            narc.reindex(reindexValue);
            WindowManager.uncoverOrCreateWindow(text.parent);
            filesRadio.setOptions(narc.getFilenames().toArray(new String[0]), narc.getFilenames().get(0));
        });
    }

    public void registerNarcGUI(Narc narc) {
        LOGGER.info("Creating GUI for Narc File: " + narc.getFileName());
        pushFolder(narc.getFileName())/*)*/;

        List<AbstractNode> applyNodes = registerNarcApplyNodes(narc);
        List<AbstractNode> extractNodes = registerExtractionButtons(narc);

        registerActions(narc, applyNodes,extractNodes);
        popFolder();
        LOGGER.info("Created GUI for Narc File: " + narc.getFileName());
    }

    public NSCRFolderNode registerNscrGUI(NSCR nscr) {
        LOGGER.info("Creating GUI for NSCR File: " + nscr.getFileName());
        NSCRFolderNode imageFolderNode = scrRes(nscr.getFileName(), nscr);
        LOGGER.info("Created GUI for NSCR File: " + nscr.getFileName());
        return imageFolderNode;
    }

    public NCGRFolderNode registerNcgrGUI(NCGR ncgr) {
        LOGGER.info("Creating GUI for NCGR File: " + ncgr.getFileName());
        NCGRFolderNode imageFolderNode = image(ncgr.getFileName(), ncgr);
        LOGGER.info("Created GUI for NCGR File: " + ncgr.getFileName());
        return imageFolderNode;
    }

    public NCLRFolderNode registerNclrGUI(NCLR nclr) {
        LOGGER.info("Creating GUI for NCLR File: " + nclr.getFileName());
        NCLRFolderNode paletteFolderNode = palette(nclr);
        LOGGER.info("Created GUI for NCLR File: " + nclr.getFileName());
        return paletteFolderNode;
    }

    public EvoFolderNode registerEvoGUI(EvolutionNFSFile evo) {
        LOGGER.info("Creating GUI for Evolution Bin File: " + evo.getFileName());
        EvoFolderNode paletteFolderNode = evolution(evo);
        LOGGER.info("Created GUI for Evolution Bin File: " + evo.getFileName());
        return paletteFolderNode;
    }
}
