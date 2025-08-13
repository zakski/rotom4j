package com.szadowsz.gui.test;

import com.szadowsz.gui.RotomGuiSettings;
import com.szadowsz.gui.layout.RBorderLayout;
import com.szadowsz.rotom4j.app.RotomGuiImpl;
import com.szadowsz.rotom4j.app.RotomGuiManagerImpl;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.n2d.narc.NARC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PConstants;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;


public class ProcessingTest extends PApplet {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ProcessingTest.class);

    public final static Preferences prefs = Preferences.userNodeForPackage(ProcessingTest.class);

    protected RotomGuiImpl gui;
    protected RotomGuiSettings settings;


    private final String path = "C:\\Code\\pokemon\\srcs\\changes\\Gen 4\\prologmon-masters\\pm_dp_ose\\src\\graphic\\font.narc";

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void settings() {
        LOGGER.debug("DO Settings");
        setLookAndFeel();
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = (int) Math.floor(gd.getDisplayMode().getWidth() * 0.8);
        int height = (int) Math.floor(gd.getDisplayMode().getHeight() * 0.8);

        size(width, height, PConstants.P2D);
    }

    @Override
    public void setup() {
        LOGGER.debug("DO setup");
        surface.setTitle("Rotom4J");
        surface.setResizable(true);
        surface.setLocation(100,100);

        gui = (RotomGuiImpl) RotomGuiManagerImpl.embedGui(this,settings);

        gui.startSetup();

        RBorderLayout layout = new RBorderLayout();
        layout.setSpacing(8,8,8,8);
        gui.setLayout(layout);

        gui.pushFolder("Window", RBorderLayout.RLocation.CENTER);
        try {
            gui.narc("narc",new NARC(path));
        } catch (NitroException e) {
            throw new RuntimeException(e);
        }
        gui.popWindow();


        gui.endSetup();

//        RegisterGeneralUI.buildFileDropdown(gui);
//        RegisterGeneralUI.buildViewDropdown(gui);
//        RegisterGeneralUI.buildOptionsDropdown(gui);
    }

    @Override
    public void draw() {
        if (frameCount == 0){
            LOGGER.debug("DO Draw");
        }
        background(30,40,189);
    }


    @Override
    public void mousePressed() {
        // NOOP
    }
}
