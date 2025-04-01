package com.szadowsz.rotom4j.app;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.RotomGuiSettings;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.rotom4j.component.nitro.nanr.NANRFolder;
import com.szadowsz.rotom4j.component.nitro.ncer.NCERFolder;
import com.szadowsz.rotom4j.component.nitro.ncgr.NCGRFolder;
import com.szadowsz.rotom4j.component.nitro.nclr.NCLRFolder;
import com.szadowsz.rotom4j.component.nitro.nscr.NSCRFolder;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.data.evo.EvolutionNFSFile;
import com.szadowsz.rotom4j.file.data.learnset.LearnsetNFSFile;
import com.szadowsz.rotom4j.file.data.stats.GrowNFSFile;
import com.szadowsz.rotom4j.file.data.stats.StatsNFSFile;
import com.szadowsz.rotom4j.file.nitro.nanr.NANR;
import com.szadowsz.rotom4j.file.nitro.narc.NARC;
import com.szadowsz.rotom4j.file.nitro.ncer.NCER;
import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;
import com.szadowsz.rotom4j.file.nitro.nclr.NCLR;
import com.szadowsz.rotom4j.file.nitro.nscr.NSCR;
import com.szadowsz.rotom4j.component.bin.evo.EvoFolderComponent;
import com.szadowsz.rotom4j.component.bin.growth.GrowthFolderComponent;
import com.szadowsz.rotom4j.component.bin.learn.LearnFolderComponent;
import com.szadowsz.rotom4j.component.bin.stats.StatsFolderComponent;
import com.szadowsz.rotom4j.component.nitro.narc.NARCFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;

public class RotomGuiImpl extends RotomGui {
    final static Logger LOGGER = LoggerFactory.getLogger(RotomGuiImpl.class);
    final static String selectNarcFile = "Select Narc File";

    public RotomGuiImpl(PApplet sketch, RotomGuiSettings settings) {
        super(sketch, settings);
    }


    public NARCFolder narc(String path, NARC narc) {
        String fullPath = getCurrentPath() + path;
        if (tree.isPathTakenByUnexpectedType(fullPath, NARCFolder.class)) {
            return null;//defaultOption == null ? options[0] : defaultOption;
        }
        NARCFolder component = (NARCFolder) tree.getComponent(fullPath);
        if (component == null) {
            RFolder parentFolder = tree.getParentFolder(fullPath);
            component = new NARCFolder(this,fullPath, parentFolder, narc);
            tree.insertAtPath(component);
        }
        return component;
    }

    public NANRFolder animeRes(String path, NANR nanr) throws NitroException {
        String fullPath = getCurrentPath() + path;
        if (tree.isPathTakenByUnexpectedType(fullPath, NANRFolder.class)) {
            return null;//defaultOption == null ? options[0] : defaultOption;
        }
        NANRFolder component = (NANRFolder) tree.getComponent(fullPath);
        if (component == null) {
            RFolder parentFolder = tree.getParentFolder(fullPath);
            component = new NANRFolder(this,fullPath, parentFolder, nanr);
            tree.insertAtPath(component);
        }
        return component;
    }

    public NCERFolder cellBank(String path, NCER ncer) throws NitroException {
        String fullPath = getCurrentPath() + path;
        if (tree.isPathTakenByUnexpectedType(fullPath, NCERFolder.class)) {
            return null;//defaultOption == null ? options[0] : defaultOption;
        }
        NCERFolder component = (NCERFolder) tree.getComponent(fullPath);
        if (component == null) {
            RFolder parentFolder = tree.getParentFolder(fullPath);
            component = new NCERFolder(this, fullPath, parentFolder, ncer);
            tree.insertAtPath(component);
        }
        return component;
    }

    public NSCRFolder scrRes(String path, NSCR nscr) {
        String fullPath = getCurrentPath() + path;
        if (tree.isPathTakenByUnexpectedType(fullPath, NSCRFolder.class)) {
            return null;//defaultOption == null ? options[0] : defaultOption;
        }
        NSCRFolder component = (NSCRFolder) tree.getComponent(fullPath);
        if (component == null) {
            RFolder parentFolder = tree.getParentFolder(fullPath);
            component = new NSCRFolder(this, fullPath, parentFolder, nscr);
            tree.insertAtPath(component);
        }
        return component;
    }

    public NCGRFolder image(String path, NCGR ncgr) {
        String fullPath = getCurrentPath() + path;
        if (tree.isPathTakenByUnexpectedType(fullPath, NCLRFolder.class)) {
            return null;//defaultOption == null ? options[0] : defaultOption;
        }
        NCGRFolder component = (NCGRFolder) tree.getComponent(fullPath);
        if (component == null) {
            RFolder parentFolder = tree.getParentFolder(fullPath);
            component = new NCGRFolder(this, fullPath, parentFolder, ncgr);
            tree.insertAtPath(component);
        }
        return component;
    }

    public NCLRFolder palette(String path, NCLR nclr) {
        String fullPath = getCurrentPath() + path;
        if (tree.isPathTakenByUnexpectedType(fullPath, NCLRFolder.class)) {
            return null;
        }
        NCLRFolder component = (NCLRFolder) tree.getComponent(fullPath);
        if (component == null) {
            RFolder folder = tree.getParentFolder(fullPath);
            component = new NCLRFolder(this, fullPath, folder, nclr);
            tree.insertAtPath(component);
        }
        return component;
    }

