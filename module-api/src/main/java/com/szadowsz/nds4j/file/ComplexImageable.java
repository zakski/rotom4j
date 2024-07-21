package com.szadowsz.nds4j.file;

import com.szadowsz.nds4j.file.nitro.ncgr.NCGR;

public interface ComplexImageable extends Imageable {

    NCGR getNCGR();

    void setNCGR(NCGR ncgr);
}
