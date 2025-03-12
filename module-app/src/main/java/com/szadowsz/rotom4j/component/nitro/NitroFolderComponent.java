package com.szadowsz.rotom4j.component.nitro;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.component.input.slider.RSlider;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.rotom4j.app.utils.ImageUtils;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.Drawable;
import processing.core.PGraphics;
import processing.core.PImage;

import java.awt.image.BufferedImage;


public abstract class NitroFolderComponent<I extends Drawable> extends RFolder {

    protected static final String PREVIEW_NODE = "Preview";
    protected static final String ZOOM_NODE = "Zoom";
    protected static final String RESET_NODE = "Reset";

    protected static final String SELECT_NCLR_FILE = "Select NClR";

    protected I drawable;
    protected String selectName;

    public NitroFolderComponent(RotomGui gui, String path, RGroup parent, I drawable, String selectName) {
        super(gui, path, parent);
        this.drawable = drawable;
        this.selectName = selectName;
    }

    /**
     * Utility Method to create pre-packaged Zoom Node
     *
     * @return Slider representing the current zoom
     */
    protected RSlider createZoom() {
        return new RSlider(
                gui,
                NitroFolderComponent.this.path + "/" + ZOOM_NODE,
                NitroFolderComponent.this,
                1.0f,
                1.0f,
                4.0f,
                true,
                0.01f
        ) {
            @Override
            protected void onValueChange() {
                try {
                    recolorImage();
                } catch (NitroException e) {
                    throw new RuntimeException(e);
                }
            }

        };
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        drawTextLeft(pg, (shouldDisplayName()) ? name : selectName);
        drawBackdropRight(pg, RLayoutStore.getCell());
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
        RSlider zoomNode = (RSlider) findChildByName(ZOOM_NODE);
        if (zoomNode != null) {
            float zoom = zoomNode.getValueAsFloat();
            pImage.resize(Math.round(pImage.width * zoom), 0);
        }
        return pImage;
    }

    /**
     * Check to see if node should display regular name, or selection name
     *
     * @return true if regular, false otherwise
     */
    protected boolean shouldDisplayName() {
        return drawable != null;
    }

    /**
     * Method to reinitialise and resupply an image to the PreviewNode
     * <p>
     * This should be called when something happens that should cause the preview image to change
     *
     * @throws NitroException if the change fails to take
     */
    public abstract void recolorImage() throws NitroException;

    /**
     * Change the Nitro Obj currently displayed
     *
     * @param drawable the obj to now use
     */
    public void setDrawable(I drawable) {
        this.drawable = drawable;
    }

    /**
     * Change the Nitro Obj currently displayed
     *
     * @return the current obj
     */
    public I getDrawable() {
        return drawable;
    }
}
