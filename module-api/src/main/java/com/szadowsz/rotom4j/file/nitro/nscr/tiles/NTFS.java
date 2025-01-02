package com.szadowsz.rotom4j.file.nitro.nscr.tiles;

public class NTFS implements Cloneable {             // Nintedo Tile Format Screen
    public int nTile; //        0-9     (0-1023)    (a bit less in 256 color mode, because there'd be otherwise no room for the bg map)
    public int transform;
    public byte xFlip;  //      10    Horizontal Flip (0=Normal, 1=Mirrored)
    public byte yFlip;  //      11    Vertical Flip   (0=Normal, 1=Mirrored)

    public int nPalette; //    12-15    (0-15)      (Not used in 256 color/1 palette mode)

    public NTFS() {
    }

    public NTFS(NTFS original) {
        nPalette = original.nPalette;
        transform = original.transform;
        xFlip = original.xFlip;
        yFlip = original.yFlip;
        nTile = original.nTile;
    }
}
