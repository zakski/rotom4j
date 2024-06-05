package com.szadowsz.nds4j.app.nodes.nclr;

import com.szadowsz.ui.store.ShaderStore;
import com.szadowsz.nds4j.file.nitro.NCLR;
import com.szadowsz.ui.input.keys.GuiKeyEvent;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.NodeType;
import processing.core.PGraphics;
import processing.opengl.PShader;

import java.awt.*;

import static processing.core.PConstants.CORNER;

class NCLRPreviewNode extends AbstractNode {

    final NCLRFolderNode parentColorPickerFolder;
    final String checkerboardShaderPath = "checkerboard.glsl";
    private int numRows;

    NCLRPreviewNode(String path, NCLRFolderNode parentColorPickerFolder) {
        super(NodeType.TRANSIENT, path, parentColorPickerFolder);
        this.parentColorPickerFolder = parentColorPickerFolder;
        NCLR nclr = parentColorPickerFolder.getNCLR();
        numRows = nclr.getNumColors()/16 + ((nclr.getNumColors()%16==0)?0:1);
        masterInlineNodeHeightInCells = numRows;
        ShaderStore.getorLoadShader(checkerboardShaderPath);
    }

    @Override
    protected void drawNodeBackground(PGraphics pg) {
        drawCheckerboard(pg);
        drawColorPreview(pg);
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {

    }

    @Override
    public float getRequiredWidthForHorizontalLayout() {
        return size.x;
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

    private void drawColorPreview(PGraphics pg) {
        NCLR nclr = parentColorPickerFolder.getNCLR();
        float dwidth = size.x / 16;
        float dheight = size.y / numRows;
        for(int i = 0; i < nclr.getNumColors();i++) {
            Color c = nclr.getColor(i);
            pg.fill(c.getRed(),c.getGreen(),c.getBlue());
            pg.noStroke();
            int xpos = i % 16;
            int ypos = i / 16;
            pg.rect(xpos*dwidth, ypos*dheight, dwidth, dheight);
        }
    }

    @Override
    public void keyPressedOverNode(GuiKeyEvent e, float x, float y) {
        parentColorPickerFolder.keyPressedOverNode(e, x, y);
    }
}
