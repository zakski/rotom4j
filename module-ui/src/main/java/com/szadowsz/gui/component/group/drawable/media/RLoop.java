package com.szadowsz.gui.component.group.drawable.media;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.input.toggle.RToggle;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import processing.core.PApplet;
import processing.core.PGraphics;

public class RLoop extends RToggle {

    float cx, cy;
    float h;
    float a;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui           the gui for the window that the component is drawn under
     * @param path          the path in the component tree
     * @param parent        the parent component reference
     * @param startingValue
     */
    public RLoop(RotomGui gui, String path, RGroup parent, boolean startingValue) {
        super(gui, path, parent, startingValue);

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
        drawToggleHandleRight(pg, value);
    }

    @Override
    protected void drawToggleHandleRight(PGraphics pg, boolean value) {
        pg.noStroke();
        if(isMouseOver){
            pg.fill(RThemeStore.getRGBA(RColorType.FOCUS_FOREGROUND));
        }else{
            pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND));
        }

        if (value) {
            pg.circle(cx, cy, h);
            if (isMouseOver) {
                pg.fill(RThemeStore.getRGBA(RColorType.FOCUS_BACKGROUND));
            } else {
                pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND));
            }
            pg.circle(cx, cy, (h / 4) * 3);
        } else {
            pg.rect(cx-h/2, cy-h/4, h, h/2);
        }
    }
}
