package com.szadowsz.gui.component.input.color;



import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.component.group.RColorPicker;
import com.szadowsz.gui.input.keys.RKeyEvent;
import processing.core.PGraphics;

import java.awt.*;


public final class RColorHex extends RComponent {

    private final RColorPicker parentColorPicker;

    public RColorHex(RotomGui gui, String path, RColorPicker picker) {
        super(gui,path,picker);
        this.parentColorPicker = picker;
    }

    @Override
    protected void drawBackground(PGraphics pg) {

    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        fillForeground(pg);
        drawTextLeft(pg, name);
        drawTextRightNoOverflow(pg, parentColorPicker.getHexString(), name, false);
    }

    @Override
    public float suggestWidth() {
        return 0;
    }
}
