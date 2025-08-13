package com.szadowsz.rotom4j.app.data;

import com.szadowsz.rotom4j.file.data.evo.EvolutionNFSFile;
import com.szadowsz.rotom4j.file.data.learnset.LearnsetNFSFile;
import com.szadowsz.rotom4j.file.data.stats.StatsNFSFile;
import com.szadowsz.rotom4j.file.nitro.n2d.ncgr.NCGR;
import com.szadowsz.rotom4j.file.nitro.n2d.nclr.NCLR;

public class Pokemon {
    private StatsNFSFile stats;
    private EvolutionNFSFile evolutions;
    private LearnsetNFSFile learnset;
    private NCGR[] frontSprites;
    private NCGR[] backSprites;
    private NCLR normalPalette;
    private NCLR shinyPalette;
}
