package com.szadowsz.gui.input.keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.event.KeyEvent;

public final class RKeyboard {
    private static final Logger LOGGER = LoggerFactory.getLogger(RKeyboard.class);


    private void pressKey(RKeyEvent e) {
    }

    private void releaseKey(RKeyEvent e) {
    }

    private void typedKey(RKeyEvent e) {
    }


    /**
     * Process the KeyEvent And notify aLl Subscribers
     *
     * @param event event to process
     */
    public void keyEvent(KeyEvent event)  {
        RKeyEvent e = new RKeyEvent(event);
        LOGGER.debug("KeyEvent: {}", e); // TODO Consider Need to Note Window here
        // Call reaction methods contextually
        switch (event.getAction()){
            case KeyEvent.PRESS -> pressKey(e);
            case KeyEvent.RELEASE -> releaseKey(e);
            case KeyEvent.TYPE -> typedKey(e);
        }
    }


}
