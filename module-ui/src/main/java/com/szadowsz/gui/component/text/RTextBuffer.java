package com.szadowsz.gui.component.text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.awt.PGraphicsJava2D;
import processing.core.PApplet;
import processing.core.PGraphics;

public final class RTextBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RTextBuffer.class);

    private final PApplet sketch;
    private final RTextBase text;

    private PGraphicsJava2D buffer = null;

    private boolean isBufferInvalid = true;
    private boolean isReInitRequired = true;

    private int sizeX;
    private int sizeY;

    public RTextBuffer(PApplet sketch, RTextBase component) {
        this.sketch = sketch;
        this.text = component;
    }

    private void createBuffer(float sizeX, float sizeY) {
        createBuffer((int) sizeX, (int) sizeY);
    }

    private void createBuffer(int sizeX, int sizeY) {
        LOGGER.trace("{} Text Buffer Init - Old Size: [{},{}], New Size: [{},{}]",text.getName(),this.sizeX,this.sizeY,sizeX,sizeY);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        buffer = (PGraphicsJava2D) sketch.createGraphics(sizeX, sizeY, PApplet.JAVA2D);
        buffer.rectMode(PApplet.CORNER);
        buffer.beginDraw();
        buffer.endDraw();
    }
    
    private synchronized void redrawIfNecessary(){
        if (isReInitRequired){
            createBuffer(text.getWidth(), text.getHeight());
            isReInitRequired = false;
        }
        if (isBufferInvalid) {
            drawText();
            isBufferInvalid = false;
        }
    }

    private void drawText() {
        buffer.beginDraw();
        text.display(buffer);
        buffer.endDraw();
    }

    public PGraphicsJava2D getNative(){
        return buffer;
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
