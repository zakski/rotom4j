package com.old.ui.input;

import com.old.ui.input.mouse.GuiMouseEvent;

public interface MouseInteractable extends UserInputSubscriber {

    /**
     * Method to handle the controller's reaction to the mouse being pressed.
     *
     * @param e the change made by the mouse was pressed.
     */
    public void mousePressedEvent(GuiMouseEvent e);

    /**
     * Method to handle the controller's reaction to the mouse being released over node.
     *
     * @param e the change made by the mouse was released.
     */
    public default void mouseReleasedOverNodeEvent(GuiMouseEvent e){

    }

    /**
     * Method to handle the controller's reaction to the mouse being released anywhere.
     *
     * @param e the change made by the mouse was released anywhere.
     */
    public void mouseReleasedAnywhereEvent(GuiMouseEvent e);

    /**
     * Method to process mouse wheel moving while controller is in focus.
     *
     * @note movement will be negative if the mouse wheel was rotated up or away from the user, and positive in the other
     *       direction. On Mac OS X, this will be reversed when "natural" scrolling is enabled in System Preferences.
     *
     * @param e the change made by the mouse wheel.
     */
    public default void mouseWheelEvent(GuiMouseEvent e){
        
    }

    public void mouseDragNodeContinueEvent(GuiMouseEvent e);
}
