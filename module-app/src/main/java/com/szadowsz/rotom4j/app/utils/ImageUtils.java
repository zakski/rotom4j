package com.szadowsz.rotom4j.app.utils;

import processing.core.PConstants;
import processing.core.PImage;

import java.awt.image.BufferedImage;

public class ImageUtils {
    private ImageUtils(){}

    public static PImage convertToPImage(BufferedImage bImage) {
        PImage img = new PImage(bImage.getWidth(), bImage.getHeight(), PConstants.ARGB);
        bImage.getRGB(0, 0, img.width, img.height, img.pixels, 0, img.width);
        img.updatePixels();
        return img;
    }
}
