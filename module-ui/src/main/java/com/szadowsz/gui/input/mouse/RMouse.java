package com.szadowsz.gui.input.mouse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PVector;
import processing.event.MouseEvent;

public final class RMouse {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMouse.class);

    private final PVector current;
    private final PVector previous;

    /**
     * A mouse pointer that stores the x and y position as well as the pressed status.
     *
     * @param x current Mouse X Coordinate
     * @param y
     */
    public RMouse(int x, int y) {
        current = new PVector(x, y);
        previous = new PVector(x, y);
    }

    private void setPreviousX(float x) {
        previous.x = x;
    }

    private void setPreviousY(float y) {
        previous.y = y;
    }

    private void setX(int x) {
        setPreviousX(current.x);
        current.x = x;
    }

    private void setY(int y) {
        setPreviousY(current.y);
        current.y = y;
    }

    private void setPosition(int x, int y) {
        setX(x);
        setY(y);
    }

    private void mouseClicked(RMouseEvent e) {
    }

    private void mouseDragged(RMouseEvent e) {
    }

    private void mouseEntered(RMouseEvent e) {
    }

    private void mouseExited(RMouseEvent e) {
    }

    private void mouseMoved(RMouseEvent e) {
    }

    private void mousePressed(RMouseEvent e) {
    }

    private void mouseReleased(RMouseEvent e) {
    }

    private void mouseWheel(RMouseEvent e) {
    }

    public void mouseEvent(MouseEvent event) {
        setPosition(event.getX(), event.getY());
        RMouseEvent e = new RMouseEvent(current.x, current.y, previous.x, previous.y, event.getButton(), event.getCount());
        switch (event.getAction()){
            case MouseEvent.ENTER -> mouseEntered(e);
            case MouseEvent.EXIT -> mouseExited(e);
            case MouseEvent.MOVE -> mouseMoved(e);
            case MouseEvent.DRAG -> mouseDragged(e);
            case MouseEvent.PRESS -> mousePressed(e);
            case MouseEvent.RELEASE -> mouseReleased(e);
            case MouseEvent.CLICK -> mouseClicked(e);
            case MouseEvent.WHEEL -> mouseWheel(e);
        }
    }
}
