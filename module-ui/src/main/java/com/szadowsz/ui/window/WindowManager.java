package com.szadowsz.ui.window;

import com.szadowsz.ui.node.NodeTree;
import com.szadowsz.ui.store.LayoutStore;
import processing.core.PGraphics;
import processing.core.PVector;

import com.szadowsz.ui.node.impl.FolderNode;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 */
public class WindowManager {
    private static final CopyOnWriteArrayList<Window> windows = new CopyOnWriteArrayList<>(); // TODO should be set?
    private static final ArrayList<Window> windowsToSetFocusOn = new ArrayList<>();

    /**
     * Found out if the window
     *
     * @param folderNode    the corresponding folder node
     * @param setFocus      true if the window is in focus, false otherwise
     * @param pos           expected position
     * @return true if found, false otherwise
     */
    private static boolean findWindow(FolderNode folderNode, boolean setFocus, PVector pos) {
        boolean windowFound = false;
        for (Window w : windows) {
            if (w.folder.path.equals(folderNode.path)) {
                w.posX = pos.x;
                w.posY = pos.y;
                if (LayoutStore.doesFolderRowClickCloseWindowIfOpen() && w.isVisible) {
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
     * Method to add the root window to be managed
     */
    public static void addRootWindow(boolean createToolbar) {
        if (createToolbar){
            addWindow(new Window(NodeTree.getOrCreateRoot(createToolbar), 0,0, LayoutStore.cell * LayoutStore.defaultWindowWidthInCells));
        } else {
            addWindow(new Window(NodeTree.getOrCreateRoot(createToolbar), LayoutStore.cell, LayoutStore.cell, LayoutStore.cell * LayoutStore.defaultWindowWidthInCells));
        }
    }

    /**
     * Method to add a window to be managed
     *
     * @param window the window to add
     */
    public static void addWindow(Window window) {
        windows.add(window);
    }

    /**
     * Create or make visible a window for the passed in folder
     *
     * @param folderNode the corresponding folder node
     */
    public static void uncoverOrCreateWindow(FolderNode folderNode) {
        uncoverOrCreateWindow(folderNode, true, null, null, null);
    }

    /**
     * Create or make visible a window for the passed in folder
     *
     * @param folderNode    the corresponding folder node
     * @param setFocus      true if the window is in focus, false otherwise
     * @param nullablePosX
     * @param nullablePosY
     * @param nullableSizeX
     */
    public static void uncoverOrCreateWindow(FolderNode folderNode, boolean setFocus, Float nullablePosX, Float nullablePosY, Float nullableSizeX) {
        PVector pos = new PVector(LayoutStore.cell, LayoutStore.cell);
        if (folderNode.parent != null) {
            Window parentWindow = folderNode.parent.window;
            if (parentWindow != null) {
                pos = new PVector(parentWindow.posX + parentWindow.windowSizeX + LayoutStore.cell, parentWindow.posY);
            }
        }
        if (nullablePosX != null) {
            pos.x = nullablePosX;
        }
        if (nullablePosY != null) {
            pos.y = nullablePosY;
        }
        boolean windowFound = findWindow(folderNode, setFocus, pos);
        if (!windowFound) {
            Window window = new Window(folderNode, pos.x, pos.y, nullableSizeX);
            windows.add(window);
            window.open(setFocus);
        }
        if (windowFound && folderNode.parent == null) {
            folderNode.window.posX = pos.x;
            folderNode.window.posY = pos.y;
            if (nullableSizeX != null) {
                folderNode.window.windowSizeX = nullableSizeX;
            }
        }
    }

    /**
     * Create or make visible a window for the passed in folder
     *
     * @param folderNode the corresponding folder node
     */
    public static void uncoverOrCreateTempWindow(FolderNode folderNode) {
        uncoverOrCreateTempWindow(folderNode, true, null, null, null);
    }

    /**
     * Create or make visible a window for the passed in folder
     *
     * @param folderNode    the corresponding folder node
     * @param setFocus      true if the window is in focus, false otherwise
     * @param nullablePosX
     * @param nullablePosY
     * @param nullableSizeX
     */
    public static void uncoverOrCreateTempWindow(FolderNode folderNode, boolean setFocus, Float nullablePosX, Float nullablePosY, Float nullableSizeX) {
        PVector pos = new PVector(LayoutStore.cell, LayoutStore.cell);
        if (folderNode.parent != null) {
            Window parentWindow = folderNode.parent.window;
            if (parentWindow != null) {
                pos = new PVector(parentWindow.posX + parentWindow.windowSizeX + LayoutStore.cell, parentWindow.posY);
            }
        }
        if (nullablePosX != null) {
            pos.x = nullablePosX;
        }
        if (nullablePosY != null) {
            pos.y = nullablePosY;
        }
        boolean windowFound = findWindow(folderNode, setFocus, pos);
        if (!windowFound) {
            Window window = new TempWindow(folderNode, pos.x, pos.y, nullableSizeX);
            windows.add(window);
            window.open(setFocus);
        }
        if (windowFound && folderNode.parent == null) {
            folderNode.window.posX = pos.x;
            folderNode.window.posY = pos.y;
            if (nullableSizeX != null) {
                folderNode.window.windowSizeX = nullableSizeX;
            }
        }
    }

    /**
     * Update and draw all windows
     *
     * @param pg graphics context
     */
    public static void updateAndDrawWindows(PGraphics pg) {
        if (!windowsToSetFocusOn.isEmpty()) {
            for (Window w : windowsToSetFocusOn) {
                windows.remove(w);
                windows.add(w);
            }
            windowsToSetFocusOn.clear();
        }
        for (Window win : windows) {
            win.drawWindow(pg);
        }
    }

    /**
     * Check if the window is focused upon
     *
     * @param window the window to check
     * @return true if the window is focused upon, false otherwise
     */
    static boolean isFocused(Window window) {
        return windows.get(windows.size() - 1).equals(window);
    }

    /**
     * Set the App's focus on a particular window
     *
     * @param window the window to focus on
     */
    public static void setFocus(Window window) {
        windowsToSetFocusOn.add(window);
    }

    /**
     * Close all windows, except for the root
     */
    public static void closeAllWindows() {
        for (Window win : windows) {
            if (!win.isRoot()) {
                win.close();
            }
        }
    }

    /**
     * Snap all open unmoving windows to the grid
     */
    public static void snapAllStaticWindowsToGrid() {
        for (Window w : windows) {
            if (!w.isVisible || w.isBeingDragged) {
                continue;
            }
            PVector newPos = SnapToGrid.trySnapToGrid(w.posX, w.posY);
            w.posX = newPos.x;
            w.posY = newPos.y;
            w.windowSizeX = SnapToGrid.trySnapToGrid(w.windowSizeX, 0).x;
        }
    }

}
