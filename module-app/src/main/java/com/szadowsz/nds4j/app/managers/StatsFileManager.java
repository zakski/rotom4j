package com.szadowsz.nds4j.app.managers;


import com.szadowsz.nds4j.app.NDSGuiImpl;
import com.szadowsz.nds4j.file.bin.StatsNFSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatsFileManager {
    static Logger LOGGER = LoggerFactory.getLogger(NitroFileManager.class);

    private static StatsFileManager singleton;

    private final Map<String, StatsNFSFile> stasFileMap = new ConcurrentHashMap<>();

    private StatsFileManager() {
    }

    public static StatsFileManager getInstance() {
        if (singleton == null) {
            singleton = new StatsFileManager();
        }
        return singleton;
    }

    public StatsNFSFile registerPersonal(NDSGuiImpl gui, StatsNFSFile stats) {
        if (!stasFileMap.containsKey(stats.getFileName())) {
            LOGGER.info("Registering GUI for Personal File: " + stats.getFileName());
            stasFileMap.put(stats.getFileName(), stats);
            gui.registerStatsGUI(stats);
        }
        return stats;
    }
}
