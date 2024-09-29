package com.old.gui.window.internal;

import com.old.gui.RotomGui;
import com.old.gui.component.RComponent;
import com.old.gui.component.folder.RFolder;
import com.old.gui.config.RLayoutStore;
import com.old.gui.input.mouse.RMouseEvent;
import com.old.gui.input.mouse.RMouseHiding;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PApplet.round;

public class RWindowToolbar extends RWindowInt {

    public RWindowToolbar(PApplet app, RotomGui gui, RFolder folder, String title, PVector pos, PVector size) {
        super(app, gui, folder, title, pos, size);

    }

    public RWindowToolbar(PApplet app, RotomGui gui, RFolder folder, String title, float yPos) {
        this(app, gui, folder, title, new PVector(0, yPos), new PVector(app.width, RLayoutStore.getCell()));
    }


    @Override
    protected void initScrollbar() {
        // NOOP
    }

    /**
     * Draw The Content of The Window
     *
     * @param pg Processing Graphics Context
     */
    @Override
    protected void drawContent(PGraphics pg) {
        if (!folder.getChildren().isEmpty()) {
            pg.pushMatrix();
            pg.translate(pos.x, pos.y);
            if (vsb.isPresent() && vsb.get().visible) {
                int yDiff = round((sizeUnconstrained.y - size.y) * vsb.map(s -> s.value).orElse(0.0f));
                pg.image(contentBuffer.draw().get(0, yDiff, (int) contentSize.x, (int) (size.y - RLayoutStore.getCell())), 0, 0);
            } else {
                pg.image(contentBuffer.draw(), 0, 0);
            }
            pg.popMatrix();
        }
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
