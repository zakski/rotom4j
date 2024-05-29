package com.szadowsz.nds4j.app.data;

import com.szadowsz.nds4j.file.bin.EvolutionNFSFile;
import com.szadowsz.nds4j.file.bin.StatsNFSFile;
import com.szadowsz.nds4j.file.nitro.NCGR;
import com.szadowsz.nds4j.file.nitro.NCLR;

public class Pokemon {
    private StatsNFSFile stats;
    private EvolutionNFSFile evolutions;
    private NCGR[] frontSprites;
    private NCGR[] backSprites;
    private NCLR normalPalette;
    private NCLR shinyPalette;
}
