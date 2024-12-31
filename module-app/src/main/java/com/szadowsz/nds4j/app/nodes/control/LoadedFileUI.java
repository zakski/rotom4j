package com.szadowsz.nds4j.app.nodes.control;

import com.szadowsz.nds4j.app.NDSGuiImpl;
import com.szadowsz.nds4j.app.managers.*;
import com.szadowsz.nds4j.app.utils.FileChooser;
import com.szadowsz.nds4j.file.BaseNFSFile;
import com.szadowsz.nds4j.file.bin.evo.EvolutionNFSFile;
import com.szadowsz.nds4j.file.bin.learnset.LearnsetNFSFile;
import com.szadowsz.nds4j.file.bin.stats.GrowNFSFile;
import com.szadowsz.nds4j.file.bin.stats.StatsNFSFile;
import com.szadowsz.nds4j.file.nitro.nanr.NANR;
import com.szadowsz.nds4j.file.nitro.narc.NARC;
import com.szadowsz.nds4j.file.nitro.ncer.NCER;
import com.szadowsz.nds4j.file.nitro.ncgr.NCGR;
import com.szadowsz.nds4j.file.nitro.nclr.NCLR;
import com.szadowsz.nds4j.file.nitro.nscr.NSCR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import static com.szadowsz.nds4j.app.nodes.control.ControlConstants.*;
import static com.szadowsz.nds4j.app.Processing.prefs;

public class LoadedFileUI {
    protected static final Logger LOGGER = LoggerFactory.getLogger(LoadedFileUI.class);

    private LoadedFileUI(){}

    private static <N extends BaseNFSFile> void createUI(
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

    public static void createNarcUI(NDSGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectNarcFile(gui.getGuiCanvas().parent, p, selectEvoFile);
        FileLoader<NARC> parser = NARC::fromFile;
        FileConsumer<NARC> creator = f -> NarcManager.getInstance().registerNarc(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createNanrUI(NDSGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectNanrFile(gui.getGuiCanvas().parent, p, selectEvoFile);
        FileLoader<NANR> parser = NANR::fromFile;
        FileConsumer<NANR> creator = f -> NitroFileManager.getInstance().registerNANR(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createNcerUI(NDSGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectNcerFile(gui.getGuiCanvas().parent, p, selectEvoFile);
        FileLoader<NCER> parser = NCER::fromFile;
        FileConsumer<NCER> creator = f -> NitroFileManager.getInstance().registerNCER(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createNscrUI(NDSGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectNscrFile(gui.getGuiCanvas().parent, p, selectEvoFile);
        FileLoader<NSCR> parser = NSCR::fromFile;
        FileConsumer<NSCR> creator = f -> NitroFileManager.getInstance().registerNSCR(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createNcgrUI(NDSGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectNcgrFile(gui.getGuiCanvas().parent, p, selectEvoFile);
        FileLoader<NCGR> parser = NCGR::fromFile;
        FileConsumer<NCGR> creator = f -> NitroFileManager.getInstance().registerNCGR(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createNclrUI(NDSGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectNclrFile(gui.getGuiCanvas().parent, p, selectEvoFile);
        FileLoader<NCLR> parser = NCLR::fromFile;
        FileConsumer<NCLR> creator = f -> NitroFileManager.getInstance().registerNCLR(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createEvoUI(NDSGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectBinFile(gui.getGuiCanvas().parent, p, selectEvoFile);
        FileLoader<EvolutionNFSFile> parser = EvolutionNFSFile::fromFile;
        FileConsumer<EvolutionNFSFile> creator = f -> EvoFileManager.getInstance().registerEvo(gui, f);
        createUI(chooser, parser, creator);
    }


    public static void createStatsUI(NDSGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectBinFile(gui.getGuiCanvas().parent, p, selectStatsFile);
        FileLoader<StatsNFSFile> parser = StatsNFSFile::fromFile;
        FileConsumer<StatsNFSFile> creator = f -> StatsFileManager.getInstance().registerPersonal(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createLearnUI(NDSGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectBinFile(gui.getGuiCanvas().parent, p, selectStatsFile);
        FileLoader<LearnsetNFSFile> parser = LearnsetNFSFile::fromFile;
        FileConsumer<LearnsetNFSFile> creator = f -> LearnFileManager.getInstance().registerLearnset(gui, f);
        createUI(chooser, parser, creator);
    }

    public static void createGrowthUI(NDSGuiImpl gui) {
        Function<String, String> chooser = p -> FileChooser.selectBinFile(gui.getGuiCanvas().parent, p, selectStatsFile);
        FileLoader<GrowNFSFile> parser = GrowNFSFile::fromFile;
        FileConsumer<GrowNFSFile> creator = f -> GrowFileManager.getInstance().registerGrowth(gui, f);
        createUI(chooser, parser, creator);
    }
}
