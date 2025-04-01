package com.szadowsz.rotom4j.component.nitro.narc.files;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.bined.RBinHeader;
import com.szadowsz.gui.component.bined.RBinMain;
import com.szadowsz.gui.component.bined.RBinPageSlider;
import com.szadowsz.gui.component.bined.settings.RCodeAreaSection;
import com.szadowsz.gui.component.bined.settings.RSelectingMode;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.utils.RComponentScrollbar;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RRect;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.narc.NARC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

import static com.szadowsz.gui.utils.RCoordinates.isPointInRect;

public class NARCFilesGroup extends R4JComponent<NARC> {
    private final static Logger LOGGER = LoggerFactory.getLogger(NARCFilesGroup.class);

    protected static final int MAX_CHILD_DISPLAY = 32;
    protected static final String MAIN = "Main";

    protected final NARC narc;

    protected float actualHeight;

    // Vertical Scrollbar
    protected RComponentScrollbar vsb;

    public NARCFilesGroup(RotomGui gui, String path, RGroup parent, NARC data) {
        super(gui, path, parent);
        narc = data;

        initDimensions();

        children.add(new NARCFilesMain(gui, path + "/" + MAIN, this, narc));
        vsb = new RComponentScrollbar(
                this,
                new PVector(pos.x + children.getFirst().getWidth(), pos.y),
                new PVector(RLayoutStore.getCell(), getHeight()),
                actualHeight,
                16
        );
        LOGGER.info("Bin Editor {} created scrollbar with Pos [{}, {}] Size [{},{}]", getName(), vsb.getPosX(), vsb.getPosY(), vsb.getWidth(), vsb.getHeight());
        vsb.setVisible(true);

    }

    protected void initDimensions() {
        int childCount = narc.getFiles().size();

        actualHeight = childCount * RLayoutStore.getCell();

        int childDisplayCount = Math.min(MAX_CHILD_DISPLAY, childCount);

        // Relay the size to the proper place
        size.x = suggestWidth();
        size.y = childDisplayCount * RLayoutStore.getCell();
    }

    /**
     * Check if the point is inside the scroll bar of the Window
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the point is inside the scroll bar, false otherwise
     */
    protected boolean isPointInsideScrollbar(float x, float y) {
        if (vsb == null || !vsb.isVisible()) {
            return false;
        }
        float cx = pos.x + children.getFirst().getWidth();
        return isPointInRect(x, y,
                cx, pos.y, RLayoutStore.getCell(), size.y);
    }

    protected boolean isMouseInsideScrollbar(RMouseEvent e, float adjustedMouseY) {
        return isVisible && isPointInsideScrollbar(e.getX(), adjustedMouseY);
    }

    public int getVerticalScroll() {
        float yDiff = actualHeight - getHeight();
        float value = (vsb != null) ? vsb.getValue() : 0.0f;
        return (int) (yDiff * value);
    }

    @Override
    public boolean isMouseOver() {
        return super.isMouseOver() || (vsb != null && vsb.isMouseOver());
    }

    @Override
    public boolean isDragged() {
        return super.isDragged() || (vsb != null && vsb.isDragged());
    }

    @Override
    public void setLayout(RLayoutBase layout) {

    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        pg.pushMatrix();
        NARCFilesMain files = (NARCFilesMain) findChildByName(MAIN);
        pg.translate(files.getRelPosX(), files.getRelPosY());
        files.draw(pg);
        pg.popMatrix();

        pg.pushMatrix();
        if (vsb != null && vsb.isVisible()) {
            vsb.draw(pg,
                    pos.x,
                    pos.y,
                    files.getWidth()
            );
        }
        pg.popMatrix();
    }

    @Override
    public void drawToBuffer() {
        NARCFilesMain files = (NARCFilesMain) findChildByName(MAIN);
        files.drawToBuffer();
        if (vsb != null && vsb.isVisible()) {
            vsb.drawToBuffer( pos.x,
                    pos.y,
                    files.getWidth(),
                    getHeight(),
                    actualHeight);
        }
        buffer.redraw();
    }

