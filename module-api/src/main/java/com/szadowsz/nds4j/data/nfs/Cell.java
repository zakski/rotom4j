package com.szadowsz.nds4j.data.nfs;

import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.NCER;
import com.szadowsz.nds4j.file.nitro.NCGR;

import java.awt.image.BufferedImage;

/**
 * An individual "Cell", or "Bank" within an NCER.
 * In theory, this represents one assembled image.
 */
public class Cell {
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
    public Cell(NCER ncer, int oamCount, int partitionOffset, int partitionSize, int tacuData) {
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

    public OAM.OamImage[] getImages() throws NitroException {
        int[] index = null;

        OAM.OamImage[] images = new OAM.OamImage[oams.length];

        for (int i = 0; i < oams.length; i++) {
            OAM oam = oams[i];

            if (oam == null)
                break;

            images[i] = oam.getImage(i, index);
        }

        return images;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTacuData() {
        return tacuData;
    }

    public void setTacuData(int tacuData) {
        this.tacuData = tacuData;
    }

    public CellAttribute getAttributes() {
        return attributes;
    }

    public void setAttributes(CellAttribute attributes) {
        this.attributes = attributes;
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

    public void setOams(OAM[] oams) {
        this.oams = oams;
    }

    public void setOam(int index, int yCoord, byte attr0, short attr1, short attr2) {
        oams[index].yCoord = yCoord; // bits 0-7
        oams[index].rotation = (attr0 & 1) == 1; //bit 8
        oams[index].sizeDisable = ((attr0 >> 1) & 1) == 1; //bit 9 Obj Size (if rotation) or Obj Disable (if not rotation)
        oams[index].mode = (attr0 >> 2) & 3; //bits 10-11
        oams[index].mosaic = ((attr0 >> 4) & 1) == 1; //bit 12
        oams[index].colors = ((attr0 >> 5) & 1) == 0 ? 16 : 256; //bit 13
        oams[index].shape = (attr0 >> 6) & 3; //bits 14-15

        oams[index].xCoord = (attr1 & 0x01ff) >= 0x100 ? (attr1 & 0x01ff) - 0x200 : (attr1 & 0x01ff);
        oams[index].rotationScaling = (attr1 >> 9) & 0x1F;
        oams[index].size = (attr1 >> 14) & 3;

        oams[index].tileOffset = attr2 & 0x3FF;
        oams[index].priority = (attr2 >> 10) & 3;
        oams[index].palette = (attr2 >> 12) & 0xF;
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

    public CellImage getImage() throws NitroException {
        return new CellImage();
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
        boolean sizeDisable;
        int mode;
        boolean mosaic;
        int colors;
        int shape;

        // attr1
        int xCoord;
        int rotationScaling;
        int size;

        // attr2
        int tileOffset;
        int priority;
        int palette;

        public int getCoordX(){
            return xCoord;
        }

        public int getCoordY(){
            return yCoord;
        }

        public int getShape() {
            return shape;
        }

        public int getSize() {
            return size;
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

    /**
     * This is a visual representation of a given <code>Cell</code> within its parent NCGR (<code>NCGR</code>).
     */
    public class CellImage {
        private NCGR cellImage;
        private boolean update;
        private OAM.OamImage[] oamImages;

        private CellImage() throws NitroException {
            generateImageData();
        }

        private void generateImageData() throws NitroException {
           // cellImage = new NCGR(maxY - minY + 1, maxX - minX + 1, ncer.getBitDepth(), ncer.getNCLR());

            int startX;
            int startY;
            oamImages = getImages();

            for (int i = 0; i < oamImages.length; i++) {
                OAM oam = oams[i];
                startX = oam.xCoord + cellImage.getWidth() / 2;
                startY = oam.yCoord + cellImage.getHeight() / 2;

                for (int row = 0; row < oamImages[i].getHeight(); row++) {
                    for (int col = 0; col < oamImages[i].getWidth(); col++) {
                        //cellImage.setPixelValue(startX + col, startY + row, oamImages[i].getPixelValue(col, row));
                    }
                }
            }

            update = false;
        }

        public BufferedImage getImage() throws NitroException {
            if (update) {
                generateImageData();
            }
            return cellImage.getImage();
        }
    }
}
