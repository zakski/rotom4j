package com.szadowsz.nds4j.app.managers;

import com.szadowsz.nds4j.app.NDSGuiImpl;
import com.szadowsz.nds4j.file.nitro.NCLR;
import com.szadowsz.ui.NDSGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NclrManager {
    static Logger LOGGER = LoggerFactory.getLogger(NclrManager.class);

    private static NclrManager singleton;

    private final Map<String, NCLR> nclrFileMap = new ConcurrentHashMap<>();

    private NclrManager() {
    }

    public static NclrManager getInstance() {
        if (singleton == null) {
            singleton = new NclrManager();
        }
        return singleton;
    }

    public NCLR registerNCLR(NDSGuiImpl gui, NCLR ncgr) {
        if (!nclrFileMap.containsKey(ncgr.getFileName())) {
            LOGGER.info("Registering GUI for NCGR File: " + ncgr.getFileName());
            nclrFileMap.put(ncgr.getFileName(), ncgr);
            gui.registerNclrGUI(ncgr);
        }
        return ncgr;
    }
}
