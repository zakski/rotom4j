package com.szadowsz.rotom4j.component;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.component.input.slider.RSlider;
import com.szadowsz.rotom4j.app.utils.ImageUtils;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.RotomFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;

import java.awt.image.BufferedImage;

public abstract class R4JComponent<R extends RotomFile> extends RGroupDrawable {
    private static final Logger LOGGER = LoggerFactory.getLogger(R4JComponent.class);

    protected static final String PREVIEW_COMP = "Preview";
    protected static final String ZOOM_COMP = "Zoom";
    protected static final String CLEAR_COMP = "Reset";

    protected R data;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected R4JComponent(RotomGui gui, String path, RGroup parent) {
        super(gui, path, parent);
    }

    /**
     * Method to convert a BufferedImage to a PImage and resize it according to an optional zoom node
     *
     * @param image to convert
     * @return appropriately scaled PImage
     */
    protected PImage resizeImage(BufferedImage image) {
        if (image == null) {
            return null;
        }
        PImage pImage = ImageUtils.convertToPImage(image);
        RSlider zoomNode = (RSlider) findChildByName(ZOOM_COMP);
        if (zoomNode != null) {
            float zoom = zoomNode.getValueAsFloat();
            int resizedWidth = Math.round(pImage.width * zoom);
            LOGGER.info("Resized Image width {} with zoom {} to {}", pImage.width, zoom, resizedWidth);
            pImage.resize(resizedWidth, 0);
        }
        return pImage;
    }

    /**
     * Method to reinitialise and resupply an image to the PreviewNode
     * <p>
     * This should be called when something happens that should cause the preview image to change
     *
     * @throws NitroException if the change fails to take
     */
    public abstract void recolorImage() throws NitroException;

    @Override
    public float suggestWidth() {
        return layout.calcPreferredSize(getName(), children).x;
    }
}