    @Override
    public void mouseOver(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (isMouseInsideScrollbar(mouseEvent, adjustedMouseY)) {
            LOGGER.debug("Bin Editor {} mouse over scrollbar", getName());
            if (isChildMouseOver()) {
                buffer.invalidateBuffer();
            }
            if (!vsb.isMouseOver()) {
                redrawBuffers();
            }
            vsb.mouseMoved(mouseEvent, adjustedMouseY);
            setMouseOverThisOnly(gui.getComponentTree(), mouseEvent);
            mouseEvent.consume();
        } else {
            super.mouseOver(mouseEvent, adjustedMouseY);
        }
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (!gui.hasFocus(this)) {
            gui.takeFocus(this);
        }
        if (mouseEvent.isLeft()) {
            if (isMouseInsideScrollbar(mouseEvent, adjustedMouseY)) {
                LOGGER.debug("Bin Editor {} mouse [{},{}] pressed over scrollbar with Pos {{}, {}] Size [{},{}]", getName(), mouseEvent.getX(), adjustedMouseY, vsb.getPosX(), vsb.getPosY(), vsb.getWidth(), vsb.getHeight());
                vsb.mousePressed(mouseEvent, adjustedMouseY);
                mouseEvent.consume();
            } else {
                RComponent child = findVisibleComponentAt(mouseEvent.getX(), adjustedMouseY);
                child.mousePressed(mouseEvent, adjustedMouseY);
                mouseEvent.consume();
            }
        }
    }

    @Override
    public void mouseDragged(RMouseEvent mouseEvent) {
        if (gui.hasFocus(this)) {
            if (vsb != null && vsb.isDragged()) {
                LOGGER.debug("Narc Folder {} mouse dragged with scrollbar Size [{},{}]", getName(), vsb.getWidth(), vsb.getHeight());
                vsb.mouseDragged(mouseEvent);
                redrawBuffers(); // REDRAW-VALID: we should redraw the buffer solely on the basis that the user dragged the mouse
            } else {
                super.mouseDragged(mouseEvent);
                mouseEvent.consume();
                redrawBuffers(); // REDRAW-VALID: we should redraw the binary editor if the user is selecting multiple chars
            }
        }
    }

    @Override
    public void mouseReleasedAnywhere(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (isDragged() || (vsb != null && vsb.isDragged())) {
            if (vsb.isDragged()) {
                LOGGER.info("Bin Editor {} mouse released scrollbar anywhere", getName());
                vsb.mouseReleased(mouseEvent, adjustedMouseY);
            } else {
                super.mouseReleasedAnywhere(mouseEvent, adjustedMouseY);
                setFocus(false);
            }
            mouseEvent.consume();
            redrawBuffers(); // REDRAW-VALID: we should redraw the buffer solely on the basis that the user released the mouse
        }
    }

    /**
     * Method to handle the component's reaction to the mouse being released over it
     *
     * @param mouseEvent     the change made by the mouse
     * @param adjustedMouseY adjust for scrollbar
     */
    @Override
    public void mouseReleasedOverComponent(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (isDragged() || (vsb != null && vsb.isDragged())) {
            if (vsb.isDragged()) {
                LOGGER.debug("Bin Editor {} mouse released over scrollbar", getName());
                vsb.mouseReleased(mouseEvent, adjustedMouseY);
            } else {
                setFocus(true);
            }
            super.mouseReleasedOverComponent(mouseEvent, adjustedMouseY);
            mouseEvent.consume();
            redrawBuffers(); // REDRAW-VALID: we should redraw the buffer solely on the basis that the user released the mouse
        }
        redrawBuffers(); // REDRAW-VALID: we should redraw the buffer solely on the basis that the user released the mouse
    }

    @Override
    public void redrawBuffers() {
        if (vsb != null) {
            vsb.invalidateBuffer();
        }
        super.redrawBuffers();
    }

    @Override
    public void recolorImage() throws NitroException {
    }

    @Override
    public void updateCoordinates(float bX, float bY, float rX, float rY, float w, float h) {
        children.getFirst().updateCoordinates(bX, bY, rX, rY, w - RLayoutStore.getCell(), h); // main
        updateComponentCoordinates(bX, bY, rX, rY, w, h);
        if (vsb != null) {
            vsb.updateCoordinates(pos.x + children.getFirst().getWidth(), pos.y, RLayoutStore.getCell(), h, actualHeight);
        }
        buffer.resetBuffer();
    }

    @Override
    public float suggestWidth() {
        return super.suggestWidth() + RLayoutStore.getCell();
    }



}
