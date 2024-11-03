package com.szadowsz.gui.component.action;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.config.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.gui.input.mouse.RMouseAction;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static processing.core.PConstants.CENTER;

/**
 * Bog-standard Button Component
 */
public class RButton extends RComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RButton.class);

    // Whether its currently pressed
    protected boolean value = false;

    // A little bit of history
    protected boolean pressedLastFrame = false;

    // Mouse Actions, i.e. what to do when the button is pressed/released
    protected List<RMouseAction> pressActions = new CopyOnWriteArrayList<>();
    protected List<RMouseAction> releaseActions = new CopyOnWriteArrayList<>();

    /**
     * Default Constructor
     *
     * @param gui gui controller for the window
     * @param path component tree path
     * @param group parent component
     */
    public RButton(RotomGui gui, String path, RGroup group) {
        super(gui, path, group);
        size.x = suggestWidth();
        isDraggable = false;
    }

    /**
     * Draw the button part of the button
     *
     * @param pg processing graphics reference to draw on
     */
    protected void drawButtonRight(PGraphics pg) {
        pg.noFill();
        pg.pushMatrix();
        pg.translate(size.x - RLayoutStore.getCell() * 0.5f, RLayoutStore.getCell() * 0.5f);
        fillBackground(pg);
        pg.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND));
        pg.rectMode(CENTER);
        float outerButtonSize = RLayoutStore.getCell() * 0.6f;
        pg.rect(0, 0, outerButtonSize, outerButtonSize);
        pg.stroke(RThemeStore.getRGBA(isDragged ? RColorType.FOCUS_FOREGROUND : RColorType.NORMAL_FOREGROUND));
        if (isMouseOver) {
            if (isDragged) {
                LOGGER.debug("Doing Button Focus Fill {} Component",  getName());
                pg.fill(RThemeStore.getRGBA(RColorType.FOCUS_FOREGROUND));
            } else {
                pg.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND));
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

    /**
     * Execute the pressed actions, in the order they were added
     */
    protected void executePressActions() {
        pressActions.forEach(RMouseAction::execute);
    }

    /**
     * Execute the released actions, in the order they were added
     */
    protected void executeReleaseActions() {
        releaseActions.forEach(RMouseAction::execute);
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float mouseY) {
        super.mousePressed(mouseEvent,mouseY);
        executePressActions();
    }

    @Override
    public void mouseReleasedOverComponent(RMouseEvent mouseEvent, float mouseY) {
        super.mouseReleasedOverComponent(mouseEvent,mouseY);
        executeReleaseActions();
    }

    /**
     * Add functions to the button
     *
     * @param type pressed or released
     * @param action the action lambda to do
     */
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
