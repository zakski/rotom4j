package com.szadowsz.rotom4j.component.nitro.n2d.narc.files;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLinearLayout;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.bin.BinFolder;
import com.szadowsz.rotom4j.component.nitro.n2d.nanr.NANRFolder;
import com.szadowsz.rotom4j.component.nitro.n2d.narc.NARCFolder;
import com.szadowsz.rotom4j.component.nitro.n2d.ncer.NCERFolder;
import com.szadowsz.rotom4j.component.nitro.n2d.ncgr.NCGRFolder;
import com.szadowsz.rotom4j.component.nitro.n2d.nclr.NCLRFolder;
import com.szadowsz.rotom4j.component.nitro.n2d.nscr.NSCRFolder;
import com.szadowsz.rotom4j.component.nitro.n3d.nsbca.NSBCAFolder;
import com.szadowsz.rotom4j.component.nitro.n3d.nsbmd.NSBMDFolder;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.RotomFile;
import com.szadowsz.rotom4j.file.data.DataFile;
import com.szadowsz.rotom4j.file.nitro.n2d.nanr.NANR;
import com.szadowsz.rotom4j.file.nitro.n2d.narc.NARC;
import com.szadowsz.rotom4j.file.nitro.n2d.ncer.NCER;
import com.szadowsz.rotom4j.file.nitro.n2d.ncgr.NCGR;
import com.szadowsz.rotom4j.file.nitro.n2d.nclr.NCLR;
import com.szadowsz.rotom4j.file.nitro.n2d.nscr.NSCR;
import com.szadowsz.rotom4j.file.nitro.n3d.nsbca.NSBCA;
import com.szadowsz.rotom4j.file.nitro.n3d.nsbmd.NSBMD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

import java.awt.image.BufferedImage;
import java.util.List;

import static com.szadowsz.gui.utils.RCoordinates.isPointInRect;

public class NarcFilesPage extends R4JComponent<NARC> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NarcFilesPage.class);

    protected final NARCFilesPages filesGroup;

    protected final NARC narc;

    private PImage toDraw;

    public NarcFilesPage(RotomGui gui, String path, NARCFilesPages filesGroup, NARC data, int pageNumber) {
        super(gui, path, filesGroup);
        ((RLinearLayout) layout).setReduce(false);
        this.filesGroup = filesGroup;
        narc = data;
        try {
            initFiles(pageNumber);
        } catch (NitroException ignored) {

        }
    }

    protected void initFiles(int pageNumber) throws NitroException {
        if (!children.isEmpty()) {
            return;
        }
        List<RotomFile> files = narc.getFiles();
        int filesOnCurrentPage = filesGroup.getFilesOnPage(pageNumber);
        int maxFilesOnAPage = filesGroup.getMaxFilesPerPage();

        int start = maxFilesOnAPage *pageNumber;
        int end = start + filesOnCurrentPage;

        for (int i = start; i < end; i++) {
            RotomFile file = files.get(i);
            children.add(
                    switch (file) {
                        // 3D
                        case NSBCA nsbca -> new NSBCAFolder(gui, path + "/" + nsbca.getFileName(), this, nsbca);
                        case NSBMD nsbmd -> new NSBMDFolder(gui, path + "/" + nsbmd.getFileName(), this, nsbmd);
                        // 2D
                        case NANR nanr -> new NANRFolder(gui, path + "/" + nanr.getFileName(), this, nanr);
                        case NCER ncer -> new NCERFolder(gui, path + "/" + ncer.getFileName(), this, ncer);
                        case NSCR nscr -> new NSCRFolder(gui, path + "/" + nscr.getFileName(), this, nscr);
                        case NCGR ncgr -> new NCGRFolder(gui, path + "/" + ncgr.getFileName(), this, ncgr);
                        case NCLR nclr -> new NCLRFolder(gui, path + "/" + nclr.getFileName(), this, nclr);
                        case NARC narc -> new NARCFolder(gui, path + "/" + narc.getFileName(), this, narc);
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
            if (!component.isVisible()) {
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
    protected void redrawBuffers() {
        super.redrawBuffers();
    }

    @Override
    public PVector getBufferSize(){
        return new PVector(getWidth(),  filesGroup.getActualHeight());
    }

    @Override
    public PVector getPreferredSize() {
        return new PVector(getWidth(), filesGroup.getHeight());
    }

    /**
     * Method to check if this window of this component is visible, and if all filesGroup nodes are visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isVisibleParentAware(RComponent child) {
        boolean visible = isVisible() && (child == null || isChildDrawable(child));
        if (filesGroup != null) {
            return visible && filesGroup.isVisibleParentAware(this);
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
        children.forEach(RComponent::drawToBuffer);
        buffer.redraw();
        toDraw = buffer.draw().get(0, filesGroup.getVerticalScroll(), (int) size.x, (int) filesGroup.getHeight());
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