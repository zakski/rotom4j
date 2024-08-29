package com.szadowsz.gui.test;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.RotomGuiManager;
import com.szadowsz.gui.RotomGuiSettings;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.folder.RDropdownMenu;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.gui.input.mouse.RMouseAction;
import com.szadowsz.gui.layout.RBorderLayout;
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

        gui = RotomGuiManager.embedGui(this,settings);
        gui.setLayout(new RBorderLayout());
//        gui.pushPane("File Pane", RBorderLayout.RLocation.LEFT);
//        gui.checkbox("test1",true);
//        gui.checkbox("test2",false);
//        gui.popWindow();

        gui.pushToolbar("Titlebar", RBorderLayout.RLocation.TOP);
        RButton test3 = gui.button("test3");
        test3.registerAction(RActivateByType.RELEASE, new RMouseAction() {
            @Override
            public void execute() {
                System.out.println("TEST3"); // MAT DAMON
            }
        });

        RDropdownMenu options = gui.pushDropdown("Options");
        RButton test4 = gui.button("test4");
        test4.registerAction(RActivateByType.RELEASE, new RMouseAction() {
            @Override
            public void execute() {
                System.out.println("TEST4"); // MAT DAMON
            }
        });
        gui.popWindow();

        gui.popWindow();

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
