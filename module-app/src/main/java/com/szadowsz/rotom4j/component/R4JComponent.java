package com.szadowsz.rotom4j.component;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RGroupBuffer;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.RotomFile;
import processing.core.PGraphics;
import processing.core.PVector;

public abstract class R4JComponent<R extends RotomFile> extends RGroupDrawable {

    protected static final String PREVIEW_NODE = "Preview";
    protected static final String ZOOM_NODE = "Zoom";
    protected static final String RESET_NODE = "Reset";


    protected final RGroupBuffer buffer;
    protected R data;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected R4JComponent(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
        PVector preferredSize = getPreferredSize();
        buffer = new RGroupBuffer(this,preferredSize.x,preferredSize.y);
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        pg.pushMatrix();
        pg.image(buffer.draw(), 0, 0);
        pg.popMatrix();
    }

    /**
     * Method to reinitialise and resupply an image to the PreviewNode
     * <p>
     * This should be called when something happens that should cause the preview image to change
     *
     * @throws NitroException if the change fails to take
     */
    public abstract void recolorImage() throws NitroException;

    @Override
    public void redrawBuffer(){
        buffer.invalidateBuffer();
        super.redrawBuffer();
    }

    @Override
    public float suggestWidth() {
        return layout.calcPreferredSize(getName(), children).x;
    }
}
