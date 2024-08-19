package com.szadowsz.gui.window.internal;

import com.szadowsz.gui.config.theme.RThemeColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.RInputListener;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import processing.core.PApplet;
import processing.core.PGraphics;

import static com.szadowsz.ui.utils.Coordinates.isPointInRect;

/**
 * Vertical Scrollbar for Windows
 */
public class RScrollbar implements RInputListener {
    private static final float CORNER_RADIUS = 6;
    private final RWindowInt win;

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
    public RScrollbar(RWindowInt win, float xp, float yp, float sw, float sh, float wh, int l) {
        this.win = win;
        updateValues(xp, yp, sw, sh, wh, l);
    }

    private void updateValues(float xp, float yp, float sw, float sh, float wh, int l) {
        posX = xp;
        posY = yp;
        width = sw;
        height = sh;
        factor =  (wh > 0) ? sh / wh: 0;
        handleSize = (wh > 0) ? sh * factor : width;
        minReal = posY;
        maxReal = minReal + height - handleSize;
        maxMaxReal = minReal + height - width;
        variance = maxReal-minReal;
        loose = l;
    }

    /**
     * @param visible the visibility to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        this.bufferInvalid = true;
    }

    protected void updateBuffer(float posX, float posY, float windowSizeY, float windowHeight) {
        this.posX = posX;
        this.posY = posY;
        float factor = windowSizeY / windowHeight;
        if (this.factor != factor || bufferInvalid) {
            updateValues(posX, posY, width, windowSizeY, windowHeight, loose);

            buffer = win.app.createGraphics((int) width, (int) windowSizeY, PApplet.JAVA2D);
            buffer.rectMode(PApplet.CORNER);
            buffer.beginDraw();
            // Draw the track
            if (over) {
                buffer.fill(RThemeStore.getRGBA(RThemeColorType.FOCUS_BACKGROUND));
            } else {
                buffer.fill(RThemeStore.getRGBA(RThemeColorType.NORMAL_BACKGROUND));
            }
            buffer.noStroke();
            buffer.rect(8, 3, width - 8, windowSizeY - 5);

            // ****************************************
            buffer.strokeWeight(1);
            buffer.stroke(3);

            // draw thumb
            buffer.translate(0, variance * value);
            if (dragging)
                buffer.fill(RThemeStore.getRGBA(RThemeColorType.FOCUS_FOREGROUND));
            else
                buffer.fill(RThemeStore.getRGBA(RThemeColorType.NORMAL_FOREGROUND));
            buffer.rect(8, 1, width - 8, handleSize - 2, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS);

            buffer.endDraw();
            bufferInvalid = false;
        }
    }

    private void updateHandle(float mouseY) {
        float newY = Math.min(Math.max(mouseY, minReal), maxMaxReal);
        float midY = minReal + variance*value + handleSize/2;
        float midYOld = midY;
        float diffY = newY-midY;
        if (Math.abs(diffY) > 1) {
            midY = midY + (diffY)/loose;
        }
        diffY = (diffY)/loose;
        float valueDiff = diffY / Math.max(midYOld,midY);
        value = Math.min(Math.max(value + valueDiff, 0), 1);
    }

    /**
     * All GUI components are registered for mouseEvents
     */

    @Override
    public void mousePressed(RMouseEvent e) {
        if (!visible)
            return;
        if (isPointInRect(e.getX(), e.getY(), posX, posY + variance*value, width, handleSize)) {
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(RMouseEvent e) {
        if (!visible)
            return;
        if (dragging) {
            updateHandle(e.getY());
            over = isPointInRect(e.getX(), e.getY(), posX, posY+variance*value, width, handleSize);
            dragging = false;
            isValueChanging = false;
            bufferInvalid = true;
        }
    }

    @Override
    public void mouseDragged(RMouseEvent e) {
        if (!visible)
            return;

        if (dragging) {
            updateHandle(e.getY());
            isValueChanging = true;
            bufferInvalid = true;
        }
    }

    @Override
    public void mouseMoved(RMouseEvent e) {
        if (!visible)
            return;
        over = isPointInRect(e.getX(), e.getY(), posX, posY+variance*value, width, handleSize);
    }

    @Override
    public void mouseWheel(RMouseEvent e) {
        if (!visible)
            return;
        invalidateBuffer();
        value = Math.min(Math.max(value + (float) e.getRotation()/(float)loose, 0), 1);
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

    public void invalidateBuffer() {
        bufferInvalid = true;
    }
}
