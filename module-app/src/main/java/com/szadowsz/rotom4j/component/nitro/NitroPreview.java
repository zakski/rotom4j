package com.szadowsz.rotom4j.component.nitro;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.rotom4j.app.utils.ImageUtils;
import com.szadowsz.rotom4j.file.nitro.Drawable;
import com.szadowsz.rotom4j.utils.Configuration;
import processing.core.PGraphics;
import processing.core.PImage;

public class NitroPreview extends RComponent {

    private final Drawable drawable;
    PImage image;
    PImage background;

    public NitroPreview(RotomGui gui, String path, RGroup parent, Drawable drawable) {
        super(gui, path, parent);
        this.drawable = drawable;
        heightInCells = drawable.getHeight() / RLayoutStore.getCell() + ((drawable.getHeight() % RLayoutStore.getCell() != 0) ? 1 : 0);
        size.x = drawable.getWidth();
        size.y = drawable.getHeight();
        loadImage(ImageUtils.convertToPImage((drawable.getImage())));
    }

    protected void drawCheckerboard(PGraphics pg) {
        pg.image(background, 0, 0);
    }


    @Override
    protected void drawBackground(PGraphics pg) {
        drawCheckerboard(pg);
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
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
    public float suggestWidth() {
        if (image != null) {
            return image.width;
        } else {
            return 0;
        }
    }

    public void loadImage(PImage pImage) {
        this.image = pImage;
        heightInCells = image.height / RLayoutStore.getCell() + ((image.height % RLayoutStore.getCell() != 0) ? 1 : 0);
        size.x = image.width;
        size.y = image.height;
        createBG();
    }

    public PImage getImage(){
        return image;
    }

    @Override
    public void updateCoordinates(float bX, float bY, float rX, float rY, float w, float h) {
        super.updateCoordinates(bX, bY, rX, rY, w, h);
//        PImage pImage = ImageUtils.convertToPImage((drawable.getImage()));
//        pImage.resize(Math.round(size.x),Math.round(size.y));
//        loadImage(pImage);
    }
}
