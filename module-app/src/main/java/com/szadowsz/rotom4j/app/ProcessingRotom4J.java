package com.szadowsz.rotom4j.app;

import com.szadowsz.gui.RotomGuiSettings;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.layout.RBorderLayout;
import com.szadowsz.rotom4j.component.control.RegisterGeneralUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;


public class ProcessingRotom4J extends PApplet {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ProcessingRotom4J.class);

    public final static Preferences prefs = Preferences.userNodeForPackage(ProcessingRotom4J.class);

    protected RotomGuiImpl gui;
    protected RotomGuiSettings settings;
//    private PGraphics buffer0;
//    private PGraphics buffer1;

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void settings() {
        setLookAndFeel();

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = (int) Math.floor(gd.getDisplayMode().getWidth() * 0.8);
        int height = (int) Math.floor(gd.getDisplayMode().getHeight() * 0.8);

        size(width, height, PConstants.P2D);
    }

    @Override
    public void setup() {
        surface.setTitle("Rotom4J");
        surface.setResizable(true);
        surface.setLocation(100,100);
//        RFontStore.init(this);
//
//
//        buffer0 = createGraphics(500,500, PConstants.P2D);
//        buffer1 = createGraphics(300,300, PConstants.P2D);
        gui = RotomGuiManagerImpl.embedGuiImpl(this,settings);
        gui.startSetup();

        RBorderLayout layout = new RBorderLayout();
        layout.setSpacing(8,8,8,8);
        gui.setLayout(layout);

        gui.pushToolbar("Titlebar", RBorderLayout.RLocation.TOP);
        RegisterGeneralUI.buildFileDropdown(gui);
        //RegisterGeneralUI.buildViewDropdown(gui);
        RegisterGeneralUI.buildOptionsDropdown(gui);
        gui.popWindow();

        gui.pushPanel("Loaded Files", RBorderLayout.RLocation.LEFT);
        gui.popWindow();

        gui.endSetup();
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
