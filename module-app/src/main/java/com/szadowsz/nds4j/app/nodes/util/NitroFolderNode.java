package com.szadowsz.nds4j.app.nodes.util;

import com.szadowsz.nds4j.app.nodes.ncgr.NCGRFolderNode;
import com.szadowsz.nds4j.app.utils.ImageUtils;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.node.impl.SliderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PImage;

import java.awt.image.BufferedImage;

import static com.szadowsz.ui.store.LayoutStore.cell;

public abstract class NitroFolderNode extends FolderNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(NCGRFolderNode.class);

    protected final String ZOOM_NODE = "Zoom";
    protected final String SELECT_NCGR_FILE = "Select NCGR";
    protected final String SELECT_NCLR_FILE = "Select NClR";


    public NitroFolderNode(String path, FolderNode parent, LayoutType layout) {
        super(path, parent, layout);
    }

    protected SliderNode createZoom(){
        SliderNode zoom = new SliderNode(path + "/" + ZOOM_NODE, this, 1.0f, 1.0f, 4.0f, true){
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
        zoom.increasePrecision();
        return zoom;
    }

    protected PImage resizeImage(BufferedImage image){
        PImage pImage = ImageUtils.convertToPImage(image);
        float zoom = ((SliderNode) findChildByName(ZOOM_NODE)).valueFloat;
        pImage.resize(Math.round(pImage.width*zoom),0);
        return pImage;
    }

    protected abstract void recolorImage() throws NitroException;

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        drawLeftText(pg, name);
        drawRightBackdrop(pg, cell);
    }
}
