package com.old.ui.input;


import com.old.ui.input.keys.GuiKeyEvent;
import com.old.ui.input.mouse.GuiMouseEvent;

public interface UserInputSubscriber  {
    // everything is default empty
    // because I only want the implementing classes to
    // use what methods they want and not mention the rest

    /**
     * Method subscribed to PApplet input events, not meant for library users.
     * @param e event
     */
    default void keyPressed(GuiKeyEvent e) {

    }

    /**
     * Method subscribed to PApplet input events, not meant for library users.
     * @param e event
     */
    @SuppressWarnings("unused")
    default void keyReleased(GuiKeyEvent e) {

    }

    /**
     * Method subscribed to PApplet input events, not meant for library users.
     * @param e event
     */
    default void mousePressed(GuiMouseEvent e) {

    }

    /**
     * Method subscribed to PApplet input events, not meant for library users.
     * @param e event
     */
    default void mouseReleased(GuiMouseEvent e) {

    }

    /**
     * Method subscribed to PApplet input events, not meant for library users.
     * @param e event
     */
    default void mouseMoved(GuiMouseEvent e) {

    }

    /**
     * Method subscribed to PApplet input events, not meant for library users.
     * @param e event
     */
    default void mouseDragged(GuiMouseEvent e) {

    }

    /**
     * Method subscribed to PApplet input events, not meant for library users.
     * @param e event
     */
    default void mouseWheelMoved(GuiMouseEvent e) {

    }
}
