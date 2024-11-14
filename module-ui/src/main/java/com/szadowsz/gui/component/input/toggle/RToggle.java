package com.szadowsz.gui.component.input.toggle;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import processing.core.PGraphics;

import static processing.core.PConstants.CENTER;

/**
 *  Variant component for binary user input controls, that is also known as an Option, or a Switch.
 */
public class RToggle extends RToggleBase {

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui           the gui for the window that the component is drawn under
     * @param path          the path in the component tree
     * @param parentFolder  the parent component folder reference // TODO consider if needed
     * @param startingValue
     */
    public RToggle(RotomGui gui, String path, RFolder parentFolder, boolean startingValue) {
        super(gui, path, parentFolder, startingValue);
    }

    @Override
    protected void drawToggleHandleRight(PGraphics pg, boolean value) {
        float rectWidth = RLayoutStore.getCell() * 0.3f;
        float rectHeight = RLayoutStore.getCell() * 0.25f;
        pg.rectMode(CENTER);
        pg.translate(size.x - RLayoutStore.getCell() * 0.5f, size.y * 0.5f);
        if(isMouseOver){
            pg.stroke(RThemeStore.getRGBA(RColorType.FOCUS_FOREGROUND));
        }else{
            pg.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND));
        }
        float turnedOffHandleScale = 0.25f;
        if(value){
            pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND));
            pg.rect(-rectWidth*0.5f,0, rectWidth, rectHeight);
            pg.fill(RThemeStore.getRGBA(RColorType.FOCUS_FOREGROUND));
            pg.rect(rectWidth*0.5f,0, rectWidth, rectHeight);
        }else{
            pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND));
            pg.rect(0,0, rectWidth*2, rectHeight);
            pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND));
            pg.rect(-rectWidth*0.5f,0, rectWidth*turnedOffHandleScale, rectHeight*turnedOffHandleScale);
        }
    }
}
