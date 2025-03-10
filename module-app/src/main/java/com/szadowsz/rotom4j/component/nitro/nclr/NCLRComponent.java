package com.szadowsz.rotom4j.component.nitro.nclr;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.drawable.RColorPicker;
import com.szadowsz.gui.component.input.slider.RSlider;
import com.szadowsz.gui.component.input.slider.RSliderInt;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.rotom4j.app.utils.ImageUtils;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.nitro.NitroPreview;
import com.szadowsz.rotom4j.file.nitro.nclr.NCLR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;

import java.awt.*;
import java.io.IOException;

public class NCLRComponent extends R4JComponent<NCLR> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NCLRComponent.class);

    protected static final String PALETTE_NODE_NAME = "pal";
    protected static final String PICKER_NODE_NAME = "picker";
    protected static final String COLOR_NODE_NAME = "col";
    protected static final String R_NODE_NAME = "R";
    protected static final String G_NODE_NAME = "G";
    protected static final String B_NODE_NAME = "B";

    private NCLRFolder parentFolder;
    private int index;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected NCLRComponent(RotomGui gui, String path, RGroup parent, NCLR data) {
        super(gui, path, parent);
        parentFolder = (NCLRFolder) getParentFolder();
        parentFolder.setDisplay(this);
        this.data = data;
        initComponents();
    }

    protected void initComponents() {
        if (!children.isEmpty()) {
            return;
        }
        children.add(new NitroPreview(gui,path + "/" + PREVIEW_COMP, this,data));
        RSlider pal = new RSliderInt(gui,path + "/" + PALETTE_NODE_NAME, this, 0, 0, data.getPaletteCount()-1, true) {
            @Override
            protected void onValueChange() {
                super.onValueChange();
                recolorImage();
            }
        };

        children.add(pal);
        children.add(new RSliderInt(gui,path + "/" + COLOR_NODE_NAME, this,0,0,getNumColors(),true));
        children.add(new RColorPicker(gui,path + "/" + PICKER_NODE_NAME,this,getColor(),false));
        if (parentFolder.getSpriteFolder() != null) {
            RButton reset = new RButton(gui,path + "/" + CLEAR_COMP, this);
            reset.registerAction(RActivateByType.RELEASE, this::resetPalette);
            children.add(reset);
        }
    }

    protected void resetPalette() {
        NCLR original = data;
        try {
            LOGGER.debug("Resetting NCLR File: " + original.getFileName());
            data = NCLR.DEFAULT;
            LOGGER.info("Reset NCLR File to Default: " + original.getFileName());
            recolorImage();
            if (parentFolder.getSpriteFolder() != null) {
                parentFolder.getSpriteFolder().recolorImage();
            }
            this.getParentWindow().close();
        } catch (IOException e) {
            LOGGER.error("NCLR Load Failed", e);
            try {
                data = original;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }


    /**
     * Get number of Colors Per Palette
     *
     * @return number of colors
     */
    public int getNumColors() {
        return data.getNumColorsPerPalette(); // TODO is there a bug with usage?
    }

    /**
     * Get number of Current Palette
     *
     * @return palette value
     */
    public int getPaletteNum() {
        return (int) ((RSlider) findChildByName(PALETTE_NODE_NAME)).getValueAsFloat();
    }

    /**
     * Get Current Color
     *
     * @return the color object
     */
    public Color getColor() {
        return data.getColorInPalette(getPaletteNum(),index);
    }

    /**
     * Get R value of Current Color
     *
     * @return red value
     */
    public float getRed() {
        return data.getColorInPalette(getPaletteNum(),index).getRed();
    }

    /**
     * Get G value of Current Color
     *
     * @return red value
     */
    public float getGreen() {
        return data.getColorInPalette(getPaletteNum(),index).getGreen();
    }

    /**
     * Get B value of Current Color
     *
     * @return red value
     */
    public float getBlue() {
        return data.getColorInPalette(getPaletteNum(),index).getBlue();
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void setLayout(RLayoutBase layout) {

    }

    @Override
    public void recolorImage() {
        int palette = getPaletteNum();
        index = 0;
        PImage pImage = ImageUtils.convertToPImage(data.getImage(palette));
        ((NitroPreview) findChildByName(PREVIEW_COMP)).loadImage(pImage);
    }

    @Override
    public float suggestWidth() {
        return getPreferredSize().x;
    }
}
