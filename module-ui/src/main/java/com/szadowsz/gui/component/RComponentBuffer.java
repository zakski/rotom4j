package com.szadowsz.gui.component;

import com.szadowsz.gui.RBuffer;
import com.szadowsz.gui.config.text.RFontStore;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class RComponentBuffer extends RBuffer {

    protected final RComponent component;

    public RComponentBuffer(RComponent component) {
        this.component = component;
    }

    @Override
    protected String getName() {
        return component.getName();
    }

    @Override
    protected PApplet getSketch() {
        return component.getGui().getSketch();
    }

    @Override
    protected PVector calculateBufferSize() {
        return component.getBufferSize();
    }

    @Override
    protected void drawContent() {
        buffer.beginDraw();
        buffer.clear();
        buffer.push();
        buffer.textFont(RFontStore.getMainFont());
        buffer.textAlign(PApplet.LEFT, PApplet.CENTER);
        component.drawContent(buffer);
        buffer.pop();
        buffer.endDraw();
    }

    public synchronized PGraphics redraw(){
        redrawIfNecessary();
        return buffer;
    }

    public synchronized PGraphics draw(){
        // redrawIfNecessary(); Have to take this out to avoid confusing Processing
        return buffer;
    }
}
