package com.szadowsz.gui.window.internal;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.config.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RLinearLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import static com.szadowsz.ui.utils.Coordinates.isPointInRect;

/**
 * Gui Temporary Window Node Organisation and Drawing
 */
public class RWindowTemp extends RWindowInt {
    private static final Logger LOGGER = LoggerFactory.getLogger(RWindowTemp.class);

    public RWindowTemp(PApplet app, RotomGui gui, RFolder folder, PVector pos, PVector size) {
        super(app,gui,folder,"", new PVector(pos.x, pos.y), new PVector(size.x,size.y));
    }

    /**
     * Constructor for Internal Temporary Window
     *
     * @param app    PApplet to render window inside
      * @param xPos   initial X display location in PApplet
     * @param yPos   initial Y display location in PApplet
     * @param width  initial window width
     * @param height initial window height
     */
    public RWindowTemp(PApplet app, RotomGui gui, RFolder folder, float xPos, float yPos, float width, float height) {
        this(app, gui, folder, new PVector(xPos, yPos), new PVector(width, height));
    }

    @Override
    protected void constrainHeight(PGraphics pg, float preferredHeight) {
        if (!RLayoutStore.shouldKeepWindowsInBounds()) {
            return;
        }
        size.y = preferredHeight;
        contentSize.y = size.y;
        sizeUnconstrained.y = size.y;
        if (pos.y + preferredHeight > pg.height) {
                size.y = pg.height - pos.y;
                 vsb.ifPresent(s -> s.setVisible(true));
        }
    }

    @Override
    protected RComponent findComponentAt(float x, float y) {
        for (RComponent node : folder.getChildren()) {
            if (!node.isVisible()) {
                continue;
            }
            if (isPointInRect(x, y, node.getPosX(), node.getPosY(), node.getWidth(), node.getHeight())) {
                return node;
            }
        }
        return null;
    }

    /**
     * Check if the mouse is inside the content of the Window
     *
     * @param e mouse event
     * @return true if the mouse is inside the content, false otherwise
     */
    @Override
    protected boolean isMouseInsideContent(RMouseEvent e) {
        return isPointInsideWindow(e.getX(), e.getY());
    }

    @Override
    protected boolean isPointInsideTitleBar(float x, float y) {
        return false;
    }

    @Override
    protected boolean isPointInsideCloseButton(float x, float y) {
        return false;
    }


    @Override
    protected boolean isPointInsideResizeBorder(float x, float y) {
        return false;
    }

    /**
     * Method to Check if the mouse is inside the parent window
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true if it is inside the parent window, false otherwise
     */
    public boolean isInParentWindow(float x, float y){
        return folder.getParentFolder() != null && folder.getParentFolder().getWindow().isPointInsideWindow(x,y);
    }

    /**
     * Method to Check if the mouse is inside a child window
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true if it is inside a child window, false otherwise
     */
    public boolean isInChildWindow(float x, float y){
        return folder.getChildren().stream().filter(n -> n instanceof RFolder)
                .map(n -> (RFolder) n)
                .anyMatch(n -> n.getWindow() != null && (n.getWindow().isPointInsideWindow(x,y) ||
                        ((n.getWindow() instanceof RWindowTemp) && ((RWindowTemp) n.getWindow()).isInChildWindow(x, y))));
    }

    @Override
    protected void drawContent(PGraphics pg) {
        if (!folder.getChildren().isEmpty()) {
            pg.pushMatrix();
            pg.translate(pos.x, pos.y);
            pg.image(contentBuffer.draw(), 0, 0);
            pg.popMatrix();
        }
    }
    @Override
    public void drawWindow(PGraphics pg) {
        pg.textFont(RFontStore.getMainFont());
        setScrollbarHighlighted(isVisible && (isPointInsideScrollbar(app.mouseX, app.mouseY) && !isBeingDragged) || folder.isMouseOver());
        if (!isVisible || !folder.isVisibleParentAware()) {
            return;
        }
        constrainBounds(pg);
        pg.pushMatrix();
        drawBackgroundWithWindowBorder(pg, true);
        vsb.ifPresent(s ->
                s.draw(
                        pg,
                        pos.x,
                        pos.y + ((folder.shouldDrawTitle())?RLayoutStore.getCell():0),
                        contentSize.x,
                        contentSize.y,
                        sizeUnconstrained.y - ((folder.shouldDrawTitle())?RLayoutStore.getCell():0)
                )
        );
        if (!folder.getChildren().isEmpty()) {
            drawContent(pg);
        }
        drawBackgroundWithWindowBorder(pg, false);
        pg.popMatrix();
    }


    @Override
    public void mouseMoved(RMouseEvent e) {
        if (isMouseInsideScrollbar(e)) {
            if (folder.getChildren().stream().anyMatch(RComponent::isMouseOver)) {
                contentBuffer.invalidateBuffer();
            }
            e.consume();
            vsb.ifPresent(s -> s.mouseMoved(e));
            folder.setIsMouseOverThisNodeOnly(gui.getComponentTree());
        } else if (isMouseInsideContent(e)) {
            LOGGER.debug("Mouse Inside Content: X {} Y {} WinX {} WinY {} Width {} Height {}", e.getX(), e.getY(), pos.x, pos.y, size.x, size.y);
            RComponent node = findComponentAt(e.getX(), e.getY());
            if (node != null && !node.isMouseOver()) {
                LOGGER.debug("Inside {} Component MX {} MY {} NX {} NY {} Width {} Height {}",  node.getName(), e.getX(), e.getY(), node.getPosX(), node.getPosY(), node.getWidth(), node.getHeight());
                contentBuffer.invalidateBuffer();
            }
            if (node != null && node.isParentWindowVisible()) {
                LOGGER.debug("Consume Mouse Move for {} Component",  node.getName());
                node.setIsMouseOverThisNodeOnly(gui.getComponentTree());
                e.consume();
            }
        } else {
            if (!isPointInRect(e.getX(),e.getY(),pos.x-5,pos.y-5,size.x+10,size.y+10)) {
                gui.setAllMouseOverToFalse(this.folder);
                if (!isInParentWindow(e.getX(), e.getY()) && !isInChildWindow(e.getX(), e.getY())) {
                    close();
                }
            }
        }
    }

    @Override
    public void mouseReleased(RMouseEvent e) {
        super.mouseReleased(e);
    }

    @Override
    public float getContentHeight(){
        switch (folder.getLayout()) {
            case RLinearLayout linear -> {
                return (int) contentSize.y;
            }
            default -> throw new IllegalStateException("Unexpected value: " + folder.getLayout());
        }
    }
}
