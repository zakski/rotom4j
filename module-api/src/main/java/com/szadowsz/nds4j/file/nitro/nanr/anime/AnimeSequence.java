package com.szadowsz.nds4j.file.nitro.nanr.anime;

public class AnimeSequence {

    public int nFrames; // u16
    public FrameData[] frames;

    public int loopStartFrame; // u16

    public int animationElement; // u16

    public int animationType; // u16

    public long playbackMode; // u32

    public int startFrameOffset; // u32
}

