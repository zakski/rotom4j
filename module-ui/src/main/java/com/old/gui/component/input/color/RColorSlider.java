package com.old.gui.component.input.color;

import com.jogamp.newt.event.KeyEvent;
import com.old.gui.RotomGui;
import com.old.gui.component.group.RColorPicker;
import com.old.gui.component.input.slider.RSliderInt;
import com.old.gui.config.RLayoutStore;
import com.old.gui.config.RShaderStore;
import com.old.gui.config.theme.RThemeColorType;
import com.old.gui.config.theme.RThemeStore;
import com.old.gui.input.keys.RKeyEvent;
import com.old.gui.input.mouse.RMouseEvent;
import processing.core.PGraphics;

/**
 * Slider Node to represent the R, G, B or A (if that way inclined) values of a Color
 */
public class RColorSlider extends RSliderInt { // TODO create other config options for the Color Values

    protected final RColorPicker parentColorPicker;
    protected final String colorShaderPath = "backgroundRGB.glsl";

    public RColorSlider(RotomGui gui, String path, RColorPicker group, float currentValue) {
        super(gui, path, group, currentValue, 0, 255, true);
        this.parentColorPicker = group;
    }

    protected void updateColorInParentFolder() {
        if(parentColorPicker == null) {
            return;
        }
        parentColorPicker.loadValuesFromRGB();
    }

    protected int foregroundMouseOverBrightnessAwareColor(){
//        if(isMouseOver){
//            return NormColorStore.color(1);
//        }else{
            return RThemeStore.getRGBA(RThemeColorType.NORMAL_FOREGROUND);
//        }
    }

    @Override
    protected void drawBackground(PGraphics pg) {
        super.drawBackground(pg);
        if(isDragged){
            pg.stroke(foregroundMouseOverBrightnessAwareColor());
            pg.strokeWeight(1);
            pg.line(size.x / 2f, 0f, size.x / 2f, size.y-1f);
        }
    }

    @Override
    public void initSliderBackgroundShader() {
        RShaderStore.getOrLoadShader(gui,colorShaderPath);
    }

    @Override
    public float suggestWidth() {
        return RLayoutStore.getCell() * 8;
    }

    @Override
    public void keyPressedOverComponent(RKeyEvent e, float x, float y) {
        super.keyPressedOverComponent(e, x, y); // handle the value change inside SliderNode
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_V) {
            // reflect the value change in the resulting color
            updateColorInParentFolder();
        }
    }

    @Override
    public void mouseDragContinues(RMouseEvent e) {
        super.mouseDragContinues(e);
        updateColorInParentFolder();
    }

    @Override
    public void mouseReleasedOverComponent(RMouseEvent e) {
        super.mouseReleasedOverComponent(e);
        updateColorInParentFolder();
    }

    public void setValueFromParent(float floatToSet) {
        value = floatToSet;
    }
}
