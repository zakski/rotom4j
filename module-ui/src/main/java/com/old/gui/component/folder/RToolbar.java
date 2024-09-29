package com.old.gui.component.folder;

import com.old.gui.RotomGui;
import com.old.gui.component.group.RGroup;
import com.old.gui.input.mouse.RMouseEvent;
import com.old.gui.layout.RDirection;
import com.old.gui.layout.RLinearLayout;

public class RToolbar extends RFolder {
    /**
     * Construct a RFolder with a Specified Layout
     *
     * @param gui
     * @param path   folder path
     * @param parent parent folder
     */
    public RToolbar(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
        RLinearLayout layoutL = new RLinearLayout(RDirection.HORIZONTAL);
        // TODO any additional config around the layout here
        this.layout = layoutL;
    }

    /**
     * Method to tell the window whether to draw the title
     *
     * @return true if it should draw, false otherwise
     */
    public boolean shouldDrawTitle() {
        return false;
    }



    @Override
    public void mousePressed(RMouseEvent e) {
        super.mousePressed(e);
        gui.getWinManager().uncoverOrCreateToolbar(this,true,null);
        this.isDragged = false;
    }
}
