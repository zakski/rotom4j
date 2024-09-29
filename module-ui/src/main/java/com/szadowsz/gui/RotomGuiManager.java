package com.szadowsz.gui;

import com.szadowsz.gui.exception.RWindowException;
import com.szadowsz.gui.window.sketch.RWindowSketch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * GUI Management System For Multiple Windows
 */
public class RotomGuiManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RotomGuiManager.class);

    protected static List<PApplet> windows = new CopyOnWriteArrayList<>();
    protected static List<RotomGui> guis = new CopyOnWriteArrayList<>();

    /**
     * Embed GUi in Processing Sketch, with Provided Settings
     *
     * @param sketch to add the GUI to
     * @param settings user defined settings
     * @return the GUI instance
     */
    public static RotomGui embedGui(PApplet sketch, RotomGuiSettings settings){
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
        RotomGui rotomGui = new RotomGui(sketch, settings);
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
        return embedGui(sketch,new RotomGuiSettings());
    }

    /**
     * Register a Window created outside of the Manager for Global Processing
     *
     * @param external the window to be registered
     */
    public static void registerWindow(RWindowSketch external) {
        if (!windows.contains(external)) {
            windows.add(external);
            guis.add(external.getGui());
            LOGGER.info("Registered Window: {}", external.getTitle());
        }
    }

    /**
     * Unregister a Window from Global Processing
     *
     * @param external the window to be unregistered
     */
    public static void unregisterWindow(RWindowSketch external) {
        if (windows.contains(external)) {
            windows.remove(external);
            guis.remove(external.getGui());
            LOGGER.info("UnRegistered Window: {}", external.getTitle());
        }
    }
}
