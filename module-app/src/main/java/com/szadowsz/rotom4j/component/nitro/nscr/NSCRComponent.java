package com.szadowsz.rotom4j.component.nitro.nscr;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.component.input.slider.RSlider;
import com.szadowsz.gui.component.input.slider.RSliderInt;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.R4JResourceFolder;
import com.szadowsz.rotom4j.component.nitro.NitroCmpFolderComponent;
import com.szadowsz.rotom4j.component.nitro.NitroPreview;
import com.szadowsz.rotom4j.component.nitro.ncer.NCERComponent;
import com.szadowsz.rotom4j.component.nitro.ncgr.NCGRFolder;
import com.szadowsz.rotom4j.component.nitro.nclr.NCLRFolder;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;
import com.szadowsz.rotom4j.file.nitro.nscr.NSCR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;


public class NSCRComponent extends R4JComponent<NSCR> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NSCRComponent.class);

    private final NSCRFolder parentFolder;

    private final String IMAGE_COMP = "image";

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected NSCRComponent(RotomGui gui, String path, RGroup parent, NSCR data) {
        super(gui, path, parent);
        parentFolder = (NSCRFolder) getParentFolder();
        parentFolder.setDisplay(this);

        this.data = data;
        initComponents();
    }


    /**
     * Utility Method to create pre-packaged Zoom Node
     *
     * @return Slider representing the current zoom
     */
    protected RSlider createZoom() {
        return new RSlider(
                gui,
                NSCRComponent.this.path + "/" + ZOOM_COMP,
                NSCRComponent.this,
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
        children.add(new NCGRFolder(gui, path + "/" + IMAGE_COMP, this, data.getNCGR()));
    }

    @Override
    public void recolorImage() throws NitroException {

        PImage pImage = resizeImage(data.getImage());
        ((NitroPreview) findChildByName(PREVIEW_COMP)).loadImage(pImage);

        resetBuffer();
    }

    public void resizeImage() throws NitroException {
        NCGRFolder ncgrFolder = ((NCGRFolder) findChildByName(IMAGE_COMP));

        data.setNCGR(ncgrFolder.getObj());

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
}
