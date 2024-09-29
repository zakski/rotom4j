package com.old.gui.window.external;

import com.old.gui.RotomGui;
import com.old.gui.RotomGuiSettings;
import com.old.gui.window.RWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * Base Class for External Windows
 */
public abstract class RWindowExt extends PApplet implements RWindow {

    private static final Logger LOGGER = LoggerFactory.getLogger(RWindowExt.class);

    // name to display in the window title bar
    protected String title;

    // initial window position
    protected final PVector pos;

    // initial window dimensions
    protected int defaultWidth, defaultHeight;

    // True if using PConstants.P3D renderer, otherwise false
    protected boolean is3D = false;

    // Renderer Type can be either PConstants.JAVA2D, PConstants.P2D or PConstants.P3D
    protected String renderer;

    protected RotomGui gui;

    /**
     * Constructor for External Window
     *
     * @param title  title to give the window
     * @param xPos   initial X display location on Screen
     * @param yPos   initial Y display location on Screen
     * @param width  initial window width
     * @param height initial window height
     */
    public RWindowExt(String title, int xPos, int yPos, int width, int height, RotomGuiSettings s) {
        this(title, new PVector(xPos, yPos), width, height, s);

    }

    /**
     * Constructor for External Window
     *
     * @param title title to give the window
     * @param pos   PVector for initial display location
     * @param w     initial window width
     * @param h     initial window height
     */
    public RWindowExt(String title, PVector pos, int w, int h, RotomGuiSettings s) {
        super();

        this.title = title;

        this.pos = pos;

        this.defaultWidth = w;
        this.defaultHeight = h;



        registerMethods();
    }

    /**
     * Register this External Window for all standard PApplet methods.
     */
    protected void registerMethods() {
        registerMethod("pre", this);
        registerMethod("draw", this);
        registerMethod("post", this);
        registerMethod("mouseEvent", this);
        registerMethod("keyEvent", this);
        LOGGER.debug("Registered All methods with {}", title);
    }

    /**
     * Execute any draw handler for this window.
     */
    public void draw() {
        // NOOP
    }

    /**
     * Execute any pre-draw handling associated with this window and its components
     */
    public void pre() {
        // NOOP
    }

    /**
     * Execute any post-draw handling associated with this window and its controls.
     */
    public void post() {
        // NOOP
    }


    /**
     * Execute any mouse event handler associated with this window and its controls
     *
     * @param event the mouse event to process
     */
    public void mouseEvent(MouseEvent event) {
        // NOOP
    }

    /**
     * Execute any key event handler associated with this window and its controls
     *
     * @param event the key event to process
     */
    public void keyEvent(KeyEvent event) {
        // NOOP
    }


    @Override
    public void settings() {
        size(defaultWidth, defaultHeight, renderer);
    }

    @Override
    public void setup() {
        surface.setTitle(title);
        surface.setResizable(false); // TODO add Resizing Handling
        surface.setLocation((int) pos.x, (int) pos.y);
    }

    /**
     * Getter for Window's PApplet
     *
     * @return this Window
     */
    @Override
    public PApplet getSketch() {
        return this;
    }

    /**
     * Getter for Window's embedded GUI
     *
     * @return this Window's GUI Instance
     */
    public RotomGui getGui() {
        return gui;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public float getPosX() {
        return pos.x; // TODO, no way to retrieve current window location relative to Screen
    }

    @Override
    public float getPosY() {
        return pos.y; // TODO, no way to retrieve current window location relative to Screen
    }

    @Override
    public PVector getPos() {
        return new PVector(pos.x, pos.y); // TODO, no way to retrieve window location relative to Screen
    }

    @Override
    public int getWidth() {
        return width; // Returns current, rather than default width
    }

    @Override
    public int getHeight() {
        return height;  // Returns current, rather than default height
    }

    @Override
    public PVector getSize() {
        return new PVector(width, height);  // Returns current, rather than default dimensions
    }
}
