package com.szadowsz.gui.test;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.RotomGuiManager;
import com.szadowsz.gui.RotomGuiSettings;
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

        gui = RotomGuiManager.embedGui(this,settings);
        gui.startSetup();
//        RBorderLayout layout = new RBorderLayout();
//        layout.setSpacing(8,8,8,8);
//        gui.setLayout(layout);
//        gui.pushPane("File Pane", RBorderLayout.RLocation.LEFT);
//        gui.checkbox("test1",true);
//        gui.toggle("test2",false);
//        gui.colorPicker("color1",new Color(0,0,255));
//        gui.slider("slider1",125,0,255);
//        gui.slider("slider2",125.0f,0.0f,255.0f);
//        gui.popWindow();
//
//        gui.pushToolbar("Titlebar", RBorderLayout.RLocation.TOP);
//        RButton test3 = gui.button("test3");
//        test3.registerAction(RActivateByType.RELEASE, new RMouseAction() {
//            @Override
//            public void execute() {
//                System.out.println("TEST3"); // MAT DAMON
//            }
//        });
//
//        RDropdownMenu options = gui.pushDropdown("Options");
//        RButton test4 = gui.button("test4");
//        test4.registerAction(RActivateByType.RELEASE, new RMouseAction() {
//            @Override
//            public void execute() {
//                System.out.println("TEST4"); // MAT DAMON
//            }
//        });
//        gui.popWindow();
//
//        gui.popWindow();
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
