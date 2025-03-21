package com.szadowsz.rotom4j.component.nitro.nanr;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.drawable.media.RMediaControl;
import com.szadowsz.gui.component.input.slider.RSlider;
import com.szadowsz.gui.component.input.slider.RSliderInt;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.nitro.NitroPreview;
import com.szadowsz.rotom4j.component.nitro.ncer.NCERFolder;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.nanr.NANR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;

import java.awt.image.BufferedImage;


public class NANRComponent extends R4JComponent<NANR> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NANRComponent.class);

    private final NANRFolder parentFolder;

    private final String MEDIA_COMP = "media";
    private final String IMAGE_COMP = "image";

    private final String ANI_SLIDER = "animations";

    private int currentFrame;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected NANRComponent(RotomGui gui, String path, RGroup parent, NANR data) {
        super(gui, path, parent);
        parentFolder = (NANRFolder) getParentFolder();
        parentFolder.setDisplay(this);

        this.data = data;
        initComponents();
    }

    protected int getCurrentAnimation() {
        RSliderInt ani = (RSliderInt) findChildByName(ANI_SLIDER);
        return ani.getValueAsInt();
    }

    /**
     * Utility Method to create pre-packaged Zoom Node
     *
     * @return Slider representing the current zoom
     */
    protected RSlider createAnimationsSlider() {
        return new RSliderInt(
                gui,
                NANRComponent.this.path + "/" + ANI_SLIDER,
                NANRComponent.this,
                0,
                0,
                data.getAnimationsCount() - 1,
                true
        ) {
            @Override
            protected void onValueChange() {
                super.onValueChange();
                try {
                    changeAnimation();
                } catch (NitroException e) {

                }
            }

        };
    }

    /**
     * Utility Method to create pre-packaged Zoom Node
     *
     * @return Slider representing the current zoom
     */
    protected RSlider createZoom() {
        return new RSlider(
                gui,
                NANRComponent.this.path + "/" + ZOOM_COMP,
                NANRComponent.this,
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
                    resizeImage();
                    getParentWindow().resizeForComponent();
                } catch (NitroException e) {
                    throw new RuntimeException(e);
                }
            }

        };
    }

    protected void initComponents() {
        children.add(new NitroPreview(gui, path + "/" + PREVIEW_COMP, this, data));
        children.add(new RMediaControl(gui,path + "/" + MEDIA_COMP,this));
        children.add(createAnimationsSlider());
        children.add(createZoom());
        children.add(new NCERFolder(gui, path + "/" + IMAGE_COMP, this, data.getNCER()));
    }

    protected void changeAnimation() throws NitroException {
        currentFrame = 0;
        PImage pImage = resizeImage(data.getImage(getCurrentAnimation(),0));
        ((NitroPreview) findChildByName(PREVIEW_COMP)).loadImage(pImage);

        resetBuffer();
    }


    @Override
    public void recolorImage() throws NitroException {
        PImage pImage = resizeImage(data.getImage(getCurrentAnimation(),currentFrame));
        ((NitroPreview) findChildByName(PREVIEW_COMP)).loadImage(pImage);

        resetBuffer();
    }

    public void resizeImage() throws NitroException {
        NCERFolder ncerFolder = ((NCERFolder) findChildByName(IMAGE_COMP));

        data.setNCER(ncerFolder.getObj());

        PImage pImage = resizeImage(data.getImage());
        ((NitroPreview) findChildByName(PREVIEW_COMP)).loadImage(pImage);

        resetBuffer();
        if (getParentWindow() != null) {
            getParentWindow().reinitialiseBuffer();
        }
    }

    @Override
    public void resetBuffer() {
        if (!buffer.isReInitRequired()) {
            super.resetBuffer();
            findChildByName(IMAGE_COMP).resetBuffer();
            findChildByName(ZOOM_COMP).resetBuffer();
            findChildByName(PREVIEW_COMP).resetBuffer();
        }
    }

    @Override
    public void setLayout(RLayoutBase layout) {
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
            resizeImage();
        } catch (NitroException e) {
            throw new RuntimeException(e);
        }
        layout.setCompLayout(pos, size, children);
    }
}
