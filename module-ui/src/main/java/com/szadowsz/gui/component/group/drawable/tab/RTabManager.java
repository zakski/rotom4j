package com.szadowsz.gui.component.group.drawable.tab;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLayoutConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.*;

import static com.szadowsz.gui.utils.RCoordinates.isPointInRect;

public class RTabManager extends RGroupDrawable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RGroupDrawable.class);

    protected static final String HEADER = "header";

    protected int active;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    public RTabManager(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
        layout = null;
        children.add(new RTabHeader(gui, path + "/" + HEADER, this));
    }

    protected PVector getPreferredTabSize() {
        float maxWidth = 0.0f;
        float maxHeight = 0.0f;
        for (RTab tab : getTabs()){
            PVector size = tab.getPreferredSize();
            maxWidth = Math.max(size.x, maxWidth);
            maxHeight = Math.max(size.y, maxHeight);
        }
        return new PVector(maxWidth, maxHeight);
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        RComponent header = findChildByName(HEADER);
        pg.pushMatrix();
        pg.translate(header.getRelPosX(), header.getRelPosY());
        header.draw(pg);
        RTab tab = getActiveTab();
        if (tab != null) {
            pg.translate(tab.getRelPosX(), tab.getRelPosY());
            drawHorizontalSeparator(pg);
            tab.draw(pg);
        }
        pg.popMatrix();
    }


    protected void updateChildrenCoordinates() { // TODO Fancyify
        float availableVerticalSpace = size.y;
        float availableHorizontalSpace = size.x;
        LOGGER.trace("{} Available Space [{},{}]",getName(),availableHorizontalSpace,availableVerticalSpace);

        final Map<RComponent, PVector> fittingMap = new IdentityHashMap<>();

        RComponent header = findChildByName(HEADER);
        PVector preferredSize = header.getPreferredSize();
        LOGGER.info("{} Tab Header Preferred Size [{},{}]",getName(),preferredSize.x,preferredSize.y);
        PVector headerFittingSize = new PVector(
                Math.min(availableHorizontalSpace, preferredSize.x),
                preferredSize.y);
        if (availableHorizontalSpace > headerFittingSize.x) {
            headerFittingSize.x = availableHorizontalSpace;
        }
        LOGGER.info("{} Tab Header Fitting Size [{},{}]",getName(),headerFittingSize.x,headerFittingSize.y);

        preferredSize = getPreferredTabSize();
        LOGGER.info("{} Active Tab Preferred Size [{},{}]",getName(),preferredSize.x,preferredSize.y);
        PVector tabFittingSize = new PVector(
                Math.min(availableHorizontalSpace, preferredSize.x),
                preferredSize.y);
        if (availableHorizontalSpace > tabFittingSize.x) {
            tabFittingSize.x = availableHorizontalSpace;
        }
        LOGGER.info("{} Active Tab Fitting Size [{},{}]",getName(),tabFittingSize.x,tabFittingSize.y);

        PVector start = getPosition();
        header.updateCoordinates(start, new PVector(), headerFittingSize);

        for (RTab tab: getTabs()) {
            tab.updateCoordinates(start, new PVector(0,headerFittingSize.y), tabFittingSize);
        }
    }

    List<RTab> getTabs() {
        return children.stream().filter(c -> c instanceof RTab).map(t -> (RTab) t).toList();
    }

    RTabHeader getHeader() {
        return (RTabHeader) children.getFirst();
    }

    RTab getActiveTab() {
        if (active > 0) {
            return (RTab) children.get(active);
        } else {
            return null;
        }
    }

    @Override
    public RLayoutConfig getCompLayoutConfig() {
        return null;
    }

    @Override
    public PVector getPreferredSize() {
        PVector tabSize = getPreferredTabSize();
        PVector headerSize = getHeader().getPreferredSize();
        return new PVector(Math.max(tabSize.x,headerSize.x), headerSize.y + tabSize.y);
    }

    @Override
    public void setLayout(RLayoutBase layout) {

    }

    public void setActive(RTab rTab) {
        int nIndex = children.indexOf(rTab);
        if (nIndex > 0) {
            active = nIndex;
        }
    }

    public void addTab(RTabFunction function) {
        children.add(new RTab(gui, path + "/tab" + (children.size()-1), this, function));
        RTab tab = ((RTab)children.getLast());
        active = children.size() - 1;
        getHeader().addTab(tab.getTitle(),tab.getAction());
    }

    @Override
    public void mouseOver(RMouseEvent mouseEvent, float adjustedMouseY){
        if (!isVisible() || !this.isVisibleParentAware()) {
            return;
        }
        RComponent header = findChildByName(HEADER);
        RTab tab = getActiveTab();
        if (isPointInRect(mouseEvent.getX(), adjustedMouseY, header.getPosX(), header.getPosY(), header.getWidth(), header.getHeight())) {
            if (!header.isMouseOver()){
                redrawBuffer();
            }
            header.mouseOver(mouseEvent,adjustedMouseY);
            mouseEvent.consume();
        } else if (isPointInRect(mouseEvent.getX(), adjustedMouseY, tab.getPosX(), tab.getPosY(), tab.getWidth(), tab.getHeight())) {
            if (!tab.isMouseOver()){
                redrawBuffer();
            }
            tab.mouseOver(mouseEvent,adjustedMouseY);
            mouseEvent.consume();
        }
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float adjustedMouseY){
        if (!isVisible() || !this.isVisibleParentAware()) {
            return;
        }
        RComponent header = findChildByName(HEADER);
        RTab tab = getActiveTab();
        if (isPointInRect(mouseEvent.getX(), adjustedMouseY, header.getPosX(), header.getPosY(), header.getWidth(), header.getHeight())) {
            LOGGER.debug("Mouse Pressed for tab header {} [{}, {}, {}, {}, {}, {}]", header.getName(),mouseEvent.getX(),adjustedMouseY,header.getPosX(),header.getPosY(),header.getWidth(),header.getHeight());
            header.mousePressed(mouseEvent,adjustedMouseY);
            mouseEvent.consume();
        } else if (isPointInRect(mouseEvent.getX(), adjustedMouseY, tab.getPosX(), tab.getPosY(), tab.getWidth(), tab.getHeight())) {
            LOGGER.debug("Mouse Pressed for active tab {} [{}, {}, {}, {}, {}, {}]", tab.getName(),mouseEvent.getX(),adjustedMouseY,tab.getPosX(),tab.getPosY(),tab.getWidth(),tab.getHeight());
            tab.mousePressed(mouseEvent,adjustedMouseY);
            mouseEvent.consume();
        }
        if (mouseEvent.isConsumed()){
            redrawBuffer();
        }
    }


    @Override
    public float suggestWidth() {
        float maxWidth = getTabs().stream().map(RGroupDrawable::suggestWidth).max(Float::compareTo).orElse(0f);
        return Math.max(maxWidth, getHeader().getWidth())*2;
    }

    @Override
    public void updateCoordinates(float bX, float bY, float rX, float rY, float w, float h) {
        LOGGER.debug("Update Coordinates for Drawable Group [{}, {}, {}, {}, {}, {}]", bX,bY,rX,rY,w,h);
        pos.x = bX + rX;
        pos.y = bY + rY;
        relPos.x = rX;
        relPos.y = rY;
        size.x = w;
        size.y = h;
        updateChildrenCoordinates();
    }
}
