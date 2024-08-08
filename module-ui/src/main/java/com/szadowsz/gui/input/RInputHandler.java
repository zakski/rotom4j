package com.szadowsz.gui.input;

import com.szadowsz.gui.input.keys.RKeyboard;
import com.szadowsz.gui.input.mouse.RMouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI key and mouse state handler.
 */
public class RInputHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RInputHandler.class);

    private final RKeyboard keyboard;
    private final RMouse mouse;


    public RInputHandler() {
        this.keyboard = new RKeyboard();
        this.mouse = new RMouse();
    }
}
