package com.szadowsz.ui.window;

import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.constants.theme.ThemeColorType;
import com.szadowsz.ui.constants.theme.ThemeStore;
import com.szadowsz.ui.input.UserInputSubscriber;
import com.szadowsz.ui.input.mouse.GuiMouseEvent;
import processing.awt.PGraphicsJava2D;
import processing.core.PApplet;
import processing.core.PGraphics;

import static com.szadowsz.ui.store.LayoutStore.cell;

public class Scrollbar implements UserInputSubscriber {
    private static final float CORNER_RADIUS = 6;
    private static final int TRACK = 5;

    protected float value = 0.0f;
    protected float filler = .5f;

    /**
     * Whether to show background or not
     */
    protected boolean autoHide = true;

    /* Is the scrollbar visible or not */
    protected boolean visible = true;
    protected boolean isValueChanging = false;
    protected boolean dragging = false;

    // Scrollbar has an image buffer which is only redrawn if it has been invalidated
    protected PGraphics buffer = null;
    protected boolean bufferInvalid = true;
    protected int currSpot = -1;
    private float last_ox;
    private float last_oy;
    private float oldFactor;
    private float height;

    /**
     * Create the scroll bar
     *
     * @param theApplet
     */
    public Scrollbar(PApplet theApplet) {
    }

    /**
     * If set to true then the scroll bar is only displayed when needed.
     *
     * @param autoHide true if only displayed when needed,false otherwise
     */
    public void setAutoHide(boolean autoHide) {
        if (this.autoHide != autoHide) {
            this.autoHide = autoHide;
            if (this.autoHide && filler > 0.99999f) {
                visible = false;
            }
            bufferInvalid = true;
        }
    }

    /**
     * @param visible the visibility to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Set the position of the thumb. If the value forces the thumb past the end of
     * the scrollbar, reduce the filler.
     *
     * @param value must be in the range 0.0 to 1.0
     */
    public void setValue(float value) {
        if (value + filler > 1) {
            filler = 1 - value;
        }
        this.value = value;
        if (autoHide && filler > 0.99999f) {
            visible = false;
        } else {
            visible = true;
        }
        bufferInvalid = true;
    }

    /**
     * Set the value and the thumb size. Force the value to be valid depending on
     * filler.
     *
     * @param value  must be in the range 0.0 to 1.0
     * @param filler must be >0 and <= 1
     */
    public void setValue(float value, float filler) {
        if (value + filler > 1) {
            value = 1 - filler;
        }
        this.value = value;
        this.filler = filler;
        if (autoHide && this.filler > 0.99999f) {
            visible = false;
        } else {
            visible = true;
        }
        bufferInvalid = true;
    }

    protected void updateBuffer(float windowSizeY, float windowHeight, boolean isScrollbarHighlighted) {
        float factor = windowSizeY / windowHeight;
        if (oldFactor != factor || bufferInvalid) {
            buffer = (PGraphicsJava2D) GlobalReferences.app.createGraphics((int) cell, (int) windowSizeY, PApplet.JAVA2D);
            buffer.rectMode(PApplet.CORNER);
            buffer.beginDraw();
            buffer.endDraw();
            oldFactor = factor;
            height = windowSizeY;
            bufferInvalid = false;
            buffer.beginDraw();
            // Draw the track
            if (isScrollbarHighlighted) {
                buffer.fill(ThemeStore.getColor(ThemeColorType.FOCUS_BACKGROUND));
            } else {
                buffer.fill(ThemeStore.getColor(ThemeColorType.NORMAL_BACKGROUND));
            }
            buffer.noStroke();
            buffer.rect(8, 3, cell - 8, windowSizeY - 5);

            // ****************************************
            buffer.strokeWeight(1);
            buffer.stroke(3);

            // draw thumb
            float thumbHeight = windowSizeY * oldFactor;
            buffer.translate(0, windowSizeY * value);
            if (currSpot == 10)
                buffer.fill(ThemeStore.getColor(ThemeColorType.FOCUS_FOREGROUND));
            else
                buffer.fill(ThemeStore.getColor(ThemeColorType.NORMAL_FOREGROUND));
            buffer.rect(8, 1, cell - 8, thumbHeight - 2, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS);

            buffer.endDraw();
        }
    }


    /**
     * All GUI components are registered for mouseEvents
     */

    @Override
    public void mousePressed(GuiMouseEvent e) {
        if (!visible)
            return;

        dragging = false;
        last_ox = e.getX();
        last_oy = e.getY();
    }

    @Override
    public void mouseReleased(GuiMouseEvent e) {
        if (!visible)
            return;
        if (dragging) {
            dragging = false;
            isValueChanging = false;
            bufferInvalid = true;
        }
    }

    @Override
    public void mouseDragged(GuiMouseEvent e) {
        if (!visible)
            return;
//        if (spot == 10) {
        float movement = e.getY() - last_oy;
        last_ox = e.getY();
        float deltaV = movement / (height - 32);
        value += deltaV;
        value = PApplet.constrain(value, 0, 1.0f - filler);
        isValueChanging = true;
        bufferInvalid = true;
        dragging = true;
//        }
    }

    @Override
    public void mouseMoved(GuiMouseEvent e) {
        if (!visible)
            return;

//        calcTransformedOrigin(e.getX(), e.getY());
//        int spot = whichHotSpot(ox, oy);
    }

    @Override
    public void mouseWheelMoved(GuiMouseEvent event) {
        if (!visible)
            return;

//        calcTransformedOrigin(winApp.mouseX, winApp.mouseY);

//        int spot = whichHotSpot(ox, oy);

        // If over the track then see if we are over the thumb
//        if (spot >= 9 && isOverThumb(ox, oy)) {
//            spot = 10;
//        }
//
//        if (spot != currSpot) {
//            currSpot = spot;
//            bufferInvalid = true;
//        }
//
//        if (currSpot >= 0 || focusIsWith == this)
//            cursorIsOver = this;
//        else if (cursorIsOver == this)
//            cursorIsOver = null;
//
//
//        if (currSpot > -1 && z >= focusObjectZ()) {
//            float pv = value + event.getCount() * 0.01f * G4P.wheelForScrollbar;
//            pv = pv < 0 ? 0 : pv > 1 ? 1 : pv;
//            setValue(pv, filler);
//            isValueChanging = true;
//            bufferInvalid = true;
//            dragging = true;
//        }
    }

    public void draw(PGraphics pg, float posX, float posY, float windowSizeX, float windowSizeY, float windowHeight, boolean isScrollbarHighlighted) {
        if (!visible)
            return;
        pg.pushMatrix();
        updateBuffer(windowSizeY, windowHeight, isScrollbarHighlighted);
        pg.translate(posX + windowSizeX, posY);
        pg.image(buffer, 0, 0);
        pg.popMatrix();
    }

    public void invalidateBuffer() {
        bufferInvalid=true;
    }
}
