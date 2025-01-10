package com.szadowsz.gui.component.group;

import com.szadowsz.gui.component.RComponent;
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

public final class RGroupBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RGroupBuffer.class);

    private final RGroupDrawable group;

    private PGraphics buffer = null;

    private boolean isBufferInvalid = true;
    private boolean isReInitRequired = true;

    private int sizeX;
    private int sizeY;

    public RGroupBuffer(RGroupDrawable group, float sizeX, float sizeY) {
        this.group = group;
        createBuffer(sizeX, sizeY);
    }

    private void createBuffer(float sizeX, float sizeY) {
        createBuffer((int) sizeX, (int) sizeY);
    }

    private void createBuffer(int sizeX, int sizeY) {
        LOGGER.trace("{} Content Buffer Init - Old Size: [{},{}], New Size: [{},{}]",group.getName(),this.sizeX,this.sizeY,sizeX,sizeY);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        buffer = group.getGui().getSketch().createGraphics(sizeX, sizeY, PConstants.P2D);
        try {
            buffer.beginDraw();
            buffer.endDraw();
        } catch (final Exception e) {}
    }

//    private float calcColWidth(List<RComponent> children, int col) {
//        float spaceForName = 0;
//        float spaceForValue = 0;
//
//        for (RComponent child : children) {
//            if (col != child.getColumn())
//                continue;
//
//            float nameTextWidth = child.calcNameTextWidth();
//            float valueTextWidth = child.findValueTextWidthRoundedUpToWholeCells();
//
//            spaceForName = Math.max(spaceForName, nameTextWidth);
//            spaceForValue = Math.max(spaceForValue, valueTextWidth);
//        }
//
//        return spaceForName + spaceForValue;
//    }

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

    private void drawChildren(float width) {
        List<RComponent> children = group.getChildren();

        int index = 0;
        for (RComponent component : children) {
            if (!component.isVisible()) {
                index++;
                continue;
            }
            buffer.pushMatrix();
            PVector relPos = component.getRelPosTo(group);
            buffer.translate(relPos.x, relPos.y);
            drawChildComponent(component);
            if (index > 0) { // TODO if as to kind of separator to draw
                // separator
                if (group.getLayout() instanceof RLinearLayout linear) {
                    buffer.pushStyle();
                    if (linear.getDirection() == RDirection.VERTICAL) {
                        drawHorizontalSeparator((int) width);
                    } else {
                        drawVerticalSeparator();
                    }
                    buffer.popStyle();
                }
            }
            index++;
            buffer.popMatrix();
        }
    }

    /**
     * Draw The Content of The Window
     *
     */
    private void drawContent() {
        long time = System.currentTimeMillis();
        if (!group.getChildren().isEmpty()) {
            buffer.beginDraw();
            buffer.clear();
            buffer.textFont(RFontStore.getMainFont());
            buffer.textAlign(LEFT, CENTER);
            RLayoutBase layout = group.getLayout();
            LOGGER.debug("{} Content Buffer [{},{}] Layout {}",group.getName(),buffer.width,buffer.height,layout);
            PVector pos = group.getPosition();
            LOGGER.debug("{} Layout [{},{}]",group.getName(),group.getWidth(),group.getHeight());
            layout.setCompLayout(pos,group.getSize(), group.getChildren());
            drawChildren(buffer.width);

            buffer.endDraw();
        }
        LOGGER.debug("{} Content Buffer [{},{}] Draw Duration {}", group.getName(),buffer.width,buffer.height,System.currentTimeMillis() - time);
    }

    private synchronized void redrawIfNecessary(){
        if (isReInitRequired){
            createBuffer(group.getWidth(), group.getHeight());
            isReInitRequired = false;
        }
        if (isBufferInvalid) {
            drawContent();
            isBufferInvalid = false;
        }
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
