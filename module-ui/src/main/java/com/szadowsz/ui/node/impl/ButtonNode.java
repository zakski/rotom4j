package com.szadowsz.ui.node.impl;

import com.szadowsz.ui.constants.theme.ThemeColorType;
import com.szadowsz.ui.constants.theme.ThemeStore;
import com.szadowsz.ui.input.mouse.GuiMouseEvent;
import com.szadowsz.ui.input.mouse.MouseAction;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.input.ActionInteractable;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.node.NodeType;
import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.szadowsz.ui.constants.GlobalReferences.app;
import static com.szadowsz.ui.store.LayoutStore.cell;
import static processing.core.PConstants.CENTER;

public class ButtonNode extends AbstractNode implements ActionInteractable {
    public ButtonNode(String path, FolderNode folder) {
        super(NodeType.TRANSIENT, path, folder);
        isInlineNodeDraggable = false;
    }

    boolean valueBoolean = false;
    private boolean mousePressedLastFrame = false;

    protected List<MouseAction> pressActions = new CopyOnWriteArrayList<>();
    protected List<MouseAction> releaseActions = new CopyOnWriteArrayList<>();

    protected void drawRightButton(PGraphics pg) {
        pg.noFill();
        pg.translate(size.x - cell *0.5f, cell * 0.5f);
        fillBackgroundBasedOnMouseOver(pg);
        pg.stroke(ThemeStore.getColor(ThemeColorType.NORMAL_FOREGROUND));
        pg.rectMode(CENTER);
        float outerButtonSize = cell * 0.6f;
        pg.rect(0,0, outerButtonSize, outerButtonSize);
        pg.stroke(ThemeStore.getColor(isInlineNodeDragged ? ThemeColorType.FOCUS_FOREGROUND : ThemeColorType.NORMAL_FOREGROUND));
        if(isMouseOverNode){
            if (isInlineNodeDragged){
                pg.fill(ThemeStore.getColor(ThemeColorType.FOCUS_FOREGROUND));
            }else{
                pg.fill(ThemeStore.getColor(ThemeColorType.NORMAL_FOREGROUND));
            }
        }
        float innerButtonSize = cell * 0.35f;
        pg.rect(0,0, innerButtonSize, innerButtonSize);
    }

    @Override
    protected void drawNodeBackground(PGraphics pg) {
        boolean mousePressed = app.mousePressed;
        valueBoolean = isMouseOverNode && mousePressedLastFrame && !mousePressed;
        mousePressedLastFrame = mousePressed;
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        drawLeftText(pg, name);
        drawRightBackdrop(pg, cell);
        drawRightButton(pg);
    }

    @Override
    public float getRequiredWidthForHorizontalLayout() {
        return findTextWidthRoundedUpToWholeCells(name) + cell * 2;
    }



    @Override
    public void mouseDragNodeContinueEvent(GuiMouseEvent e) {
        super.mouseDragNodeContinueEvent(e);
        e.setConsumed(true);
    }

    public boolean getBooleanValueAndSetItToFalse() {
        boolean result = valueBoolean;
        valueBoolean = false;
        if(result){
            onValueChangingActionEnded();
        }
        return result;
    }

    @Override
    public void mousePressedEvent(GuiMouseEvent e) {
        super.mousePressedEvent(e);
        executePressActions();
    }

    @Override
    public void mouseReleasedOverNodeEvent(GuiMouseEvent e) {
        super.mouseReleasedOverNodeEvent(e);
        executeReleaseActions();
    }
        @Override
    public void registerAction(ActivateByType type, MouseAction action) {
        switch (type){
            case PRESS -> pressActions.add(action);
            case RELEASE -> releaseActions.add(action);
        }

    }

    @Override
    public void executePressActions() {
        pressActions.forEach(MouseAction::doAction);
    }

    @Override
    public void executeReleaseActions() {
        releaseActions.forEach(MouseAction::doAction);
    }
}
