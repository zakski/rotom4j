package com.krab.lazy.input.mouse;

/**
 * Interface to base reactions to mouse input off.
 *
 * @author Zakski : 25/09/2015.
 */
public interface MouseListener {
    /**
     * Method to handle object's reaction to the mouse being moved.
     *
     * @param curr the current location of the mouse.
     * @param prev the previous location of the mouse.
     */
    void mouseMovedEvent(LazyMouseEvent e);

    /**
     * Method to handle object's reaction to the mouse being moved.
     *
     * @param curr the current location of the mouse.
     * @param prev the previous location of the mouse.
     */
    void mouseDraggedEvent(LazyMouseEvent e);

    /**
     * Method to handle object's reaction to the mouse being pressed.
     */
    void mousePressedEvent(LazyMouseEvent e);

    /**
     * Method to handle object's reaction to the mouse being released.
     */
    void mouseReleasedEvent(LazyMouseEvent e);

    /**
     * Method to process mouse wheel movement while the object is in focus.
     *
     * @note movement will be negative if the mouse wheel was rotated up or away from the user, and positive in the other
     *       direction. On Mac OS X, this will be reversed when "natural" scrolling is enabled in System Preferences.
     *
     * @param movement the change made by the mouse wheel.
     */
    void mouseWheelEvent(LazyMouseEvent e);

}
