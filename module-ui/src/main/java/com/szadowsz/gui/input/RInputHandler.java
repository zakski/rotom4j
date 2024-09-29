package com.szadowsz.gui.input;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.input.keys.RKeyboard;
import com.szadowsz.gui.input.mouse.RMouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     * @param g use the underlying applet to establish current mouse position
     */
    public RInputHandler(RotomGui gui) {
        this.keyboard = new RKeyboard(5000L,33L);
        this.mouse = new RMouse(gui,gui.getSketch().mouseX,gui.getSketch().mouseY);
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

    public void setFocus(RInputListener subscriber){
        mouse.setFocus(subscriber);
        keyboard.setFocus(subscriber);
    }

    public void subscribe(RInputListener subscriber) {
        mouse.subscribe(subscriber);
        keyboard.subscribe(subscriber);
    }

    public void reset() {
        keyboard.clear();
    }
}
