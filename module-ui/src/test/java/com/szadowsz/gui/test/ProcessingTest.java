package com.szadowsz.gui.test;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.RotomGuiManager;
import com.szadowsz.gui.RotomGuiSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PConstants;

import javax.swing.*;
import java.util.prefs.Preferences;


public class ProcessingTest extends PApplet {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ProcessingTest.class);

    public final static Preferences prefs = Preferences.userNodeForPackage(ProcessingTest.class);

    protected RotomGui gui;
    protected RotomGuiSettings settings;


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

        gui = RotomGuiManager.embedGui(this,settings);
//        RegisterGeneralUI.buildFileDropdown(gui);
//        RegisterGeneralUI.buildViewDropdown(gui);
//        RegisterGeneralUI.buildOptionsDropdown(gui);
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
