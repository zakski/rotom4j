package com.szadowsz.gui.config.theme;

import processing.core.PApplet;
import processing.core.PGraphics;

import static processing.core.PConstants.*;

/**
 * Color Converter calculation for GUI
 */
public class RColorConverter {

    private static PGraphics colorStoreHSB = null;
    private static PGraphics colorStoreRGB = null;

    private RColorConverter(){/*NOOP*/}

    /**
     * Static initialisation of store
     */
    public static void init(PApplet sketch) {
        if (colorStoreHSB == null) {
            colorStoreHSB = sketch.createGraphics(256, 256, P2D);
            colorStoreHSB.colorMode(HSB, 1, 1, 1, 1);
            colorStoreRGB = sketch.createGraphics(256, 256, P2D);
            colorStoreRGB.colorMode(RGB, 255, 255, 255, 255);
        }
    }

    /**
     * Color according to HSB model
     *
     * @param br  color's brightness
     * @return color as an integer value
     */
    public static int color(float br) {
        return color(0, 0, br, 1);
    }

    /**
     * Color according to HSB model
     *
     * @param br  color's brightness
     * @param alpha color's transparency value
     * @return color as an integer value
     */
    public static int color(float br, float alpha) {
        return color(0, 0, br, alpha);
    }

    /**
     * Color according to HSB model
     *
     * @param hue color's hue
     * @param sat color's saturation
     * @param br  color's brightness
     * @return color as an integer value
     */
    public static int color(float hue, float sat, float br) {
        return color(hue, sat, br, 1);
    }

    /**
     * Color according to HSB model
     *
     * @param hue color's hue
     * @param sat color's saturation
     * @param br  color's brightness
     * @param alpha color's transparency value
     * @return color as an integer value
     */
    public static int color(float hue, float sat, float br, float alpha) {
        return colorStoreHSB.color(hue, sat, br, alpha);
    }

    /**
     * Color according to RGBA model
     *
     * @param r color's red
     * @param g color's green
     * @param b color's blue
     * @param alpha color's transparency value
     * @return color as an integer value
     */
    public static int colorRGB(float r, float g, float b, float alpha) {
        return colorStoreRGB.color(r, g, b, alpha);
    }

    /**
     * Get Color's Red value
     *
     * @param hex color as an integer value
     * @return color's red value
     */
    public static float red(int hex){
        return colorStoreHSB.red(hex);
    }

    /**
     * Get Color's Green value
     *
     * @param hex color as an integer value
     * @return color's green value
     */
    public static float green(int hex){
        return colorStoreHSB.green(hex);
    }

    /**
     * Get Color's Blue value
     *
     * @param hex color as an integer value
     * @return color's blue value
     */
    public static float blue(int hex){
        return colorStoreHSB.blue(hex);
    }

    /**
     * Get Color's hue value
     *
     * @param hex color as an integer value
     * @return color's hue
     */
    public static float hue(int hex){ return colorStoreHSB.hue(hex); }

    /**
     * Get Color's saturation value
     *
     * @param hex color as an integer value
     * @return color's saturation
     */
    public static float sat(int hex){ return colorStoreHSB.saturation(hex); }

    /**
     * Get Color's brightness value
     *
     * @param hex color as an integer value
     * @return color's brightness
     */
    public static float br(int hex){ return colorStoreHSB.brightness(hex); }

    /**
     * Get Color's transparency value
     *
     * @param hex color as an integer value
     * @return color's transparency
     */
    public static float alpha(int hex) {
        return colorStoreHSB.alpha(hex);
    }

    /**
     * Get the HSB Store
     *
     * @return the color store
     */
    public static PGraphics getColorStoreHSB() {
        return colorStoreHSB;
    }

    /**
     * Get the RGB Store
     *
     * @return the color store
     */
    public static PGraphics getColorStoreRGB() {
        return colorStoreRGB;
    }

    /**
     * Make the color transparent
     *
     * @param hex color as an integer value
     * @return transparent color as an integer value
     */
    public static int toTransparent(int hex) {
        if(hex == 0x00000000){
            hex = 0xFF010101;
        }
        return colorStoreHSB.color(hex, 0);
    }
}
