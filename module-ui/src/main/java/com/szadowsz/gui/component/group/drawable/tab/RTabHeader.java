package com.szadowsz.gui.component.group.drawable.tab;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.RGroupBuffer;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.gui.input.mouse.RMouseAction;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLinearLayout;
import com.szadowsz.gui.window.pane.RWindowPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

public final class RTabHeader extends RGroupDrawable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RTabHeader.class);

    private final RGroupBuffer buffer;
    private final RTabManager tabManager;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected RTabHeader(RotomGui gui, String path, RTabManager parent) {
        super(gui, path, parent);
        tabManager = parent;
        layout = new RLinearLayout(this, RDirection.HORIZONTAL);
        PVector preferredSize = getPreferredSize();
        buffer = new RGroupBuffer(this,preferredSize.x,preferredSize.y);
    }

    @Override
    public void setLayout(RLayoutBase layout) {

    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        pg.image(buffer.draw(), 0, 0);
    }

    public void addTab(String name, RMouseAction action) {
        RButton button = new RButton(gui, path + "/" + name, this);
        button.registerAction(RActivateByType.RELEASE, action);
        children.add(button);
        getParentWindow().redrawBuffer();
    }


    @Override
    public void mouseOver(RMouseEvent mouseEvent, float adjustedMouseY) {
        RComponent underMouse = findComponentAt(mouseEvent.getX(), adjustedMouseY);
        if (underMouse != null) {
            if (!underMouse.isMouseOver()) {
                LOGGER.debug("Inside Tab Header {} [NX {} NY {} Width {} Height {}]", underMouse.getName(), underMouse.getPosX(), underMouse.getPosY(), underMouse.getWidth(), underMouse.getHeight());
            }
            underMouse.mouseOver(mouseEvent, adjustedMouseY);
            getParentWindow().redrawBuffer();
        }
        mouseEvent.consume();
    }

    @Override
    public void redrawBuffer(){
        buffer.invalidateBuffer();
        super.redrawBuffer();
    }
}
