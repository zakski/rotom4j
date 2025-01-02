package com.szadowsz.rotom4j.app.managers;


import com.szadowsz.rotom4j.file.data.evo.EvolutionNFSFile;
import com.szadowsz.rotom4j.app.RotomGuiImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EvoFileManager {
    static Logger LOGGER = LoggerFactory.getLogger(EvoFileManager.class);

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

    public EvolutionNFSFile registerEvo(RotomGuiImpl gui, EvolutionNFSFile evo) {
        if (!evoFileMap.containsKey(evo.getFileName())) {
            LOGGER.info("Registering GUI for Evolution File: " + evo.getFileName());
            evoFileMap.put(evo.getFileName(), evo);
            gui.registerEvoGUI(evo);
        }
        return evo;
    }
}
