/*
 * Copyright (c) 2023 Turtleisaac.
 *
 * This file is part of Nds4j.
 *
 * Nds4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nds4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nds4j. If not, see <https://www.gnu.org/licenses/>.
 */

package com.szadowsz.nds4j.file.nitro;

import com.szadowsz.nds4j.NFSFactory;
import com.szadowsz.nds4j.compression.CompFormat;
import com.szadowsz.nds4j.data.NFSFormat;
import com.szadowsz.nds4j.data.nfs.cells.CellInfo;
import com.szadowsz.nds4j.data.nfs.cells.CellPojo;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.reader.MemBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

/**
 * An object representation of an NCER file
 */
public class NCER extends GenericNFSFile {
    private static final Logger logger = LoggerFactory.getLogger(NCER.class);

    public static final int GX_OBJVRAMMODE_CHAR_2D = 0x000000;
    public static final int GX_OBJVRAMMODE_CHAR_1D_32K = 0x000010;
    public static final int GX_OBJVRAMMODE_CHAR_1D_64K = 0x100010;
    public static final int GX_OBJVRAMMODE_CHAR_1D_128K = 0x200010;
    public static final int GX_OBJVRAMMODE_CHAR_1D_256K = 0x300010;

    public static final int[] mappingModes = {
            GX_OBJVRAMMODE_CHAR_1D_32K,
            GX_OBJVRAMMODE_CHAR_1D_64K,
            GX_OBJVRAMMODE_CHAR_1D_128K,
            GX_OBJVRAMMODE_CHAR_1D_256K,
            GX_OBJVRAMMODE_CHAR_2D
    };

    //format is (width, height)
    public static final int[][][] oamSize = new int[][][] {
            { // square
                    {8, 8}, {16, 16}, {32, 32}, {64, 64}
            },
            { // horizontal
                    {16, 8}, {32, 8}, {32, 16}, {64, 32}
            },
            { // vertical
                    {8, 16}, {8, 32}, {16, 32}, {32, 64}
            }
    };

    // CEBK (CEll BanK) section
    private String cebkId;
    private long cebkSectionSize;
    private int cebkNumBanks;
    private int cebkBankType;
    private long cebkBankDataOffset;
    private int cebkMappingType;
    private int cebkMappingMode;
    private int cebkPartitionDataOffset;
    private int cebkUnused;
    private int cebkTacuOffset;
    private int cebkMaxPartitionSize;
    private int cebkFirstPartitionDataOffset;

    private boolean vramTransfer;
    private boolean tacu;

    private CellPojo[] cellPojos;
    private CellInfo[] cells;

    private NCGR ncgr;

    private String lablID;
    private int lablSectionSize;
    private byte[] labl;

    private String uextID;
    private int uextSectionSize;
    private int uextUnknown;


    /**
     * Generates an object representation of an NCER file
     * @param data a <code>byte[]</code> representation of an NCER file
     */
    public NCER(String path, String name, CompFormat comp, byte[] compData, byte[] data) throws NitroException {
        super(NFSFormat.NCER, path, name, comp, compData, data);

        MemBuf dataBuf = MemBuf.create(data);
        MemBuf.MemBufReader reader = dataBuf.reader();
        int fileSize = dataBuf.writer().getPosition();

        readGenericNtrHeader(reader);

        File[] ncgrs = new File(path).getParentFile().listFiles(f -> (f.getName().endsWith(".NCGR") ||
                f.getName().endsWith(".NCBR")) &&
                f.getName().substring(0, f.getName().lastIndexOf('.')).equals(this.fileName));
        if (ncgrs.length > 0) {
            this.ncgr = NCGR.fromFile(ncgrs[0]);
        }

        // reader position is now 0x10
        readFile(reader);
    }

    private void readCells(MemBuf.MemBufReader reader) {
        cellPojos = new CellPojo[cebkNumBanks];
        cells = new CellInfo[cebkNumBanks];
        for (int i = 0; i < cebkNumBanks; i++) {
            int cellCount = reader.readUInt16();
            int cellAttr = reader.readUInt16();
            CellPojo cell = cellPojos[i] = new CellPojo(cellCount,cellAttr);
            int cellOffset = reader.readInt();
            System.out.println("CELL COUNT: " + cellCount);
            if (cellCount != 0) {
                int storedPos = reader.getPosition();
                if (cebkBankType == 0) {
                    reader.setPosition(reader.getPosition() + (cebkNumBanks - (i + 1)) * 8 + cellOffset);
                } else {
                    reader.setPosition(reader.getPosition() + (cebkNumBanks - (i + 1)) * 0x10 + cellOffset);
                }
                for (int j = 0; j < cellCount; j++) {
                    cell.setOamAttrs(j, reader.readShort(), reader.readShort(), reader.readShort());
                }
                reader.setPosition(storedPos);
                if (cebkBankType == 1) {
                    cell.setBounds(reader.readShort(),reader.readShort(),reader.readShort(),reader.readShort());
                }
            } else {
                // Provide at least one OAM attribute for empty cells
                cell.setEmptyAttributes(); // Disable rendering
            }
        }
    }

