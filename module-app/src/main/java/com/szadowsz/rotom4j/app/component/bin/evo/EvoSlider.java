package com.szadowsz.rotom4j.app.component.bin.evo;


import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.input.slider.RSliderInt;
import com.szadowsz.gui.config.theme.RColorConverter;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.keys.RKeyEvent;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import processing.core.PGraphics;

import static java.awt.event.KeyEvent.VK_V;

abstract class EvoSlider extends RSliderInt {

    final EvoFolderComponent parentFolder;

    EvoSlider(RotomGui gui, String path, EvoFolderComponent parentFolder, int upperLimit) {
        super(gui, path, parentFolder, 0, 0, upperLimit, true);
        this.parentFolder = parentFolder;
        initSliderBackgroundShader();
    }

    public void setValueFromParent(float floatToSet){
        this.value = floatToSet;
    }

    @Override
    public void mouseDragged(RMouseEvent e) {
        super.mouseDragged(e);
        updateParentFolder();
        e.consume();
    }

    @Override
    public void mouseReleasedOverComponent(RMouseEvent mouseEvent, float mouseY) {
        super.mouseReleasedOverComponent(mouseEvent, mouseY);
        updateParentFolder();
    }

    @Override
    protected void onValueChange() {
        super.onValueChange();
        updateParentFolder();
    }

    protected void updateParentFolder() {
        if(parentFolder == null) {
            return;
        }
       parentFolder.loadValues();
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
    protected void drawForeground(PGraphics pg, String name) {
        pg.fill(foregroundMouseOverBrightnessAwareColor());
        drawTextLeft(pg, name);
        drawTextRight(pg, getDisplayValue(), false);
    }

    protected int foregroundMouseOverBrightnessAwareColor(){
        if(isMouseOver){
//            if(parentColorPickerFolder.brightness() > 0.7f){
//                return NormColorStore.color(0);
//            }else{
                return RColorConverter.color(1);
//            }
        }else{
            return RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND);
        }
    }


    @Override
    public void keyPressedOver(RKeyEvent keyEvent, float mouseX, float mouseY) {
        super.keyPressedOver(keyEvent, mouseX, mouseY); // handle the value change inside SliderNode
        if (keyEvent.isControlDown() && keyEvent.getKeyCode() == VK_V) {
            // reflect the value change in the resulting color
            updateParentFolder();
        }
    }

    static class EvoSelectSlider extends EvoSlider {
        EvoSelectSlider(RotomGui gui, String path, EvoFolderComponent parentFolder) {
            super(gui, path, parentFolder, parentFolder.getNumEvolutions());
        }
    }
}
