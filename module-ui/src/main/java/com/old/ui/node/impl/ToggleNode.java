package com.old.ui.node.impl;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.old.ui.constants.theme.ThemeColorType;
import com.old.ui.constants.theme.ThemeStore;
import com.old.ui.input.mouse.GuiMouseEvent;
import com.old.ui.node.AbstractNode;
import com.old.ui.node.NodeType;
import com.old.ui.store.JsonSaveStore;
import processing.core.PGraphics;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static com.old.ui.store.LayoutStore.cell;
import static processing.core.PConstants.CENTER;

public class ToggleNode extends AbstractNode {

    @Expose
    public
    boolean valueBoolean;
    protected boolean armed = false;

    protected List<Consumer<Boolean>> actions = new CopyOnWriteArrayList<>();


    public ToggleNode(String path, FolderNode folder, boolean defaultValue) {
        this(NodeType.VALUE, path, folder,defaultValue);
    }

    protected ToggleNode(NodeType type, String path, FolderNode folder, boolean defaultValue) {
        super(type, path, folder);
        valueBoolean = defaultValue;
        isInlineNodeDraggable = false;
        JsonSaveStore.overwriteWithLoadedStateIfAny(this);
    }

    @Override
    protected void drawNodeBackground(PGraphics pg) {

    }

    protected void drawRightToggleHandle(PGraphics pg, boolean valueBoolean) {
        float rectWidth = cell * 0.3f;
        float rectHeight = cell * 0.25f;
        pg.rectMode(CENTER);
        pg.translate(size.x - cell * 0.5f, size.y * 0.5f);
        if(isMouseOverNode){
            pg.stroke(ThemeStore.getColor(ThemeColorType.FOCUS_FOREGROUND));
        }else{
            pg.stroke(ThemeStore.getColor(ThemeColorType.NORMAL_FOREGROUND));
        }
        float turnedOffHandleScale = 0.25f;
        if(valueBoolean){
            pg.fill(ThemeStore.getColor(ThemeColorType.NORMAL_BACKGROUND));
            pg.rect(-rectWidth*0.5f,0, rectWidth, rectHeight);
            pg.fill(ThemeStore.getColor(ThemeColorType.FOCUS_FOREGROUND));
            pg.rect(rectWidth*0.5f,0, rectWidth, rectHeight);
        }else{
            pg.fill(ThemeStore.getColor(ThemeColorType.NORMAL_BACKGROUND));
            pg.rect(0,0, rectWidth*2, rectHeight);
            pg.fill(ThemeStore.getColor(ThemeColorType.NORMAL_FOREGROUND));
            pg.rect(-rectWidth*0.5f,0, rectWidth*turnedOffHandleScale, rectHeight*turnedOffHandleScale);
        }
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        drawLeftText(pg, name);
        drawRightBackdrop(pg, cell);
        drawRightToggleHandle(pg, valueBoolean);
    }

    @Override
    public void mousePressedEvent(GuiMouseEvent e) {
        super.mousePressedEvent(e);
        armed = true;
    }

    @Override
    public void mouseReleasedOverNodeEvent(GuiMouseEvent e){
        super.mouseReleasedOverNodeEvent(e);
        if(armed){
            valueBoolean = !valueBoolean;
            actions.forEach(a -> a.accept(valueBoolean));
            onValueChangingActionEnded();
        }
        armed = false;
    }

    @Override
    public void mouseDragNodeContinueEvent(GuiMouseEvent e) {
        super.mouseDragNodeContinueEvent(e);
        e.setConsumed(true);
    }

    public void overwriteState(JsonElement loadedNode) {
        JsonElement booleanElement = loadedNode.getAsJsonObject().get("valueBoolean");
        if(booleanElement != null){
            valueBoolean = booleanElement.getAsBoolean();
        }
    }

    @Override
    public float getRequiredWidthForHorizontalLayout() {
        return findTextWidthRoundedUpToWholeCells(name) +
                Math.max(cell * 2,findTextWidthRoundedUpToWholeCells(getValueAsString()));
    }

    @Override
    public String getValueAsString() {
        return String.valueOf(valueBoolean);
    }

    public void registerAction(Consumer<Boolean> o) {
        actions.add(o);
    }
}
