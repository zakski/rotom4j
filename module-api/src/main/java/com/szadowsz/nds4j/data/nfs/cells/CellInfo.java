package com.szadowsz.nds4j.data.nfs.cells;

import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.NCER;
import com.szadowsz.nds4j.file.nitro.NCGR;

import java.awt.image.BufferedImage;

/**
 * An individual "Cell", or "Bank" within an NCER.
 * In theory, this represents one assembled image.
 */
public class CellInfo {
    private static final int[][] widths = new int[][]{ {8, 16, 32, 64}, {16, 32, 32, 64}, {8, 8, 16, 32} };
    private static final int[][] heights = new int[][]{ {8, 16, 32, 64}, {8, 8, 16, 32}, {16, 32, 32, 64} };


    private final NCER ncer;
    String name;
    int tacuData;

    CellAttribute attributes;
    short maxX;
    short maxY;
    short minX;
    short minY;
    OAM[] oams;

    private int partitionOffset;
    private int partitionSize;

    /**
     * Creates a new Cell for use in a CellBank
     *
     * @param oamCount an <code>int</code> representing the number of OAMs in the cell
     */
    public CellInfo(NCER ncer, int oamCount, int partitionOffset, int partitionSize, int tacuData) {
        this.ncer = ncer;
        attributes = new CellAttribute();
        oams = new OAM[oamCount];
        for (int i = 0; i < oamCount; i++) {
            oams[i] = new OAM();
        }
        name = "";
        this.partitionOffset = partitionOffset;
        this.partitionSize = partitionSize;
        this.tacuData = tacuData;
    }

