package com.szadowsz.nds4j.app.nodes.ncgr;

import com.szadowsz.nds4j.app.utils.ImageUtils;
import com.szadowsz.nds4j.file.nitro.NCGR;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.NodeType;
import com.szadowsz.ui.node.impl.FolderNode;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

import java.awt.image.BufferedImage;

import static com.szadowsz.ui.store.LayoutStore.cell;


class NCGRPreviewNode extends AbstractNode {

    PImage image;

    NCGRPreviewNode(String path, FolderNode folder, NCGR ncgr) {
        super(NodeType.TRANSIENT,path, folder);
        BufferedImage bufferedImage = ncgr.getImage();
        loadImage(ImageUtils.convertToPImage(bufferedImage));
    }

    public void loadImage(PImage pImage) {
        image = pImage;
        masterInlineNodeHeightInCells = image.height / cell;
        size.x = image.width;
        size.y = image.height;
    }

//    @Override
//    public void mouseReleasedOverNodeEvent(LazyMouseEvent e){
//        if(armed && !valueBoolean){ // can only toggle manually to true, toggle to false happens automatically
//            valueBoolean = true;
//            onValueChangingActionEnded();
//        }
//        armed = false;
//    }

    @Override
    protected void drawNodeBackground(PGraphics pg) {

    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        //drawLeftText(pg, name);
        //drawRightBackdrop(pg, size.x);
        pg.image(image,0,0);
    }

    @Override
    public float getRequiredWidthForHorizontalLayout() {
        return image.width;
    }
}