    private int[][] readVramTransfer(MemBuf.MemBufReader reader) {
        int[][] partitionData = new int[cebkNumBanks][2];
        if (vramTransfer) {
            reader.setPosition(NTR_HEADER_SIZE + cebkPartitionDataOffset + 8);
            cebkMaxPartitionSize = reader.readInt();
            cebkFirstPartitionDataOffset = reader.readInt();
            reader.skip(cebkFirstPartitionDataOffset - 8);
            for (int i= 0; i < cebkNumBanks; i++) {
                int partitionOffset = reader.readInt();
                int partitionSize = reader.readInt();
                partitionData[i] = new int[] {partitionOffset, partitionSize};
            }
        }
        return partitionData;
    }

    private void readCellBank(MemBuf.MemBufReader reader) throws NitroException {
        //cell bank data
        cebkId = reader.readString(4); // 0x10
        if (!cebkId.equals("KBEC")) {
            throw new NitroException("Not a valid NCER file.");
        }
        cebkSectionSize = reader.readUInt32(); // 0x14

        cebkNumBanks = reader.readUInt16(); // 0x18
        cebkBankType = reader.readUInt16(); // 0x1A // 1 - with bounding rectangle, 0 - without

        cebkBankDataOffset = reader.readUInt32(); // 0x1C

        cebkMappingType = (int) (reader.readUInt32() & 0xFF); // 0x20
        if (cebkMappingType < 5) {
            cebkMappingMode = mappingModes[cebkMappingType];
        } else {
            cebkMappingMode = GX_OBJVRAMMODE_CHAR_1D_32K;
        }
        readCells(reader);

        // Check for VRAM transfer
        cebkPartitionDataOffset = reader.readInt(); // 0x24
        vramTransfer = cebkPartitionDataOffset != 0;

        cebkUnused = reader.readInt();
        cebkTacuOffset = reader.readInt();
        tacu = cebkTacuOffset != 0;

        int[][] partitionData = readVramTransfer(reader);

        for (int i = 0; i < cebkNumBanks;i++){
            CellPojo pojo = cellPojos[i];
            int[] partition = partitionData[i];
            cells[i] = new CellInfo(this,pojo,partition);
        }
        if (tacu) {
            logger.warn("TACU NOT IMPLEMENTED");
        }
    }

    private void readLabels(MemBuf.MemBufReader reader) {
        //label data
        lablID = reader.readString(4); // 0x10
        if (!lablID.equals("LBAL")) {
            throw new RuntimeException("Not a valid RECN file.");
        }
        lablSectionSize = reader.readInt() - 8;
        labl = reader.readBytes(lablSectionSize);
//        long[] stringOffsets = new long[cebkNumBanks + 1];
//        stringOffsets[stringOffsets.length - 1] = lablSectionSize - (4L *cebkNumBanks);
//        for (int i = 0; i < cebkNumBanks; i++) {
//            long offset = reader.readUInt32();
//            if (offset >= lablSectionSize) {
//                reader.setPosition(reader.getPosition() - 4);
//                offset = -1;
//            }
//            stringOffsets[i] = offset;
//        }
//
//        for (int i = 0; i < stringOffsets.length - 1; i++) {
//            if (stringOffsets[i] != -1) {
//                cells[i].setName(reader.readString((int) (stringOffsets[i+1] - stringOffsets[i])).trim());
//            }
//        }
    }

    @Override
    protected void readFile(MemBuf.MemBufReader reader) throws NitroException {
        boolean labelEnabled = numBlocks != 1;
        readCellBank(reader);
        if (!labelEnabled)
            return;

        reader.setPosition(NTR_HEADER_SIZE + cebkSectionSize);
        readLabels(reader);

        //uext data
        uextID = reader.readString(4); // (note: this isn't guaranteed to be 4-byte aligned)

        if (!uextID.equals("TXEU")) {
            throw new RuntimeException("Not a valid RECN file.");
        }

        uextSectionSize = reader.readInt();
        uextUnknown = reader.readInt();
    }

    /**
     * Sets the parent <code>NCGR</code> used to display image data from this <code>CellBank</code>
     * @param image a <code>NCGR</code>
     */
    public void setNCGR(NCGR image) {
        this.ncgr = image;
    }

    public void setNCLR(NCLR image) {
        this.ncgr.setPalette(image);
    }

    public NCGR getNCGR(){
        return ncgr;
    }

    public NCLR getNCLR(){
        return (ncgr !=null)? ncgr.getNCLR():NCLR.DEFAULT;
    }

