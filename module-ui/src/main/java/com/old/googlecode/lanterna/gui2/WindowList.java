/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.old.googlecode.lanterna.gui2;

import com.old.googlecode.lanterna.gui2.Window.Hint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author ginkoblongata
 */
public class WindowList {
    
    private final List<Window> windows = new LinkedList<>();
    private final List<Window> stableOrderingOfWindows = new ArrayList<>();
    
    private Window activeWindow = null;
    private boolean hadWindowAtSomePoint = false;

    public List<Window> getWindowsInZOrder() {
        return Collections.unmodifiableList(windows);
    }
    
    public List<Window> getWindowsInStableOrder() {
        return Collections.unmodifiableList(stableOrderingOfWindows);
    }
    
    public void setActiveWindow(Window activeWindow) {
        this.activeWindow = activeWindow;
        if (activeWindow != null) {
            moveToTop(activeWindow);
        }
    }

    public Window getActiveWindow() {
        return activeWindow;
    }
    
    public void addWindow(Window window) {
        if (!stableOrderingOfWindows.contains(window)) {
            stableOrderingOfWindows.add(window);
        }
        if(!windows.contains(window)) {
            windows.add(window);
        }
        if(!window.getHints().contains(Hint.NO_FOCUS)) {
            setActiveWindow(window);
        }
        hadWindowAtSomePoint = true;
    }
    
    /**
     * Removes the window from this WindowList.
     * @return true if this WindowList contained the specified Window
     */
    public boolean removeWindow(Window window) {
        boolean contained = windows.remove(window);
        stableOrderingOfWindows.remove(window);
        
        if(activeWindow == window) {
            // in case no suitable window is found, so pass control back to the background pane
            setActiveWindow(null);
            
            //Go backward in reverse and find the first suitable window
            for(int index = windows.size() - 1; index >= 0; index--) {
                Window candidate = windows.get(index);
                if(!candidate.getHints().contains(Hint.NO_FOCUS)) {
                    setActiveWindow(candidate);
                    break;
                }
            }
        }
        
        return contained;
    }
    
    public boolean isHadWindowAtSomePoint() {
        return hadWindowAtSomePoint;
    }

    public void moveToTop(Window window) {
        if(!windows.contains(window)) {
            throw new IllegalArgumentException("Window " + window + " isn't in MultiWindowTextGUI " + this);
        }
        windows.remove(window);
        windows.add(window);
    }
    
    public void moveToBottom(Window window) {
        if(!windows.contains(window)) {
            throw new IllegalArgumentException("Window " + window + " isn't in MultiWindowTextGUI " + this);
        }
        windows.remove(window);
        windows.add(0, window);
    }
    /**
     * Switches the active window by cyclically shuffling the window list. If {@code reverse} parameter is {@code false}
     * then the current top window is placed at the bottom of the stack and the window immediately behind it is the new
     * top. If {@code reverse} is set to {@code true} then the window at the bottom of the stack is moved up to the
     * front and the previous top window will be immediately below it
     * @param reverse Direction to cycle through the windows
     */
    public WindowList cycleActiveWindow(boolean reverse) {
        if(windows.isEmpty() || windows.size() == 1 || (activeWindow != null && activeWindow.getHints().contains(Hint.MODAL))) {
            return this;
        }
        Window originalActiveWindow = activeWindow;
        Window nextWindow;
        if(activeWindow == null) {
            // Cycling out of active background pane
            nextWindow = reverse ? windows.get(windows.size() - 1) : windows.get(0);
        } else {
            // Switch to the next window
            nextWindow = getNextWindow(reverse, activeWindow);
        }

        int noFocusWindows = 0;
        while(nextWindow.getHints().contains(Hint.NO_FOCUS)) {
            ++noFocusWindows;
            if(noFocusWindows == windows.size()) {
                // All windows are NO_FOCUS, so give up
                return this;
            }
            nextWindow = getNextWindow(reverse, nextWindow);
            if(nextWindow == originalActiveWindow) {
                return this;
            }
        }

        if(reverse) {
            moveToTop(nextWindow);
        } else if (originalActiveWindow != null) {
            moveToBottom(originalActiveWindow);
        }
        setActiveWindow(nextWindow);
        return this;
    }

    private Window getNextWindow(boolean reverse, Window window) {
        int index = windows.indexOf(window);
        if(reverse) {
            if(++index >= windows.size()) {
                index = 0;
            }
        } else {
            if(--index < 0) {
                index = windows.size() - 1;
            }
        }
        return windows.get(index);
    }
}
