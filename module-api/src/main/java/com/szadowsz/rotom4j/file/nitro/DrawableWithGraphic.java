package com.szadowsz.rotom4j.file.nitro;

import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;

public interface DrawableWithGraphic extends DrawableWithPalette {

    NCGR getNCGR();

    void setNCGR(NCGR ncgr);
}
