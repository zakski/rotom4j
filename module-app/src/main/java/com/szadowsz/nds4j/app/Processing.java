package com.szadowsz.nds4j.app;

import com.szadowsz.nds4j.app.managers.*;
import com.szadowsz.nds4j.app.utils.FileChooser;
import com.szadowsz.nds4j.file.bin.EvolutionNFSFile;
import com.szadowsz.nds4j.file.bin.StatsNFSFile;
import com.szadowsz.nds4j.file.nitro.NCGR;
import com.szadowsz.nds4j.file.nitro.NCLR;
import com.szadowsz.nds4j.file.nitro.NSCR;
import com.szadowsz.nds4j.file.nitro.Narc;
import com.szadowsz.ui.NDSGuiSettings;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.node.impl.ButtonNode;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.window.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PConstants;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import static com.szadowsz.ui.store.LayoutStore.cell;


public class Processing extends PApplet {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Processing.class);

    public final static Preferences prefs = Preferences.userNodeForPackage(Processing.class);

    protected NDSGuiImpl gui;
    protected NDSGuiSettings settings;

    final static String selectNarcFile = "Open Narc File";
    final static String selectNSCRFile = "Open NSCR File";
    final static String selectNCGRFile = "Open NCGR File";
    final static String selectNCLRFile = "Open NClR File";

    final static String selectEvoFile = "Open Evo File";
    final static String selectStatsFile = "Open Personal File";

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Throwable e) {
        }
    }

    @Override
    public void settings() {
        setLookAndFeel();
        size(1920, 1080, PConstants.P2D);
    }

    private void createNarcUI() {
        String lastPath = prefs.get("openNarcPath", System.getProperty("user.dir"));
        String narcPath = FileChooser.selectNarcFile(gui.getGuiCanvas().parent, lastPath,selectNarcFile);
        if (narcPath != null) {
            prefs.put("openNarcPath", new File(narcPath).getParentFile().getAbsolutePath());
            try {
                LOGGER.info("Loading Narc File: " + narcPath);
                NarcManager.getInstance().registerNarc(gui, Narc.fromFile(narcPath));
                LOGGER.info("Loaded Narc File: " + narcPath);
            } catch (IOException e) {
                LOGGER.error("Narc Load Failed",e);
            }
        }
    }
    private void createNscrUI() {
        String lastPath = prefs.get("openNarcPath", System.getProperty("user.dir"));
        String nscrPath = FileChooser.selectNscrFile(gui.getGuiCanvas().parent, lastPath,selectNSCRFile);
        if (nscrPath != null) {
            prefs.put("openNarcPath", new File(nscrPath).getParentFile().getAbsolutePath());
            try {
                LOGGER.info("Loading NSCR File: " + nscrPath);
                NscrManager.getInstance().registerNSCR(gui, NSCR.fromFile(nscrPath));
                LOGGER.info("Loaded NSCR File: " + nscrPath);
            } catch (IOException e) {
                LOGGER.error("NSCR Load Failed",e);
            }
        }
    }

    private void createNcgrUI() {
        String lastPath = prefs.get("openNarcPath", System.getProperty("user.dir"));
        String ncgrPath = FileChooser.selectNcgrFile(gui.getGuiCanvas().parent, lastPath,selectNCGRFile);
        if (ncgrPath != null) {
            prefs.put("openNarcPath", new File(ncgrPath).getParentFile().getAbsolutePath());
            try {
                LOGGER.info("Loading NCGR File: " + ncgrPath);
                NcgrManager.getInstance().registerNCGR(gui, NCGR.fromFile(ncgrPath));
                LOGGER.info("Loaded NCGR File: " + ncgrPath);
            } catch (IOException e) {
                LOGGER.error("NCGR Load Failed",e);
            }
        }
    }

    private void createNclrUI() {
        String lastPath = prefs.get("openNarcPath", System.getProperty("user.dir"));
        String nclrPath = FileChooser.selectNclrFile(gui.getGuiCanvas().parent, lastPath,selectNCLRFile);
        if (nclrPath != null) {
            prefs.put("openNarcPath", new File(nclrPath).getParentFile().getAbsolutePath());
            try {
                LOGGER.info("Loading NCLR File: " + nclrPath);
                NclrManager.getInstance().registerNCLR(gui, NCLR.fromFile(nclrPath));
                LOGGER.info("Loaded NCLR File: " + nclrPath);
            } catch (IOException e) {
                LOGGER.error("NCLR Load Failed",e);
            }
        }
    }
    private void createEvoUI() {
        String lastPath = prefs.get("openNarcPath", System.getProperty("user.dir"));
        String evoPath = FileChooser.selectBinFile(gui.getGuiCanvas().parent, lastPath,selectEvoFile);
        if (evoPath != null) {
            prefs.put("openNarcPath", new File(evoPath).getParentFile().getAbsolutePath());
            try {
                LOGGER.info("Loading Evolution Bin File: " + evoPath);
                EvoFileManager.getInstance().registerEvo(gui, EvolutionNFSFile.fromFile(evoPath));
                LOGGER.info("Loaded Evolution Bin File: " + evoPath);
            } catch (IOException e) {
                LOGGER.error("Evolution Bin Load Failed",e);
            }
        }
    }

    private void createStatsUI() {
        String lastPath = prefs.get("openNarcPath", System.getProperty("user.dir"));
        String statsPath = FileChooser.selectBinFile(gui.getGuiCanvas().parent, lastPath,selectStatsFile);
        if (statsPath != null) {
            prefs.put("openNarcPath", new File(statsPath).getParentFile().getAbsolutePath());
            try {
                LOGGER.info("Loading Personal Bin File: " + statsPath);
                StatsFileManager.getInstance().registerPersonal(gui, StatsNFSFile.fromFile(statsPath));
                LOGGER.info("Loaded Personal Bin File: " + statsPath);
            } catch (IOException e) {
                LOGGER.error("Evolution Bin Load Failed",e);
            }
        }
    }

    private void registerNarcButton() {
        ButtonNode selectNarc = gui.button(selectNarcFile);
        selectNarc.registerAction(ActivateByType.RELEASE, this::createNarcUI);
    }

    private void register2DFileButtons() {
        // Tier 1a open
        gui.pushDropdown("Open 2D File");
        ButtonNode selectNscr = gui.button(selectNSCRFile);
        selectNscr.registerAction(ActivateByType.RELEASE, this::createNscrUI);
        ButtonNode selectNcgr = gui.button(selectNCGRFile);
        selectNcgr.registerAction(ActivateByType.RELEASE, this::createNcgrUI);
        ButtonNode selectNcLr = gui.button(selectNCLRFile);
        selectNcLr.registerAction(ActivateByType.RELEASE, this::createNclrUI);
        // Tier 1a close
        gui.popFolder();
    }

    private void registerDataButtons() {
        // Tier 1b open
        gui.pushDropdown("Open Data File");
        ButtonNode selectEvo = gui.button(selectEvoFile);
        selectEvo.registerAction(ActivateByType.RELEASE, this::createEvoUI);
        ButtonNode selectStats = gui.button(selectStatsFile);
        selectStats.registerAction(ActivateByType.RELEASE, this::createStatsUI);
        // Tier 1b close
        gui.popFolder();
    }

    private void registerOpenFilesView() {
        // Tier 1bc open
        FolderNode loaded = gui.pushFolder("Loaded Files");
        WindowManager.uncoverOrCreateWindow(loaded,false,cell,cell*2,null);
        // Tier 1c close
        gui.popFolder();
    }

    private void buildFileDropdown() {
        // Tier 0a open
        gui.pushDropdown("File");
        registerNarcButton();
        register2DFileButtons();
        registerDataButtons();
        // Tier 0a close
        gui.popFolder();
    }

    private void buildViewDropdown() {
        // Tier 0b open
        gui.pushDropdown("View");
        registerOpenFilesView();
        // Tier 0b close
        gui.popFolder();
    }

    @Override
    public void setup() {
        surface.setTitle("NDS4J");
        surface.setResizable(true);
        surface.setLocation(100,100);
        gui = new NDSGuiImpl(this,settings);
        buildFileDropdown();
        buildViewDropdown();
    }

    @Override
    public void draw() {
        background(30,40,189);
    }


    @Override
    public void mousePressed() {
        // NOOP
    }
}
