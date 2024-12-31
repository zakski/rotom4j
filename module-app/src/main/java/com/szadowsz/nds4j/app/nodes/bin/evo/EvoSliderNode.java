package com.szadowsz.nds4j.app.nodes.bin.evo;


import com.google.gson.JsonElement;
import com.old.ui.constants.theme.ThemeColorType;
import com.old.ui.constants.theme.ThemeStore;
import com.old.ui.input.keys.GuiKeyEvent;
import com.old.ui.input.mouse.GuiMouseEvent;
import com.old.ui.node.impl.SliderNode;
import com.old.ui.store.NormColorStore;
import com.old.ui.utils.KeyCodes;
import processing.core.PGraphics;

abstract class EvoSliderNode extends SliderNode {

    final EvoFolderNode parentFolder;

    EvoSliderNode(String path, EvoFolderNode parentFolder, int upperLimit) {
        super(path, parentFolder, 0, 0, upperLimit, true);
        this.parentFolder = parentFolder;
        showPercentIndicatorWhenConstrained = false;
        setPrecisionIndexAndValue(precisionRange.indexOf(1f));
        maximumFloatPrecisionIndex = precisionRange.indexOf(1f);
        initSliderBackgroundShader();
    }

    public void setValueFromParent(float valueFloat){
        this.valueFloat = valueFloat;
    }

    @Override
    public void mouseDragNodeContinueEvent(GuiMouseEvent e) {
        super.mouseDragNodeContinueEvent(e);
        updateParentFolder();
        e.setConsumed(true);
    }

    @Override
    public void mouseReleasedOverNodeEvent(GuiMouseEvent e) {
        super.mouseReleasedOverNodeEvent(e);
        updateParentFolder();
    }

    @Override
    protected void onValueFloatChanged() {
        super.onValueFloatChanged();
        updateParentFolder();
    }

    protected void updateParentFolder() {
        if(parentFolder == null) {
            return;
        }
       parentFolder.loadValues();
    }

    @Override
    protected void drawNodeBackground(PGraphics pg) {
        super.drawNodeBackground(pg);
        if(isInlineNodeDragged){
            pg.stroke(foregroundMouseOverBrightnessAwareColor());
            pg.strokeWeight(1);
            pg.line(size.x / 2f, 0f, size.x / 2f, size.y-1f);
        }
    }



    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        pg.fill(foregroundMouseOverBrightnessAwareColor());
        drawLeftText(pg, name);
        drawRightText(pg, getValueToDisplay(), false);
    }

    protected int foregroundMouseOverBrightnessAwareColor(){
        if(isMouseOverNode){
//            if(parentColorPickerFolder.brightness() > 0.7f){
//                return NormColorStore.color(0);
//            }else{
                return NormColorStore.color(1);
//            }
        }else{
            return ThemeStore.getColor(ThemeColorType.NORMAL_FOREGROUND);
        }
    }


    @Override
    public void keyPressedOverNode(GuiKeyEvent e, float x, float y) {
        super.keyPressedOverNode(e, x, y); // handle the value change inside SliderNode
        if (e.isControlDown() && e.getKeyCode() == KeyCodes.V) {
            // reflect the value change in the resulting color
            updateParentFolder();
        }
    }
    @Override
    public void overwriteState(JsonElement loadedNode) {
    }

    static class EvoSelectNode extends EvoSliderNode {
        EvoSelectNode(String path, EvoFolderNode parentFolder) {
            super(path, parentFolder, parentFolder.getNumEvolutions());
        }
    }
}
