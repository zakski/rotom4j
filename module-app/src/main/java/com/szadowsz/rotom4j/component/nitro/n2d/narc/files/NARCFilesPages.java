package com.szadowsz.rotom4j.component.nitro.n2d.narc.files;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.utils.RComponentScrollbar;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.n2d.narc.NARC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

import static com.szadowsz.gui.utils.RCoordinates.isPointInRect;

public class NARCFilesPages extends R4JComponent<NARC> {
    private final static Logger LOGGER = LoggerFactory.getLogger(NARCFilesPages.class);

    protected static final int MAX_CHILD_DISPLAY = 32;
    protected static final int MAX_CHILD_PAGE = MAX_CHILD_DISPLAY*4;

    protected static final String SLIDER = "page";
    protected static final String PAGE = "page_";

    protected final NARC narc;

    protected int totalPages;

    protected float actualHeight;

    // Vertical Scrollbar
    protected RComponentScrollbar vsb;

    public NARCFilesPages(RotomGui gui, String path, RGroup parent, NARC data) {
        super(gui, path, parent);
        narc = data;

        initDimensions();

        children.add(new NARCFilesPageSlider(gui, path + "/" + SLIDER, this));
        for (int i = 0; i < totalPages;i++) {
            children.add(new NarcFilesPage(gui, path + "/" + PAGE + i, this, narc, i));
        }
        ((NarcFilesPage)children.get(1)).turnTo();
        vsb = new RComponentScrollbar(
                this,
                new PVector(pos.x + children.getFirst().getWidth(), pos.y),
                new PVector(RLayoutStore.getCell(), getHeight()),
                actualHeight,
                16
        );

        LOGGER.info("Bin Editor {} created scrollbar with Pos [{}, {}] Size [{},{}]", getName(), vsb.getPosX(), vsb.getPosY(), vsb.getWidth(), vsb.getHeight());
        vsb.setVisible(getFilesOnPage(0) > MAX_CHILD_DISPLAY);
    }

