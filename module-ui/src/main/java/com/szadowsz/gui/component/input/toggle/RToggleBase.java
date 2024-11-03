package com.szadowsz.gui.component.input.toggle;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.config.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 *  Base component for binary user input controls (etc. Toggle, Checkbox)
 */
public abstract class RToggleBase extends RComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RToggleBase.class);

    protected boolean armed;
    protected boolean value;
    protected List<Consumer<Boolean>> actions = new CopyOnWriteArrayList<>();

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui           the gui for the window that the component is drawn under
     * @param path          the path in the component tree
     * @param parentFolder  the parent component folder reference // TODO consider if needed
     * @param startingValue
     */
    protected RToggleBase(RotomGui gui, String path, RFolder parentFolder, boolean startingValue) {
        super(gui, path, parentFolder);
        size.x = suggestWidth();
        value = startingValue;
    }

    @Override
    protected void drawBackground(PGraphics pg) {
        // NOOP
    }

    /**
     * Draws the difference between a toggle, and a checkbox
     *
     * @param pg graphics reference to draw on
     * @param value toggle state to draw
     */
    protected abstract void drawToggleHandleRight(PGraphics pg, boolean value); // TODO LazyGui

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        drawTextLeft(pg, name);
        drawBackdropRight(pg, RLayoutStore.getCell());
        drawToggleHandleRight(pg, value);

    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float mouseY) {
        super.mousePressed(mouseEvent,mouseY);
        armed = true;
        LOGGER.debug("{} Armed",name);
    }

    @Override
    public void mouseReleasedAnywhere(RMouseEvent mouseEvent, float mouseY) {
        super.mouseReleasedAnywhere(mouseEvent,mouseY);
        armed = false;
        LOGGER.debug("{} Released Outside",name);
    }

    @Override
    public void mouseReleasedOverComponent(RMouseEvent mouseEvent, float mouseY) {
        super.mouseReleasedOverComponent(mouseEvent,mouseY);
        if(armed){
            value = !value;
            LOGGER.debug("{} Toggled to {}",name,value);
            actions.forEach(a -> a.accept(value));
            onValueChangeEnd();
        }
        armed = false;
    }

    @Override
    public void mouseDragged(RMouseEvent mouseEvent) {
        super.mouseDragged(mouseEvent);
        mouseEvent.consume();
    }

    @Override
    public float suggestWidth() {
        return RFontStore.calcMainTextWidth(name, RLayoutStore.getCell()) + RLayoutStore.getCell() * 2;
    }

    /**
     * Register an action to be performed when the mouse is released ove the component
     *
     * @param o action to execute
     */
    public void registerAction(Consumer<Boolean> o) {
        actions.add(o);
    }

}
