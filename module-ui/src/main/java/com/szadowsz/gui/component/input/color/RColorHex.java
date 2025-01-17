package com.szadowsz.gui.component.input.color;



import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.RSingle;
import com.szadowsz.gui.component.group.drawable.RColorPicker;
import processing.core.PGraphics;


public final class RColorHex extends RSingle {

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
