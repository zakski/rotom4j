package com.szadowsz.rotom4j.file;

import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;

public interface ImageableWithGraphic extends ImageableWithPalette {

    NCGR getNCGR();

    void setNCGR(NCGR ncgr);
}
