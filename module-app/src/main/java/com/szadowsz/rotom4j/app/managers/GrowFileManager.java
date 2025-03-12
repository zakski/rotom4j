package com.szadowsz.rotom4j.app.managers;


import com.szadowsz.rotom4j.file.data.stats.GrowNFSFile;
import com.szadowsz.rotom4j.app.RotomGuiImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GrowFileManager {
    static Logger LOGGER = LoggerFactory.getLogger(GrowFileManager.class);

    private static GrowFileManager singleton;

    private final Map<String, GrowNFSFile> growFileMap = new ConcurrentHashMap<>();

    private GrowFileManager() {
    }

    public static GrowFileManager getInstance() {
        if (singleton == null) {
            singleton = new GrowFileManager();
        }
        return singleton;
    }

    public GrowNFSFile registerGrowth(RotomGuiImpl gui, GrowNFSFile grow) {
        if (!growFileMap.containsKey(grow.getFileName())) {
            LOGGER.info("Registering GUI for Growth File: " + grow.getFileName());
            growFileMap.put(grow.getFileName(), grow);
            gui.registerGrowthGUI(grow);
        }
        return grow;
    }
}
