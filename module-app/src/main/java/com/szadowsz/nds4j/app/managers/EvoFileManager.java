package com.szadowsz.nds4j.app.managers;


import com.szadowsz.nds4j.app.NDSGuiImpl;
import com.szadowsz.nds4j.file.bin.EvolutionNFSFile;
import com.szadowsz.ui.NDSGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EvoFileManager {
    static Logger LOGGER = LoggerFactory.getLogger(NcgrManager.class);

    private static EvoFileManager singleton;

    private final Map<String, EvolutionNFSFile> evoFileMap = new ConcurrentHashMap<>();

    private EvoFileManager() {
    }

    public static EvoFileManager getInstance() {
        if (singleton == null) {
            singleton = new EvoFileManager();
        }
        return singleton;
    }

    public EvolutionNFSFile registerEvo(NDSGuiImpl gui, EvolutionNFSFile evo) {
        if (!evoFileMap.containsKey(evo.getFileName())) {
            LOGGER.info("Registering GUI for NCGR File: " + evo.getFileName());
            evoFileMap.put(evo.getFileName(), evo);
            gui.registerEvoGUI(evo);
        }
        return evo;
    }
}