    protected void initDimensions() {
        int childCount = narc.getFiles().size();
        totalPages = childCount / MAX_CHILD_PAGE + (((childCount % MAX_CHILD_PAGE) > 0)?1:0);

        actualHeight = RLayoutStore.getCell() + Math.min(MAX_CHILD_PAGE, childCount) * RLayoutStore.getCell();
        int childDisplayCount = Math.min(MAX_CHILD_DISPLAY, childCount);

        // Relay the size to the proper place
        size.x = suggestWidth();
        size.y = RLayoutStore.getCell() + childDisplayCount * RLayoutStore.getCell();
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

    protected boolean shouldDisplayVerticalScrollbar() {
       if (getCurrentPageNumber() == getTotalPages()-1) {
                return narc.getFiles().size() % (MAX_CHILD_DISPLAY*4) > MAX_CHILD_DISPLAY;
            } else {
                return true;
            }
    }

    protected int getCurrentPageNumber(){
        NARCFilesPageSlider pages = ((NARCFilesPageSlider) findChildByName(SLIDER));
        return (pages != null)? pages.getValueAsInt():1;
    }

    protected NarcFilesPage getCurrentPage(){
       return (NarcFilesPage) children.get(getCurrentPageNumber()+1);
    }

    protected int getTotalPages(){
        return totalPages;
    }

    protected int getMaxFilesPerPage(){
        if (totalPages > 1){
            return MAX_CHILD_PAGE;
        } else {
            return narc.getFiles().size();
        }
     }

    protected int getFilesOnPage(int pageNumber){
        if (pageNumber < getTotalPages()-1){
            return getMaxFilesPerPage();
        } else {
            int last = narc.getFiles().size() % MAX_CHILD_PAGE;
            return (last > 0)?last: MAX_CHILD_PAGE;
        }
    }


    @Override
    protected void drawForeground(PGraphics pg, String name) {
        pg.pushMatrix();
        NARCFilesPageSlider slider = (NARCFilesPageSlider) findChildByName(SLIDER);
        pg.translate(slider.getRelPosX(), slider.getRelPosY());
        slider.draw(pg);
        pg.popMatrix();

        pg.pushMatrix();
        NarcFilesPage files = (NarcFilesPage) findChildByName(PAGE + getCurrentPageNumber());
        pg.translate(files.getRelPosX(), files.getRelPosY());
        files.draw(pg);
        pg.popMatrix();

        pg.pushMatrix();
        if (vsb != null && vsb.isVisible()) {
            vsb.draw(pg,
                    0,
                    0,
                    files.getWidth()
            );
        }
        pg.popMatrix();
    }

    float getActualHeight() {
        return actualHeight;
    }

    void turnPage() {
        LOGGER.info("Turning to " + narc.getFileName() + " page " + getCurrentPageNumber());
        NarcFilesPage files = (NarcFilesPage) findChildByName(PAGE + getCurrentPageNumber());
        files.turnTo();
        initDimensions();
        updateChildrenCoordinates();
        vsb.setVisible(shouldDisplayVerticalScrollbar());
        resetBuffer();
    }

    @Override
    public boolean isMouseOver() {
        return super.isMouseOver() || (vsb != null && vsb.isMouseOver());
    }

    @Override
    public boolean isDragged() {
        return super.isDragged() || (vsb != null && vsb.isDragged());
    }

    public int getVerticalScroll() {
        float yDiff = actualHeight - getHeight();
        float value = (vsb != null) ? vsb.getValue() : 0.0f;
        return (int) (yDiff * value);
    }

    @Override
    public PVector getPreferredSize() {
        PVector full = layout.calcPreferredSize(getParentFolder().getName(), children);
        NARCFilesPageSlider pages = (NARCFilesPageSlider) findChildByName(SLIDER);

        return new PVector(full.x, pages.getPreferredSize().y + Math.min(MAX_CHILD_DISPLAY,getFilesOnPage(getCurrentPageNumber())) * RLayoutStore.getCell());
    }

    @Override
    public void setLayout(RLayoutBase layout) {

    }

    @Override
    public void drawToBuffer() {
        NARCFilesPageSlider pages = (NARCFilesPageSlider) findChildByName(SLIDER);
        pages.drawToBuffer();
        NarcFilesPage files = (NarcFilesPage) findChildByName(PAGE + getCurrentPageNumber());
        files.drawToBuffer();
        if (vsb != null && vsb.isVisible()) {
            vsb.drawToBuffer( 0,
                    0,
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
                if (child != null) {
                    child.mousePressed(mouseEvent, adjustedMouseY);
                    mouseEvent.consume();
                }
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
        if (!buffer.isBufferInvalid()) {
            if (vsb != null) {
                vsb.invalidateBuffer();
            }
            super.redrawBuffers();
            NARCFilesPageSlider pages = (NARCFilesPageSlider) findChildByName(SLIDER);
            pages.redrawBuffers();
            NarcFilesPage files = (NarcFilesPage) findChildByName(PAGE + getCurrentPageNumber());
            files.redrawBuffers();
         }
    }

    @Override
    public void recolorImage() throws NitroException {
    }

    @Override
    public void updateCoordinates(float bX, float bY, float rX, float rY, float w, float h) {
        NARCFilesPageSlider pages = (NARCFilesPageSlider) findChildByName(SLIDER);
        pages.updateCoordinates(bX, bY, rX, rY, w - RLayoutStore.getCell(), RLayoutStore.getCell()); // page
        NarcFilesPage files = (NarcFilesPage) findChildByName(PAGE + getCurrentPageNumber());
        files.updateCoordinates(bX, bY, rX, rY + RLayoutStore.getCell(), w - RLayoutStore.getCell(), h - RLayoutStore.getCell()); // main
        updateComponentCoordinates(bX, bY, rX, rY, w, h);
        if (vsb != null) {
            vsb.updateCoordinates(pos.x + children.getFirst().getWidth(), pos.y + RLayoutStore.getCell(), RLayoutStore.getCell(), h - RLayoutStore.getCell(), actualHeight);
        }
        buffer.resetBuffer();
    }

    public void updateChildrenCoordinates() {
        // This gets called from inside the buffer without using updateCoordinates first
        NARCFilesPageSlider pages = (NARCFilesPageSlider) findChildByName(SLIDER);
        pages.updateCoordinates(pos.x, pos.y, relPos.x, relPos.y, getWidth() - RLayoutStore.getCell(), RLayoutStore.getCell()); // page
        NarcFilesPage files = (NarcFilesPage) findChildByName(PAGE + getCurrentPageNumber());
        files.updateCoordinates(pos.x, pos.y + RLayoutStore.getCell(), relPos.x, relPos.y + RLayoutStore.getCell(), getWidth() - RLayoutStore.getCell(), getHeight() - RLayoutStore.getCell()); // main
        if (vsb != null) {
            vsb.updateCoordinates(pos.x + children.getFirst().getWidth(), pos.y, RLayoutStore.getCell(), getHeight(), actualHeight);
        }
    }

    @Override
    public float suggestWidth() {
        return super.suggestWidth() + RLayoutStore.getCell();
    }

    public void reindex() {
        for (int i = 1; i < children.size(); i++) {
            ((NarcFilesPage)children.get(i)).reindex();
        }
        redrawBuffers();
    }
}
