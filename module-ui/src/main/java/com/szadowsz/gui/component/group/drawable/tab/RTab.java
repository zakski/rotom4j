package com.szadowsz.gui.component.group.drawable.tab;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.input.mouse.RMouseAction;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RLayoutBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        children.getFirst().draw(pg);
    }

    public RMouseAction getAction() {
        return () -> {
            manager.setActive(RTab.this);
            manager.redrawBuffer();
        };
    }

    @Override
    public void updateCoordinates(float bX, float bY, float rX, float rY, float w, float h) {
        LOGGER.info("Update Coordinates for Tab {} [{}, {}, {}, {}, {}, {}]", name, bX,bY,rX,rY,w,h);
        super.updateCoordinates(bX, bY, rX, rY, w, h);
        layout.setCompLayout(pos,size,children);
    }

    public String getTitle(){
        return children.getFirst().getName();
    }
}
