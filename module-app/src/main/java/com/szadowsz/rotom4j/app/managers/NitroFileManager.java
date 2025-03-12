package com.szadowsz.rotom4j.app.managers;


import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.BaseNFSFile;
import com.szadowsz.rotom4j.file.nitro.nanr.NANR;
import com.szadowsz.rotom4j.file.nitro.ncer.NCER;
import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;
import com.szadowsz.rotom4j.file.nitro.nclr.NCLR;
import com.szadowsz.rotom4j.file.nitro.nscr.NSCR;
import com.szadowsz.rotom4j.app.RotomGuiImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NitroFileManager {
    static Logger LOGGER = LoggerFactory.getLogger(NitroFileManager.class);

    private static NitroFileManager singleton;

    private final Map<String, BaseNFSFile> nitroFileMap = new ConcurrentHashMap<>();

    private NitroFileManager() {
    }

    public static NitroFileManager getInstance() {
        if (singleton == null) {
            singleton = new NitroFileManager();
        }
        return singleton;
    }

    public NANR registerNANR(RotomGuiImpl gui, NANR nanr) throws NitroException {
        if (!nitroFileMap.containsKey(nanr.getFileName())) {
            LOGGER.info("Registering GUI for NANR File: " + nanr.getFileName());
            nitroFileMap.put(nanr.getFileName(), nanr);
            gui.registerNanrGUI(nanr);
        }
        return nanr;
    }

    public NCER registerNCER(RotomGuiImpl gui, NCER ncer) throws NitroException {
        if (!nitroFileMap.containsKey(ncer.getFileName())) {
            LOGGER.info("Registering GUI for NCER File: " + ncer.getFileName());
            nitroFileMap.put(ncer.getFileName(), ncer);
            gui.registerNcerGUI(ncer);
        }
        return ncer;
    }

    public NSCR registerNSCR(RotomGuiImpl gui, NSCR nscr) {
        if (!nitroFileMap.containsKey(nscr.getFileName())) {
            LOGGER.info("Registering GUI for NSCR File: " + nscr.getFileName());
            nitroFileMap.put(nscr.getFileName(), nscr);
            gui.registerNscrGUI(nscr);
        }
        return nscr;
    }

    public NCGR registerNCGR(RotomGuiImpl gui, NCGR ncgr) {
        if (!nitroFileMap.containsKey(ncgr.getFileName())) {
            LOGGER.info("Registering GUI for NCGR File: " + ncgr.getFileName());
            nitroFileMap.put(ncgr.getFileName(), ncgr);
            gui.registerNcgrGUI(ncgr);
        }
        return ncgr;
    }

    public NCLR registerNCLR(RotomGuiImpl gui, NCLR nclr) {
        if (!nitroFileMap.containsKey(nclr.getFileName())) {
            LOGGER.info("Registering GUI for NCLR File: " + nclr.getFileName());
            nitroFileMap.put(nclr.getFileName(), nclr);
            gui.registerNclrGUI(nclr);
        }
        return nclr;
    }
}
