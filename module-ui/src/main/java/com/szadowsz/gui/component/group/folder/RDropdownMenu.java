package com.szadowsz.gui.component.group.folder;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLinearLayout;

/**
 * A dropdown list component.
 * <p>
 * The number of items in the list is not restricted but the user can define the maximum number of items to be displayed
 * in the drop list. If there are too many items to display a vertical scroll bar is provide to scroll through all the
 * items.
 */
public class RDropdownMenu extends RFolder {

    public RDropdownMenu(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float mouseY) {
        isDragged = true;
        isMouseOver = true;
        if (window == null || !window.isVisible()) {
            RFolder pfolder = getParentFolder();
            if (pfolder != null &&
                    pfolder.getLayout() instanceof RLinearLayout &&
                    ((RLinearLayout) pfolder.getLayout()).getDirection() == RDirection.HORIZONTAL) {
                gui.getWinManager().uncoverOrCreateTempWindow(this, true, pos.x, pos.y + RLayoutStore.getCell(), null);
            } else {
                gui.getWinManager().uncoverOrCreateTempWindow(this, true, pos.x + size.x, pos.y, null);
            }
        } else {
            window.close();
        }
        isDragged = false;
    }

    @Override
    public final boolean shouldDrawTitle() {
        return false;
    }
}
