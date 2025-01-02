package com.szadowsz.rotom4j.app.managers;


import com.szadowsz.rotom4j.file.data.learnset.LearnsetNFSFile;
import com.szadowsz.rotom4j.app.RotomGuiImpl;
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

    public LearnsetNFSFile registerLearnset(RotomGuiImpl gui, LearnsetNFSFile learn) {
        if (!learnFileMap.containsKey(learn.getFileName())) {
            LOGGER.info("Registering GUI for Evolution File: " + learn.getFileName());
            learnFileMap.put(learn.getFileName(), learn);
            gui.registerLearnGUI(learn);
        }
        return learn;
    }
}
