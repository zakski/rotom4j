package com.szadowsz.ui.input.mouse;

import com.szadowsz.ui.node.NodeTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Mouse {

    protected Logger LOGGER = LoggerFactory.getLogger(Mouse.class);

    protected PVector current;
    protected PVector previous;

    protected boolean isPressed = false;
    protected List<MouseListener> handlers = new CopyOnWriteArrayList<>();

    /**
     * A mouse pointer that stores the x and y position as well as the pressed status.
     *
     * @param x
     * @param y
     */
    public Mouse(int x, int y) {
       current = new PVector(x, y);
       previous = new PVector(x, y);
    }

    public int getX() {
        return (int) current.x;
    }

    public int getY() {
            return (int) current.y;
    }

    public PVector getCurrent() {
        return current;
    }

    public int getDX(){
        return getX() - getPX();
    }

    public int getDY() {
        return getY() - getPY();
    }

    public PVector getDelta() {
        return new PVector(getDX(),getDY());
    }

    public int getPX() {
        return (int) previous.x;
    }

    public int getPY(){
        return (int) previous.y;
    }

    public PVector getPrevious() {
        return previous;
    }

    public boolean isPressed(){
        return isPressed;
    }

    protected void setPreviousX(float x) {
        previous.x = x;
    }

    protected void setPreviousY(float y) {
        previous.y = y;
    }


    public Mouse setPosition(int x, int y)  {
        setX(x);
        setY(y);
        return this;
    }

    public Mouse setX(int x) {
        setPreviousX(current.x);
        current.x = x;
        return this;
    }

    public Mouse setY(int y) {
        setPreviousY(current.y);
        current.y = y;
        return this;
    }

    public Mouse setPressed(boolean flag) {
        isPressed = flag;
        return this;
    }

    public void mouseEvent(MouseEvent event) {
        setPosition(event.getX(), event.getY());
         if (event.getAction() == MouseEvent.MOVE) {
             GuiMouseEvent e = new GuiMouseEvent(current.x, current.y, previous.x, previous.y, event.getButton());
             handlers.forEach(l -> l.mouseMovedEvent(e));
             if (!e.isConsumed()){
                 NodeTree.setAllNodesMouseOverToFalse();
             }
         } else if (event.getAction() == MouseEvent.DRAG){
             GuiMouseEvent e = new GuiMouseEvent(current.x, current.y, previous.x, previous.y, event.getButton());
             handlers.forEach(l -> l.mouseDraggedEvent(e));
        }else if (event.getAction() == MouseEvent.PRESS) {
            setPressed(true);
             GuiMouseEvent e = new GuiMouseEvent(current.x, current.y, previous.x, previous.y, event.getButton());
             handlers.forEach(l -> l.mousePressedEvent(e));

        } else if (event.getAction() == MouseEvent.RELEASE) {
             setPressed(false);
             GuiMouseEvent e = new GuiMouseEvent(current.x, current.y, previous.x, previous.y, event.getButton());
             handlers.forEach(l -> l.mouseReleasedEvent(e));

        } else if (event.getAction() == MouseEvent.WHEEL) {
             GuiMouseEvent e = new GuiMouseEvent(event.getCount());
             handlers.forEach(l -> l.mouseWheelEvent(e));
        }
    }

    public void attachHandler(MouseListener handle) {
      handlers.add(handle);
    }

    public void detachHandler(MouseListener handle){
        handlers.remove(handle);
    }
}
