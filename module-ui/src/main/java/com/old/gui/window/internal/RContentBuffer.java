package com.old.gui.window.internal;

import com.old.gui.component.RComponent;
import com.old.gui.component.folder.RFolder;
import com.old.gui.config.theme.RThemeStore;
import com.old.gui.config.RFontStore;
import com.old.gui.config.RLayoutStore;
import com.old.gui.layout.RDirection;
import com.old.gui.layout.RLayoutBase;
import com.old.gui.layout.RLinearLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.List;

import static com.old.gui.config.theme.RThemeColorType.WINDOW_BORDER;
import static processing.core.PConstants.*;

public final class RContentBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RContentBuffer.class);

    private final RWindowInt win;

    private PGraphics buffer = null;

    private boolean isBufferInvalid = true;
    private boolean isReInitRequired = true;

    private int sizeX;
    private int sizeY;

    public RContentBuffer(RWindowInt win) {
        this.win = win;
    }

    private void createBuffer(float sizeX, float sizeY) {
        createBuffer((int) sizeX, (int) sizeY);
    }

    private void createBuffer(int sizeX, int sizeY) {
        LOGGER.trace("{} Content Buffer Init - Old Size: [{},{}], New Size: [{},{}]",win.getFolder().getName(),this.sizeX,this.sizeY,sizeX,sizeY);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        buffer = win.getSketch().createGraphics(sizeX, sizeY, PConstants.P2D);
        buffer.beginDraw();
        buffer.endDraw();
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
     * @param pg Processing Graphics Context
     */
    private void drawHorizontalSeparator(PGraphics pg) {
        drawHorizontalSeparator(pg, pg.width);
    }

    /**
     * Draw A Horizontal separator between two nodes
     *
     * @param pg    Processing Graphics Context
     * @param width separator width
     */
    private void drawHorizontalSeparator(PGraphics pg, int width) {
        boolean show = RLayoutStore.isShowHorizontalSeparators();
        float weight = RLayoutStore.getHorizontalSeparatorStrokeWeight();
        if (show) {
            pg.strokeCap(SQUARE);
            pg.strokeWeight(weight);
            pg.stroke(RThemeStore.getRGBA(WINDOW_BORDER));
            pg.line(0, 0, width, 0);
        }
    }

    /**
     * Draw A Horizontal separator between two nodes
     *
     * @param pg Processing Graphics Context
     */
    private void drawVerticalSeparator(PGraphics pg) {
        //boolean show = LayoutStore.isShowHorizontalSeparators();
        float weight = RLayoutStore.getHorizontalSeparatorStrokeWeight();
        // if (show) {
        pg.strokeCap(SQUARE);
        pg.strokeWeight(weight);
        pg.stroke(RThemeStore.getRGBA(WINDOW_BORDER));
        pg.line(0, 0, 0, pg.height);
        // }
    }

    private void drawChildren(RFolder folder, float width) {
        List<RComponent> children = folder.getChildren();

        int index = 0;
        for (RComponent component : children) {
            if (!component.isVisible()) {
                index++;
                continue;
            }
            buffer.pushMatrix();
            buffer.translate(component.getRelPosX(), component.getRelPosY());
            drawChildComponent(component);
            if (index > 0) { // TODO if as to kind of separator to draw
                // separator
                if (folder.getLayout() instanceof RLinearLayout linear) {
                    buffer.pushStyle();
                    if (linear.getDirection() == RDirection.VERTICAL) {
                        drawHorizontalSeparator(buffer, (int) width);
                    } else {
                        drawVerticalSeparator(buffer);
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
     * @param folder Container of UI Nodes
     */
    private void drawContent(RFolder folder) {
        long time = System.currentTimeMillis();
        if (!folder.getChildren().isEmpty()) {
            buffer.beginDraw();
            buffer.clear();

            buffer.textFont(RFontStore.getMainFont());
            buffer.textAlign(LEFT, CENTER);
            RLayoutBase layout = folder.getLayout();
            LOGGER.debug("{} Content Buffer [{},{}] Layout {}",folder.getName(),buffer.width,buffer.height,layout);
            PVector pos = folder.getWindow().getContentStart();
            LOGGER.info("{} Layout [{},{}]",folder.getName(),folder.getWindow().contentSize.x,folder.getWindow().contentSize.y);
            layout.setCompLayout(pos,folder.getWindow().contentSize, folder.getChildren());
            drawChildren(folder, buffer.width);

            buffer.endDraw();
        }
        LOGGER.info("{} Content Buffer [{},{}] Draw Duration {}", folder.getName(),buffer.width,buffer.height,System.currentTimeMillis() - time);
    }

    private synchronized void redrawIfNecessary(){
        if (isReInitRequired){
            createBuffer(win.getContentWidth(), win.getContentHeight());
            isReInitRequired = false;
        }
        if (isBufferInvalid) {
            drawContent(win.getFolder());
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
