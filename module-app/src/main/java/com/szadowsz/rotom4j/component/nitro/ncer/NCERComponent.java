package com.szadowsz.rotom4j.component.nitro.ncer;


import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.input.slider.RSlider;
import com.szadowsz.gui.component.input.slider.RSliderInt;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.nitro.NitroPreview;
import com.szadowsz.rotom4j.component.nitro.ncgr.NCGRFolder;
import com.szadowsz.rotom4j.component.nitro.nclr.NCLRFolder;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.ncer.NCER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;

public class NCERComponent extends R4JComponent<NCER> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NCERComponent.class);

    private final String CELL_SLIDER = "Cell";
    private final String IMAGE_COMP = "image";

    private final NCERFolder parentFolder;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected NCERComponent(RotomGui gui, String path, RGroup parent, NCER data) {
        super(gui, path, parent);

        parentFolder = (NCERFolder) getParentFolder();
        parentFolder.setDisplay(this);

        this.data = data;

        initComponents();
    }

    /**
     * Utility Method to create pre-packaged Zoom Node
     *
     * @return Slider representing the current zoom
     */
    protected RSlider createCell() {
        return new RSliderInt(
                gui,
                NCERComponent.this.path + "/" + CELL_SLIDER,
                NCERComponent.this,
                0,
                0,
                data.getCellsCount() - 1,
                true
        ) {
            @Override
            protected void onValueChange() {
                super.onValueChange();
                try {
                    recolorImage();
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
                NCERComponent.this.path + "/" + ZOOM_COMP,
                NCERComponent.this,
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
        children.add(createZoom());
        children.add(createCell());
        children.add(new NCGRFolder(gui, path + "/" + IMAGE_COMP, this, data.getNCGR()));
    }

    @Override
    public void setLayout(RLayoutBase layout) {
        // NOOP
    }

    @Override
    public void recolorImage() throws NitroException {
        RSliderInt cell = (RSliderInt) findChildByName(CELL_SLIDER);

        PImage pImage = resizeImage(data.getImage(cell.getValueAsInt()));
        ((NitroPreview) findChildByName(PREVIEW_COMP)).loadImage(pImage);

        resetBuffer();
    }

    public void resizeImage() throws NitroException {
        NCGRFolder ncgrFolder = ((NCGRFolder) findChildByName(IMAGE_COMP));
        RSliderInt cell = (RSliderInt) findChildByName(CELL_SLIDER);

        data.setNCGR(ncgrFolder.getObj());

        PImage pImage = resizeImage(data.getImage(cell.getValueAsInt()));
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
            findChildByName(CELL_SLIDER).resetBuffer();
            findChildByName(PREVIEW_COMP).resetBuffer();
        }
    }
}
