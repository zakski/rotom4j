package com.szadowsz.gui.component.group.drawable.tab;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.gui.input.mouse.RMouseAction;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLinearLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RTabHeader extends RGroupDrawable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RTabHeader.class);

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
    }

    @Override
    public void setLayout(RLayoutBase layout) {

    }

    public void addTab(String name, RMouseAction action) {
        RButton button = new RButton(gui, path + "/" + name, this);
        button.registerAction(RActivateByType.RELEASE, action);
        children.add(button);
        redrawBuffers();
    }


    @Override
    public void mouseOver(RMouseEvent mouseEvent, float adjustedMouseY) {
        RComponent underMouse = findComponentAt(mouseEvent.getX(), adjustedMouseY);
        if (underMouse != null) {
            if (!underMouse.isMouseOver()) {
                LOGGER.debug("Inside Tab Header {} [NX {} NY {} Width {} Height {}]", underMouse.getName(), underMouse.getPosX(), underMouse.getPosY(), underMouse.getWidth(), underMouse.getHeight());
            }
            underMouse.mouseOver(mouseEvent, adjustedMouseY);
            redrawBuffers();
        }
        mouseEvent.consume();
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (!isVisible() || !this.isVisibleParentAware()) {
            return;
        }
        RComponent node = findComponentAt(mouseEvent.getX(), adjustedMouseY);
        if (node != null) {
            LOGGER.debug("Mouse Pressed for Tab {} [{}, {}, {}, {}, {}, {}]", node.getName(), mouseEvent.getX(), adjustedMouseY, node.getPosX(), node.getPosY(), node.getWidth(), node.getHeight());
            node.mousePressed(mouseEvent, adjustedMouseY);
            redrawBuffers();
        }
    }
}
