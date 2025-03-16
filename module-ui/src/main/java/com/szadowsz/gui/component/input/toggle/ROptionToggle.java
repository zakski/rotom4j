package com.szadowsz.gui.component.input.toggle;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RSingle;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.text.RFontStore;
import processing.core.PGraphics;

/**
 *  Toggles between a list of options
 *
 *  Related to, but not the same as, binary user input controls (etc. Toggle, Checkbox)
 */
public class ROptionToggle<LO> extends RSingle {

    protected final LO[] options;
    protected int index;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    public ROptionToggle(RotomGui gui, String path, RGroup parent, LO[] list, int index) {
        super(gui, path, parent);
        this.options = list;
        this.index = index;
        if (this.index >= options.length) {
            this.index = 0;
        }
    }

    @Override
    protected void drawBackground(PGraphics pg) {

    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        drawTextLeft(pg, getValue().toString());
    }

    public LO getValue() {
        return options[index];
    }

    @Override
    public float suggestWidth() {
        return RFontStore.calcMainTextWidth(getValue().toString(), RLayoutStore.getCell());
    }
}
