package com.szadowsz.ui.input.mouse;

import com.szadowsz.ui.constants.GlobalReferences;
import processing.core.PConstants;

import static processing.core.PConstants.CENTER;

public class GuiMouseEvent {
    private boolean consumed;
    private final float x, y, px, py;
    private final int scrollWheelRotation;
    private final int button;

    GuiMouseEvent(float x, float y, float px, float py, int button) {
        scrollWheelRotation = 0;
        this.x = x;
        this.y = y;
        this.px = px;
        this.py = py;
        this.button = button;
    }

    GuiMouseEvent(int scrollWheelRotation) {
        this.scrollWheelRotation = scrollWheelRotation;
        x = GlobalReferences.app.mouseX;
        y = GlobalReferences.app.mouseY;
        px = GlobalReferences.app.pmouseX;
        py = GlobalReferences.app.pmouseY;
        button = CENTER;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public boolean isLeft(){
        return button == PConstants.LEFT;
    }

    public boolean isRight(){
        return button == PConstants.LEFT;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getPrevX(){
        return px;
    }

    public float getPrevY(){
        return py;
    }

    public void setConsumed(boolean valueToSet) {
        consumed = valueToSet;
    }

    public int getRotation() {
        return scrollWheelRotation;
    }

    @Override
    public String toString() {
        return "x " + x + " | " + "px " + px + " | " + "y " + y + " | " + "py " + py;
    }

    public int getButton() {
        return button;
    }
}
