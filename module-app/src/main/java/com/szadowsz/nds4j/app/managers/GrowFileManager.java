package com.szadowsz.nds4j.app.managers;


import com.szadowsz.nds4j.app.NDSGuiImpl;
import com.szadowsz.nds4j.file.bin.GrowNFSFile;
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

    public GrowNFSFile registerGrowth(NDSGuiImpl gui, GrowNFSFile grow) {
        if (!growFileMap.containsKey(grow.getFileName())) {
            LOGGER.info("Registering GUI for Growth File: " + grow.getFileName());
            growFileMap.put(grow.getFileName(), grow);
            gui.registerGrowthGUI(grow);
        }
        return grow;
    }
}
