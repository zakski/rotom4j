package com.szadowsz.gui.window;

import com.szadowsz.gui.RotomReferences;
import com.szadowsz.gui.exception.RWindowException;
import com.szadowsz.gui.window.external.RWindowAWT;
import com.szadowsz.gui.window.external.RWindowExt;
import com.szadowsz.gui.window.external.RWindowNEWT;
import com.szadowsz.gui.window.internal.RWindowInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Builder to Create Internal and External Windows
 */
public class RWindowBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(RWindowBuilder.class);

    private PApplet applet = RotomReferences.getApplet(); // By Default, we use the Main Window Processing Applet

    // window display name
    private String title = (applet!=null)?applet.toString():"main";

    // window coordinates
    private int xPos;
    private int yPos;

    // window dimensions
    private int width;
    private int height;

    // window renderer
    private String renderer=null;

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
            return new RWindowInt(applet, title, xPos, yPos, width, height);
        } else {
            LOGGER.debug("Constructing External Window {}", title);
            RWindowExt external = switch (renderer) {
                case PConstants.JAVA2D -> new RWindowAWT(title, xPos, yPos, width, height);
                case PConstants.P2D ->  new RWindowNEWT(title, xPos, yPos, width, height, false);
                case PConstants.P3D ->  new RWindowNEWT(title, xPos, yPos, width, height, true);
                default -> throw new RWindowException("Unexpected Renderer value: " + renderer);
            };
            String path = "--sketch-path=" + RotomReferences.getApplet().sketchPath();
            String loc = "--location=" + xPos + "," + yPos;
            String className = external.getClass().getName();
            String[] args = { path, loc, className };
            RotomReferences.getGui().registerWindow(external);

            LOGGER.debug("Running External Window {}", title);
            PApplet.runSketch(args, external);

            return external;
        }
    }
}
