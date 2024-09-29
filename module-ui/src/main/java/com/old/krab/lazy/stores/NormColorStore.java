package com.old.krab.lazy.stores;

import processing.core.PGraphics;

import static processing.core.PConstants.*;

public class NormColorStore {

    private static PGraphics colorStoreHSB = null;
    private static PGraphics colorStoreRGB = null;

    public static void init() {
        colorStoreHSB = GlobalReferences.app.createGraphics(256, 256, P2D);
        colorStoreHSB.colorMode(HSB, 1, 1, 1, 1);
        colorStoreRGB = GlobalReferences.app.createGraphics(256, 256, P2D);
        colorStoreRGB.colorMode(RGB, 255, 255, 255, 255);
    }

    public static int color(float br) {
        return color(0, 0, br, 1);
    }

    public static int color(float br, float alpha) {
        return color(0, 0, br, alpha);
    }

    public static int color(float hue, float sat, float br) {
        return color(hue, sat, br, 1);
    }

    public static int color(float hue, float sat, float br, float alpha) {
        return colorStoreHSB.color(hue, sat, br, alpha);
    }

    public static int colorRGB(float r, float g, float b, float alpha) {
        return colorStoreRGB.color(r, g, b, alpha);
    }

    public static float red(int hex){
        return colorStoreHSB.red(hex);
    }

    public static float green(int hex){
        return colorStoreHSB.green(hex);
    }

    public static float blue(int hex){
        return colorStoreHSB.blue(hex);
    }

    public static float hue(int hex){ return colorStoreHSB.hue(hex); }

    public static float sat(int hex){ return colorStoreHSB.saturation(hex); }

    public static float br(int hex){ return colorStoreHSB.brightness(hex); }

    public static float alpha(int hex) {
        return colorStoreHSB.alpha(hex);
    }

    public static PGraphics getColorStoreHSB() {
        return colorStoreHSB;
    }
    public static PGraphics getColorStoreRGB() {
        return colorStoreRGB;
    }

    public static int toTransparent(int hex) {
        if(hex == 0x00000000){
            hex = 0xFF010101;
        }
        return colorStoreHSB.color(hex, 0);
    }
}
