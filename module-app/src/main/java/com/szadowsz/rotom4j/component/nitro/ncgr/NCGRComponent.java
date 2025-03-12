package com.szadowsz.rotom4j.component.nitro.ncgr;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.input.slider.RSlider;
import com.szadowsz.gui.input.mouse.RActivateByType;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.R4JResourceFolder;
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

    private R4JResourceFolder<?> resourceFolder;

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

        resourceFolder = parentFolder.getResourceFolder();

        this.data = data;
        initComponents();
    }

    protected void initComponents() {
        children.add(new NitroPreview(gui, path + "/" + PREVIEW_COMP, this, data));
        children.add(createZoom());
        if (resourceFolder != null) {
            RButton clearance = new RButton(gui, path + "/" + CLEAR_COMP, this);
            clearance.registerAction(RActivateByType.RELEASE, this::resetImage);
            children.add(clearance);
        }
        children.add(new NCLRFolder(gui, path + "/" + PALETTE_COMP, this, data.getNCLR()));
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
                    resizeImage();
                    getParentWindow().resizeForComponent();
                } catch (NitroException e) {
                    throw new RuntimeException(e);
                }
            }

        };
    }

    @Override
    public void setLayout(RLayoutBase layout) {
        // NOOP
    }

    protected void resetImage() {
        NCGR original = data;
        try {

            LOGGER.debug("Resetting {} NCGR File", original.getFileName());
            data = null;
            LOGGER.info("Reset {} NCGR File to Default", original.getFileName());

            if (resourceFolder != null) {
                resourceFolder.recolorImage();
            }

            getParentWindow().close();

        } catch (IOException e) {
            LOGGER.error("{} NCGR Clearance Failed", original.getFileName(), e);
            try {
                data = original;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    @Override
    public void recolorImage() throws NitroException {
        data.setNCLR(((NCLRFolder) findChildByName(PALETTE_COMP)).getObj());
        data.recolorImage();

        PImage pImage = resizeImage(data.getImage());

        ((NitroPreview) findChildByName(PREVIEW_COMP)).loadImage(pImage);

        redrawBuffers();
        if (getParentWindow() != null) {
            getParentWindow().redrawBuffer();
        }

        if (resourceFolder != null) {
            resourceFolder.recolorImage();
        }
    }

    public void resizeImage() throws NitroException {
        data.setNCLR(((NCLRFolder) findChildByName(PALETTE_COMP)).getObj());
        data.recolorImage();

        PImage pImage = resizeImage(data.getImage());

        ((NitroPreview) findChildByName(PREVIEW_COMP)).loadImage(pImage);

        resetBuffer();
        if (getParentWindow() != null) {
            getParentWindow().reinitialiseBuffer();
        }

        if (resourceFolder != null) {
            resourceFolder.recolorImage();
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

    @Override
    public void mousePressed(RMouseEvent mouseEvent, float adjustedMouseY) {
        if (!isVisible()) {
            return;
        }

        RComponent child = findVisibleComponentAt(mouseEvent.getX(), adjustedMouseY);
        if (child != null) {
            LOGGER.info("Mouse Pressed for NCGR Component {} [{}, {}, {}, {}, {}, {}]", child.getName(), mouseEvent.getX(), adjustedMouseY, child.getPosX(), child.getPosY(), child.getWidth(), child.getHeight());
            child.mousePressed(mouseEvent, adjustedMouseY);
            redrawBuffers(); // REDRAW-VALID: we should redraw the group buffer if the user pressed the mouse over a child
        }
    }
}
