package com.szadowsz.gui.window.pane;

import com.szadowsz.gui.layers.RBuffer;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLinearLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.List;

import static com.szadowsz.gui.config.theme.RColorType.WINDOW_BORDER;
import static processing.core.PConstants.*;

public class RWinBuffer extends RBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RWinBuffer.class);

    protected final RWindowPane win;
    protected final RFolder folder;

    public RWinBuffer(RWindowPane win) {
        this.win = win;
        this.folder = win.getFolder();
    }

    @Override
    protected String getName() {
        return win.getTitle();
    }

    @Override
    protected PApplet getSketch() {
        return win.getSketch();
    }

    @Override
    protected PVector calculateBufferSize() {
        return new PVector(win.getContentWidth(),win.getContentHeight());
    }

    /**
     * Draw Child Component
     *
     * @param child
     */
    protected void drawChildComponent(RComponent child) {
        buffer.push();
        child.draw(buffer);
        buffer.pop();
    }

    /**
     * Draw A Horizontal separator between two nodes
     *
     * @param width separator width
     */
    protected void drawHorizontalSeparator(int width) {
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
    protected void drawVerticalSeparator() {
        //boolean show = LayoutStore.isShowHorizontalSeparators();
        float weight = RLayoutStore.getHorizontalSeparatorStrokeWeight();
        // if (show) {
        buffer.strokeCap(SQUARE);
        buffer.strokeWeight(weight);
        buffer.stroke(RThemeStore.getRGBA(WINDOW_BORDER));
        buffer.line(0, 0, 0, buffer.height);
        // }
    }

    protected void drawSeparator(int width) {
        if (folder.getLayout() instanceof RLinearLayout linear) {
            buffer.push();
            if (linear.getDirection() == RDirection.VERTICAL) {
                drawHorizontalSeparator(width);
            } else {
                drawVerticalSeparator();
            }
            buffer.pop();
        }
    }

    protected void drawChildren(float width) {
        List<RComponent> children = folder.getChildren();

        int index = 0;
        for (RComponent component : children) {
            if (!component.isVisible()) {
                index++;
                continue;
            }
            buffer.push();
            buffer.translate(component.getRelPosX(), component.getRelPosY());
            drawChildComponent(component);
            if (index > 0) { // TODO if as to kind of separator to draw
                // separator
                drawSeparator((int) width);
            }
            index++;
            buffer.pop();
        }
        drawSeparator((int) width);
    }

    @Override
    protected void drawContent() {
        long time = System.currentTimeMillis();
        if (!folder.getChildren().isEmpty()) {
            buffer.beginDraw();
            buffer.clear();

            buffer.textFont(RFontStore.getMainFont());
            buffer.textAlign(LEFT, CENTER);
            drawChildren(buffer.width);

            buffer.endDraw();
        }
        LOGGER.debug("{} Content Buffer [{},{}] Draw Duration {}", folder.getName(),buffer.width,buffer.height,System.currentTimeMillis() - time);
    }

    @Override
    protected synchronized void reinitialisationIfNecessary() {
        if (isReInitRequired){
            PVector size = calculateBufferSize();
            createBuffer(size.x, size.y);

            // Resizings have to be done before we draw the content buffer
            RLayoutBase layout = folder.getLayout();
            LOGGER.debug("{} Win Buffer [{},{}] Layout {}",folder.getName(),buffer.width,buffer.height,layout);
            PVector pos = folder.getWindow().getContentStart();
            LOGGER.debug("{} Layout [{},{}]",folder.getName(),folder.getWindow().contentSize.x,folder.getWindow().contentSize.y);
            layout.setCompLayout(pos,folder.getWindow().contentSize, folder.getChildren());

            isReInitRequired = false;
        }
    }

    @Override
    protected synchronized void redrawIfNecessary() {
        reinitialisationIfNecessary();
        if (isBufferInvalid) {
            // Redraws have to be done before we draw the content buffer
            folder.getChildren().forEach(RComponent::drawToBuffer);

            drawContent();

            isBufferInvalid = false;
        }
    }
}
