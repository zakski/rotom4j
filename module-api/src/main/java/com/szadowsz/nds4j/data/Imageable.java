package com.szadowsz.nds4j.data;

import com.szadowsz.nds4j.file.nitro.NCLR;

import java.awt.image.BufferedImage;

public interface Imageable {

    int getWidth();

    int getHeight();

    BufferedImage getImage();

    NCLR getNCLR();

    void setNCLR(NCLR nclr);
}
