package com.szadowsz.rotom4j.app;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.RotomGuiManager;
import com.szadowsz.gui.RotomGuiSettings;
import com.szadowsz.gui.exception.RWindowException;
import com.szadowsz.gui.window.sketch.RWindowSketch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;

public class RotomGuiManagerImpl extends RotomGuiManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RotomGuiManagerImpl.class);

    public static RotomGui embedGui(PApplet sketch, RotomGuiSettings settings){
        return RotomGuiManager.embedGui(sketch, settings);
    }

    /**
     * Embed GUi in Processing Sketch, with Provided Settings
     *
     * @param sketch to add the GUI to
     * @param settings user defined settings
     * @return the GUI instance
     */
    public static RotomGuiImpl embedGuiImpl(PApplet sketch, RotomGuiSettings settings){
        if (sketch == null){
            throw new RWindowException("sketch cannot be null");
        }
        if (windows.contains(sketch)){
            throw new RWindowException("sketch " + sketch + "  cannot be registered twice");
        }
        if (settings == null){
            settings = new RotomGuiSettings();
        }
        LOGGER.info("Embedding GUI for Window: {}", sketch);
        RotomGuiImpl rotomGui = new RotomGuiImpl(sketch, settings);
        guis.add(rotomGui);
        windows.add(sketch);
        return rotomGui;
    }
    /**
     * Embed GUi in Processing Sketch, with Default Settings
     *
     * @param sketch to add the GUI to
     * @return the GUI instance
     */
    public static RotomGui embedGui(PApplet sketch){
        return RotomGuiManager.embedGui(sketch);
    }

    /**
     * Register a Window created outside of the Manager for Global Processing
     *
     * @param external the window to be registered
     */
    public static void registerWindow(RWindowSketch external) {
        RotomGuiManager.registerWindow(external);
    }

    /**
     * Unregister a Window from Global Processing
     *
     * @param external the window to be unregistered
     */
    public static void unregisterWindow(RWindowSketch external) {
        RotomGuiManager.unregisterWindow(external);
    }
}
