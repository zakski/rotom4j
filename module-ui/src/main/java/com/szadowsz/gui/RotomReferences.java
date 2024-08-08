package com.szadowsz.gui;

import processing.core.PApplet;

/**
 * Global GUI References
 */
public class RotomReferences {

    private static PApplet app = null;
    private static RotomGui gui = null;

    /**
     * Retrieve The Main Window
     *
     * @return The Main PApplet
     */
    public synchronized static PApplet getApplet(){
        return app;
    }

    /**
     * Retrieve The GUI Singleton
     *
     * @return RotomGui singleton instance
     */
    public synchronized static RotomGui getGui(){
        if(gui == null){
            gui = new RotomGui();
        }
        return gui;
    }
}

