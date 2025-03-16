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

    float c;
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

        float t = value?1:0;
        float t2 = ((tri(t)+1)/2);
        float s = (h/10)*t2;
        float w1 = a*(((h/2)-s)/h)+(a*t2*(1-(((h/2)-s)/h)));
        float w2 = a*(((h/2)+s)/h)+(a*t2*(1-(((h/2)+s)/h)));
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
