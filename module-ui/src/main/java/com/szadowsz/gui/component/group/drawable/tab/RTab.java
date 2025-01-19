package com.szadowsz.gui.component.group.drawable.tab;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.RComponentTree;
import com.szadowsz.gui.component.RPaths;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseAction;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLayoutConfig;
import com.szadowsz.gui.window.pane.RWindowPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PConstants.*;

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

    /**
     * Make a call to flag a redraw of the component buffer due to a change in state, most likely by user input.
     *
     * This should not need to call redraw on parent buffers, because the parent should flag it needs to redraw after
     * seeing the mouse/key event consumed.
     */
    protected void redrawBuffers() {
        buffer.invalidateBuffer();
        RWindowPane win = getParentWindow(); // TODO check if needed
        if (win != null) {
            win.redrawBuffer();
        }
    }

    protected void resetBuffer() {
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
        LOGGER.info("Update Coordinates for Tab {} [{}, {}, {}, {}, {}, {}]", name, bX,bY,rX,rY,w,h);
        super.updateCoordinates(bX, bY, rX, rY, w, h);
        children.getFirst().updateCoordinates(bX, bY, rX, rY, w, h);
    }

    @Override
    public void draw(PGraphics pg) {
        drawContent(pg);
    }
}
