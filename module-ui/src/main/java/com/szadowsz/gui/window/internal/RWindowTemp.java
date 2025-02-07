package com.szadowsz.gui.window.internal;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.input.mouse.RMouseHiding;
import com.szadowsz.gui.layout.RLinearLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import static com.old.ui.utils.Coordinates.isPointInRect;

/**
 * Gui Temporary Window Node Organisation and Drawing
 */
public class RWindowTemp extends RWindowImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(RWindowTemp.class);

    public RWindowTemp(PApplet app, RotomGui gui, RFolder folder, PVector pos, PVector size) {
        super(app,gui,folder,"", (int) pos.x, (int) pos.y, (int) size.x, (int) size.y);
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
    protected RComponent findVisibleComponentAt(float x, float y) {
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
        return folder.getParentFolder() != null && folder.getParentWindow().isPointInsideWindow(x,y);
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
    public void drawWindow(PGraphics canvas) {
        canvas.textFont(RFontStore.getMainFont());
        setScrollbarHighlighted(isVisible && (isPointInsideScrollbar(sketch.mouseX, sketch.mouseY) && !isBeingDragged) || folder.isMouseOver());
        if (!isVisible || !folder.isVisibleParentAware()) {
            return;
        }
        constrainBounds(canvas);
        canvas.pushMatrix();
        drawBackgroundWithWindowBorder(canvas, true);
        vsb.ifPresent(s ->
                s.draw(
                        canvas,
                        pos.x,
                        pos.y,
                        contentSize.x,
                        contentSize.y,
                        sizeUnconstrained.y
                )
        );
        if (!folder.getChildren().isEmpty()) {
            drawContent(canvas);
        }
        drawBackgroundWithWindowBorder(canvas, false);
        canvas.popMatrix();
    }

    @Override
    public void mouseMoved(RMouseEvent mouseEvent) {
        if (!isVisible()) {
            return;
        }

        if (isMouseInsideContent(mouseEvent)) {
            LOGGER.debug("Mouse Inside Content: X {} Y {} WinX {} WinY {} Width {} Height {}", mouseEvent.getX(), mouseEvent.getY(), pos.x, pos.y, size.x, size.y);
            RComponent child = findVisibleComponentAt(mouseEvent.getX(), mouseEvent.getY());
            if (child != null) {
                if (!child.isMouseOver()) {
                    LOGGER.debug("Inside Component {} [NX {} NY {} Width {} Height {}]", child.getName(), child.getPosX(), child.getPosY(), child.getWidth(), child.getHeight());
                    contentBuffer.invalidateBuffer();
                }
                child.mouseOver(mouseEvent, mouseEvent.getY());
            }
        } else {
            if (!isPointInRect(mouseEvent.getX(),mouseEvent.getY(),pos.x-5,pos.y-5,size.x+10,size.y+10)) {
                if (folder.isChildMouseOver()) {
                    LOGGER.debug("Child Was Over {} Window",title);
                    contentBuffer.invalidateBuffer();
                }

                gui.setAllMouseOverToFalse(this.folder);
                if (!isInParentWindow(mouseEvent.getX(), mouseEvent.getY()) && !isInChildWindow(mouseEvent.getX(), mouseEvent.getY())) {
                    close();
                }
            }
        }
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent) {
        if (!isVisible()) {
            return;
        }
        // Make sure Window Grabs focus
        if (isMouseInsideWindow(mouseEvent)) {
            if (!isFocused()) {
                setFocusOnThis();
            }
        }
        // Reset Values
        isCloseInProgress = false;
        isBeingDragged = false;
        isBeingResized = false;


        // Then Check Window Parts
        if (canBeClosed() && ((isMouseInsideCloseButton(mouseEvent) && mouseEvent.isLeft()) || (isMouseInsideWindow(mouseEvent) && mouseEvent.isRight()))) {
            isCloseInProgress = true;
            mouseEvent.consume();
        } else if (isPointInsideContent(mouseEvent.getX(), mouseEvent.getY())) {
            RComponent child = findVisibleComponentAt(mouseEvent.getX(), mouseEvent.getY());
            if (child != null) {
                contentBuffer.invalidateBuffer();
                child.mousePressed(mouseEvent, mouseEvent.getY());
            }
        }
    }

    @Override
    public void mouseReleased(RMouseEvent mouseEvent) {
        if (!isVisible()) {
            return;
        }

        // Check Window Parts
        if (isCloseInProgress && ((isMouseInsideCloseButton(mouseEvent) && mouseEvent.isLeft()) || (isMouseInsideWindow(mouseEvent) && mouseEvent.isRight()))) {
            close();
            mouseEvent.consume();
        } else {
            RComponent released = findVisibleComponentAt(mouseEvent.getX(), mouseEvent.getY());
            for (RComponent child : folder.getChildren()) {
                boolean isReleased = child.equals(released);
                if (isReleased) {
                    contentBuffer.invalidateBuffer();
                }
                child.mouseReleased(mouseEvent, mouseEvent.getY(), isReleased);
            }
            if (mouseEvent.isConsumed()){
                RMouseHiding.tryRevealMouseAfterDragging(sketch);
            }
        }
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
