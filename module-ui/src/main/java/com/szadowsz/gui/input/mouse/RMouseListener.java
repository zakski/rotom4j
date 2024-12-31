package com.szadowsz.gui.input.mouse;

public interface RMouseListener {

    /**
     * Method to handle the component's reaction to the mouse being pressed.
     *
     * @param e the change made by the mouse
     */
    void mousePressed(RMouseEvent e);

    /**
     * Method to handle the component's reaction to the mouse being released.
     *
     * @param e the change made by the mouse
     */
    void mouseReleased(RMouseEvent e);


    /**
     * Method to handle the component's reaction to the mouse being dragged.
     *
     * @param e the change made by the mouse
     */
    void mouseDragged(RMouseEvent e);


    /**
     * Method to handle the component's reaction to the mouse being moved.
     *
     * @param e the change made by the mouse
     */
    void mouseMoved(RMouseEvent e);

    /**
     * a mouse button is clicked (pressed and released)
     *
     * @param e the change made by the mouse
     */
    void mouseClicked(RMouseEvent e);

    /**
     * the mouse cursor enters the unobscured part of component's geometry
     *
     * @param e the change made by the mouse
     */
    void mouseEntered(RMouseEvent e);

    /**
     * the mouse cursor leaves the unobscured part of component's geometry
     *
     * @param e the change made by the mouse
     */
    void mouseExited(RMouseEvent e);

    /**
     * Method to process mouse wheel moving
     *
     * @note movement will be negative if the mouse wheel was rotated up or away from the user, and positive in the other
     *       direction. On Mac OS X, this will be reversed when "natural" scrolling is enabled in System Preferences.
     *
     * @param e the change made by the mouse wheel.
     */
    void mouseWheel(RMouseEvent e);
}
