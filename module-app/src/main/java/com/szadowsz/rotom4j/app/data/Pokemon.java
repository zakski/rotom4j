package com.szadowsz.rotom4j.app.data;

import com.szadowsz.nds4j.file.bin.evo.EvolutionNFSFile;
import com.szadowsz.nds4j.file.bin.learnset.LearnsetNFSFile;
import com.szadowsz.nds4j.file.bin.stats.StatsNFSFile;
import com.szadowsz.nds4j.file.nitro.ncgr.NCGR;
import com.szadowsz.nds4j.file.nitro.nclr.NCLR;

public class Pokemon {
    private StatsNFSFile stats;
    private EvolutionNFSFile evolutions;
    private LearnsetNFSFile learnset;
    private NCGR[] frontSprites;
    private NCGR[] backSprites;
    private NCLR normalPalette;
    private NCLR shinyPalette;
}
