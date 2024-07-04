package com.szadowsz.nds4j.app.nodes.nscr;

import com.szadowsz.ui.store.ShaderStore;
import com.szadowsz.nds4j.file.nitro.NSCR;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.NodeType;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PShader;

import java.awt.image.BufferedImage;

import static com.szadowsz.ui.store.LayoutStore.cell;
import static processing.core.PConstants.CORNER;

public class NSCRPreviewNode extends AbstractNode {

    private final String checkerboardShaderPath = "checkerboard.glsl";
    private final NSCR nscr;
    PImage image;

    public NSCRPreviewNode(String path, NSCRFolderNode folder, NSCR nscr) {
        super(NodeType.TRANSIENT, path, folder);
        this.nscr = nscr;
        masterInlineNodeHeightInCells = nscr.getHeight() / cell + ((nscr.getHeight() % cell != 0) ? 1 : 0);
        size.x = nscr.getWidth();
        size.y = nscr.getHeight();
        loadImage(nscr.getImage());
    }

    private void drawCheckerboard(PGraphics pg) {
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
        masterInlineNodeHeightInCells = image.height / cell;
        size.x = image.width;
        size.y = image.height;
    }
}
