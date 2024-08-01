package com.szadowsz.nds4j.app;

import com.szadowsz.nds4j.app.nodes.control.RegisterUI;
import com.szadowsz.ui.NDSGuiSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PConstants;

import javax.swing.*;
import java.util.prefs.Preferences;


public class Processing extends PApplet {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Processing.class);

    public final static Preferences prefs = Preferences.userNodeForPackage(Processing.class);

    protected NDSGuiImpl gui;
    protected NDSGuiSettings settings;


    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void settings() {
        setLookAndFeel();
        size(1920, 1080, PConstants.P2D);
    }

    @Override
    public void setup() {
        surface.setTitle("NDS4J");
        surface.setResizable(true);
        surface.setLocation(100,100);
        gui = new NDSGuiImpl(this,settings);
        RegisterUI.buildFileDropdown(gui);
        RegisterUI.buildViewDropdown(gui);
        RegisterUI.buildOptionsDropdown(gui);
    }

    @Override
    public void draw() {
        background(30,40,189);
    }


    @Override
    public void mousePressed() {
        // NOOP
    }
}
