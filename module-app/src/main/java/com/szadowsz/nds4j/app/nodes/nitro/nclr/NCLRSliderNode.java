package com.szadowsz.nds4j.app.nodes.nitro.nclr;

import com.old.ui.constants.theme.ThemeColorType;
import com.old.ui.constants.theme.ThemeStore;
import com.old.ui.store.NormColorStore;
import com.old.ui.store.ShaderStore;
import com.google.gson.JsonElement;
import com.old.ui.input.keys.GuiKeyEvent;
import com.old.ui.input.mouse.GuiMouseEvent;
import com.old.ui.node.impl.SliderNode;
import com.old.ui.utils.KeyCodes;
import processing.core.PGraphics;
import processing.opengl.PShader;

import java.awt.*;

abstract class NCLRSliderNode extends SliderNode {

    final NCLRFolderNode parentColorPickerFolder;
    private final String colorShaderPath = "paletteBackgroundRGB.glsl";
    protected int shaderColorMode = -1;

    NCLRSliderNode(String path, NCLRFolderNode parentFolder, float value) {
        super(path, parentFolder, value, 0, 255, true);
        this.parentColorPickerFolder = parentFolder;
        showPercentIndicatorWhenConstrained = false;
        setPrecisionIndexAndValue(precisionRange.indexOf(1f));
        maximumFloatPrecisionIndex = precisionRange.indexOf(1f);
        initSliderBackgroundShader();
        ShaderStore.getorLoadShader(colorShaderPath);
    }

    NCLRSliderNode(String path, NCLRFolderNode parentFolder, int numColors) {
        super(path, parentFolder, 0, 0, numColors, true);
        this.parentColorPickerFolder = parentFolder;
        showPercentIndicatorWhenConstrained = false;
        setPrecisionIndexAndValue(precisionRange.indexOf(1f));
        maximumFloatPrecisionIndex = precisionRange.indexOf(1f);
        initSliderBackgroundShader();
        ShaderStore.getorLoadShader(colorShaderPath);
    }

    public void setValueFromParent(float valueFloat){
        this.valueFloat = valueFloat;
    }

    @Override
    public void mouseDragNodeContinueEvent(GuiMouseEvent e) {
        super.mouseDragNodeContinueEvent(e);
        updateColorInParentFolder();
        e.setConsumed(true);
    }

    @Override
    public void mouseReleasedOverNodeEvent(GuiMouseEvent e) {
        super.mouseReleasedOverNodeEvent(e);
        updateColorInParentFolder();
    }

    @Override
    protected void onValueFloatChanged() {
        super.onValueFloatChanged();
        updateColorInParentFolder();
    }

    protected void updateColorInParentFolder() {
        if(parentColorPickerFolder == null) {
            return;
        }
       parentColorPickerFolder.loadValuesFromRGB();
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
    protected void updateBackgroundShader(PGraphics pg) {
        PShader bgShader = ShaderStore.getorLoadShader(colorShaderPath);
        bgShader.set("quadPos", pos.x, pos.y);
        bgShader.set("quadSize", size.x, size.y);
        Color c = parentColorPickerFolder.getColor();
        bgShader.set("paletteCol", c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0f);
        bgShader.set("redValue", parentColorPickerFolder.getRed() / 255.0f);
        bgShader.set("greenValue", parentColorPickerFolder.getGreen() / 255.0f);
        bgShader.set("blueValue", parentColorPickerFolder.getBlue() / 255.0f);
        bgShader.set("mode", shaderColorMode);
        pg.shader(bgShader);
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
            updateColorInParentFolder();
        }
    }
    @Override
    public void overwriteState(JsonElement loadedNode) {
    }

    static class ColorSelectNode extends NCLRSliderNode {
        ColorSelectNode(String path, NCLRFolderNode parentFolder) {
            super(path, parentFolder, parentFolder.getNumColors());
            shaderColorMode = 0;
        }
    }

    static class RSelectNode extends NCLRSliderNode {
        RSelectNode(String path, NCLRFolderNode parentFolder) {
            super(path, parentFolder, parentFolder.getRed());
            shaderColorMode = 1;
        }
    }
    static class GSelectNode extends NCLRSliderNode {
        GSelectNode(String path, NCLRFolderNode parentFolder) {
            super(path, parentFolder, parentFolder.getGreen());
            shaderColorMode = 2;
        }
    }

    static class BSelectNode extends NCLRSliderNode {
        BSelectNode(String path, NCLRFolderNode parentFolder) {
            super(path, parentFolder, parentFolder.getBlue());
            shaderColorMode = 3;
        }
    }
}
