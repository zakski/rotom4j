package com.szadowsz.gui.window.internal;

import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.config.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.List;

import static com.szadowsz.gui.config.theme.RThemeColorType.WINDOW_BORDER;
import static processing.core.PConstants.*;

public class RContentBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RContentBuffer.class);

    private final RWindowInt win;

    private PGraphics buffer = null;

    private boolean isBufferInvalid = true;
    private boolean isReInitRequired = true;

    public RContentBuffer(RWindowInt win) {
        this.win = win;
    }

    private void createBuffer(float sizeX, float sizeY) {
        createBuffer((int) sizeX, (int) sizeY);
    }

    private void createBuffer(int sizeX, int sizeY) {
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
     * @param node       child node
       */
    private void drawChildComponent(RComponent node) {
        buffer.pushMatrix();
        buffer.pushStyle();
        node.draw(buffer);
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

    private void drawChildren(List<RComponent> colChildren, float x, float width) {
        float y = (win.getFolder().shouldDrawTitle())?RLayoutStore.getCell():0; // Account for titlebar
        int index = 0;
        for (RComponent node : colChildren) {
            if (!node.isVisible()) {
                index++;
                continue;
            }
            float nodeHeight = node.getHeight();
            drawChildComponent(node);
            if (index > 0) {
                // separator
                buffer.pushStyle();
                drawHorizontalSeparator(buffer, (int) width);
                buffer.popStyle();
            }
            y += nodeHeight;
            buffer.translate(0, nodeHeight);
            index++;
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
            folder.getLayout().setCompLayout(folder.getWindow().contentSize, folder.getChildren());
            drawChildren(folder.getChildren(), 0, buffer.width);

            buffer.endDraw();
        }
        LOGGER.debug("Content Buffer Draw Duration {}", System.currentTimeMillis() - time);
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
