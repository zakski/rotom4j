package com.szadowsz.nds4j;

import com.szadowsz.nds4j.data.ref.RomFormat;

public class NFSConstants {

    private static RomFormat rom = RomFormat.FEB17;

    private NFSConstants(){}


    public static RomFormat getExpectedRom(){
        return rom;
    }

    public static void setExpectedRom(RomFormat r){
        rom = r;
    }
}
