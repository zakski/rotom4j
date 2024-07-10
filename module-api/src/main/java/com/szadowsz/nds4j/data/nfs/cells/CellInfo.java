package com.szadowsz.nds4j.data.nfs.cells;

import com.szadowsz.nds4j.file.nitro.NCER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An individual "Cell", or "Bank" within an NCER.
 * In theory, this represents one assembled image.
 */
public class CellInfo {
    private static final Logger logger = LoggerFactory.getLogger(CellInfo.class);

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
     */
    public CellInfo(NCER ncer, CellPojo pojo, int[] partition) {
        this.ncer = ncer;
        this.maxX = pojo.maxX;
        logger.info("Cell maxX: " + this.maxX);
        this.maxY = pojo.maxY;
        logger.info("Cell maxY: " + this.maxY);
        this.minX = pojo.minX;
        logger.info("Cell minX: " + this.minX);
        this.minY = pojo.minY;
        logger.info("Cell minY: " + this.minY);
        oams = new OAM[pojo.nAttribs];
        for (int i = 0; i < oams.length; i++) {
            oams[i] = new OAM();
            setOam(i,pojo.getOamAttrs(i));
        }
        attributes = new CellAttribute();
        setAttributes(pojo.cellAttr);
        this.partitionOffset = partition[0];
        logger.info("Cell partitionOffset: " + this.partitionOffset);
        this.partitionSize = partition[1];
        logger.info("Cell partitionSize: " + this.partitionSize);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAttributes(int cellAttrs) {
        attributes.hFlip = ((cellAttrs >> 8) & 1) == 1;
        logger.info("Cell hFlip=" + this.attributes.hFlip);
        attributes.vFlip = ((cellAttrs >> 9) & 1) == 1;
        logger.info("Cell vFlip=" + this.attributes.vFlip);
        attributes.hvFlip = ((cellAttrs >> 10) & 1) == 1;
        logger.info("Cell hvFlip=" + this.attributes.hvFlip);
        attributes.boundingRectangle = ((cellAttrs >> 11) & 1) == 1;
        logger.info("Cell boundingRectangle=" + this.attributes.boundingRectangle);
        attributes.boundingSphereRadius = cellAttrs & 0x3F;
        logger.info("Cell boundingSphereRadius: " + this.attributes.boundingSphereRadius);
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
        logger.info("Oam @ " + index + " xCoord: " + oams[index].xCoord);
        oams[index].yCoord = attr0 & 0xFF; // bits 0-7
        logger.info("Oam @ " + index + " yCoord: " + oams[index].yCoord);
        oams[index].shape = attr0 >> 14; // bits 14-15
        logger.info("Oam @ " + index + " shape: " + oams[index].shape);
        oams[index].size = attr1 >> 14;
        logger.info("Oam @ " + index + " size: " + oams[index].size);

        int[] dims = CellGetObjDimensions(oams[index].shape,  oams[index].size);
        oams[index].width = dims[0];
        oams[index].height = dims[1];
        logger.info("Oam @ " + index + " width: " + oams[index].width);
        logger.info("Oam @ " + index + " height: " + oams[index].height);

        oams[index].tileOffset = attr2 & 0x3FF;
        logger.info("Oam @ " + index + " tileOffset: " + oams[index].tileOffset);
        oams[index].priority = (attr2 >> 10) & 0x3;
        logger.info("Oam @ " + index + " priority: " + oams[index].priority);
        oams[index].palette = (attr2 >> 12) & 0xF;
        logger.info("Oam @ " + index + " palette: " + oams[index].palette);
        oams[index].mode = (attr0 >> 10) & 3; //bits 10-11
        logger.info("Oam @ " + index + " mode: " + oams[index].mode);
        oams[index].mosaic = ((attr0 >> 12) & 1); //bit 12
        logger.info("Oam @ " + index + " mosaic: " + oams[index].mosaic);

        oams[index].rotation = (attr0 >> 8) == 1; //bit 8
        logger.info("Oam @ " + index + " rotation=" + oams[index].rotation);
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
        logger.info("Oam @ " + index + " flipX=" + oams[index].flipX);
        logger.info("Oam @ " + index + " flipY=" + oams[index].flipY);
        logger.info("Oam @ " + index + " doubleSize: " + oams[index].doubleSize);
        logger.info("Oam @ " + index + " sizeDisable: " + oams[index].sizeDisable);
        logger.info("Oam @ " + index + " rotationScaling: " + oams[index].rotationScaling);
        boolean is8 = ((attr0 >> 13) & 1) == 1;
        oams[index].characterBits = 4;
        if (is8) {
            oams[index].characterBits = 8;
        }
        logger.info("Oam @ " + index + " characterBits: " + oams[index].characterBits);
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

        public int getX() {
            return xCoord;
        }

        public int getY() {
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

        public boolean getSizeDisable() {
            return sizeDisable == 1;
        }

        public boolean getDoubleSize() {
            return doubleSize == 1;
        }

        public int getDoubleSizeAsInt() {
            return doubleSize;
        }
    }
}
