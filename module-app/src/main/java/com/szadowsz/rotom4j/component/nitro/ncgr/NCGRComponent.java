package com.szadowsz.rotom4j.component.nitro.ncgr;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.input.slider.RSlider;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.rotom4j.app.utils.ImageUtils;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.nitro.NitroCmpFolderComponent;
import com.szadowsz.rotom4j.component.nitro.NitroPreview;
import com.szadowsz.rotom4j.component.nitro.nclr.NCLRFolder;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class NCGRComponent extends R4JComponent<NCGR> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NCGRComponent.class);

    private static final String PALETTE_COMP = "palette";

    private final NCGRFolder parentFolder;

    private NitroCmpFolderComponent<?> cmpFolder;

    /**
     * Default Constructor
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected NCGRComponent(RotomGui gui, String path, RGroup parent, NCGR data) {
        super(gui, path, parent);
        parentFolder = (NCGRFolder) getParentFolder();
        parentFolder.setDisplay(this);

        cmpFolder = parentFolder.getCmpFolder();

        this.data = data;
        initComponents();
    }

    protected void initComponents() {
        children.add(new NitroPreview(gui, path + "/" + PREVIEW_COMP, this, data));
        children.add(createZoom());
        children.add(new NCLRFolder(gui, path + "/" + PALETTE_COMP, this, data.getNCLR()));
        if (cmpFolder != null) {
            RButton reset = new RButton(gui, path + "/" + RESET_COMP, this);
            reset.registerAction(RActivateByType.RELEASE, this::resetImage);
            children.add(reset);
        }
    }

    /**
     * Utility Method to create pre-packaged Zoom Node
     *
     * @return Slider representing the current zoom
     */
    protected RSlider createZoom() {
        return new RSlider(
                gui,
                NCGRComponent.this.path + "/" + ZOOM_COMP,
                NCGRComponent.this,
                1.0f,
                1.0f,
                4.0f,
                true,
                0.01f
        ) {
            @Override
            protected void onValueChange() {
                super.onValueChange();
                try {
                    recolorImage();
                    getParentWindow().resizeForComponent();
                } catch (NitroException e) {
                    throw new RuntimeException(e);
                }
            }

        };
    }

    @Override
    public void setLayout(RLayoutBase layout) {

    }

    protected void resetImage() {
        NCGR original = data;
        try {
            LOGGER.debug("Resetting NCGR File: " + original.getFileName());
            data = null;
            LOGGER.info("Reset NCGR File to Default: " + original.getFileName());
            //recolorImage();
            if (cmpFolder != null) {
                cmpFolder.recolorImage();
            }
            getParentWindow().close();
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

    @Override
    public void recolorImage() throws NitroException {
        data.setNCLR(((NCLRFolder) findChildByName(PALETTE_COMP)).getObj());
        data.recolorImage();

        PImage pImage = resizeImage(data.getImage());

        ((NitroPreview) findChildByName(PREVIEW_COMP)).loadImage(pImage);

        resetBuffer();
        if (getParentWindow() != null) {
            getParentWindow().reinitialiseBuffer();
        }

        if (cmpFolder != null) {
            cmpFolder.recolorImage();
        }
    }

    @Override
    public void updateCoordinates(float bX, float bY, float rX, float rY, float w, float h) {
        boolean init = size.x == 0.0f;


        pos.x = bX + rX;
        pos.y = bY + rY;
        relPos.x = rX;
        relPos.y = rY;

        if (size.x != w) {
            resetBuffer(); // RESET-VALID: we should resize the buffer if the size changes
            size.x = w;
        }


        if (size.y != h) {
            resetBuffer(); // RESET-VALID: we should resize the buffer if the size changes
            size.y = h;
        }

        if (init) {
            LOGGER.info("Initialising zoom level for {}", getName());
            BufferedImage image = data.getImage();
            float zoom = size.x / image.getWidth();
            LOGGER.info("Setting zoom for {} to {}", getParent().getName(), zoom);
            RSlider component = (RSlider) findChildByName(ZOOM_COMP);
            component.setValueFromParent(zoom);
        }
        try {
            recolorImage();
        } catch (NitroException e) {
            throw new RuntimeException(e);
        }
        layout.setCompLayout(pos, size, children);
    }

    @Override
    public float suggestWidth() {
        return ((NitroPreview) children.getFirst()).getImage().width;
    }

    @Override
    public void resetBuffer() {
        if (!buffer.isReInitRequired()) {
            super.resetBuffer();
            findChildByName(ZOOM_COMP).resetBuffer();
            findChildByName(PREVIEW_COMP).resetBuffer();
            findChildByName(PALETTE_COMP).resetBuffer();
        }
    }
}
