package com.szadowsz.nds4j.file;

import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.nclr.NCLR;

public interface ImageableWithPalette extends Imageable {

    NCLR getNCLR();

    void setNCLR(NCLR nclr) throws NitroException;
}
