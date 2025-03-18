package com.szadowsz.gui.component.group.drawable.media;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import processing.core.PApplet;
import processing.core.PGraphics;

public class RSkip extends RButton {

    protected final boolean forwards;

    float cx, cy;
    float h;

    /**
     * Default Constructor
     *
     * @param gui   gui controller for the window
     * @param path  component tree path
     * @param group parent component
     */
    public RSkip(RotomGui gui, String path, RGroup group, boolean forwards) {
        super(gui, path, group);
        this.forwards = forwards;

        cx = RLayoutStore.getCell();
        cy = RLayoutStore.getCell()/2;
        h = RLayoutStore.getCell() * 0.75f;
    }

    protected float tri(float t) {
        return PApplet.min(PApplet.max(5*PApplet.abs(2*(t-PApplet.floor(t+((float) 1 /2))))-2.5f, -1), 1);
    }


    @Override
    public float suggestWidth() {
        return RLayoutStore.getCell()*2;
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        drawSymbol(pg, value);
    }

    protected void drawSymbol(PGraphics pg, boolean value) {
        pg.noStroke();
        if(isMouseOver){
            pg.fill(RThemeStore.getRGBA(RColorType.FOCUS_FOREGROUND));
        }else{
            pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND));
        }

        pg.pushMatrix();
        pg.translate(cx, cy);
        if (!forwards){
            pg.rotate(PApplet.PI);
        }

        pg.triangle(-cx/2, -(h/2), -cx/2, (h/2), 0, 0);
        pg.triangle(-cx/16, -(h/2), -cx/16, (h/2), cx/2, 0);
        pg.rect((cx/16)*6,-h/2,(cx/16)*2,h);
        pg.popMatrix();
    }
}
