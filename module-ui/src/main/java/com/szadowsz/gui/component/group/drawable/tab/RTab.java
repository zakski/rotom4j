package com.szadowsz.gui.component.group.drawable.tab;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.drawable.RGroupDrawable;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.input.mouse.RMouseAction;
import com.szadowsz.gui.layout.RLayoutBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PConstants;
import processing.core.PGraphics;

public class RTab extends RGroupDrawable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RTab.class);

    private final RTabManager manager;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     * @param function tab component creation function
     */
    protected RTab(RotomGui gui, String path, RTabManager parent, RTabFunction function) {
        super(gui, path, parent);
        manager = parent;
        children.add(function.createComponent(this));
    }

    @Override
    public void setLayout(RLayoutBase layout) {

    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        PGraphics graphics = getGui().getSketch().createGraphics((int) size.x, (int) size.y, PConstants.P2D);
        graphics.beginDraw();
        graphics.endDraw();
        graphics.beginDraw();
        graphics.textFont(RFontStore.getMainFont());
        graphics.fill(255);
//        updateAssessors();
//        pg.pushMatrix();
//        int yDiff = 0;
//        pg.image(buffer.draw().get(0, yDiff, (int) size.x, (int) size.y), 0, 0);
//        pg.popMatrix();
        graphics.text("TEST", size.x/2, size.y/2);
        graphics.endDraw();
        pg.image(graphics, 0, 0);
    }

    public RMouseAction getAction() {
        return () -> {
            manager.setActive(RTab.this);
            manager.redrawWinBuffer();
        };
    }

    @Override
    public void updateCoordinates(float bX, float bY, float rX, float rY, float w, float h) {
        LOGGER.info("Update Coordinates for Tab {} [{}, {}, {}, {}, {}, {}]", name, bX,bY,rX,rY,w,h);
        super.updateCoordinates(bX, bY, rX, rY, w, h);
        layout.setCompLayout(getParentWindow().getPos(),pos,size,children);
    }

    public String getTitle(){
        return children.getFirst().getName();
    }
}
