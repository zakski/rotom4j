package com.old.gui.window.internal;

import com.old.gui.RotomGui;
import com.old.gui.component.RComponent;
import com.old.gui.component.folder.RFolder;
import com.old.gui.component.folder.RPane;
import com.old.gui.input.mouse.RMouseEvent;
import com.old.gui.input.mouse.RMouseHiding;
import processing.core.PApplet;
import processing.core.PVector;

public class RWindowPane extends RWindowInt{

    public RWindowPane(PApplet app, RotomGui gui, RFolder folder, String title, PVector pos, PVector size) {
        super(app, gui, folder, title, pos, size);
     }

    public RWindowPane(PApplet app, RotomGui gui, RPane pane, String title, float xPos, float yPos) {
        this(app, gui, pane, title, new PVector(xPos, yPos), new PVector(0, app.height-yPos));
    }

    public RWindowPane(PApplet app, RotomGui gui, RPane pane, float xPos, float yPos) {
        this(app, gui, pane, pane.getName(), xPos, yPos);
    }

    @Override
    public void mouseDragged(RMouseEvent e) {
        if (!isVisible()) {
            return;
        }
        if (isBeingResized) {
            handleBeingResized(e);
        } else if (vsb.map(s -> s.dragging).orElse(false)) {
            vsb.ifPresent(s -> s.mouseDragged(e));
        }
        for (RComponent child : folder.getChildren()) {
            if (child.isDragged() && child.isParentWindowVisible()) {
                child.mouseDragContinues(e);
                if (e.isConsumed() && child.isDraggable()) {
                    RMouseHiding.tryHideMouseForDragging(app);
                }
            }
        }
    }
}