    public int getBitDepth(){
        return ncgr.getBitDepth();
    }

    public int getCellsCount(){
        return cellPojos.length;
    }

    /**
     * Generates an object representation of an NCER file from a file on disk
     *
     * @param path a <code>String</code> containing the path to a NCER file on disk
     * @return an <code>NCER</code> object
     */
    public static NCER fromFile(String path) throws NitroException {
        return fromFile(new File(path));
    }

    /**
     * Generates an object representation of an NCER file from a file on disk
     *
     * @param file a <code>File</code> containing the path to a NCER file on disk
     * @return an <code>NCER</code> object
     */
    public static NCER fromFile(File file) throws NitroException {
        return (NCER) NFSFactory.fromFile(file);
    }

    private int calcByteBoundary(int m){
        return (1<<((((m)>>20)&0x7)+5));
    }

    private int calcCHName(int x, int m, int b){
        return (calcByteBoundary(m)*(x)/((b)<<3));
    }

    private void renderCellObj(CellInfo.OAM info, int mapping, CellInfo vramTransfer, Color[] out, int[] width, int[] height, boolean checker) {
        width[0] = info.getWidth();
        height[0] = info.getHeight();

        int tilesX = width[0] / 8;
        int tilesY = height[0] / 8;

        if (ncgr != null) {
            int charSize = ncgr.getBitDepth() * 8;
            int ncgrStart = calcCHName(info.getOffset(), mapping, ncgr.getBitDepth());
            for (int y = 0; y < tilesY; y++) {
                for (int x = 0; x < tilesX; x++) {
                    Color[] block = new Color[64];

                    int bitsOffset = x * 8 + (y * 8 * tilesX * 8);
                    int index;
                    if (NCGR.calcIsNCGR2D(mapping)) {
                        int ncx = x + ncgrStart % ncgr.getTileWidth();
                        int ncy = y + ncgrStart / ncgr.getTileWidth();
                        index = ncx + ncgr.getTileWidth() * ncy;
                    } else {
                        index = ncgrStart + x + y * tilesX;
                    }

                    ncgr.chrRenderCharacterTransfer(index, vramTransfer, block, info.getPalette(), true);
                    for (int i = 0; i < 8; i++) {
                        System.arraycopy(block, i * 8, out, bitsOffset + tilesX * 8 * i, 8);
                    }
                }
            }
        }

        // render checker
//        if (checker) {
//            for (int i = 0; i < info.getWidth() * info.getHeight(); i++) {
//                int x = i % info.getWidth();
//                int y = i / info.getWidth();
//                int ch = ((x >> 2) ^ (y >> 2)) & 1;
//                int c = out[i];
//                if ((c & 0xFF000000) == 0) {
//                    out[i] = ch != 0 ? 0xFFFFFF : 0xC0C0C0;
//                }
//            }
//        }
    }

//    private BufferedImage renderCellObj(CellInfo.OAM info, int mapping, BufferedImage out,  boolean checker) {
//	    BufferedImage image = new BufferedImage(info.getWidth(),info.getHeight(),BufferedImage.TYPE_INT_RGB);
//        int tilesX = image.getWidth() / 8;
//        int tilesY = image.getHeight() / 8;
//
//        if (ncgr != null) {
//            int charSize = ncgr.getBitDepth() * 8;
//            int ncgrStart = calcCHName(info.getOffset(), mapping, ncgr.getBitDepth());
//            for (int y = 0; y < tilesY; y++) {
//                for (int x = 0; x < tilesX; x++) {
//                    int bitsOffset = x * 8 + (y * 8 * tilesX * 8);
//                    int index;
//                    if (calcIsNCGR2D(mapping)) {
//                        int ncx = x + ncgrStart % ncgr.getTileWidth();
//                        int ncy = y + ncgrStart / ncgr.getTileWidth();
//                        index = ncx + ncgr.getTileWidth() * ncy;
//                    } else {
//                        index = ncgrStart + x + y * tilesX;
//                    }
//
//                    ncgr.renderCharacterTransfer(index, info, image, vramTransfer,true);
//                    for (int i = 0; i < 8; i++) {
//                        memcpy(out + bitsOffset + tilesX * 8 * i, block + i * 8, 32);
//                    }
//                }
//            }
//        }
//
//        //render checker
//        if (checker) {
//            for (int i = 0; i < info.getWidth() * info.getHeight(); i++) {
//                int x = i % info.getWidth();
//                int y = i / info.getWidth();
//                int ch = ((x >> 2) ^ (y >> 2)) & 1;
//                Color c = out[i];
//                if ((c & 0xFF000000) == 0) {
//                    out[i] = ch ? 0xFFFFFF : 0xC0C0C0;
//                }
//            }
//        }
//    }

//    public BufferedImage renderCell(Color[] px, CellInfo cell, int xOffs, int yOffs){
//        //BufferedImage block = new BufferedImage(64,64,BufferedImage.TYPE_INT_RGB);
//        Color[] block = new Color[64 * 64];
//        for (int i = cell.getOams().length - 1; i >= 0; i--) {
//            CellInfo.OAM info = cell.getOams()[i];
//            int[] entryWidth = new int[1];
//            int[] entryHeight = new int[1];
//            renderCellObj(info, cebkMappingMode, cell, block,entryWidth,entryHeight,false);
//
//            //HV flip? Only if not affine!
//            if (!info.getRotationScaling()) {
//                int[] temp = new int[64];
//                if (info.getFlipY()) {
//                    for (int j = 0; j < info.getHeight() / 2; j++) {
//                        System.arraycopy(block, j * info.getWidth(), temp, 0, info.getWidth());
//                        System.arraycopy(block, (info.getHeight() - 1 - j) * info.getWidth(), block, j * info.getWidth(), info.getWidth());
//                        System.arraycopy(temp, 0, block, (info.getHeight() - 1 - j) * info.getWidth(), info.getWidth());
//                    }
//                }
//                if (info.getFlipX()) {
//                    for (int j = 0; j < info.getWidth() / 2;j++) {
//                        for (int k = 0; k < info.getHeight(); k++) {
//                            Color left = block[j + k * info.getWidth()];
//                            block[j + k * info.getWidth()] = block[info.getWidth() - 1 - i + j * info.getWidth()];
//                            block[info.getWidth() - 1 - j + k * info.getWidth()] = left;
//                        }
//                    }
//                }
//            }
//
//            if (!info.getSizeDisable()) {
//                int x = info.getX();
//                int y = info.getY();
//                //adjust for double size
//                if (info.getDoubleSize()) {
//                    x += info.getWidth() / 2;
//                    y += info.getHeight() / 2;
//                }
//                //copy data
//                if (!info.getRotationScaling()) {
//                    for (int j = 0; j < info.getHeight(); j++) {
//                        int _y = (y + j + yOffs) & 0xFF;
//                        for (int k = 0; k < info.getWidth(); k++) {
//                            int _x = (x + k + xOffs) & 0x1FF;
//                            Color col = block[j * info.getWidth() + k];
//                            if (col.getRGB() >>> 24 != 0) {
//                                 px[_x + _y * 512] = block[j * info.getWidth() + k];
//                            }
//                        }
//                    }
//                } else {
//                    //transform about center
//                    int realWidth = info.getWidth() << info.getDoubleSizeAsInt();
//                    int realHeight = info.getHeight() << info.getDoubleSizeAsInt();
//                    int cx = realWidth / 2;
//                    int cy = realHeight / 2;
//                    int realX = x - (realWidth - info.getWidth()) / 2;
//                    int realY = y - (realHeight - info.getHeight()) / 2;
//                    for (int j = 0; j < realHeight; j++) {
//                        int destY = (realY + j + yOffs) & 0xFF;
//                        for (int k = 0; k < realWidth; k++) {
//                            int destX = (realX + k + xOffs) & 0x1FF;
//
//                            int srcX = (int) ((k - cx) * 1.0 + (j - cy) * 1.0) + cx;
//                            int srcY = (int) ((k - cx) * 1.0 + (j - cy) * 1.0) + cy;
//
//                            if (info.getDoubleSize()) {
//                                srcX -= realWidth / 4;
//                                srcY -= realHeight / 4;
//                            }
//                            if (srcX >= 0 && srcY >= 0 && srcX < info.getWidth() && srcY < info.getHeight()) {
//                                Color src = block[srcY * info.getWidth() + srcX];
//                                if (src.getRGB() >>> 24 != 0)  px[destX + destY * 512] = src;
//                            }
//
//                        }
//                    }
//                }
//            }
//        }
//        return block;
//    }

    public CellInfo getCell(int index){
        return cells[index];
    }

    public BufferedImage getImage(int cellNum, int oamNum) {
        CellInfo cell = cells[cellNum];
        CellInfo.OAM info = cell.getOam(oamNum);
        int[] entryWidth = new int[1];
        int[] entryHeight = new int[1];

        Color[] bits = new Color[info.getWidth() * info.getHeight()];
        renderCellObj(info, cebkMappingMode, cell, bits, entryWidth,entryHeight,false);
        return createBitmap(entryWidth[0], entryHeight[0], bits);
    }

    private BufferedImage createBitmap(int width, int height, Color[] bits) {
        int pos = 0;
        BufferedImage image = new BufferedImage(width,height,TYPE_INT_RGB);
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                Color color = bits[pos];
                pos++;
                image.setRGB(w, h, color.getRGB());
            }
        }
        return image;
    }
}
