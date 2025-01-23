package com.szadowsz.gui.component.group;

import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.RComponentBuffer;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLinearLayout;
import com.szadowsz.gui.window.pane.RWindowPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.List;

import static com.szadowsz.gui.config.theme.RColorType.WINDOW_BORDER;
import static processing.core.PConstants.*;

public class RGroupBuffer extends RComponentBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RGroupBuffer.class);

    private final RGroupDrawable group;

    public RGroupBuffer(RGroupDrawable group) {
        super(group);
        this.group = group;
    }

    /**
     * Draw Child Component
     *
     * @param child
     */
    private void drawChildComponent(RComponent child) {
        buffer.pushMatrix();
        buffer.pushStyle();
        child.draw(buffer);
        buffer.popStyle();
        buffer.popMatrix();
    }

    /**
     * Draw A Horizontal separator between two nodes
     *
     * @param width separator width
     */
    private void drawHorizontalSeparator(int width) {
        boolean show = RLayoutStore.isShowHorizontalSeparators();
        float weight = RLayoutStore.getHorizontalSeparatorStrokeWeight();
        if (show) {
            buffer.strokeCap(SQUARE);
            buffer.strokeWeight(weight);
            buffer.stroke(RThemeStore.getRGBA(WINDOW_BORDER));
            buffer.line(0, 0, width, 0);
        }
    }

    /**
     * Draw A Horizontal separator between two nodes
     */
    private void drawVerticalSeparator() {
        //boolean show = LayoutStore.isShowHorizontalSeparators();
        float weight = RLayoutStore.getHorizontalSeparatorStrokeWeight();
        // if (show) {
        buffer.strokeCap(SQUARE);
        buffer.strokeWeight(weight);
        buffer.stroke(RThemeStore.getRGBA(WINDOW_BORDER));
        buffer.line(0, 0, 0, buffer.height);
        // }
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

    @Override
    protected synchronized void redrawIfNecessary() {
        reinitialisationIfNecessary();
        if (isBufferInvalid) {
            // Redraws have to be done before we draw the content buffer
            group.getChildren().forEach(RComponent::drawToBuffer);

            drawContent();

            isBufferInvalid = false;
        }
    }
}
