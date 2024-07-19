package com.szadowsz.nds4j.data;

import com.szadowsz.nds4j.file.nitro.NCGR;

public interface ComplexImageable extends Imageable {

    NCGR getNCGR();

    void setNCGR(NCGR ncgr);
}
