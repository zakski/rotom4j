package com.szadowsz.gui.component.utils;

import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import static com.old.ui.utils.Coordinates.isPointInRect;

/**
 * Vertical/Horizontal Scrollbar for Components
 */
public class RComponentScrollbar {
    private static final float CORNER_RADIUS = 6;
    private final RComponent component;

    protected float filler = .5f;

    /* Is the scrollbar visible or not */
    protected boolean visible;
    protected boolean isValueChanging = false;

    // Scrollbar has an image buffer which is only redrawn if it has been invalidated
    protected PGraphics buffer = null;
    protected boolean bufferInvalid = true;

    // vertical scrollbar position info
    private float posX; // x-coordinate
    private float posY; // y-coordinate

    private float width;
    private float height;

    protected float value = 0.0f;   // percentage scroll bar is between min and real height
    private float handleSize;       // Size of the scroll handle
    private float factor;           // difference between unconstrained and constrained height
    private float minReal;          // minimum real height
    private float maxReal;          // maximum real height
    private float maxMaxReal;          // maximum real height
    private float variance;
    int loose;                      // how loose/heavy

    protected boolean over;           // is the mouse over the scroll handle?
    protected boolean dragging = false;

    /**
     * Create the scroll bar
     */
    protected RComponentScrollbar(RComponent component, float xp, float yp, float sw, float sh, float wh, int l) {
        this.component = component;
        updateValues(xp, yp, sw, sh, wh, l);
    }

    public RComponentScrollbar(RComponent component, PVector scrollbarStart, PVector scrollbarBounds, float y, int i) {
        this(component,scrollbarStart.x,scrollbarStart.y,scrollbarBounds.x,scrollbarBounds.y,y,i);
    }

    private void updateValues(float xp, float yp, float sw, float sh, float wh, int l) {
        posX = xp;
        posY = yp;
        width = sw;
        height = sh;
        factor = (wh > 0) ? sh / wh : 0;
        handleSize = (wh > 0) ? sh * factor : width;
        minReal = posY;
        maxReal = minReal + height - handleSize;
        maxMaxReal = minReal + height - width;
        variance = maxReal - minReal;
        loose = l;
    }

    public float getValue() {
        return value;
    }

    public boolean isDragging() {
        return dragging;
    }

    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible the visibility to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        this.bufferInvalid = true;
    }

    public void draw(PGraphics pg, float posX, float posY, float windowSizeX, float windowSizeY, float windowHeight) {
        if (!visible || windowSizeY <= 0)
            return;
        pg.pushMatrix();
        updateBuffer(posX + windowSizeX, posY, windowSizeY, windowHeight);
        pg.translate(posX + windowSizeX, posY);
        pg.image(buffer, 0, 0);
        pg.popMatrix();
    }

    protected void updateBuffer(float posX, float posY, float componentSizeY, float componentHeight) {
        this.posX = posX;
        this.posY = posY;
        float factor = componentSizeY / componentHeight;
        if (this.factor != factor || bufferInvalid) {
            updateValues(posX, posY, width, componentSizeY, componentHeight, loose);

            buffer = component.getGui().getSketch().createGraphics((int) width, (int) componentSizeY, PApplet.JAVA2D);
            buffer.rectMode(PApplet.CORNER);
            buffer.beginDraw();
            // Draw the track
            if (over) {
                buffer.fill(RThemeStore.getRGBA(RColorType.FOCUS_BACKGROUND));
            } else {
                buffer.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND));
            }
            buffer.noStroke();
            buffer.rect(8, 3, width - 8, componentSizeY - 5);

            // ****************************************
            buffer.strokeWeight(1);
            buffer.stroke(3);

            // draw thumb
            buffer.translate(0, variance * value);
            if (dragging)
                buffer.fill(RThemeStore.getRGBA(RColorType.FOCUS_FOREGROUND));
            else
                buffer.fill(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND));
            buffer.rect(8, 1, width - 8, handleSize - 2, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS);

            buffer.endDraw();
            bufferInvalid = false;
        }
    }

    private void updateHandle(float mouseY) {
        float newY = Math.min(Math.max(mouseY, minReal), maxMaxReal);
        float midY = minReal + variance * value + handleSize / 2;
        float midYOld = midY;
        float diffY = newY - midY;
        if (Math.abs(diffY) > 1) {
            midY = midY + (diffY) / loose;
        }
        diffY = (diffY) / loose;
        float valueDiff = diffY / Math.max(midYOld, midY);
        value = Math.min(Math.max(value + valueDiff, 0), 1);
    }

    public void invalidateBuffer() {
        bufferInvalid = true;
    }

    /**
     * All GUI components are registered for mouseEvents
     */
    public void mouseMoved(RMouseEvent mouseEvent) {
        if (!visible)
            return;
        over = isPointInRect(mouseEvent.getX(), mouseEvent.getY(), posX, posY + variance * value, width, handleSize);
    }

    public void mousePressed(RMouseEvent mouseEvent) {
        if (!visible)
            return;
        if (isPointInRect(mouseEvent.getX(), mouseEvent.getY(), posX, posY + variance * value, width, handleSize)) {
            dragging = true;
        }
    }


    public void mouseReleased(RMouseEvent mouseEvent) {
        if (!visible)
            return;
        if (dragging) {
            updateHandle(mouseEvent.getY());
            over = isPointInRect(mouseEvent.getX(), mouseEvent.getY(), posX, posY + variance * value, width, handleSize);
            dragging = false;
            isValueChanging = false;
            bufferInvalid = true;
        }
    }


    public void mouseDragged(RMouseEvent mouseEvent) {
        if (!visible)
            return;

        if (dragging) {
            updateHandle(mouseEvent.getY());
            isValueChanging = true;
            bufferInvalid = true;
        }
    }

    public void mouseWheel(RMouseEvent mouseEvent) {
        if (!visible)
            return;
        invalidateBuffer();
        value = Math.min(Math.max(value + (float) mouseEvent.getRotation() / (float) loose, 0), 1);
    }
}