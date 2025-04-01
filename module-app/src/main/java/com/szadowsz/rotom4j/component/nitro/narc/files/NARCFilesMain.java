package com.szadowsz.rotom4j.component.nitro.narc.files;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLinearLayout;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.bin.BinFolder;
import com.szadowsz.rotom4j.component.nitro.nanr.NANRFolder;
import com.szadowsz.rotom4j.component.nitro.ncer.NCERFolder;
import com.szadowsz.rotom4j.component.nitro.ncgr.NCGRFolder;
import com.szadowsz.rotom4j.component.nitro.nclr.NCLRFolder;
import com.szadowsz.rotom4j.component.nitro.nscr.NSCRFolder;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.RotomFile;
import com.szadowsz.rotom4j.file.data.DataFile;
import com.szadowsz.rotom4j.file.nitro.nanr.NANR;
import com.szadowsz.rotom4j.file.nitro.narc.NARC;
import com.szadowsz.rotom4j.file.nitro.ncer.NCER;
import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;
import com.szadowsz.rotom4j.file.nitro.nclr.NCLR;
import com.szadowsz.rotom4j.file.nitro.nscr.NSCR;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

import java.awt.image.BufferedImage;
import java.util.List;

import static com.szadowsz.gui.utils.RCoordinates.isPointInRect;

public class NARCFilesMain extends R4JComponent<NARC> {


    protected final NARCFilesGroup filesGroup;

    protected final NARC narc;

    private PImage toDraw;

    public NARCFilesMain(RotomGui gui, String path, NARCFilesGroup parent, NARC data) {
        super(gui, path, parent);
        ((RLinearLayout) layout).setReduce(false);
        filesGroup = parent;
        narc = data;
        try {
            initFiles();
        } catch (NitroException ignored) {

        }
    }

    protected void initFiles() throws NitroException {
        if (!children.isEmpty()) {
            return;
        }
        List<RotomFile> files = narc.getFiles();

        for (RotomFile file : files) {
            children.add(
                    switch (file) {
                        case NANR nanr -> new NANRFolder(gui, path + "/" + nanr.getFileName(), this, nanr);
                        case NCER ncer -> new NCERFolder(gui, path + "/" + ncer.getFileName(), this, ncer);
                        case NSCR nscr -> new NSCRFolder(gui, path + "/" + nscr.getFileName(), this, nscr);
                        case NCGR ncgr -> new NCGRFolder(gui, path + "/" + ncgr.getFileName(), this, ncgr);
                        case NCLR nclr -> new NCLRFolder(gui, path + "/" + nclr.getFileName(), this, nclr);
                        default ->
                                new BinFolder(gui, path + "/" + file.getFileName(), this, (DataFile) file);
                    }
            );
        }
    }

    @Override
    protected PImage resizeImage(BufferedImage image) {
        return null;
    }

    protected boolean isChildDrawable(RComponent child) {
        int yDiff = filesGroup.getVerticalScroll();
        return !(child.getRelPosY() + child.getHeight() < yDiff) && !(child.getRelPosY() > yDiff + getHeight());
    }

    protected void drawChildren(PGraphics pg) {

        int index = 0;

        for (RComponent component : children) {
            if (!component.isVisibleParentAware(null)) {
                index++;
                continue;
            }
            pg.pushMatrix();
            pg.translate(component.getRelPosX(), component.getRelPosY());
            drawChildComponent(pg, component);
            if (index > 0) { // TODO if as to kind of separator to draw
                // separator
                if (layout instanceof RLinearLayout linear) {
                    pg.pushStyle();
                    if (linear.getDirection() == RDirection.VERTICAL) {
                        drawHorizontalSeparator(pg);
                    } else {
                        drawVerticalSeparator(pg);
                    }
                    pg.popStyle();
                }
            }
            index++;
            pg.popMatrix();
        }
    }

    @Override
    public PVector getBufferSize(){
        return new PVector(getWidth(), getHeight());
    }

    @Override
    public PVector getPreferredSize() {
        return new PVector(getWidth(), parent.getHeight());
    }

    /**
     * Method to check if this window of this component is visible, and if all parent nodes are visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isVisibleParentAware(RComponent child) {
        boolean visible = isVisible() && (child == null || isChildDrawable(child));
        if (parent != null) {
            if (parent instanceof RFolder) {
                return visible && parent.isVisible();
            } else {
                return visible && parent.isVisibleParentAware(this);
            }
        }
        return visible;
    }

    @Override
    public void setLayout(RLayoutBase layout) {

    }



    @Override
    public void draw(PGraphics pg) {
        // the component knows its absolute position but here the current matrix is already translated to it
        pg.image(toDraw, 0, 0);
    }



    @Override
    public void drawToBuffer() {
      children.stream()
                .filter(this::isChildDrawable)
                .forEach(RComponent::drawToBuffer);
        buffer.redraw();
        toDraw = buffer.draw().get(0, filesGroup.getVerticalScroll(), (int) size.x, (int) parent.getHeight());
    }

    /**
     * Find A Component At The Given Coordinates
     *
     * @param x X-Coordinate
     * @param y Y-Coordinate
     * @return The Component at these Coordinates, if one exists, null otherwise
     */
    public RComponent findVisibleComponentAt(float x, float y) {
        for (RComponent component : children) {
            if (!component.isVisibleParentAware(null)) {
                continue;
            }
            if (isPointInRect(x, y, component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight())) {
                return component;
            }
        }
        return null;
    }


    @Override
    public void recolorImage() throws NitroException {
    }

    @Override
    public void updateChildrenCoordinates() {
        layout.setCompLayout(pos, size, children);
    }
}