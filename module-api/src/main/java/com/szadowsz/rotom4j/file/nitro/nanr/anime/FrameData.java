package com.szadowsz.rotom4j.file.nitro.nanr.anime;

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

   public int rotation;
   public int scaleX;
   public int scaleY;

   public int xDisplace;
   public int yDisplace;
}
