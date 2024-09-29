package com.szadowsz.gui.window;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.window.pane.RWindowPane;
import processing.core.PGraphics;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Internal/Pane Window Manager
 */
public class RWindowManager {

    private final RotomGui gui;

    private final CopyOnWriteArrayList<RWindowPane> windowsToSetFocusOn = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<RWindowPane> windows = new CopyOnWriteArrayList<>(); // TODO should be set?

    /**
     * Manager Constructor
     *
     * @param rotomGui the Gui to manage the internal windows for
     */
    public RWindowManager(RotomGui rotomGui) {
            gui = rotomGui;
    }

    public void setFocus(RWindowPane window) {
    }

    /**
     * Update and draw all windows
     *
     * @param canvas graphics context
     */
    public void updateAndDrawWindows(PGraphics canvas) { // TODO LazyGui
        if (!windowsToSetFocusOn.isEmpty()) {
            for (RWindowPane w : windowsToSetFocusOn) {
                windows.remove(w);
                windows.add(w);
            }
            windowsToSetFocusOn.clear();
        }
        for (RWindowPane win : windows) {
            win.drawWindow(canvas);
        }
    }
}
