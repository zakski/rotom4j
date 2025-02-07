package com.szadowsz.gui.component.group;

import com.szadowsz.gui.component.RComponentBuffer;
import com.szadowsz.gui.config.text.RFontStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static processing.core.PConstants.*;

public class RGroupBuffer extends RComponentBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RGroupBuffer.class);

    private final RGroupDrawable group;

    public RGroupBuffer(RGroupDrawable group) {
        super(group);
        this.group = group;
    }

    /**
     * Draw The Content of The Window
     *
     */
    @Override
    protected void drawContent() {
        long time = System.currentTimeMillis();
        group.updateChildrenCoordinates();
        if (!group.getChildren().isEmpty()) {
            buffer.beginDraw();
            buffer.clear();
            buffer.textFont(RFontStore.getMainFont());
            buffer.textAlign(LEFT, CENTER);
            group.drawContent(buffer);
            buffer.endDraw();
        }
        LOGGER.debug("{} Content Buffer [{},{}] Draw Duration {}", group.getName(),buffer.width,buffer.height,System.currentTimeMillis() - time);
    }
}
