package com.szadowsz.gui.window.internal;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.folder.RPanel;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.input.mouse.RMouseHiding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PVector;

public class RWindowPanel extends RWindowImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(RWindowPanel.class);

    public RWindowPanel(PApplet app, RotomGui gui, RPanel folder, String title, PVector pos, PVector size) {
        super(app, gui, folder, title, pos, size);
     }

    public RWindowPanel(PApplet app, RotomGui gui, RPanel pane, String title, float xPos, float yPos) {
        this(app, gui, pane, title, new PVector(xPos, yPos), new PVector(0, app.height-yPos));
    }

    public RWindowPanel(PApplet app, RotomGui gui, RPanel pane, float xPos, float yPos) {
        this(app, gui, pane, pane.getName(), xPos, yPos);
    }

    @Override
    public void mouseDragged(RMouseEvent mouseEvent) {
        if (!isVisible()) {
            return;
        }
        if (isBeingResized) {
            handleBeingResized(mouseEvent);
        } else if (vsb.map(RScrollbar::isDragging).orElse(false)) {
            vsb.ifPresent(s -> s.mouseDragged(mouseEvent));
        }
        for (RComponent child : folder.getChildren()) {
            LOGGER.debug("Mouse Drag Check for Content {}", child.getName());
            if (child.isDragged()) {
                LOGGER.debug("Mouse Dragged for Content {}", child.getName());
                child.mouseDragged(mouseEvent);
                if (mouseEvent.isConsumed() && child.isDraggable()) {
                    RMouseHiding.tryHideMouseForDragging(sketch);
                }
                if (mouseEvent.isConsumed()) {
                    break;
                }
            }
        }
    }
}
