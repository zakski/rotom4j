package com.szadowsz.nds4j.app.nodes.nitro;

import com.szadowsz.nds4j.app.utils.ImageUtils;
import com.szadowsz.nds4j.file.Imageable;
import com.szadowsz.nds4j.utils.Configuration;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.NodeType;
import com.szadowsz.ui.node.impl.FolderNode;
import processing.core.PGraphics;
import processing.core.PImage;

import static com.szadowsz.ui.store.LayoutStore.cell;

public class PreviewNode extends AbstractNode {

    private final Imageable imageable;
    PImage image;
    PImage background;

    public PreviewNode(String path, FolderNode folder, Imageable imageable) {
        super(NodeType.TRANSIENT, path, folder);
        this.imageable = imageable;
        masterInlineNodeHeightInCells = imageable.getHeight() / cell + ((imageable.getHeight() % cell != 0) ? 1 : 0);
        size.x = imageable.getWidth();
        size.y = imageable.getHeight();
        loadImage(ImageUtils.convertToPImage((imageable.getImage())));
    }

    protected void drawCheckerboard(PGraphics pg) {
        pg.image(background, 0, 0);
    }


    @Override
    protected void drawNodeBackground(PGraphics pg) {
        drawCheckerboard(pg);
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
       if (image != null) {
            pg.image(image, 0, 0);
        }
    }

    protected void drawHorizontalGuidelines(int centerColor, int auxColor, int minorColor) {
        for (int i = 0; i < image.width; i++) {
            // major guideline
            int c = background.get(i,image.height/2);
            if ((i & 1) == 1) {
                background.set(i,image.height/2,c ^ centerColor);
            }

            // auxiliary guidelines
            c = background.get(i,image.height/4);
            if ((i & 1) == 1) {
                background.set(i,image.height/4,c ^ auxColor);
            }

            c = background.get(i,(image.height/4)*3);
            if ((i & 1) == 1) {
                background.set(i,(image.height/4)*3,c ^ auxColor);
            }

            //minor guidelines
            for (int j = 0; j < image.height; j += 8) {
                if (j == image.height/4 || j == image.height/2 || j == image.height/4*3) {
                    continue;
                }

                c = background.get(i,j);
                if ((i & 1) == 1) {
                    background.set(i,j,c ^ minorColor);
                }
            }
        }
    }


    protected void drawVerticalGuidelines(int centerColor, int auxColor, int minorColor) {
        for (int i = 0; i < image.height; i++) {
            //major guideline
            int c = background.get(image.width / 2, i);
            if ((i & 1) == 1) {
                background.set(image.width / 2, i, c ^ centerColor);
            }

            //auxiliary guidelines
            c = background.get(image.width / 4, i);
            if ((i & 1) == 1) {
                background.set(image.width / 4, i, c ^ auxColor);
            }

            c = background.get((image.width/4)*3, i);
            if ((i & 1) == 1) {
                background.set((image.width / 4)*3, i, c ^ auxColor);
            }

            //minor guidelines
            for (int j = 0; j < image.width; j += 8) {
                if (j == image.width/4 || j == image.width/2 || j == (image.width/4)*3) continue;

                c = background.get(j,i);
                if ((i & 1) == 1) {
                    background.set(j,i,c ^ minorColor);
                }
            }
        }
    }

    protected void createBG() {
        // create checker background
        background = new PImage(image.width, image.height, PImage.RGB);
        background.updatePixels();
        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                int p = ((x >> 2) ^ (y >> 2)) & 1;
                background.set(x, y, (p != 0) ? 0xFFFFFF : 0xC0C0C0);
            }
        }

        //draw editor guidelines if enabled
        if (Configuration.isShowGuidelines()) {
            //dotted lines at X=0 an Y=0
            int centerColor = 0xFF0000; //red
            int auxColor = 0x00FF00; //green
            int minorColor = 0x002F00;

            drawHorizontalGuidelines(centerColor, auxColor, minorColor);
            drawVerticalGuidelines(centerColor, auxColor, minorColor);
        }
    }


    @Override
    public float getRequiredWidthForHorizontalLayout() {
        return 0;
    }


    public void loadImage(PImage pImage) {
        this.image = pImage;
        masterInlineNodeHeightInCells = image.height / cell + ((image.height % cell != 0) ? 1 : 0);
        size.x = image.width;
        size.y = image.height;
        createBG();
    }

    public PImage getImage(){
        return image;
    }
}
