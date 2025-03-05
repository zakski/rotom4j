package com.szadowsz.gui.layers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public abstract class RBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RBuffer.class);

    protected PGraphics buffer = null;

    protected String className = this.getClass().getSimpleName();

    protected boolean isBufferInvalid = true;
    protected boolean isReInitRequired = true;

    protected int sizeX;
    protected int sizeY;


    protected abstract String getName();

    protected abstract PApplet getSketch();

    protected void createBuffer(float sizeX, float sizeY) {
        createBuffer((int) sizeX, (int) sizeY);
    }

    protected void createBuffer(int sizeX, int sizeY) {
        LOGGER.info("{} Creation for {} - Old Size: [{},{}], New Size: [{},{}]", className, getName(), this.sizeX, this.sizeY, sizeX, sizeY);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        buffer = getSketch().createGraphics(sizeX, sizeY, PConstants.P2D);
        buffer.beginDraw();
        buffer.endDraw();
    }

    protected abstract PVector calculateBufferSize();

    /**
     * Draw The Content of The Window
     */
    protected abstract void drawContent();


    protected synchronized void reinitialisationIfNecessary() {
        if (isReInitRequired){
            PVector size = calculateBufferSize();
            createBuffer(size.x, size.y);
            isReInitRequired = false;
        }
    }

    protected synchronized void redrawIfNecessary(){
        reinitialisationIfNecessary();
        if (isBufferInvalid) {
            drawContent();
            isBufferInvalid = false;
        }
    }

    public boolean isBufferInvalid() {
        return isBufferInvalid;
    }

    public boolean isReInitRequired() {
        return isReInitRequired;
    }

    public synchronized PGraphics draw(){
        redrawIfNecessary();
        return buffer;
    }

    public synchronized void invalidateBuffer() {
        isBufferInvalid = true;
    }

    public synchronized void resetBuffer() {
        isReInitRequired = true;
        isBufferInvalid = true;
    }
}
