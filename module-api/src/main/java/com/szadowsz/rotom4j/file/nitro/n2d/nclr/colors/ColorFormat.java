package com.szadowsz.rotom4j.file.nitro.n2d.nclr.colors;

public enum ColorFormat {

    A3I5(1,8),           // 8 bits-> 0-4: index; 5-7: alpha
    colors4(2,2),        // 2 bits for 4 colors
    colors16(3,4),       // 4 bits for 16 colors
    colors256(4,8),      // 8 bits for 256 colors
    texel4x4(5,32),       // 32bits, 2bits per Texel (only in textures)
    A5I3(6,8),           // 8 bits-> 0-2: index; 3-7: alpha
    direct(7,16),         // 16bits, color with BGR555 encoding
    colors2(8,2),        // 1 bit for 2 colors
    BGRA32(9,32),          // 32 bits -> ABGR
    A4I4(10, 8),
    ABGR32(11, 32);

    public final byte id;
    public final int bits;

    ColorFormat(int id, int bits) {
        this.id = (byte) id;
        this.bits = bits;
    }

    public static ColorFormat valueOf(int value) {
        return valueOf((byte) value);
    }

    public static ColorFormat valueOf(byte value) {
        for (ColorFormat f : ColorFormat.values()) {
            if (f.id == value) {
                return f;
            }
        }
        return null;
    }

}