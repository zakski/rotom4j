package com.szadowsz.rotom4j.component.nitro.nclr;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.drawable.tab.RTab;
import com.szadowsz.gui.component.group.drawable.tab.RTabFunction;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.rotom4j.app.ProcessingRotom4J;
import com.szadowsz.rotom4j.app.utils.FileChooser;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.R4JFolder;
import com.szadowsz.rotom4j.component.nitro.ncgr.NCGRFolderComponent;
import com.szadowsz.rotom4j.file.nitro.nclr.NCLR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static processing.core.PConstants.CENTER;

public class NCLRFolder extends R4JFolder<NCLR> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NCLRFolder.class);

    private NCGRFolderComponent spriteFolder = null;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui        the gui for the window that the component is drawn under
     * @param path       the path in the component tree
     * @param parent     the parent component reference
     * @param nclr
     */
    public NCLRFolder(RotomGui gui, String path, RGroup parent, NCLR nclr) {
        super(gui, path, parent,nclr,SELECT_NCLR_FILE);
        if (getParentFolder() instanceof NCGRFolderComponent){
            LOGGER.debug("Attached to parent NCGR");
            spriteFolder = (NCGRFolderComponent) getParentFolder();
        }
        children.add(new NCLRComponent(gui, path + "/" + nclr,this, data));
    }

    @Override
    protected void drawForeground(PGraphics pg, String nRame) {
        drawTextLeft(pg, (shouldDisplayName())? data.getFileName() : selectName);
        drawPreviewRect(pg);
    }

    protected void drawPreviewRect(PGraphics pg) {
        strokeForeground(pg);
        float previewRectSize = RLayoutStore.getCell() * 0.6f;
        pg.translate(size.x - RLayoutStore.getCell() * 0.5f, size.y * 0.5f);
        pg.rectMode(CENTER);
        NCLRComponent component = (NCLRComponent) display;
        Color color = data.getColorInPalette(component.getPaletteNum(),component.getIndex());
        pg.fill(color.getRed(),color.getGreen(),color.getBlue());
        pg.rect(0, 0, previewRectSize, previewRectSize);
    }

    protected void selectPalette() {
        String lastPath = ProcessingRotom4J.prefs.get("openNarcPath", System.getProperty("user.dir"));
        String nclrPath = FileChooser.selectNclrFile(gui.getSketch(), lastPath,SELECT_NCLR_FILE);
        if (nclrPath != null) {
            ProcessingRotom4J.prefs.put("openNarcPath", new File(nclrPath).getParentFile().getAbsolutePath());
            NCLR original = data;
            try {
                LOGGER.debug("Loading NCLR File: " + nclrPath);
                data = NCLR.fromFile(nclrPath);
                LOGGER.info("Loaded NCLR File: " + nclrPath);
                display.recolorImage();
                if (spriteFolder!= null){
                    spriteFolder.recolorImage();
                }
            } catch (IOException e) {
                LOGGER.error("NCLR Load Failed",e);
                try {
                    data = original;
                } catch (Throwable t){
                    throw new RuntimeException(t);
                }
            }
        }
    }

    @Override
    protected final RTabFunction<R4JComponent<NCLR>> createDisplay() {
        return (RTab tab) -> new NCLRComponent(gui, tab.getPath(),tab, data);
    }

    void setDisplay(NCLRComponent nclrComponent) {
        this.display = nclrComponent;
    }

    public NCGRFolderComponent getSpriteFolder() {
        return spriteFolder;
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (shouldDisplayName()) {
            super.mousePressed(mouseEvent, adjustedMouseY);
        } else {
            selectPalette();
        }
    }
}
