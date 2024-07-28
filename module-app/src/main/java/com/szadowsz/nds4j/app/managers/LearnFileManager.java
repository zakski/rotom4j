package com.szadowsz.nds4j.app.managers;


import com.szadowsz.nds4j.app.NDSGuiImpl;
import com.szadowsz.nds4j.file.bin.learnset.LearnsetNFSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LearnFileManager {
    static Logger LOGGER = LoggerFactory.getLogger(LearnFileManager.class);

    private static LearnFileManager singleton;

    private final Map<String, LearnsetNFSFile> learnFileMap = new ConcurrentHashMap<>();

    private LearnFileManager() {
    }

    public static LearnFileManager getInstance() {
        if (singleton == null) {
            singleton = new LearnFileManager();
        }
        return singleton;
    }

    public LearnsetNFSFile registerLearnset(NDSGuiImpl gui, LearnsetNFSFile learn) {
        if (!learnFileMap.containsKey(learn.getFileName())) {
            LOGGER.info("Registering GUI for Evolution File: " + learn.getFileName());
            learnFileMap.put(learn.getFileName(), learn);
            gui.registerLearnGUI(learn);
        }
        return learn;
    }
}
