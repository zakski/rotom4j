package com.szadowsz.gui;

import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.RInputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import com.szadowsz.gui.config.*;

/**
 * GUI System Representation
 */
public class RotomGui {
    private static final Logger LOGGER = LoggerFactory.getLogger(RotomGui.class);

    protected final PApplet app;
    protected final RInputHandler inputHandler;

    /**
     * Constructor for the RotomGui object which acts as a central hub for all GUI related methods within its' sketch.
     *
     * Meant to be initialized once in sketch setup() method
     *
     * Registers itself at end of the draw() method and displays the GUI whenever draw() ends.
     *
     * @param sketch main processing sketch class to display the GUI on and use keyboard and mouse input from
     * @param settings settings to apply
     */
    RotomGui(PApplet sketch, RotomGuiSettings settings) {
        app = sketch;
        inputHandler = new RInputHandler(app);
        registerListeners();
        settings.applyEarlyStartupSettings();
        RThemeStore.init();
        RFontStore.init();
//        WindowManager.addRootWindow(settings.getUseToolbarAsRoot());
        settings.applyLateStartupSettings();
    }

    /**
     * Register draw/input Methods with Processing
     */
    private void registerListeners() {
        app.registerMethod("draw", this);
        app.registerMethod("keyEvent", this);
        app.registerMethod("mouseEvent",this);
    }

    /**
     * Get the PApplet the GUi is displayed in
     *
     * @return the PApplet that the GUI is bound to
     */
    public PApplet getSketch(){
        return app;
    }
}
