package com.szadowsz.rotom4j.file.nitro;

import com.szadowsz.rotom4j.file.nitro.n2d.ncgr.NCGR;

public interface DrawableWithGraphic extends DrawableWithPalette {

    NCGR getNCGR();

    void setNCGR(NCGR ncgr);
}
