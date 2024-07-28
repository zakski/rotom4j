package com.szadowsz.nds4j.file;

import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.nclr.NCLR;

import java.awt.image.BufferedImage;

public interface Imageable {

    int getWidth();

    int getHeight();

    BufferedImage getImage();
}
