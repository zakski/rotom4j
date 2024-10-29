package com.szadowsz.gui.window;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RDropdownMenu;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.component.group.folder.RPanel;
import com.szadowsz.gui.component.group.folder.RToolbar;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.window.pane.RWindowPane;
import com.szadowsz.gui.window.pane.RWindowPanel;
import com.szadowsz.gui.window.pane.RWindowTemp;
import com.szadowsz.gui.window.pane.RWindowToolbar;
import processing.core.PGraphics;
import processing.core.PVector;

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


    /**
     * Check if the window is focused upon
     *
     * @param window the window to check
     * @return true if the window is focused upon, false otherwise
     */
    public boolean isFocused(RWindowPane window) {
        return windows.getLast().equals(window);
    }

    /**
     * Set the App's focus on a particular window
     *
     * @param window the window to focus on
     */
    public void setFocus(RWindowPane window) {
        windowsToSetFocusOn.add(window);
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
    public void uncoverOrCreateTempWindow(RDropdownMenu folder, boolean setFocus, Float nullablePosX, Float nullablePosY, Float nullableSizeX) {
        PVector pos = new PVector(RLayoutStore.getCell(), RLayoutStore.getCell());
        if (folder.getParentFolder() != null) {
            RWindowPane parentWindow = folder.getParentFolder().getWindow();
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
            RWindowPane window = new RWindowTemp(gui.getSketch(),gui, folder, pos.x, pos.y, folder.suggestWidth(),0);
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

    /**
     * Create or make visible a temporary window for the passed in folder
     *
     * @param folder    the corresponding folder node
     * @param setFocus      true if the window is in focus, false otherwise
     * @param nullablePosY  nullable windows y-coordinate
     */
    public void uncoverOrCreateToolbar(RToolbar folder, boolean setFocus, Float nullablePosY) {
        PVector pos = new PVector(RLayoutStore.getCell(), RLayoutStore.getCell());
        if (folder.getParentFolder() != null) {
            RWindowPane parentWindow = folder.getParentFolder().getWindow();
            if (parentWindow != null) {
                pos = new PVector(parentWindow.getPosX() + parentWindow.getWidth() + RLayoutStore.getCell(), parentWindow.getPosY());
            }
        }
        if (nullablePosY != null) {
            pos.y = nullablePosY;
        }
        boolean windowFound = findWindow(folder, setFocus, pos);
        if (!windowFound) {
            RWindowPane window = new RWindowToolbar(gui.getSketch(),gui, folder, folder.getName(), pos.y);
            windows.add(window);
        }
        if (windowFound && folder.getParentFolder() == null) {
            folder.getWindow().setCoordinates(pos.x,pos.y);
        }
        folder.getWindow().open(setFocus);
    }

    public void uncoverOrCreateToolbar(RToolbar folder) { // TODO LazyGui
        uncoverOrCreateToolbar(folder, true, null);
    }

    public void uncoverOrCreateWindow(RFolder folder) {
    }

    /**
     * Create or make visible a window for the passed in folder
     *
     * @param panel         the corresponding folder panel
     * @param setFocus      true if the window is in focus, false otherwise
     * @param nullablePosX  nullable windows x-coordinate
     * @param nullablePosY  nullable windows y-coordinate
     * @param nullableSizeX nullable windows width
     */
    public void uncoverOrCreatePanel(RPanel panel, boolean setFocus, Float nullablePosX, Float nullablePosY, Float nullableSizeX) {
        PVector pos = new PVector(RLayoutStore.getCell(), RLayoutStore.getCell());
        if (panel.getParentFolder() != null) {
            RWindowPane parentWindow = panel.getParentFolder().getWindow();
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
        boolean windowFound = findWindow(panel, setFocus, pos);
        if (!windowFound) {
            RWindowPane window = new RWindowPanel(gui.getSketch(),gui,panel, pos.x, pos.y);
            windows.add(window);
            window.open(setFocus);
        }
        if (windowFound && panel.getParent() == null) {
            panel.getWindow().setCoordinates(pos.x, pos.y);
            if (nullableSizeX != null) {
                panel.getWindow().setWidth(nullableSizeX);
            }
        }
    }

    public void uncoverOrCreatePanel(RPanel panel) {
        uncoverOrCreatePanel(panel, true, null, null, null);
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
        for (RWindowPane w : windows) {
            if (w.getFolder().getPath().equals(folder.getPath())) {
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
}
