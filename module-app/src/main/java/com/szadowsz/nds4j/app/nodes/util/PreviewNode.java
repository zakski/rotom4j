package com.szadowsz.nds4j.app.nodes.util;

import com.szadowsz.nds4j.app.utils.ImageUtils;
import com.szadowsz.nds4j.data.Imageable;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.NodeType;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.store.ShaderStore;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PShader;

import static com.szadowsz.ui.store.LayoutStore.cell;
import static processing.core.PConstants.CORNER;

public class PreviewNode extends AbstractNode {

    private final String checkerboardShaderPath = "checkerboard.glsl";
    private final Imageable imageable;
    PImage image;

    public PreviewNode(String path, FolderNode folder, Imageable imageable) {
        super(NodeType.TRANSIENT, path, folder);
        this.imageable = imageable;
        masterInlineNodeHeightInCells = imageable.getHeight() / cell + ((imageable.getHeight() % cell != 0) ? 1 : 0);
        size.x = imageable.getWidth();
        size.y = imageable.getHeight();
        loadImage(ImageUtils.convertToPImage((imageable.getImage())));
    }

    protected void drawCheckerboard(PGraphics pg) {
        PShader checkerboardShader = ShaderStore.getorLoadShader(checkerboardShaderPath);
        checkerboardShader.set("quadPos", pos.x, pos.y);
        pg.shader(checkerboardShader);
        pg.rectMode(CORNER);
        pg.fill(1);
        pg.noStroke();
        pg.rect(0, 0, size.x, size.y);
        pg.resetShader();
    }


    @Override
    protected void drawNodeBackground(PGraphics pg) {
        drawCheckerboard(pg);
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        //drawLeftText(pg, name);
        //drawRightBackdrop(pg, size.x);
        if (image != null) {
            pg.image(image, 0, 0);
        }
    }

    @Override
    public float getRequiredWidthForHorizontalLayout() {
        return 0;
    }

    public void loadImage(PImage pImage) {
        this.image = pImage;
        masterInlineNodeHeightInCells = imageable.getHeight() / cell + ((imageable.getHeight() % cell != 0) ? 1 : 0);
        size.x = image.width;
        size.y = image.height;
    }

    public PImage getImage(){
        return image;
    }
}
