package com.szadowsz.nds4j.app.nodes.nitro;

import com.szadowsz.nds4j.app.Processing;
import com.szadowsz.nds4j.app.utils.FileChooser;
import com.szadowsz.nds4j.app.utils.ImageUtils;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.Imageable;
import com.szadowsz.nds4j.file.nitro.nclr.NCLR;
import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.input.mouse.GuiMouseEvent;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.node.impl.SliderNode;
import com.szadowsz.ui.window.WindowManager;
import processing.core.PGraphics;
import processing.core.PImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.szadowsz.ui.store.LayoutStore.cell;

public abstract class NitroFolderNode<I extends Imageable> extends FolderNode {

    protected static final String PREVIEW_NODE = "Preview";
    protected static final String ZOOM_NODE = "Zoom";
    protected static final String RESET_NODE = "Reset";

    protected static final String SELECT_NCLR_FILE = "Select NClR";

    protected I imageable;
    protected String selectName;

    public NitroFolderNode(String path, FolderNode parent, LayoutType layout, I imageable, String selectName) {
        super(path, parent, layout);
        this.imageable = imageable;
        this.selectName = selectName;
    }

    public NitroFolderNode(String path, FolderNode parent, I imageable, String selectName) {
        super(path, parent, LayoutType.VERTICAL_1_COL);
        this.imageable = imageable;
        this.selectName = selectName;
    }

    /**
     * Utility Method to create pre-packaged Zoom Node
     *
     * @return Slider representing the current zoom
     */
    protected SliderNode createZoom() {
        return new SliderNode(
                NitroFolderNode.this.path + "/" + ZOOM_NODE,
                NitroFolderNode.this,
                1.0f,
                1.0f,
                4.0f,
                0.1f,
                true
        ) {
            @Override
            protected void onValueFloatChanged() {
                super.onValueFloatChanged();
                try {
                    recolorImage();
                } catch (NitroException e) {
                    throw new RuntimeException(e);
                }
            }

        };
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        drawLeftText(pg, (shouldDisplayName())? name : selectName);
        drawRightBackdrop(pg, cell);
    }

    /**
     * Method to convert a BufferedImage to a PImage and resize it according to an optional zoom node
     *
     * @param image to convert
     * @return appropriately scaled PImage
     */
    protected PImage resizeImage(BufferedImage image) {
        PImage pImage = ImageUtils.convertToPImage(image);
        SliderNode zoomNode = (SliderNode) findChildByName(ZOOM_NODE);
        if (zoomNode != null) {
            float zoom = zoomNode.valueFloat;
            pImage.resize(Math.round(pImage.width * zoom), 0);
        }
        return pImage;
    }

    /**
     * Check to see if node should display regular name, or selection name
     *
     * @return true if regular, false otherwise
     */
    protected boolean shouldDisplayName(){
        return imageable != null;
    }

    /**
     * Method to reinitialise and resupply an image to the PreviewNode
     *
     * This should be called when something happens that should cause the preview image to change
     *
     * @throws NitroException if the change fails to take
     */
    public abstract void recolorImage() throws NitroException;

    /**
     * Change the Nitro Obj currently displayed
     *
     * @param imageable the obj to now use
     */
    public void setImageable(I imageable){
        this.imageable = imageable;
    }

    /**
     * Change the Nitro Obj currently displayed
     *
     * @return  the current obj
     */
    public I getImageable(){
        return imageable;
    }
}
