package com.szadowsz.gui.component.input.color;

import com.jogamp.newt.event.KeyEvent;
import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.drawable.RColorPicker;
import com.szadowsz.gui.component.input.slider.RSliderInt;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.RShaderStore;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import processing.core.PGraphics;

/**
 * Slider Node to represent the R, G, B or A (if that way inclined) values of a Color
 */
public class RColorSlider extends RSliderInt { // TODO create other config options for the Color Values

    protected final RColorPicker parentColorPicker;
    protected final String colorShaderPath = "backgroundRGB.glsl";

    public RColorSlider(RotomGui gui, String path, RColorPicker group, int currentValue) {
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
            return RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND);
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

    public void setValueFromParent(float floatToSet) {
        value = floatToSet;
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
    public void keyPressedOver(RKeyEvent e, float x, float y) {
        super.keyPressedOver(e, x, y); // handle the value change inside SliderNode
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_V) {
            // reflect the value change in the resulting color
            updateColorInParentFolder();
        }
    }

    @Override
    public void mouseDragged(RMouseEvent e) {
        super.mouseDragged(e);
        updateColorInParentFolder();
    }

    @Override
    public void mouseReleasedOverComponent(RMouseEvent mouseEvent, float mouseY) {
        super.mouseReleasedOverComponent(mouseEvent,mouseY);
        updateColorInParentFolder();
    }
}
