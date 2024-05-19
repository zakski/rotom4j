package com.szadowsz.nds4j.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PConstants;

import javax.swing.*;

public class Processing extends PApplet {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Processing.class);

    @Override
    public void settings() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Throwable e) {
        }
        size(1920, 1080, PConstants.P2D);
    }

    @Override
    public void setup() {
        surface.setTitle("NDS4J");
        surface.setResizable(true);
        surface.setLocation(100,100);
    }

    @Override
    public void draw() {
        background(30,40,189);
    }


    @Override
    public void mousePressed() {

    }
}
