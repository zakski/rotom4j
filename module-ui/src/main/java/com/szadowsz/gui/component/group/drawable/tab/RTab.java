package com.szadowsz.gui.component.group.drawable.tab;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.input.mouse.RMouseAction;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.window.internal.RWindowImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

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

    public RMouseAction getAction() {
        return () -> {
            manager.setActive(RTab.this);
            RTab.this.redrawBuffers(); // REDRAW-VALID: we should redraw the tab's buffer if the tab becomes active
        };
    }

    public String getTitle(){
        return children.getFirst().getName();
    }


    @Override
    protected void redrawBuffers() {
        super.redrawBuffers();
    }

    @Override
    public void resetBuffer() { // For Access
        super.resetBuffer();
    }

    @Override
    public PVector getPreferredSize() {
        return children.getFirst().getPreferredSize();
    }

    /**
     * TODO
     *
     * @return TODO
     */
    public boolean hasFocus() {
        return gui.hasFocus(children.getFirst());
    }

    @Override
    public float suggestWidth(){
        return children.getFirst().suggestWidth();
    }

    @Override
    public void updateCoordinates(float bX, float bY, float rX, float rY, float w, float h) { // TODO LazyGui
        LOGGER.debug("Update Coordinates for Tab {} [{}, {}, {}, {}, {}, {}]", name, bX,bY,rX,rY,w,h);
        pos.x = bX + rX;
        pos.y = bY + rY;
        relPos.x = rX;
        relPos.y = rY;

        if (size.x != w){
            resetBuffer(); // RESET-VALID: we should resize the buffer if the size changes
            size.x = w;
        }


        if (size.y != h){
            resetBuffer(); // RESET-VALID: we should resize the buffer if the size changes
            size.y = h;
        }


        children.getFirst().updateCoordinates(getPosX(), getPosY(), 0, 0, w, h);
    }

    @Override
    public void draw(PGraphics pg) {
        drawContent(pg);
    }
}
