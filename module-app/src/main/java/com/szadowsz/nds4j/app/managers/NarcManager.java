package com.szadowsz.nds4j.app.managers;

import com.szadowsz.nds4j.app.NDSGuiImpl;
import com.szadowsz.nds4j.file.nitro.narc.NARC;
import com.old.ui.node.impl.FolderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NarcManager {
    static Logger LOGGER = LoggerFactory.getLogger(NarcManager.class);

    private static NarcManager singleton;

    private final Map<String, NARC> narcFileMap = new ConcurrentHashMap<>();
    private final Map<String, FolderNode> narcFolderMap = new ConcurrentHashMap<>();

    private NarcManager() {
    }

    public static NarcManager getInstance() {
        if (singleton == null) {
            singleton = new NarcManager();
        }
        return singleton;
    }

    public NARC registerNarc(NDSGuiImpl gui, NARC narc) {
        if (!narcFileMap.containsKey(narc.getFileName())) {
            LOGGER.info("Registering GUI for Narc File: " + narc.getFileName());
            narcFileMap.put(narc.getFileName(), narc);
            gui.registerNarcGUI(narc);
        }
        return narc;
    }
}
