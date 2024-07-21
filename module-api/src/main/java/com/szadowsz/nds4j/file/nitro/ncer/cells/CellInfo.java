package com.szadowsz.nds4j.file.nitro.ncer.cells;

import com.szadowsz.nds4j.file.nitro.ncer.NCER;
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
        logger.debug("Cell maxX: " + this.maxX);
        this.maxY = pojo.maxY;
        logger.debug("Cell maxY: " + this.maxY);
        this.minX = pojo.minX;
        logger.debug("Cell minX: " + this.minX);
        this.minY = pojo.minY;
        logger.debug("Cell minY: " + this.minY);
        oams = new OAM[pojo.nAttribs];
        for (int i = 0; i < oams.length; i++) {
            oams[i] = new OAM();
            setOam(i,pojo.getOamAttrs(i));
        }
        attributes = new CellAttribute();
        setAttributes(pojo.cellAttr);
        this.partitionOffset = partition[0];
        logger.debug("Cell partitionOffset: " + this.partitionOffset);
        this.partitionSize = partition[1];
        logger.debug("Cell partitionSize: " + this.partitionSize);
    }

    public String getName() {
        return name;
    }

    public short getMaxX() {
        return maxX;
    }

    public short getMaxY() {
        return maxY;
    }

    public short getMinX() {
        return minX;
    }

    public short getMinY() {
        return minY;
    }

    public int getOamCount() {
        return oams.length;
    }

    public OAM getOam(int index) {
        return oams[index];
    }


    public int getPartitionOffset(){
        return partitionOffset;
    }

    public int getPartitionSize(){
        return partitionSize;
    }

    private int[] getObjDimensions(int shape, int size) {
        return new int[] {widths[shape][size], heights[shape][size]};
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAttributes(int cellAttrs) {
        attributes.hFlip = ((cellAttrs >> 8) & 1) == 1;
        logger.debug("Cell hFlip=" + this.attributes.hFlip);
        attributes.vFlip = ((cellAttrs >> 9) & 1) == 1;
        logger.debug("Cell vFlip=" + this.attributes.vFlip);
        attributes.hvFlip = ((cellAttrs >> 10) & 1) == 1;
        logger.debug("Cell hvFlip=" + this.attributes.hvFlip);
        attributes.boundingRectangle = ((cellAttrs >> 11) & 1) == 1;
        logger.debug("Cell boundingRectangle=" + this.attributes.boundingRectangle);
        attributes.boundingSphereRadius = cellAttrs & 0x3F;
        logger.debug("Cell boundingSphereRadius: " + this.attributes.boundingSphereRadius);
    }

    public void setOam(int index, int[] attrs) {
        int attr0 = attrs[0];
        int attr1 = attrs[1];
        int attr2 = attrs[2];

        // Obj 0
        oams[index].yCoord = attr0 & 0xFF; // Bits 0-7 -> signed
        logger.debug("Oam @ " + index + " yCoord: " + oams[index].yCoord);

        oams[index].rotation = (attr0 >> 8) == 1; // Bit 8 -> Rotation / Scale flag
        logger.debug("Oam @ " + index + " rotation=" + oams[index].rotation);

        if (oams[index].rotation) {
            oams[index].doubleSize = ((attr0 >> 9) & 1); // Bit 9 -> if rotation
            logger.debug("Oam @ " + index + " doubleSize: " + oams[index].doubleSize);
        } else {
            oams[index].objDisable = ((attr0 >> 9) & 1); // Bit 9 -> if !rotation
            logger.debug("Oam @ " + index + " objDisable: " + oams[index].objDisable);
        }

        oams[index].mode = (attr0 >> 10) & 3; // Bits 10-11 -> 0 = normal; 1 = semi-trans; 2 = window; 3 = invalid
        logger.debug("Oam @ " + index + " mode: " + oams[index].mode);

        oams[index].mosaic = ((attr0 >> 12) & 1); // Bit 12
        logger.debug("Oam @ " + index + " mosaic: " + oams[index].mosaic);

        if (((attr0 >> 13) & 1) == 1) { // Bit 13 -> 0 = 4bit; 1 = 8bit
            oams[index].characterBits = 8;
        } else {
            oams[index].characterBits = 4;
        }
        logger.debug("Oam @ " + index + " characterBits: " + oams[index].characterBits);

        oams[index].shape = ((attr0 >> 14) & 3); // Bit14-15 -> 0 = square; 1 = horizontal; 2 = vertial; 3 = invalid
        logger.debug("Oam @ " + index + " shape: " + oams[index].shape);


        // Obj 1
        oams[index].xCoord = attr1 & 0x1FF;  // Bits 0-8 (unsigned)
        logger.debug("Oam @ " + index + " xCoord: " + oams[index].xCoord);
//        if (oams[index].xCoord >= 0x100) { // TODO needed?
//            oams[index].xCoord -= 0x200;
//        }
//        logger.debug("Oam @ " + index + " xCoord: " + oams[index].xCoord);


        if (oams[index].rotation) {
            oams[index].rotationScaling = (attr1 >> 9) & 0x1F;  // Bits 9-13 -> Parameter selection
            logger.debug("Oam @ " + index + " rotationScaling: " + oams[index].rotationScaling);
        } else {
            oams[index].unused = ((attr1 >> 9) & 7); // Bits 9-11

            oams[index].flipX = ((attr1 >> 12) & 1) == 1; // Bit 12
            logger.debug("Oam @ " + index + " flipX=" + oams[index].flipX);

            oams[index].flipY = ((attr1 >> 13) & 1) == 1;  // Bit 13
            logger.debug("Oam @ " + index + " flipY=" + oams[index].flipY);
        }

        oams[index].size = (attr1 >> 14) & 3; // Bits 14-15
        logger.debug("Oam @ " + index + " size: " + oams[index].size);

        // Obj 2
        oams[index].tileOffset = attr2 & 0x3FF; // Bits 0-9
        logger.debug("Oam @ " + index + " tileOffset: " + oams[index].tileOffset);

        oams[index].priority = (attr2 >> 10) & 0x3; // Bits 10-11
        logger.debug("Oam @ " + index + " priority: " + oams[index].priority);

        oams[index].palette = (attr2 >> 12) & 0xF; // Bits 12-15
        logger.debug("Oam @ " + index + " palette: " + oams[index].palette);

        int[] dims = getObjDimensions(oams[index].shape,  oams[index].size);
        oams[index].width = dims[0];
        oams[index].height = dims[1];
        logger.debug("Oam @ " + index + " width: " + oams[index].width);
        logger.debug("Oam @ " + index + " height: " + oams[index].height);
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
        int objDisable;
        int mode;
        int mosaic;
        //int colors;
        int characterBits;
        int shape;
        int doubleSize;

        // attr1
        int xCoord;
        int rotationScaling;
        int unused;
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

        public boolean getObjDisable() {
            return objDisable == 1;
        }

        public boolean getDoubleSize() {
            return doubleSize == 1;
        }

        public int getDoubleSizeAsInt() {
            return doubleSize;
        }
    }
}
