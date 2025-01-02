package com.szadowsz.rotom4j.utils;

import java.awt.*;

/**
 * Utility Class to aid with reading of Colors
 */
public class ColorUtils {

    private ColorUtils(){}

    /**
     * Conversion utility to interpret BGR555 as java Color
     *
     * @param byte1 first BGR555 byte
     * @param byte2 second BGR555 byte
     * @return java Color
     */
    public static Color bgr555ToColor(byte byte1, byte byte2) {
        int r, b, g;

        int bgr = ((byte2 & 0xff) << 8) | (byte1 & 0xff);

        r = (bgr & 0x001F) << 3;
        g = ((bgr & 0x03E0) >> 2);
        b = ((bgr & 0x7C00) >> 7);

        return new Color(r, g, b);
    }

    /**
     * Conversion utility to interpret java Color as BGR555 bytes
     *
     * @param color java Color
      * @return 2 byte BGR555 array
     */
    public static byte[] colorToBGR555(Color color) {
        byte[] d = new byte[2];

        int r = color.getRed() / 8;
        int g = (color.getGreen() / 8) << 5;
        int b = (color.getBlue() / 8) << 10;

        int bgr = r + g + b;

        d[0] = (byte) (bgr & 0xff);
        d[1] = (byte) ((bgr >> 8) & 0xff);

        return d;
    }
}
