package com.szadowsz.rotom4j.app.component.nitro.nclr;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.drawable.RColorPicker;
import com.szadowsz.gui.component.input.slider.RSlider;
import com.szadowsz.gui.component.input.slider.RSliderInt;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.theme.RColorConverter;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.rotom4j.app.ProcessingRotom4J;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.nclr.NCLR;
import com.szadowsz.rotom4j.app.component.nitro.NitroFolderComponent;
import com.szadowsz.rotom4j.app.component.nitro.NitroPreview;
import com.szadowsz.rotom4j.app.component.nitro.ncgr.NCGRFolderComponent;
import com.szadowsz.rotom4j.app.utils.FileChooser;
import com.szadowsz.rotom4j.app.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PImage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static processing.core.PApplet.CENTER;

public class NCLRFolderComponent extends NitroFolderComponent<NCLR> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NCLRFolderComponent.class);

    protected static final String PALETTE_NODE_NAME = "pal";
    protected static final String PICKER_NODE_NAME = "picker";
    protected static final String COLOR_NODE_NAME = "col";
    protected static final String R_NODE_NAME = "R";
    protected static final String G_NODE_NAME = "G";
    protected static final String B_NODE_NAME = "B";

    private NCGRFolderComponent spriteFolder = null;
    private int index;

    public NCLRFolderComponent(RotomGui gui, String path, RGroup parentFolder, NCLR nclr) {
        super(gui, path, parentFolder,nclr,SELECT_NCLR_FILE);
        if (parentFolder instanceof NCGRFolderComponent){
            LOGGER.debug("Attached to parent NCGR");
            spriteFolder = (NCGRFolderComponent) parentFolder;
        }
        initNodes();
    }

    @Override
    protected void drawForeground(PGraphics pg, String nRame) {
        drawTextLeft(pg, (shouldDisplayName())? imageable.getFileName() : selectName);
        drawPreviewRect(pg);
    }

    protected void drawPreviewRect(PGraphics pg) {
        strokeForeground(pg);
        float previewRectSize = RLayoutStore.getCell() * 0.6f;
        pg.translate(size.x - RLayoutStore.getCell() * 0.5f, size.y * 0.5f);
        pg.rectMode(CENTER);
        Color color = imageable.getColorInPalette(getPaletteNum(),index);
        pg.fill(color.getRed(),color.getGreen(),color.getBlue());
        pg.rect(0, 0, previewRectSize, previewRectSize);
    }

    protected float findChildValue(String nodeName) {
        RColorPicker picker = (RColorPicker) findChildByName(PICKER_NODE_NAME);
        return switch (nodeName) {
            case R_NODE_NAME -> picker.getRed();
            case G_NODE_NAME -> picker.getGreen();
            case B_NODE_NAME -> picker.getBlue();
            default -> 0;
        };
    }

    protected void setChildValue(String nodeName, int value) {
        RColorPicker picker = (RColorPicker) findChildByName(PICKER_NODE_NAME);
        switch (nodeName) {
            case R_NODE_NAME -> picker.setRed(value);
            case G_NODE_NAME -> picker.setGreen(value);
            case B_NODE_NAME -> picker.setBlue(value);
            default -> ((RSlider) findChildByName(COLOR_NODE_NAME)).setValueFromParent(value);
        };
    }

    protected void initNodes() {
        if (!children.isEmpty()) {
            return;
        }
        children.add(new NitroPreview(gui,path + "/" + PREVIEW_NODE, this,imageable));
        RSlider pal = new RSliderInt(gui,path + "/" + PALETTE_NODE_NAME, this, 0, 0, imageable.getPaletteCount()-1, true) {
            @Override
            protected void onValueChange() {
                super.onValueChange();
                recolorImage();
            }
        };

        children.add(pal);
        children.add(new RSliderInt(gui,path + "/" + COLOR_NODE_NAME, this,0,0,getNumColors(),true));
        children.add(new RColorPicker(gui,path + "/" + PICKER_NODE_NAME,this,getColor(),false));
        if (spriteFolder != null) {
            RButton reset = new RButton(gui,path + "/" + RESET_NODE, this);
            reset.registerAction(RActivateByType.RELEASE, this::resetPalette);
            children.add(reset);
        }
    }

    @Override
    protected boolean shouldDisplayName(){
        return imageable != null && imageable != NCLR.DEFAULT;
    }

    protected void selectPalette() {
        String lastPath = ProcessingRotom4J.prefs.get("openNarcPath", System.getProperty("user.dir"));
        String nclrPath = FileChooser.selectNclrFile(gui.getSketch(), lastPath,SELECT_NCLR_FILE);
        if (nclrPath != null) {
            ProcessingRotom4J.prefs.put("openNarcPath", new File(nclrPath).getParentFile().getAbsolutePath());
            NCLR original = imageable;
            try {
                LOGGER.debug("Loading NCLR File: " + nclrPath);
                imageable = NCLR.fromFile(nclrPath);
                LOGGER.info("Loaded NCLR File: " + nclrPath);
                recolorImage();
                if (spriteFolder!= null){
                    spriteFolder.recolorImage();
                }
            } catch (IOException e) {
                LOGGER.error("NCLR Load Failed",e);
                try {
                    imageable = original;
                } catch (Throwable t){
                    throw new RuntimeException(t);
                }
            }
        }
    }

    protected void resetPalette() {
        NCLR original = imageable;
        try {
            LOGGER.debug("Resetting NCLR File: " + original.getFileName());
            imageable = NCLR.DEFAULT;
            LOGGER.info("Reset NCLR File to Default: " + original.getFileName());
            recolorImage();
            if (spriteFolder != null) {
                spriteFolder.recolorImage();
            }
            this.window.close();
        } catch (IOException e) {
            LOGGER.error("NCLR Load Failed", e);
            try {
                imageable = original;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Reload the individual RGB values into the display nodes from the current Color
     */
    void loadValuesFromRGB() {
        PGraphics colorProvider = RColorConverter.getColorStoreRGB();
        int nIndex = (int) findChildValue(COLOR_NODE_NAME);
        if (index != nIndex){
            index = nIndex;
            if (index < 0){
                index = 0;
            } else if (index >= imageable.getNumColorsPerPalette()){
                index = imageable.getNumColorsPerPalette()-1;
            }
            Color color = imageable.getColorInPalette(getPaletteNum(),index);
            setChildValue(COLOR_NODE_NAME,index);
            setChildValue(R_NODE_NAME,color.getRed());
            setChildValue(G_NODE_NAME,color.getGreen());
            setChildValue(B_NODE_NAME,color.getBlue());
        } else{
            imageable.setColor(index, colorProvider.color(
                    findChildValue(R_NODE_NAME),
                    findChildValue(G_NODE_NAME),
                    findChildValue(B_NODE_NAME)));
            if (spriteFolder != null){
                try {
                    spriteFolder.recolorImage();
                } catch (NitroException e) {
                    LOGGER.warn("Issue updating Palette",e);
                }
            }
        }
    }

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (shouldDisplayName()){
            super.mousePressed(mouseEvent, adjustedMouseY);
        } else {
            selectPalette();
        }
    }

    @Override
    public void recolorImage() {
        int palette = getPaletteNum();
        index = 0;
        PImage pImage = ImageUtils.convertToPImage(imageable.getImage(palette));
        ((NitroPreview) findChildByName(PREVIEW_NODE)).loadImage(pImage);
    }

    /**
     * Get Current Color
     *
     * @return the color object
     */
    public Color getColor() {
        return imageable.getColorInPalette(getPaletteNum(),index);
    }

    /**
     * Get R value of Current Color
     *
     * @return red value
     */
    public float getRed() {
        return imageable.getColorInPalette(getPaletteNum(),index).getRed();
    }

    /**
     * Get G value of Current Color
     *
     * @return red value
     */
    public float getGreen() {
        return imageable.getColorInPalette(getPaletteNum(),index).getGreen();
    }

    /**
     * Get B value of Current Color
     *
     * @return red value
     */
    public float getBlue() {
        return imageable.getColorInPalette(getPaletteNum(),index).getBlue();
    }

    /**
     * Get number of Current Palette
     *
     * @return palette value
     */
    private int getPaletteNum() {
        return (int) ((RSlider) findChildByName(PALETTE_NODE_NAME)).getValueAsFloat();
    }

    /**
     * Get number of Colors Per Palette
     *
     * @return number of colors
     */
    public int getNumColors() {
        return imageable.getNumColorsPerPalette(); // TODO is there a bug with usage?
    }
}
