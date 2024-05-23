package com.szadowsz.nds4j.app;

import com.szadowsz.ui.NDSGui;
import com.szadowsz.ui.NDSGuiSettings;
import com.szadowsz.ui.node.impl.GDropList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PConstants;

import javax.swing.*;

public class Processing extends PApplet {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Processing.class);

    protected NDSGui gui;
    protected NDSGuiSettings settings;
    private GDropList bonelist;

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Throwable e) {
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
        gui = new NDSGui(this,settings);
//        bonelist = new GDropList(this, 100, 20, 200, 150, 8, 20);
//        bonelist.setItems(new String[]{"test 1", "test 2"}, 0);
    }

    @Override
    public void draw() {
        background(30,40,189);
    }


    @Override
    public void mousePressed() {

    }
}