    public StatsFolderComponent personal(StatsNFSFile stats) {
        String fullPath = getCurrentPath() + stats.getFileName();
        if (tree.isPathTakenByUnexpectedType(fullPath, StatsFolderComponent.class)) {
            return null;
        }
        StatsFolderComponent component = (StatsFolderComponent) tree.getComponent(fullPath);
        if (component == null) {
            RFolder folder = tree.getParentFolder(fullPath);
            component = new StatsFolderComponent(this, fullPath, folder, stats);
            tree.insertAtPath(component);
        }
        return component;
    }

    public EvoFolderComponent evolution(EvolutionNFSFile evo) {
        String fullPath = getCurrentPath() + evo.getFileName();
        if (tree.isPathTakenByUnexpectedType(fullPath, EvoFolderComponent.class)) {
            return null;
        }
        EvoFolderComponent component = (EvoFolderComponent) tree.getComponent(fullPath);
        if (component == null) {
            RFolder folder = tree.getParentFolder(fullPath);
            component = new EvoFolderComponent(this,fullPath, folder, evo);
            tree.insertAtPath(component);
        }
        return component;
    }

    public LearnFolderComponent learnset(LearnsetNFSFile learn) {
        String fullPath = getCurrentPath() + learn.getFileName();
        if (tree.isPathTakenByUnexpectedType(fullPath, LearnFolderComponent.class)) {
            return null;
        }
        LearnFolderComponent component = (LearnFolderComponent) tree.getComponent(fullPath);
        if (component == null) {
            RFolder folder = tree.getParentFolder(fullPath);
            component = new LearnFolderComponent(this, fullPath, folder, learn);
            tree.insertAtPath(component);
        }
        return component;
    }

    public GrowthFolderComponent growth(GrowNFSFile grow) {
        String fullPath = getCurrentPath() + grow.getFileName();
        if (tree.isPathTakenByUnexpectedType(fullPath, GrowthFolderComponent.class)) {
            return null;
        }
        GrowthFolderComponent component = (GrowthFolderComponent) tree.getComponent(fullPath);
        if (component == null) {
            RFolder folder = tree.getParentFolder(fullPath);
            component = new GrowthFolderComponent(this,fullPath, folder, grow);
            tree.insertAtPath(component);
        }
        return component;
    }

    public void registerNarcGUI(NARC narc) {
        LOGGER.info("Creating GUI for Narc File: {}", narc.getFileName());
        setFolder("Loaded Files");
        narc(narc.getFileName(), narc);
        setFolder(null);
        LOGGER.info("Created GUI for Narc File: {}", narc.getFileName());
    }

    public void registerNanrGUI(NANR nanr) throws NitroException {
        LOGGER.info("Creating GUI for NANR File: {}", nanr.getFileName());
        setFolder("Loaded Files");
        animeRes(nanr.getFileName(), nanr);
        LOGGER.info("Created GUI for NANR File: {}", nanr.getFileName());
        setFolder(null);
    }

    public void registerNcerGUI(NCER ncer) throws NitroException {
        LOGGER.info("Creating GUI for NCER File: {}", ncer.getFileName());
        setFolder("Loaded Files");
        cellBank(ncer.getFileName(), ncer);
        LOGGER.info("Created GUI for NCER File: {}", ncer.getFileName());
        setFolder(null);
    }

    public void registerNscrGUI(NSCR nscr) {
        LOGGER.info("Creating GUI for NSCR File: {}", nscr.getFileName());
        setFolder("Loaded Files");
        scrRes(nscr.getFileName(), nscr);
        LOGGER.info("Created GUI for NSCR File: {}", nscr.getFileName());
        setFolder(null);
    }

    public void registerNcgrGUI(NCGR ncgr) {
        LOGGER.info("Creating GUI for NCGR File: {}", ncgr.getFileName());
        setFolder("Loaded Files");
        image(ncgr.getFileName(), ncgr);
        setFolder(null);
        LOGGER.info("Created GUI for NCGR File: {}", ncgr.getFileName());
    }

    public void registerNclrGUI(NCLR nclr) {
        LOGGER.info("Creating GUI for NCLR File: {}", nclr.getFileName());
        setFolder("Loaded Files");
        palette(nclr.getFileName(),nclr);
        setFolder(null);
        LOGGER.info("Created GUI for NCLR File: {}", nclr.getFileName());
    }

    public void registerEvoGUI(EvolutionNFSFile evo) {
        LOGGER.info("Creating GUI for Evolution Bin File: {}", evo.getFileName());
        setFolder("Loaded Files");
        evolution(evo);
        setFolder(null);
        LOGGER.info("Created GUI for Evolution Bin File: {}", evo.getFileName());
    }

    public void registerGrowthGUI(GrowNFSFile grow) {
        LOGGER.info("Creating GUI for Growth Bin File: {}", grow.getFileName());
        setFolder("Loaded Files");
        growth(grow);
        setFolder(null);
        LOGGER.info("Created GUI for Growth Bin File: {}", grow.getFileName());
    }


    public void registerStatsGUI(StatsNFSFile stats) {
        LOGGER.info("Creating GUI for Personal Bin File: {}", stats.getFileName());
        setFolder("Loaded Files");
        personal(stats);
        setFolder(null);
        LOGGER.info("Created GUI for Personal Bin File: {}", stats.getFileName());
    }

    public void registerLearnGUI(LearnsetNFSFile learn) {
        LOGGER.info("Creating GUI for Learnset Bin File: {}", learn.getFileName());
        setFolder("Loaded Files");
        learnset(learn);
        setFolder(null);
        LOGGER.info("Created GUI for Learnset Bin File: {}", learn.getFileName());
    }
}