    public CellInfo(NCER ncer, CellPojo pojo, int[] partition) {
        this.ncer = ncer;
        this.maxX = pojo.maxX;
        this.maxY = pojo.maxY;
        this.minX = pojo.minX;
        this.minY = pojo.minY;
        oams = new OAM[pojo.nAttribs];
        for (int i = 0; i < oams.length; i++) {
            oams[i] = new OAM();
            setOam(i,pojo.getOamAttrs(i));
        }
        attributes = new CellAttribute();
        setAttributes(pojo.cellAttr);
        this.partitionOffset = partition[0];
        this.partitionSize = partition[1];

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAttributes(int cellAttrs) {
        attributes.hFlip = ((cellAttrs >> 8) & 1) == 1;
        attributes.vFlip = ((cellAttrs >> 9) & 1) == 1;
        attributes.hvFlip = ((cellAttrs >> 10) & 1) == 1;
        attributes.boundingRectangle = ((cellAttrs >> 11) & 1) == 1;
        attributes.boundingSphereRadius = cellAttrs & 0x3F;
    }

    public short getMaxX() {
        return maxX;
    }

    public void setMaxX(short maxX) {
        this.maxX = maxX;
    }

    public short getMaxY() {
        return maxY;
    }

    public void setMaxY(short maxY) {
        this.maxY = maxY;
    }

    public short getMinX() {
        return minX;
    }

    public void setMinX(short minX) {
        this.minX = minX;
    }

    public short getMinY() {
        return minY;
    }

    public void setMinY(short minY) {
        this.minY = minY;
    }

    public OAM[] getOams() {
        return oams;
    }

    public int getOamCount() {
        return oams.length;
    }

    public OAM getOam(int index) {
        return oams[index];
    }

    public void setOams(OAM[] oams) {
        this.oams = oams;
    }


    public int getPartitionOffset(){
        return partitionOffset;
    }

    public int getPartitionSize(){
        return partitionSize;
    }

    private int[] CellGetObjDimensions(int shape, int size) {
        return new int[] {widths[shape][size], heights[shape][size]};
    }

    public void setOam(int index, short[] attrs) {
        short attr0 = attrs[0];
        short attr1 = attrs[1];
        short attr2 = attrs[2];

        oams[index].xCoord = attr1 & 0x1FF;
        oams[index].yCoord = attr0 & 0xFF; // bits 0-7
        oams[index].shape = attr0 >> 14; // bits 14-15
        oams[index].size = attr1 >> 14;

        int[] dims = CellGetObjDimensions(oams[index].shape,  oams[index].size);
        oams[index].width = dims[0];
        oams[index].height = dims[1];
                
        oams[index].tileOffset = attr2 & 0x3FF;
        oams[index].priority = (attr2 >> 10) & 0x3;
        oams[index].palette = (attr2 >> 12) & 0xF;
        oams[index].mode = (attr0 >> 10) & 3; //bits 10-11
        oams[index].mosaic = ((attr0 >> 12) & 1); //bit 12

        oams[index].rotation = (attr0 >> 8) == 1; //bit 8
        if (oams[index].rotation){
            oams[index].flipX = false;
            oams[index].flipY = false;
            oams[index].doubleSize = (attr0 >> 9) & 1;
            oams[index].sizeDisable = 0; //bit 9 Obj Size (if rotation) or Obj Disable (if not rotation)
            oams[index].rotationScaling = (attr1 >> 9) & 0x1F;
        } else {
            oams[index].flipX = ((attr1 >> 12) & 1) == 1;
            oams[index].flipY = ((attr1 >> 13) & 1) == 1;
            oams[index].doubleSize = 0;
            oams[index].sizeDisable = ((attr0 >> 9) & 1); //bit 9 Obj Size (if rotation) or Obj Disable (if not rotation)
            oams[index].rotationScaling = 0;
        }
        boolean is8 = ((attr0 >> 13) & 1) == 1;
        oams[index].characterBits = 4;
        if (is8) {
            oams[index].characterBits = 8;
        }
        //oams[index].mode = (attr0 >> 2) & 3; //bits 10-11
        //oams[index].mosaic = ((attr0 >> 4) & 1) == 1; //bit 12
        //oams[index].colors = ((attr0 >> 5) & 1) == 0 ? 16 : 256; //bit 13
        //oams[index].shape = (attr0 >> 6) & 3; //bits 14-15

        //oams[index].xCoord = (attr1 & 0x01ff) >= 0x100 ? (attr1 & 0x01ff) - 0x200 : (attr1 & 0x01ff);
        //oams[index].size = (attr1 >> 14) & 3;

     }

    public int getWidth() {
        return Math.abs(maxX - minX);
    }

    public int getHeight() {
        return Math.abs(maxY - minY);
    }

    public String toString() {
        return name;
    }



    class CellAttribute {
        boolean hFlip;
        boolean vFlip;
        boolean hvFlip;
        boolean boundingRectangle;
        int boundingSphereRadius;
    }

//    public CellImage getImage() throws NitroException {
//        return new CellImage();
//    }

    /**
     * An individual OAM within an NCER (<code>CellBank</code>).
     * This represents the sub-images that make up a Cell/Bank, or more accurately,
     * the data used to generate them from an NCGR (<code>NCGR</code>).
     */
    public class OAM {
       // attr0
        int yCoord;
        boolean rotation;
        int sizeDisable;
        int mode;
        int mosaic;
        //int colors;
        int characterBits;
        int shape;
        int doubleSize;

        // attr1
        int xCoord;
        int rotationScaling;
        boolean flipX;
        boolean flipY;
        int size;

        // attr2
        int tileOffset;
        int priority;
        int palette;

        int width;
        int height;

        public int getX(){
            return xCoord;
        }

        public int getY(){
            return yCoord;
        }

        public int getShape() {
            return shape;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getOffset() {
            return tileOffset;
        }

        public int getSize() {
            return size;
        }

        public int getPalette() {
            return palette;
        }

        public boolean getFlipY() {
            return flipY;
        }

        public boolean getFlipX() {
            return flipX;
        }

        public boolean getRotationScaling() {
            return rotationScaling == 1;
        }

        public boolean getSizeDisable(){
            return sizeDisable == 1;
        }

        public boolean getDoubleSize(){
            return doubleSize == 1;
        }

        public int getDoubleSizeAsInt(){
            return doubleSize;
        }

        public OAM.OamImage getImage(int i, int[] index) throws NitroException {
            boolean draw = false;
            if (index == null)
                draw = true;
            else
                for (int j : index)
                    if (j == i) {
                        draw = true;
                        break;
                    }

            if (!draw)
                return null;

            int num_pal = palette;
            if (num_pal >= ncer.getNCLR().getNumColors())
                num_pal = 0;
//                Arrays.fill(cell_img.tilePal, num_pal);

            return new OAM.OamImage();
        }

        /**
         * This is a visual representation of a given OAM within its parent NCGR (<code>NCGR</code>) and <code>Cell</code>
         */
        public class OamImage {
            private NCGR oamImage;
            int storedWidth = 0;
            int storedHeight = 0;
            private boolean update;

            private OamImage() throws NitroException {
                generateImageData();
            }

            private void generateImageData() throws NitroException {
                if (NCER.oamSize[shape][size][0] != storedWidth || NCER.oamSize[shape][size][1] != storedHeight) {
                    storedHeight = NCER.oamSize[shape][size][1];
                    storedWidth = NCER.oamSize[shape][size][0];
                    if (ncer.getNCGR() != null) {
                        oamImage = new NCGR(storedHeight, storedWidth, ncer.getBitDepth(), ncer.getNCLR());
                    }
                }
//
//                int startByte = (tileOffset << (byte) ncer.mappingType) * (ncer.getBitDepth() * 8) + partitionOffset;
//                byte[] imageData;
//
//                switch (oamImage.getBitDepth()) {
//                    case 4:
//                        imageData = NCGR.NcgrUtils.convertToTiles4Bpp(ncer.getNCGR());
//                        NCGR.NcgrUtils.convertFromTiles4Bpp(imageData, oamImage, startByte);
//                        break;
//                    case 8:
//                        imageData = NCGR.NcgrUtils.convertToTiles8Bpp(ncer.getNCGR());
//                        NCGR.NcgrUtils.convertFromTiles8Bpp(imageData, oamImage, startByte);
//                        break;
//                }
////                    NCGR.NcgrUtils.convertOffsetToCoordinate(imageData, startByte, cell.getWidth() * cell.getHeight(), image, image.getNumTiles(), (image.getWidth() / 8) / image.getColsPerChunk(), image.getColsPerChunk(), image.getRowsPerChunk(), cell);
////                    NCGR.NcgrUtils.convertFromTiles4BppAlternate(imageData, cell, startByte);
                update = false;
            }

            /**
             * Generates and returns a visual (image) representation of the parent <code>OAM</code> given the parent
             * <code>NCGR</code> providing image data
             *
             * @return a <code>BufferedImage</code>
             */
            public BufferedImage getImage() throws NitroException {
                if (update) {
                    generateImageData();
                }
                return (oamImage!=null)?oamImage.getImage():null;
            }

            public int getPixelValue(int x, int y) {
                return 0;//oamImage.getPixelValue(x, y);
            }

            public int getHeight() {
                return oamImage.getHeight();
            }

            public int getWidth() {
                return oamImage.getWidth();
            }

            @Override
            public boolean equals(Object o) {
                return oamImage.equals(o);
            }

            @Override
            public int hashCode() {
                return oamImage.hashCode();
            }

            public void recolorImage() throws NitroException {
                oamImage.recolorImage();
            }

            @Override
            public String toString() {
                return String.format("%dx%d shadow with tile offset %d of %s", oamImage.getHeight(), oamImage.getWidth(), tileOffset, oamImage.toString());
            }
        }
    }
}
