package com.szadowsz.nds4j.file.nitro.nanr.anime;

public class FrameData {

   // Header
   public int headerPos;

   public long dataOffset;
   public int frameDuration;
   public int constant;

   // Data
   public int cellIndex;
   public int type;

   public int garbage;

   public byte[] transform;

   public int xDisplace;
   public int yDisplace;
}
