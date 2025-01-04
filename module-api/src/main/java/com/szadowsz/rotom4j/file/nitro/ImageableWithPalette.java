package com.szadowsz.rotom4j.file.nitro;

import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.nclr.NCLR;

public interface ImageableWithPalette extends Imageable {

    NCLR getNCLR();

    void setNCLR(NCLR nclr) throws NitroException;
}
