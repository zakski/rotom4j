package com.szadowsz.gui.window.internal;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.window.RWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Base Class for Internal Windows
 */
public class RWindowInt implements RWindow {

    private static final Logger LOGGER = LoggerFactory.getLogger(RWindowInt.class);

    // PApplet to draw in
    private final PApplet app;

    private final RotomGui gui;

    // Window Title
    private final String title;

    // current position
    private final PVector pos;

    // current dimensions
    private final PVector size;

    /**
     * Constructor for Internal Window
     *
     * @param app PApplet to render window inside
     * @param title title to give the window
     * @param xPos initial X display location in PApplet
     * @param yPos initial Y display location in PApplet
     * @param width initial window width
     * @param height initial window height
     */
    public RWindowInt(PApplet app, RotomGui gui, String title, int xPos, int yPos, int width, int height) {
        this(app,gui,title, new PVector(xPos, yPos), new PVector(width, height));
    }

    /**
     * Constructor for Internal Window
     *
     * @param app PApplet to render window inside
     * @param title title to give the window
     * @param pos initial display location in PApplet
     * @param size initial window dimensions
     */
    public RWindowInt(PApplet app, RotomGui gui, String title, PVector pos, PVector size) {
        this.app = app;
        this.gui = gui;
        this.title = title;
        this.pos = pos;
        this.size = size;
    }

    @Override
    public PApplet getApp() {
        return app;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public float getPosX(){
        return pos.x;
    }

    @Override
    public float getPosY(){
        return pos.y;
    }

    @Override
    public PVector getPos() {
        return new PVector(pos.x,pos.y);
    }

    @Override
    public int getWidth(){
        return (int) size.x;
    }

    @Override
    public int getHeight(){
        return (int) size.y;
    }

    @Override
    public PVector getSize() {
        return new PVector(size.x,size.y);
    }

    public boolean isVisible() {
        return false;
    }

    public void reinitialiseBuffer() {
    }
}
