package com.szadowsz.gui.input;

import com.szadowsz.gui.input.keys.RKeyboard;
import com.szadowsz.gui.input.mouse.RMouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * GUI key and mouse state handler.
 */
public class RInputHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RInputHandler.class);

    private final RKeyboard keyboard;
    private final RMouse mouse;

    /**
     * Constructor to Initialise Input Handling Components
     *
     * @param p use the applet to establish current mouse position
     */
    public RInputHandler(PApplet p) {
        this.keyboard = new RKeyboard();
        this.mouse = new RMouse(p.mouseX,p.mouseY);
    }

    /**
     * Process the KeyEvent And notify aLl Subscribers
     *
     * @param event event to process
     */
    public void keyEvent(KeyEvent event) {
        keyboard.keyEvent(event);
    }

    /**
     * Process the MouseEvent And notify aLl Subscribers
     *
     * @param event event to process
     */
    public void mouseEvent(MouseEvent event) {
        mouse.mouseEvent(event);
    }
}
