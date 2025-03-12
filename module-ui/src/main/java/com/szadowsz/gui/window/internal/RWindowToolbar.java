package com.szadowsz.gui.window.internal;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.input.mouse.RMouseHiding;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PApplet.round;

public class RWindowToolbar extends RWindowImpl {

    public RWindowToolbar(PApplet app, RotomGui gui, RFolder folder, String title, PVector pos, PVector size) {
        super(app, gui, folder, title, (int) pos.x, (int) pos.y, (int) size.x, (int) size.y);

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
            if (vsb.isPresent() && vsb.get().isVisible()) {
                int yDiff = round((sizeUnconstrained.y - size.y) * vsb.map(RScrollbar::getValue).orElse(0.0f));
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
