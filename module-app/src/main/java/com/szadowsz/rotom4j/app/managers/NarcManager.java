package com.szadowsz.rotom4j.app.managers;

import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.rotom4j.file.nitro.n2d.narc.NARC;
import com.szadowsz.rotom4j.app.RotomGuiImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NarcManager {
    static Logger LOGGER = LoggerFactory.getLogger(NarcManager.class);

    private static NarcManager singleton;

    private final Map<String, NARC> narcFileMap = new ConcurrentHashMap<>();
    private final Map<String, RFolder> narcFolderMap = new ConcurrentHashMap<>();

    private NarcManager() {
    }

    public static NarcManager getInstance() {
        if (singleton == null) {
            singleton = new NarcManager();
        }
        return singleton;
    }

    public NARC registerNarc(RotomGuiImpl gui, NARC narc) {
        if (!narcFileMap.containsKey(narc.getFileName())) {
            LOGGER.info("Registering GUI for Narc File: " + narc.getFileName());
            narcFileMap.put(narc.getFileName(), narc);
            gui.registerNarcGUI(narc);
        }
        return narc;
    }
}
