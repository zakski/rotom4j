package com.szadowsz.nds4j.app.nodes.ncer;

import com.szadowsz.nds4j.file.nitro.NCER;
import com.szadowsz.nds4j.file.nitro.NSCR;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.NodeType;
import com.szadowsz.ui.store.ShaderStore;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PShader;

import java.awt.image.BufferedImage;

import static com.szadowsz.ui.store.LayoutStore.cell;
import static processing.core.PConstants.CORNER;

public class NCERPreviewNode extends AbstractNode {

    private final String checkerboardShaderPath = "checkerboard.glsl";
    private final NCER ncer;
    PImage image;

    public NCERPreviewNode(String path, NCERFolderNode folder, NCER ncer) {
        super(NodeType.TRANSIENT,path, folder);
        this.ncer = ncer;
        masterInlineNodeHeightInCells = ncer.getHeight() / cell +  ((ncer.getHeight() % cell != 0)?1:0);
        size.x = ncer.getWidth();
        size.y = ncer.getHeight();
        loadImage(ncer.getImage());
    }

    private void drawCheckerboard(PGraphics pg) {
        PShader checkerboardShader = ShaderStore.getorLoadShader(checkerboardShaderPath);
        checkerboardShader.set("quadPos", pos.x, pos.y);
        pg.shader(checkerboardShader);
        pg.rectMode(CORNER);
        pg.fill(1);
        pg.noStroke();
        pg.rect(0,0, size.x, size.y);
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

    public void loadImage(BufferedImage bufferedImage) {
        this.image = null;
        if (bufferedImage != null) {
            this.image = new PImage(bufferedImage.getWidth(), bufferedImage.getHeight(), PConstants.RGB);
            image.loadPixels();
            for (int h = 0; h < bufferedImage.getHeight(); h++){
                for (int w = 0; w < bufferedImage.getWidth(); w++){
                    image.set(w,h, bufferedImage.getRGB(w,h));
                }
            }
            masterInlineNodeHeightInCells = image.height / cell;
            size.x = image.width;
            size.y = image.height;
        }
    }
}
