package com.szadowsz.gui.component;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import processing.core.PGraphics;

public abstract class RSingle extends RComponent{


    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected RSingle(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
        buffer = new RComponentBuffer(this);
    }

    @Override
    public void draw(PGraphics pg) {
        // the component knows its absolute position but here the current matrix is already translated to it
        pg.image(buffer.draw(),0,0);
    }

    @Override
    public void drawToBuffer() {
        buffer.redraw();
    }
}
