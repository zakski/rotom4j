package com.szadowsz.gui.window;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.window.internal.RWindowInt;
import com.szadowsz.gui.window.internal.RWindowTemp;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.concurrent.CopyOnWriteArrayList;

public final class RWindowManager {

    private final RotomGui gui;
    private final CopyOnWriteArrayList<RWindowInt> windowsToSetFocusOn = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<RWindowInt> windows = new CopyOnWriteArrayList<>(); // TODO should be set?

    public RWindowManager(RotomGui gui){ // TODO LazyGui
        this.gui = gui;
    }


    /**
     * Found out if the window
     *
     * @param folder    the corresponding folder node
     * @param setFocus      true if the window is in focus, false otherwise
     * @param pos           expected position
     * @return true if found, false otherwise
     */
    protected boolean findWindow(RFolder folder, boolean setFocus, PVector pos) {
        boolean windowFound = false;
        for (RWindowInt w : windows) {
            if (w.getFolder().path.equals(folder.path)) {
                w.setCoordinates(pos.x, pos.y);
                if (RLayoutStore.shouldFolderRowClickCloseWindowIfOpen() && w.isVisible()) {
                    w.close();
                } else {
                    w.open(setFocus);
                }
                windowFound = true;
                break;
            }
        }
        return windowFound;
    }


    /**
     * Set the App's focus on a particular window
     *
     * @param window the window to focus on
     */
    public void setFocus(RWindowInt window) { // TODO LazyGui
        windowsToSetFocusOn.add(window);
    }

    public void uncoverOrCreateWindow(RFolder folder) { // TODO LazyGui
        uncoverOrCreateWindow(folder, true, null, null, null);
    }

    /**
     * Create or make visible a window for the passed in folder
     *
     * @param folder    the corresponding folder node
     * @param setFocus      true if the window is in focus, false otherwise
     * @param nullablePosX  nullable windows x-coordinate
     * @param nullablePosY  nullable windows y-coordinate
     * @param nullableSizeX nullable windows width
     */
    public void uncoverOrCreateWindow(RFolder folder, boolean setFocus, Float nullablePosX, Float nullablePosY, Float nullableSizeX) {
        PVector pos = new PVector(RLayoutStore.getCell(), RLayoutStore.getCell());
        if (folder.getParentFolder() != null) {
            RWindowInt parentWindow = folder.getParentFolder().getWindow();
            if (parentWindow != null) {
                pos = new PVector(parentWindow.getPosX() + parentWindow.getWidth() + RLayoutStore.getCell(), parentWindow.getPosY());
            }
        }
        if (nullablePosX != null) {
            pos.x = nullablePosX;
        }
        if (nullablePosY != null) {
            pos.y = nullablePosY;
        }
        boolean windowFound = findWindow(folder, setFocus, pos);
        if (!windowFound) {
            RWindowInt window = new RWindowInt(gui.getSketch(),gui,folder, pos.x, pos.y, nullableSizeX,0);
            windows.add(window);
            window.open(setFocus);
        }
        if (windowFound && folder.getParent() == null) {
            folder.getWindow().setCoordinates(pos.x, pos.y);
            if (nullableSizeX != null) {
                folder.getWindow().setWidth(nullableSizeX);
            }
        }
    }

    /**
     * Create or make visible a temporary window for the passed in folder
     *
     * @param folder    the corresponding folder node
     * @param setFocus      true if the window is in focus, false otherwise
     * @param nullablePosX  nullable windows x-coordinate
     * @param nullablePosY  nullable windows y-coordinate
     * @param nullableSizeX nullable windows width
     */
    public void uncoverOrCreateTempWindow(RFolder folder, boolean setFocus, Float nullablePosX, Float nullablePosY, Float nullableSizeX) {
        PVector pos = new PVector(RLayoutStore.getCell(), RLayoutStore.getCell());
        if (folder.getParentFolder() != null) {
            RWindowInt parentWindow = folder.getParentFolder().getWindow();
            if (parentWindow != null) {
               pos = new PVector(parentWindow.getPosX() + parentWindow.getWidth() + RLayoutStore.getCell(), parentWindow.getPosY());
            }
        }
        if (nullablePosX != null) {
            pos.x = nullablePosX;
        }
        if (nullablePosY != null) {
            pos.y = nullablePosY;
        }
        boolean windowFound = findWindow(folder, setFocus, pos);
        if (!windowFound) {
            RWindowInt window = new RWindowTemp(gui.getSketch(),gui, folder, pos.x, pos.y, nullableSizeX);
            windows.add(window);
        }
        if (windowFound && folder.getParentFolder() == null) {
            folder.getWindow().setCoordinates(pos.x,pos.y);
            if (nullableSizeX != null) {
                folder.getWindow().setWidth(nullableSizeX);
            }
        }
        folder.getWindow().open(setFocus);
    }

    public void updateAndDrawWindows(PGraphics guiCanvas) { // TODO LazyGui
        if (!windowsToSetFocusOn.isEmpty()) {
            for (RWindowInt w : windowsToSetFocusOn) {
                windows.remove(w);
                windows.add(w);
            }
            windowsToSetFocusOn.clear();
        }
        for (RWindowInt win : windows) {
            win.drawWindow(guiCanvas);
        }
    }
}
