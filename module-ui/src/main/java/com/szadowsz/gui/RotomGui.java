package com.szadowsz.gui;

import com.szadowsz.gui.window.external.RWindowExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * GUI System Representation
 */
public class RotomGui {
    private static final Logger LOGGER = LoggerFactory.getLogger(RotomGui.class);

    protected List<RWindowExt> windows = new CopyOnWriteArrayList<>();

    /**
     * Register an External Window for Global Processing
     *
     * @param external the window to be registered
     */
    public synchronized void registerWindow(RWindowExt external) {
        if (!windows.contains(external)) {
            windows.add(external);
        }
    }
}
