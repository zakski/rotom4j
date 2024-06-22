package com.szadowsz.nds4j.app.managers;

import com.szadowsz.nds4j.app.NDSGuiImpl;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.NCER;
import com.szadowsz.nds4j.file.nitro.NSCR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NcerManager {
    static Logger LOGGER = LoggerFactory.getLogger(NcgrManager.class);

    private static NcerManager singleton;

   private final Map<String, NCER> ncerFileMap = new ConcurrentHashMap<>();

    private NcerManager() {
    }

    public static NcerManager getInstance() {
        if (singleton == null) {
            singleton = new NcerManager();
        }
        return singleton;
    }

    public NCER registerNCER(NDSGuiImpl gui, NCER ncer) throws NitroException {
        if (!ncerFileMap.containsKey(ncer.getFileName())) {
            LOGGER.info("Registering GUI for NCER File: " + ncer.getFileName());
            ncerFileMap.put(ncer.getFileName(), ncer);
            gui.registerNcerGUI(ncer);
        }
        return ncer;
    }
}
