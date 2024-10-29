package com.szadowsz.gui.window.pane;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.folder.RPanel;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.input.mouse.RMouseHiding;
import processing.core.PApplet;
import processing.core.PVector;

public class RWindowPanel extends RWindowPane {

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
    public void mouseDragged(RMouseEvent e) {
        if (!isVisible()) {
            return;
        }
        if (isBeingResized) {
            handleBeingResized(e);
        } else if (vsb.map(RScrollbar::isDragging).orElse(false)) {
            vsb.ifPresent(s -> s.mouseDragged(e));
        }
        for (RComponent child : folder.getChildren()) {
            if (child.isDragged() && child.isParentWindowVisible()) {
                child.mouseDragged(e);
                if (e.isConsumed() && child.isDraggable()) {
                    RMouseHiding.tryHideMouseForDragging(sketch);
                }
            }
        }
    }
}
