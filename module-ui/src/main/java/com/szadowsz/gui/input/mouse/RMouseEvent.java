package com.szadowsz.gui.input.mouse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PConstants;
import processing.event.MouseEvent;

import java.util.Objects;

public class RMouseEvent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMouseEvent.class);

    private boolean consumed;
    private final float x, y, px, py;
    private final int button;
    private final int scrollWheelRotation;

    private final boolean isShiftDown;
    private final boolean isControlDown;
    private final boolean isAltDown;

    public RMouseEvent(float x, float y, float px, float py, MouseEvent mouseEvent) {
        this.x = x;
        this.y = y;
        this.px = px;
        this.py = py;
        this.button = mouseEvent.getButton();
        scrollWheelRotation = mouseEvent.getCount();
        this.isShiftDown = mouseEvent.isShiftDown();
        this.isControlDown = mouseEvent.isControlDown();
        this.isAltDown = mouseEvent.isAltDown();
    }

    public float getPrevX() {
        return px;
    }

    public float getPrevY() {
        return py;
    }

    public int getRotation() {
        return scrollWheelRotation;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public boolean isLeft() {
        return button == PConstants.LEFT;
    }

    public boolean isRight() {
        return button == PConstants.RIGHT;
    }

    /**
     * Check if Alt MetaKey is Down/Pressed
     *
     * @return true if down, false otherwise
     */
    public boolean isAltDown() {
        return isAltDown;
    }

    /**
     * Check if Ctrl MetaKey is Down/Pressed
     *
     * @return true if down, false otherwise
     */
    public boolean isControlDown() {
        return isControlDown;
    }

    /**
     * Check if Shift MetaKey is Down/Pressed
     *
     * @return true if down, false otherwise
     */
    public boolean isShiftDown() {
        return isShiftDown;
    }


    /**
     * Mark the Event As Consumed
     */
    public void consume() {
        consumed = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RMouseEvent that)) return false;
        return consumed == that.consumed && Float.compare(x, that.x) == 0 && Float.compare(y, that.y) == 0 && Float.compare(px, that.px) == 0 && Float.compare(py, that.py) == 0 && button == that.button && scrollWheelRotation == that.scrollWheelRotation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumed, x, y, px, py, button, scrollWheelRotation);
    }

    @Override
    public String toString() {
        return "RMouseEvent{" +
                "consumed=" + consumed +
                ", x=" + x +
                ", y=" + y +
                ", px=" + px +
                ", py=" + py +
                ", button=" + button +
                ", scrollWheelRotation=" + scrollWheelRotation +
                '}';
    }
}
