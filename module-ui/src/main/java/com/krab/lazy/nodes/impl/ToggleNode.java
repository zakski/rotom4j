package com.krab.lazy.nodes.impl;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.krab.lazy.input.mouse.LazyMouseEvent;
import com.krab.lazy.nodes.AbstractNode;
import com.krab.lazy.nodes.NodeType;
import com.krab.lazy.stores.JsonSaveStore;
import com.krab.lazy.stores.LayoutStore;
import processing.core.PGraphics;

public class ToggleNode extends AbstractNode {

    @Expose
    public
    boolean valueBoolean;
    protected boolean armed = false;

    public ToggleNode(String path, FolderNode folder, boolean defaultValue) {
        super(NodeType.VALUE, path, folder);
        valueBoolean = defaultValue;
        isInlineNodeDraggable = false;
        JsonSaveStore.overwriteWithLoadedStateIfAny(this);
    }

    @Override
    protected void drawNodeBackground(PGraphics pg) {

    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        drawLeftText(pg, name);
        drawRightBackdrop(pg, LayoutStore.cell);
        drawRightToggleHandle(pg, valueBoolean);
    }

    @Override
    public void mousePressedEvent(LazyMouseEvent e) {
        super.mousePressedEvent(e);
        armed = true;
    }

    @Override
    public void mouseReleasedOverNodeEvent(LazyMouseEvent e){
        super.mouseReleasedOverNodeEvent(e);
        if(armed){
            valueBoolean = !valueBoolean;
            onValueChangingActionEnded();
        }
        armed = false;
    }

    @Override
    public void mouseDragNodeContinueEvent(LazyMouseEvent e) {
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
    public String getValueAsString() {
        return String.valueOf(valueBoolean);
    }
}
