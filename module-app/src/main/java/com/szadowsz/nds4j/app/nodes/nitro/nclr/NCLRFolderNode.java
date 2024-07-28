package com.szadowsz.nds4j.app.nodes.nitro.nclr;

import com.google.gson.JsonElement;

import com.szadowsz.nds4j.app.Processing;
import com.szadowsz.nds4j.app.nodes.nitro.NitroFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.PreviewNode;
import com.szadowsz.nds4j.app.nodes.nitro.ncgr.NCGRFolderNode;
import com.szadowsz.nds4j.app.utils.FileChooser;
import com.szadowsz.nds4j.app.utils.ImageUtils;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.nclr.NCLR;
import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.input.mouse.GuiMouseEvent;
import com.szadowsz.ui.node.impl.ButtonNode;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.node.impl.SliderNode;
import com.szadowsz.ui.store.NormColorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PImage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static com.szadowsz.ui.store.LayoutStore.cell;
import static processing.core.PApplet.CENTER;

public class NCLRFolderNode extends NitroFolderNode<NCLR> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NCLRFolderNode.class);

    protected static final String PALETTE_NODE_NAME = "pal";
    protected static final String COLOR_NODE_NAME = "col";
    protected static final String R_NODE_NAME = "R";
    protected static final String G_NODE_NAME = "G";
    protected static final String B_NODE_NAME = "B";

    private NCGRFolderNode spriteFolder = null;
    private int index;

    public NCLRFolderNode(String path, FolderNode parentFolder, NCLR nclr) {
        super(path, parentFolder,nclr,SELECT_NCLR_FILE);
        if (parentFolder instanceof NCGRFolderNode){
            LOGGER.debug("Attached to parent NCGR");
            spriteFolder = (NCGRFolderNode) parentFolder;
        }
        initNodes();
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        drawLeftText(pg, (shouldDisplayName())? imageable.getFileName() : selectName);
        drawPreviewRect(pg);
    }

    protected void drawPreviewRect(PGraphics pg) {
        strokeForegroundBasedOnMouseOver(pg);
        float previewRectSize = cell * 0.6f;
        pg.translate(size.x - cell * 0.5f, size.y * 0.5f);
        pg.rectMode(CENTER);
        Color color = imageable.getColorInPalette(getPaletteNum(),index);
        pg.fill(color.getRed(),color.getGreen(),color.getBlue());
        pg.rect(0, 0, previewRectSize, previewRectSize);
    }

    protected float findChildValue(String nodeName) {
        NCLRSliderNode node = ((NCLRSliderNode) findChildByName(nodeName));
        return node.valueFloat;
    }

    protected void setChildValue(String nodeName, float valueFloat) {
        NCLRSliderNode node = ((NCLRSliderNode) findChildByName(nodeName));
        node.setValueFromParent(valueFloat);
    }

    protected void initNodes() {
        if (!children.isEmpty()) {
            return;
        }
        children.add(new PreviewNode(path + "/" + PREVIEW_NODE, this,imageable));
        SliderNode pal = new SliderNode(path + "/" + PALETTE_NODE_NAME, this, 0, 0, imageable.getPaletteCount()-1, 1.0f,true) {
            @Override
            protected void onValueFloatChanged() {
                super.onValueFloatChanged();
                recolorImage();
            }
        };
        children.add(pal);
        children.add(new NCLRSliderNode.ColorSelectNode(path + "/" + COLOR_NODE_NAME, this));
        children.add(new NCLRSliderNode.RSelectNode(path + "/" + R_NODE_NAME, this));
        children.add(new NCLRSliderNode.GSelectNode(path + "/" + G_NODE_NAME, this));
        children.add(new NCLRSliderNode.BSelectNode(path + "/" + B_NODE_NAME, this));
        if (spriteFolder != null) {
            ButtonNode reset = new ButtonNode(path + "/" + RESET_NODE, this);
            reset.registerAction(ActivateByType.RELEASE, this::resetPalette);
            children.add(reset);
        }
    }

    @Override
    protected boolean shouldDisplayName(){
        return imageable != null && imageable != NCLR.DEFAULT;
    }

    protected void selectPalette() {
        String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
        String nclrPath = FileChooser.selectNclrFile(GlobalReferences.gui.getGuiCanvas().parent, lastPath,SELECT_NCLR_FILE);
        if (nclrPath != null) {
            Processing.prefs.put("openNarcPath", new File(nclrPath).getParentFile().getAbsolutePath());
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
        PGraphics colorProvider = NormColorStore.getColorStoreRGB();
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
    public void mousePressedEvent(GuiMouseEvent e) {
        if (shouldDisplayName()){
            super.mousePressedEvent(e);
        } else {
            selectPalette();
        }
    }

    @Override
    public void recolorImage() {
        int palette = getPaletteNum();
        index = 0;
        PImage pImage = ImageUtils.convertToPImage(imageable.getImage(palette));
        ((PreviewNode) findChildByName(PREVIEW_NODE)).loadImage(pImage);
    }

    @Override
    public void overwriteState(JsonElement loadedNode) {
        // NOOP
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
        return (int) ((SliderNode) findChildByName(PALETTE_NODE_NAME)).valueFloat;
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
