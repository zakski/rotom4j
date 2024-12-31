package com.szadowsz.gui.component.group.folder;


import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLinearLayout;

/**
 * Toolbar to display across a Processing Window
 */
public class RToolbar extends RFolder {

    /**
     * Construct an RToolbar
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
    public void mousePressed(RMouseEvent mouseEvent, float mouseY) {
        isMouseOver = true;
        mouseEvent.consume();
        gui.getWinManager().uncoverOrCreateToolbar(this,true,null);
    }
}
