package com.szadowsz.gui.window;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.RotomGuiManager;
import com.szadowsz.gui.RotomGuiSettings;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.exception.RWindowException;
import com.szadowsz.gui.window.internal.RWindowImpl;
import com.szadowsz.gui.window.sketch.RWindowAWT;
import com.szadowsz.gui.window.sketch.RWindowSketch;
import com.szadowsz.gui.window.sketch.RWindowJOGL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Builder to Create Internal and External Windows
 */
public class RWindowBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(RWindowBuilder.class);

    private PApplet applet = null;

    // window display name
    private String title = "main";

    // window coordinates
    private int xPos;
    private int yPos;

    // window dimensions
    private int width;
    private int height;

    // window renderer
    private String renderer=null;

    // parent window GUI
    private RotomGui gui;

    // window GUI settings
    private RotomGuiSettings settings = null;
    private RFolder folder;

    /**
     * Default Constructor For Builder
     */
    public RWindowBuilder() {}

    /**
     * Configure the PApplet the Window is drawn in
     *
     * @param p PApplet to use if an internal window
     */
    public void setPApplet(PApplet p) {
        this.applet = p;
    }

    /**
     * Configure the RotomGui the Window is drawn under
     *
     * @param g RotomGui to use if an internal window
     */
    public void setGui(RotomGui g) {
        this.gui = g;
    }

    /**
     * Configure the Folder the Window belongs to
     *
     * @param f Folder internal window belongs to
     */
    public void setFolder(RFolder f) {
        this.folder = f;
    }

    /**
     * Configure the Title Of The Window
     *
     * @param t expected title
     */
    public void setTitle(String t) {
        this.title = t;
    }

    /**
     * Configure the Position Of The Window
     *
     * @param x expected X Coordinate
     * @param y expected Y Coordinate
     */
    public void setPosition(int x, int y){
        xPos = x;
        yPos = y;
    }

    /**
     * Configure the Dimensions Of The Window
     *
     * @param width expected width
     * @param height expected height
     */
    public void setSize(int width, int height){
        this.width = width;
        this.height = height;
    }

    /**
     * Configure the Renderer
     *
     * @param r PConstants.JAVA2D, PConstants.P2D or PConstants.P3D
     */
    public void setRenderer(String r){
        renderer = r;
    }

    /**
     * Configure the Renderer
     *
     * @param s user defined GUI Settings
     */
    public void setGuiSettings(RotomGuiSettings s){
        settings = s;
    }

    /**
     * Construct and register The Window
     *
     * @return newly Created Window
     */
    public RWindow build(){
        if (width <= 0 || height <= 0){
            throw new RWindowException("Width And/OR Height supplied cannot be less than or equal to 0");
        }

        if (renderer == null) {
            LOGGER.debug("Constructing Internal Window {}", title);
            if (applet == null){
                throw new RWindowException("PApplet supplied cannot be null");
            }
            if (gui == null){
                throw new RWindowException("RotomGui supplied cannot be null");
            }
            if (folder == null){
                throw new RWindowException("RFolder supplied cannot be null");
            }
            return new RWindowImpl(applet, gui, folder, title, xPos, yPos, width, height);
        } else {
            LOGGER.debug("Constructing External Window {}", title);
            RWindowSketch external = switch (renderer) {
                case PConstants.JAVA2D -> new RWindowAWT(title, xPos, yPos, width, height, settings);
                case PConstants.P2D ->  new RWindowJOGL(title, xPos, yPos, width, height, settings, false);
                case PConstants.P3D ->  new RWindowJOGL(title, xPos, yPos, width, height, settings, true);
                default -> throw new RWindowException("Unexpected Renderer value: " + renderer);
            };
            String path = "--sketch-path=" + external.sketchPath();
            String loc = "--location=" + xPos + "," + yPos;
            String className = external.getClass().getName();
            String[] args = { path, loc, className };
            RotomGuiManager.registerWindow(external);

            LOGGER.debug("Running External Window {}", title);
            PApplet.runSketch(args, external);

            return external;
        }
    }
}
