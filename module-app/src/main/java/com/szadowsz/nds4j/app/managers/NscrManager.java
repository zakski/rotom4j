package com.szadowsz.nds4j.app.managers;

import com.szadowsz.nds4j.app.NDSGuiImpl;
import com.szadowsz.nds4j.file.nitro.NSCR;
import com.szadowsz.ui.NDSGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NscrManager{
    static Logger LOGGER = LoggerFactory.getLogger(NcgrManager.class);

    private static NscrManager singleton;

    private final Map<String, NSCR> nscrFileMap = new ConcurrentHashMap<>();

    private NscrManager() {
    }

    public static NscrManager getInstance() {
        if (singleton == null) {
            singleton = new NscrManager();
        }
        return singleton;
    }

    public NSCR registerNSCR(NDSGuiImpl gui, NSCR nscr) {
        if (!nscrFileMap.containsKey(nscr.getFileName())) {
            LOGGER.info("Registering GUI for NCGR File: " + nscr.getFileName());
            nscrFileMap.put(nscr.getFileName(), nscr);
            gui.registerNscrGUI(nscr);
        }
        return nscr;
    }
}
