package com.szadowsz.nds4j.app.managers;


import com.szadowsz.nds4j.app.NDSGuiImpl;
import com.szadowsz.nds4j.file.nitro.NCGR;
import com.szadowsz.ui.NDSGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NcgrManager {
    static Logger LOGGER = LoggerFactory.getLogger(NcgrManager.class);

    private static NcgrManager singleton;

    private final Map<String, NCGR> ncgrFileMap = new ConcurrentHashMap<>();

    private NcgrManager() {
    }

    public static NcgrManager getInstance() {
        if (singleton == null) {
            singleton = new NcgrManager();
        }
        return singleton;
    }

    public NCGR registerNCGR(NDSGuiImpl gui, NCGR ncgr) {
        if (!ncgrFileMap.containsKey(ncgr.getFileName())) {
            LOGGER.info("Registering GUI for NCGR File: " + ncgr.getFileName());
            ncgrFileMap.put(ncgr.getFileName(), ncgr);
            gui.registerNcgrGUI(ncgr);
        }
        return ncgr;
    }
}
