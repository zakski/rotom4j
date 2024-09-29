package com.old.gui.input.mouse;

import com.old.gui.RotomGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RMouse {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMouse.class);

    private final RotomGui gui;

    private final PVector current;
    private final PVector previous;

    private final List<RMouseListener> handlers = new CopyOnWriteArrayList<>();

    /**
     * A mouse pointer that stores the x and y position as well as the pressed status.
     *
     * @param x current Mouse X Coordinate
     * @param y
     */
    public RMouse(RotomGui gui, int x, int y) {
        this.gui = gui;
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
        for (RMouseListener listener : handlers) {
            listener.mouseClicked(e);
            if (e.isConsumed()) {
                break;
            }
        }
    }

    private void mouseDragged(RMouseEvent e) {
        for (RMouseListener listener : handlers) {
            listener.mouseDragged(e);
            if (e.isConsumed()) {
                break;
            }
        }
    }

    private void mouseEntered(RMouseEvent e) {
        gui.resetInput();
    }

    private void mouseExited(RMouseEvent e) {
        gui.resetInput();
    }

    private void mouseMoved(RMouseEvent e) {
        for (RMouseListener listener : handlers) {
            listener.mouseMoved(e);
            if (e.isConsumed()) {
                break;
            }
        }
    }

    private void mousePressed(RMouseEvent e) {
        for (RMouseListener listener : handlers) {
            listener.mousePressed(e);
            if (e.isConsumed()) {
                break;
            }
        }
    }

    private void mouseReleased(RMouseEvent e) {
        for (RMouseListener listener : handlers) {
            listener.mouseReleased(e);
            if (e.isConsumed()) {
                break;
            }
        }
    }

    private void mouseWheel(RMouseEvent e) {
        for (RMouseListener listener : handlers) {
            listener.mouseWheel(e);
            if (e.isConsumed()) {
                break;
            }
        }
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

    public void setFocus(RMouseListener subscriber) {
        handlers.remove(subscriber);
        handlers.addFirst(subscriber);
    }

    public void subscribe(RMouseListener subscriber) {
        handlers.addFirst(subscriber);
    }
}
