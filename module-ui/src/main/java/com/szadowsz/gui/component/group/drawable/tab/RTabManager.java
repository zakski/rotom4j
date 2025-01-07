package com.szadowsz.gui.component.group.drawable.tab;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RLayoutBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.List;

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

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        RComponent header = findChildByName(HEADER);
        pg.pushMatrix();
        pg.translate(header.getRelPosX(), header.getRelPosY());
        header.draw(pg);
        pg.popMatrix();
//        RTab tab = getActiveTab();
//        pg.pushMatrix();
//        pg.translate(tab.getRelPosX(), tab.getRelPosY());
//        tab.draw(pg);
//        pg.popMatrix();

    }

    List<RTab> getTabs() {
        return children.stream().filter(c -> c instanceof RTab).map(t -> (RTab) t).toList();
    }

    RTabHeader getHeader() {
        return (RTabHeader) children.getFirst();
    }

    RTab getActiveTab() {
        return (RTab) children.get(active);
    }

    @Override
    public PVector getPreferredSize() {
        float maxHeight = getTabs().stream().map(RGroupDrawable::getHeight).max(Float::compareTo).orElse(0f);
        return new PVector(suggestWidth(), getHeader().getHeight() + maxHeight);
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
        getHeader().addTab(tab.getName(),tab.getAction());
    }

    @Override
    public void mouseOver(RMouseEvent mouseEvent, float adjustedMouseY){
        RComponent header = findChildByName(HEADER);
        RTab tab = getActiveTab();
        if (isPointInRect(mouseEvent.getX(), adjustedMouseY, header.getPosX(), header.getPosY(), header.getWidth(), header.getHeight())) {
            header.mouseOver(mouseEvent,adjustedMouseY);
            mouseEvent.consume();
        } else if (isPointInRect(mouseEvent.getX(), adjustedMouseY, tab.getPosX(), tab.getPosY(), tab.getWidth(), tab.getHeight())) {
            tab.mouseOver(mouseEvent,adjustedMouseY);
            mouseEvent.consume();
        }
    }



    @Override
    public float suggestWidth() {
        float maxWidth = getTabs().stream().map(RGroupDrawable::suggestWidth).max(Float::compareTo).orElse(0f);
        return Math.max(maxWidth, getHeader().getWidth());
    }
}
