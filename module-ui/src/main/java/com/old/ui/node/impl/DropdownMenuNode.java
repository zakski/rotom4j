package com.old.ui.node.impl;

import com.old.ui.input.mouse.GuiMouseEvent;
import com.old.ui.node.LayoutType;
import com.old.ui.window.WindowManager;

import static com.old.ui.store.LayoutStore.cell;

/**
 * A dropdown list component.
 * <p>
 * The number of items in the list is not restricted but the user can define the maximum number of items to be displayed
 * in the drop list. If there are too many items to display a vertical scroll bar is provide to scroll through all the
 * items.
 */
public class DropdownMenuNode extends FolderNode {

    public DropdownMenuNode(String path, FolderNode parent) {
        super(path, parent);
    }

    @Override
    public void mousePressedEvent(GuiMouseEvent e) {
        isInlineNodeDragged = true;
        isMouseOverNode = true;
        if (window == null || !window.isVisible()) {
            if (parent != null && parent.getLayout() == LayoutType.HORIZONAL) {
                WindowManager.uncoverOrCreateTempWindow(this, true, pos.x, pos.y + cell, null);
            } else {
                WindowManager.uncoverOrCreateTempWindow(this, true, pos.x + size.x/*parent.window.posX + parent.window.windowSizeX*/, pos.y, null);
            }
        } else {
            window.close();
        }
        this.isInlineNodeDragged = false;
    }

    @Override
    public boolean shouldDrawTitle() {
        return false;
    }
}
