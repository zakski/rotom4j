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

    float c;
    float h;
    float a;

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

        c = RLayoutStore.getCell()/2;
        h = RLayoutStore.getCell() * 0.75f;
        a = 2*h/ PApplet.sqrt(3.0f);
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

        float t = value?1:0;
        float t2 = ((tri(t)+1)/2);
        float s = (h/10)*t2;
        float w1 = a*(((h/2)-s)/h)+(a*t2*(1-(((h/2)-s)/h)));
        float w2 = a*(((h/2)+s)/h)+(a*t2*(1-(((h/2)+s)/h)));

        if (!forwards){
            pg.rotate(PApplet.PI);
        }

        // TIP
        pg.beginShape();
        pg.vertex(c-h/2, c-a/2);
        pg.vertex(c-h/2, c+a/2);
        pg.vertex(c-s, c+w1/2);
        pg.vertex(c-s, c-w1/2);
        pg.endShape();
        // BASE
        pg.beginShape();
        pg.vertex(c+s, c-w2/2);
        pg.vertex(c+s, c+w2/2);
        pg.vertex(c+h/2, c+(a/2)*t2);
        pg.vertex(c+h/2, c-(a/2)*t2);
        pg.endShape();
    }
}
