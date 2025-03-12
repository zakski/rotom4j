package com.szadowsz.gui.component.utils;

import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.bined.RBinMain;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import static com.szadowsz.gui.utils.RCoordinates.isPointInRect;

/**
 * Vertical/Horizontal Scrollbar for Components
 */
public class RComponentScrollbar {
    private static final Logger LOGGER = LoggerFactory.getLogger(RComponentScrollbar.class);

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

    protected void updateBuffer(float posX, float posY, float componentDisplayHeight, float componentHeight) {
        float factor = componentDisplayHeight / componentHeight;
        if (this.factor != factor || bufferInvalid) {
            updateValues(posX, posY, width, componentDisplayHeight, componentHeight, loose);

            buffer = component.getGui().getSketch().createGraphics((int) width, (int) componentDisplayHeight, PApplet.JAVA2D);
            buffer.rectMode(PApplet.CORNER);
            buffer.beginDraw();
            // Draw the track
            if (over) {
                buffer.fill(RThemeStore.getRGBA(RColorType.FOCUS_BACKGROUND));
            } else {
                buffer.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND));
            }
            buffer.noStroke();
            buffer.rect(8, 3, width - 8, componentDisplayHeight - 5);

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

    public synchronized void invalidateBuffer() {
        bufferInvalid = true;
    }

    public synchronized float getPosX() {
        return posX;
    }

    public synchronized float getPosY() {
        return posY;
    }

    public synchronized float getHeight() {
        return height;
    }

    public synchronized float getWidth() {
        return width;
    }

    public synchronized float getValue() {
        return value;
    }

    public synchronized boolean isMouseOver() {
        return over;
    }

    public synchronized boolean isDragged() {
        return dragging;
    }

    public synchronized boolean isVisible() {
        return visible;
    }

    /**
     * @param visible the visibility to set
     */
    public synchronized void setVisible(boolean visible) {
        this.visible = visible;
        this.bufferInvalid = true;
    }

    public synchronized void draw(PGraphics pg, float posX, float posY, float windowSizeX) {
        if (!visible)
            return;
        pg.pushMatrix();
        pg.translate(posX + windowSizeX, posY);
        pg.image(buffer, 0, 0);
        pg.popMatrix();
    }

    public void drawToBuffer(float posX, float posY, float windowSizeX, float windowSizeY, float windowHeight) {
        updateBuffer(posX + windowSizeX, posY, windowSizeY, windowHeight);
    }

    /**
     * All GUI components are registered for mouseEvents
     */
    public synchronized void mouseMoved(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (!visible)
            return;
        over = isPointInRect(mouseEvent.getX(), adjustedMouseY, posX, posY + variance * value, width, handleSize);
    }

    public synchronized void mousePressed(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (!visible)
            return;
        if (isPointInRect(mouseEvent.getX(), adjustedMouseY, posX, posY + variance * value, width, handleSize)) {
            LOGGER.debug("Mouse pressed for {} scrollbar confirmed",component.getName());
            over = true;
            dragging = true;
        }
    }


    public synchronized void mouseReleased(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (!visible)
            return;
        if (dragging) {
            LOGGER.debug("Mouse released for {} scrollbar confirmed",component.getName());
            updateHandle(adjustedMouseY);
            over = isPointInRect(mouseEvent.getX(), adjustedMouseY, posX, posY + variance * value, width, handleSize);
            dragging = false;
            isValueChanging = false;
            bufferInvalid = true;
        }
    }


    public synchronized void mouseDragged(RMouseEvent mouseEvent) {
        if (!visible)
            return;

        if (dragging) {
            LOGGER.debug("Mouse dragged for {} scrollbar confirmed",component.getName());
            updateHandle(mouseEvent.getY());
            isValueChanging = true;
            bufferInvalid = true;
        }
    }

    public synchronized void mouseWheel(RMouseEvent mouseEvent) {
        if (!visible)
            return;
        invalidateBuffer();
        value = Math.min(Math.max(value + (float) mouseEvent.getRotation() / (float) loose, 0), 1);
    }

    public synchronized void updateCoordinates(float x, float y, float width, float height, float componentActualHeight) {
        updateValues(x, y, width, height, componentActualHeight, loose);
    }
}