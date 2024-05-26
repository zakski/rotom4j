package com.szadowsz.nds4j.app.nodes.nclr;

import com.google.gson.JsonElement;

import com.szadowsz.nds4j.app.nodes.ncgr.NCGRFolderNode;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.NCLR;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.store.NormColorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

import java.awt.*;

import static com.szadowsz.ui.store.LayoutStore.cell;
import static processing.core.PApplet.CENTER;

public class NCLRFolderNode extends FolderNode {

    protected Logger LOGGER = LoggerFactory.getLogger(NCLRFolderNode.class);

    private NCGRFolderNode spriteFolder = null;
    private NCLR nclr;
    private int index;
    private final String COLOR_NODE_NAME = "col";
    private final String R_NODE_NAME = "R";
    private final String G_NODE_NAME = "G";
    private final String B_NODE_NAME = "B";

    public NCLRFolderNode(String path, FolderNode parentFolder, NCLR nclr) {
        super(path, parentFolder);
        if (parentFolder instanceof NCGRFolderNode){
            spriteFolder = (NCGRFolderNode) parentFolder;
        }
        this.nclr = nclr;
        lazyInitNodes();
        idealWindowWidthInCells = 7;
    }

    public NCLR getNCLR() {
        return nclr;
    }


    protected float getValue(String nodeName) {
        NCLRSliderNode node = ((NCLRSliderNode) findChildByName(nodeName));
        return node.valueFloat;
    }

    public Color getColor() {
        return nclr.getColor(index);
    }

    public float getRed() {
        return nclr.getColor(index).getRed();
    }

    public float getGreen() {
        return nclr.getColor(index).getGreen();
    }
    public float getBlue() {
        return nclr.getColor(index).getBlue();
    }
    public int getNumColors() {
        return nclr.getNumColors();
    }

    protected void setValue(String nodeName, float valueFloat) {
        NCLRSliderNode node = ((NCLRSliderNode) findChildByName(nodeName));
        node.setValueFromParent(valueFloat);
    }
    public void setPalette(NCLR palette) {
        this.nclr = palette;
        index = 0;
    }

    protected void lazyInitNodes() {
        if (!children.isEmpty()) {
            return;
        }
        children.add(new NCLRPreviewNode(path + "/preview", this));
        children.add(new NCLRSliderNode.ColorSelectNode(path + "/" + COLOR_NODE_NAME, this));
        children.add(new NCLRSliderNode.RSelectNode(path + "/" + R_NODE_NAME, this));
        children.add(new NCLRSliderNode.GSelectNode(path + "/" + G_NODE_NAME, this));
        children.add(new NCLRSliderNode.BSelectNode(path + "/" + B_NODE_NAME, this));
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        drawLeftText(pg, name);
        drawPreviewRect(pg);
    }

    protected void drawPreviewRect(PGraphics pg) {
        strokeForegroundBasedOnMouseOver(pg);
        float previewRectSize = cell * 0.6f;
        pg.translate(size.x - cell * 0.5f, size.y * 0.5f);
        pg.rectMode(CENTER);
        Color color = nclr.getColor(index);
        pg.fill(color.getRed(),color.getGreen(),color.getBlue());
        pg.rect(0, 0, previewRectSize, previewRectSize);
    }

    void loadValuesFromRGB() {
        PGraphics colorProvider = NormColorStore.getColorStoreRGB();
        int nIndex = (int) getValue(COLOR_NODE_NAME);
        if (index != nIndex){
            index = nIndex;
            if (index < 0){
                index = 0;
            } else if (index >= nclr.getNumColors()){
                index = nclr.getNumColors()-1;
            }
            Color color = nclr.getColor(index);
            setValue(COLOR_NODE_NAME,index);
            setValue(R_NODE_NAME,color.getRed());
            setValue(G_NODE_NAME,color.getGreen());
            setValue(B_NODE_NAME,color.getBlue());
        } else{
            nclr.setColor(index, colorProvider.color(
                    getValue(R_NODE_NAME),
                    getValue(G_NODE_NAME),
                    getValue(B_NODE_NAME)));
            if (spriteFolder != null){
                try {
                    spriteFolder.recolorImage();
                } catch (NitroException e) {
                    LOGGER.warn("Issue updating Palette",e);
                }
            }
        }
    }

    @Override
    public void overwriteState(JsonElement loadedNode) {
    }
}
