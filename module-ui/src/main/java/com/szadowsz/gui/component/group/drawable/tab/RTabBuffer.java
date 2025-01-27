package com.szadowsz.gui.component.group.drawable.tab;

import com.szadowsz.gui.component.group.RGroupBuffer;
import com.szadowsz.gui.config.text.RFontStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PConstants;

public class RTabBuffer extends RGroupBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RTabBuffer.class);

    private final RTabManager manager;

    public RTabBuffer(RTabManager manager) {
        super(manager);
        this.manager =manager;
    }

    @Override
    protected void createBuffer(int sizeX, int sizeY) {
        LOGGER.debug("{} Creation for {} - Old Size: [{},{}], New Size: [{},{}]", className, getName(), this.sizeX, this.sizeY, sizeX, sizeY);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        buffer = getSketch().createGraphics(sizeX, sizeY, PConstants.P2D);
        buffer.beginDraw();
        buffer.endDraw();
    }

    @Override
    protected void drawContent() {
        buffer.beginDraw();
        buffer.clear();
        buffer.push();
        buffer.textFont(RFontStore.getMainFont());
        buffer.textAlign(PApplet.LEFT, PApplet.CENTER);
        manager.drawContent(buffer);
        buffer.pop();
        buffer.endDraw();
    }
}
