package com.szadowsz.rotom4j.component.control;

import com.szadowsz.rotom4j.file.RotomFile;
import com.szadowsz.rotom4j.file.data.evo.EvolutionNFSFile;
import com.szadowsz.rotom4j.file.data.learnset.LearnsetNFSFile;
import com.szadowsz.rotom4j.file.data.stats.GrowNFSFile;
import com.szadowsz.rotom4j.file.data.stats.StatsNFSFile;
import com.szadowsz.rotom4j.file.nitro.n2d.nanr.NANR;
import com.szadowsz.rotom4j.file.nitro.n2d.narc.NARC;
import com.szadowsz.rotom4j.file.nitro.n2d.ncer.NCER;
import com.szadowsz.rotom4j.file.nitro.n2d.ncgr.NCGR;
import com.szadowsz.rotom4j.file.nitro.n2d.nclr.NCLR;
import com.szadowsz.rotom4j.file.nitro.n2d.nscr.NSCR;
import com.szadowsz.rotom4j.app.RotomGuiImpl;
import com.szadowsz.rotom4j.app.managers.*;
import com.szadowsz.rotom4j.app.utils.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import static com.szadowsz.rotom4j.app.ProcessingRotom4J.prefs;

public class LoadedFileUI extends ControlConstants {
    protected static final Logger LOGGER = LoggerFactory.getLogger(LoadedFileUI.class);

    private LoadedFileUI(){
        super();
    }

    private static <N extends RotomFile> void createUI(
            Function<String, String> fileSelection,
            FileLoader<N> fileParsing,
            FileConsumer<N> uiCreator
    ) {
        String lastPath = prefs.get("openNarcPath", System.getProperty("user.dir"));
        String filePath = fileSelection.apply(lastPath);
        if (filePath != null) {
            prefs.put("openNarcPath", new File(filePath).getParentFile().getAbsolutePath());
            try {
                LOGGER.info("Loading File: {}", filePath);
                N parsedNFSFile = fileParsing.apply(filePath);
                uiCreator.accept(parsedNFSFile);
                LOGGER.info("Loaded File: {}", filePath);
            } catch (IOException e) {
                LOGGER.error("File Load of {} Failed", filePath, e);
            }
        }
    }

    public static void createNarcUI(RotomGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectNarcFile(gui.getSketch(), p, selectEvoFile);
        FileLoader<NARC> parser = NARC::fromFile;
        FileConsumer<NARC> creator = f -> NarcManager.getInstance().registerNarc(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createNanrUI(RotomGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectNanrFile(gui.getSketch(), p, selectEvoFile);
        FileLoader<NANR> parser = NANR::fromFile;
        FileConsumer<NANR> creator = f -> NitroFileManager.getInstance().registerNANR(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createNcerUI(RotomGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectNcerFile(gui.getSketch(), p, selectEvoFile);
        FileLoader<NCER> parser = NCER::fromFile;
        FileConsumer<NCER> creator = f -> NitroFileManager.getInstance().registerNCER(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createNscrUI(RotomGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectNscrFile(gui.getSketch(), p, selectEvoFile);
        FileLoader<NSCR> parser = NSCR::fromFile;
        FileConsumer<NSCR> creator = f -> NitroFileManager.getInstance().registerNSCR(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createNcgrUI(RotomGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectNcgrFile(gui.getSketch(), p, selectEvoFile);
        FileLoader<NCGR> parser = NCGR::fromFile;
        FileConsumer<NCGR> creator = f -> NitroFileManager.getInstance().registerNCGR(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createNclrUI(RotomGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectNclrFile(gui.getSketch(), p, selectEvoFile);
        FileLoader<NCLR> parser = NCLR::fromFile;
        FileConsumer<NCLR> creator = f -> NitroFileManager.getInstance().registerNCLR(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createEvoUI(RotomGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectBinFile(gui.getSketch(), p, selectEvoFile);
        FileLoader<EvolutionNFSFile> parser = EvolutionNFSFile::fromFile;
        FileConsumer<EvolutionNFSFile> creator = f -> EvoFileManager.getInstance().registerEvo(gui, f);
        createUI(chooser, parser, creator);
    }


    public static void createStatsUI(RotomGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectBinFile(gui.getSketch(), p, selectStatsFile);
        FileLoader<StatsNFSFile> parser = StatsNFSFile::fromFile;
        FileConsumer<StatsNFSFile> creator = f -> StatsFileManager.getInstance().registerPersonal(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createLearnUI(RotomGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectBinFile(gui.getSketch(), p, selectStatsFile);
        FileLoader<LearnsetNFSFile> parser = LearnsetNFSFile::fromFile;
        FileConsumer<LearnsetNFSFile> creator = f -> LearnFileManager.getInstance().registerLearnset(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createGrowthUI(RotomGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectBinFile(gui.getSketch(), p, selectStatsFile);
        FileLoader<GrowNFSFile> parser = GrowNFSFile::fromFile;
        FileConsumer<GrowNFSFile> creator = f -> GrowFileManager.getInstance().registerGrowth(gui, f);
        createUI(chooser, parser, creator);
    }
}
