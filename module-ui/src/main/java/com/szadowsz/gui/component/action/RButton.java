package com.szadowsz.gui.component.action;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.config.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.theme.RThemeColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.gui.input.mouse.RMouseAction;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.window.internal.RWindowTemp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bog-standard Button Component
 */
public class RButton extends RComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RButton.class);

    protected boolean value = false; // TODO LazyGui

    protected boolean pressedLastFrame = false; // TODO LazyGui

    protected List<RMouseAction> pressActions = new CopyOnWriteArrayList<>();
    protected List<RMouseAction> releaseActions = new CopyOnWriteArrayList<>();

    public RButton(RotomGui gui, String path, RFolder folder) {
        super(gui, path, folder);
        size.x = suggestWidth();
        isDraggable = false;
    }

    protected void drawButtonRight(PGraphics pg) { // TODO LazyGui
        pg.noFill();
        pg.pushMatrix();
        pg.translate(size.x - RLayoutStore.getCell() * 0.5f, RLayoutStore.getCell() * 0.5f);
        fillBackground(pg);
        pg.stroke(RThemeStore.getRGBA(RThemeColorType.NORMAL_FOREGROUND));
        pg.rectMode(CENTER);
        float outerButtonSize = RLayoutStore.getCell() * 0.6f;
        pg.rect(0, 0, outerButtonSize, outerButtonSize);
        pg.stroke(RThemeStore.getRGBA(isDragged ? RThemeColorType.FOCUS_FOREGROUND : RThemeColorType.NORMAL_FOREGROUND));
        if (isMouseOver) {
            if (isDragged) {
                LOGGER.debug("Doing Button Focus Fill {} Component",  getName());
                pg.fill(RThemeStore.getRGBA(RThemeColorType.FOCUS_FOREGROUND));
            } else {
                pg.stroke(RThemeStore.getRGBA(RThemeColorType.NORMAL_FOREGROUND));
            }
        }
        float innerButtonSize = RLayoutStore.getCell() * 0.35f;
        pg.rect(0, 0, innerButtonSize, innerButtonSize);
        pg.popMatrix();
    }

    @Override
    protected void drawBackground(PGraphics pg) {
        boolean mousePressed = gui.getSketch().mousePressed;
        value = isMouseOver && pressedLastFrame && !mousePressed;
        pressedLastFrame = mousePressed;
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        drawTextLeft(pg, name);
        drawBackdropRight(pg, RLayoutStore.getCell());
        drawButtonRight(pg);
    }


    protected void executePressActions() {
        pressActions.forEach(RMouseAction::execute);
    }


    protected void executeReleaseActions() {
        releaseActions.forEach(RMouseAction::execute);
    }

    @Override
    public void mousePressed(RMouseEvent e) {
        super.mousePressed(e);
        executePressActions();
    }

    @Override
    public void mouseReleasedOverComponent(RMouseEvent e) {
        super.mouseReleasedOverComponent(e);
        executeReleaseActions();
    }

    public void registerAction(RActivateByType type, RMouseAction action) {
        switch (type) {
            case PRESS -> pressActions.add(action);
            case RELEASE -> releaseActions.add(action);
        }
    }

    @Override
    public float suggestWidth() {
        return RFontStore.calcMainTextWidth(name,RLayoutStore.getCell()) + RLayoutStore.getCell() * 2;
    }
}
