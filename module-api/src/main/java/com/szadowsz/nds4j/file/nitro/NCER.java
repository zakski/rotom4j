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
import com.szadowsz.nds4j.data.ComplexImageable;
import com.szadowsz.nds4j.data.NFSFormat;
import com.szadowsz.nds4j.data.nfs.cells.CellInfo;
import com.szadowsz.nds4j.data.nfs.cells.CellPojo;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.reader.MemBuf;
import com.szadowsz.nds4j.utils.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * An object representation of an NCER file
 */
public class NCER extends GenericNFSFile implements ComplexImageable {
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

    protected static final int BACKGROUND_WIDTH = 512;
    protected static final int BACKGROUND_HEIGHT = 256;

    private NCGR ncgr;

    // CEBK (CEll BanK) section
    private String cebkId;
    private long cebkSectionSize;
    private int cebkNumCells;
    private int cebkBankType;
    private long cebkDataOffset;
    private int cebkMappingType;
    private int cebkMappingMode;
    private int cebkPartitionDataOffset;
    private byte[] cebkUnused;
    private int cebkTacuOffset;
    private int cebkMaxPartitionSize;
    private int cebkFirstPartitionDataOffset;

    private boolean vramTransfer;

    private CellPojo[] cellPojos;
    private CellInfo[] cells;

    // LABL section
    private String lablID;
    private long lablSectionSize;
    private byte[] labels;

    // UEXT section
    private String uextID;
    private long  uextSectionSize;
    private byte[] uextData;

    public NCER(String path, String fileName, CompFormat comp, byte[] compData, byte[] data) throws NitroException {
        super(NFSFormat.NCER, path, fileName, comp, compData, data);
        MemBuf dataBuf = MemBuf.create(data);
        MemBuf.MemBufReader reader = dataBuf.reader();
        int fileSize = dataBuf.writer().getPosition();
        logger.debug("\nNCER file, " + fileName + ", initialising with size of " + fileSize + " bytes");

        readGenericNtrHeader(reader);

        File[] ncgrs = new File(path).getParentFile().listFiles(f -> (f.getName().endsWith(".NCGR") ||
                f.getName().endsWith(".NCBR")) &&
                f.getName().substring(0, f.getName().lastIndexOf('.')).equals(this.fileName));
        if (ncgrs.length > 0) {
            logger.debug("Found corresponding NCGR file, " + ncgrs[0]);
            this.ncgr = NCGR.fromFile(ncgrs[0]);
            logger.debug("Read NCGR file\n");
        }

        // reader position is now 0x10
        readFile(reader);
    }

    private void readCells(MemBuf.MemBufReader reader, int perCellDataSize) {
        cellPojos = new CellPojo[cebkNumCells];
        cells = new CellInfo[cebkNumCells];
        int start = reader.getPosition();
        for (int i = 0; i < cebkNumCells; i++) {
            reader.setPosition(start + i*perCellDataSize);
            logger.debug("Cell @ " + i +" index @ " + reader.getPosition());

            int nOAMEntries = reader.readUInt16();
            logger.debug("Cell @ " + i + " OAM entry count: " + nOAMEntries);
            int cellAttr = reader.readUInt16();
            logger.debug("Cell @ " + i + " Cell attribute: " + cellAttr);
            int attrOffset = reader.readInt();
            logger.debug("Cell @ " + i + " Cell attribute offset: " + attrOffset);

            CellPojo cell = cellPojos[i] = new CellPojo(nOAMEntries, cellAttr);

            if (nOAMEntries != 0) {
                int storedPos = reader.getPosition();
                reader.setPosition(start + cebkNumCells*perCellDataSize + attrOffset);
                logger.debug("Cell @ " + i + " Oam pos: " + reader.getPosition());
                for (int j = 0; j < nOAMEntries; j++) {
                    cell.setOamAttrs(j, reader.readUInt16(), reader.readUInt16(), reader.readUInt16());
                }
                logger.debug("Cell @ " + i +" oam read index @ " + reader.getPosition());
                reader.setPosition(storedPos);
                if (cebkBankType == 1) {
                    cell.setBounds(reader.readShort(), reader.readShort(), reader.readShort(), reader.readShort());
                }
            } else {
                // Provide at least one OAM attribute for empty cells
                cell.setEmptyAttributes(); // Disable rendering
            }
        }
        logger.debug("Cell read end index @ " + reader.getPosition());
    }

    private void readCellBank(MemBuf.MemBufReader reader) throws NitroException {
        //cell bank data
        int cebkPos = reader.getPosition();

        cebkId = reader.readString(4); // 0x10
        if (!cebkId.equals("KBEC")) {
            throw new NitroException("Not a valid NCER file.");
        }
        cebkSectionSize = reader.readUInt32(); // 0x14
        logger.debug("CEBK section size " + cebkSectionSize + " bytes");

        cebkNumCells = reader.readUInt16(); // 0x18
        logger.debug("CEBK cell count " + cebkNumCells);

        cebkBankType = reader.readUInt16(); // 0x1A // 1 - with bounding rectangle, 0 - without
        int perCellDataSize = 8;
        if (cebkBankType == 1) {
            logger.debug("With Bounding Rectangle");
            perCellDataSize += 8;
        } else {
            logger.debug("Without Bounding Rectangle");
        }

        cebkDataOffset = reader.readUInt32(); // 0x1C
        logger.debug("CEBK data offset " + cebkDataOffset + " bytes");
        logger.debug("CEBK data start " + (cebkPos + cebkDataOffset + 8) + " bytes");

        cebkMappingType = (int) (reader.readUInt32() & 0xFF); // 0x20
        if (cebkMappingType < 5) {
            cebkMappingMode = mappingModes[cebkMappingType];
        } else {
            cebkMappingMode = GX_OBJVRAMMODE_CHAR_1D_32K;
        }
        logger.debug("CEBK mapping type " + cebkBankType);

        logger.debug("CEBK expected data per cell " + perCellDataSize + " bytes");

        // Check for VRAM transfer
        cebkPartitionDataOffset = reader.readInt(); // 0x24
        vramTransfer = cebkPartitionDataOffset != 0;
        logger.debug("vram transfer expected=" + vramTransfer);
        cebkUnused = reader.readBytes(8);

        readCells(reader, perCellDataSize);
    }

    private int[][] readVramTransfer(MemBuf.MemBufReader reader) {
        int[][] partitionData = new int[cebkNumCells][2];
        if (vramTransfer) {
            reader.setPosition(NTR_HEADER_SIZE + cebkPartitionDataOffset + 8);
            cebkMaxPartitionSize = reader.readInt();
            cebkFirstPartitionDataOffset = reader.readInt();
            reader.skip(cebkFirstPartitionDataOffset - 8);
            for (int i = 0; i < cebkNumCells; i++) {
                int partitionOffset = reader.readInt();
                int partitionSize = reader.readInt();
                partitionData[i] = new int[]{partitionOffset, partitionSize};
            }
        }
        return partitionData;
    }

    @Override
    protected void readFile(MemBuf.MemBufReader reader) throws NitroException {
        logger.debug("Read NCER data");
        logger.info("Current index @ " + reader.getPosition());
        int cebk = NnsG2dFindBlockBySignature(reader, "CEBK");
        logger.info("Found CEBK section @ " + cebk);
        int tacu = NnsG2dFindBlockBySignature(reader, "TACU");
        logger.info("Found TACU section @ " + tacu);
        int labl = NnsG2dFindBlockBySignature(reader, "LABL");
        logger.info("Found LABL section @ " + labl);
        int uext = NnsG2dFindBlockBySignature(reader, "UEXT");
        logger.info("Found UEXT section @ " + uext);

        readCellBank(reader); // process CEBK, current index should point to that block already

        int[][] partitionData = readVramTransfer(reader);

        for (int i = 0; i < cebkNumCells; i++) {
            logger.info("Cell @ " + i + " processing attributes");
            CellPojo pojo = cellPojos[i];
            int[] partition = partitionData[i];
            cells[i] = new CellInfo(this, pojo, partition);
        }

        if (labl != -1){
            logger.info("Reading LABL section @ " + labl);
            reader.setPosition(labl);
            lablID = reader.readString(4);
            lablSectionSize = reader.readUInt32();
            logger.info("LABL section size " + lablSectionSize + " bytes");
            int sectionSize = (int) (lablSectionSize-8);
            labels = reader.readBytes(sectionSize);
            logger.info("Labels " + new String(labels, StandardCharsets.UTF_8));
        }

        if (uext != -1) {
            logger.info("Reading UEXT section @ " + uext);
            uextID = reader.readString(4);
            uextSectionSize = reader.readUInt32();
            logger.info("UEXT section size " + uextSectionSize + " bytes");
            int sectionSize = (int) (uextSectionSize-8);
            uextData = reader.readBytes(sectionSize);
        }
    }

    public int getCellsCount(){
        return cebkNumCells;
    }

    @Override
    public int getWidth() {
        return BACKGROUND_WIDTH;
    }

    public int getHeight(){
        return BACKGROUND_HEIGHT;
    }

    public NCGR getNCGR() {
        return ncgr;
    }

    public NCLR getNCLR() {
        return (ncgr != null) ? ncgr.getNCLR() : null;
    }

    public void setNCGR(NCGR ncgr) {
        this.ncgr = ncgr;
    }

    public void setNCLR(NCLR nclr) throws NitroException {
        if (ncgr != null) {
            ncgr.setNCLR(nclr);
        }
    }

    private int calcByteBoundary(int m) {
        int result = (1 << ((((m) >> 20) & 0x7) + 5));
        logger.info("Byte Boundary result " + result);
        return result;
    }

    private int calcCHName(int x, int m, int b) {
        logger.info("Calculating CHName with tile Offset " + x + ", mapping " + m + ", bits " + b);
        return (calcByteBoundary(m) * (x) / ((b) << 3));
    }

    private void renderObj(CellInfo.OAM info, int mapping, CellInfo vramTransfer, int[] out, int[] width, int[] height, boolean checker) {
        width[0] = info.getWidth();
        height[0] = info.getHeight();

        int tilesX = width[0] / 8;
        int tilesY = height[0] / 8;

        if (ncgr != null) {
            int ncgrStart = calcCHName(info.getOffset(), mapping, ncgr.getBitDepth());
            logger.info("NCGR Tile Start = " + ncgrStart);
            for (int y = 0; y < tilesY; y++) {
                for (int x = 0; x < tilesX; x++) {
                    int[] block = new int[64];

                    int bitsOffset = x * 8 + (y * 8 * tilesX * 8);
                    int index;
                    if (NCGR.calcIsNCGR2D(mapping)) {
                        int ncx = x + ncgrStart % ncgr.getTileWidth();
                        int ncy = y + ncgrStart / ncgr.getTileWidth();
                        index = ncx + ncgr.getTileWidth() * ncy;
                    } else {
                        index = ncgrStart + x + y * tilesX;
                    }

                    ncgr.renderTile(index, this.vramTransfer,vramTransfer, block, info.getPalette(), true);
                    for (int i = 0; i < 8; i++) {
                        System.arraycopy(block, i * 8, out, bitsOffset + tilesX * 8 * i, 8);
                    }
                }
            }
        }

        // render checker
        if (checker) {
            for (int i = 0; i < info.getWidth() * info.getHeight(); i++) {
                int x = i % info.getWidth();
                int y = i / info.getWidth();
                int ch = ((x >> 2) ^ (y >> 2)) & 1;
                int c = out[i];
                if ((c & 0xFF000000) == 0) {
                    out[i] = ch != 0 ? 0xFFFFFF : 0xC0C0C0;
                }
            }
        }
    }
        protected void flipCell(CellInfo.OAM info, int[] block) {
        if (!info.getRotationScaling()) {
            int[] temp = new int[64];
            if (info.getFlipY()) {
                for (int j = 0; j < info.getHeight() / 2; j++) {
                    System.arraycopy(block, j * info.getWidth(), temp, 0, info.getWidth());
                    System.arraycopy(block, (info.getHeight() - 1 - j) * info.getWidth(), block, j * info.getWidth(), info.getWidth());
                    System.arraycopy(temp, 0, block, (info.getHeight() - 1 - j) * info.getWidth(), info.getWidth());
                }
            }
            if (info.getFlipX()) {
                for (int j = 0; j < info.getWidth() / 2; j++) {
                    for (int k = 0; k < info.getHeight(); k++) {
                        int left = block[j + k * info.getWidth()];
                        block[j + k * info.getWidth()] = block[info.getWidth() - 1 - j + k * info.getWidth()];
                        block[info.getWidth() - 1 - j + k * info.getWidth()] = left;
                    }
                }
            }
        }
    }

    protected void rotateScaleCell(int[] px, CellInfo.OAM info, int[] block, int xOffs, int yOffs, float a, float b, float c, float d) {
        int x = info.getX();
        int y = info.getY();
        // adjust for double size
        if (info.getDoubleSize()) {
            x += info.getWidth() / 2;
            y += info.getHeight() / 2;
        }
        // copy data
        if (!info.getRotationScaling()) {
            for (int j = 0; j < info.getHeight(); j++) {
                int _y = (y + j + yOffs) & 0xFF;
                for (int k = 0; k < info.getWidth(); k++) {
                    int _x = (x + k + xOffs) & 0x1FF;
                    int col = block[j * info.getWidth() + k];
                    if (col >>> 24 != 0) {
                        px[_x + _y * 512] = block[j * info.getWidth() + k];
                    }
                }
            }
        } else {
            // transform about center
            int realWidth = info.getWidth() << (info.getDoubleSize() ? 1 : 0);
            int realHeight = info.getHeight() << (info.getDoubleSize() ? 1 : 0);
            int cx = realWidth / 2;
            int cy = realHeight / 2;
            int realX = x - (realWidth - info.getWidth()) / 2;
            int realY = y - (realHeight - info.getHeight()) / 2;
            for (int j = 0; j < realHeight; j++) {
                int destY = (realY + j + yOffs) & 0xFF;
                for (int k = 0; k < realWidth; k++) {
                    int destX = (realX + k + xOffs) & 0x1FF;

                    int srcX = (int) ((k - cx) * a + (j - cy) * b) + cx;
                    int srcY = (int) ((k - cx) * c + (j - cy) * d) + cy;

                    if (info.getDoubleSize()) {
                        srcX -= realWidth / 4;
                        srcY -= realHeight / 4;
                    }
                    if (srcX >= 0 && srcY >= 0 && srcX < info.getWidth() && srcY < info.getHeight()) {
                        int src = block[srcY * info.getWidth() + srcX];
                        if (src >>> 24 != 0) px[destX + destY * 512] = src;
                    }
                }
            }
        }
    }

    protected void outlineCell(int[] px, int xOffs, int yOffs, CellInfo.OAM info) {
        int outlineWidth = info.getWidth() << (info.getDoubleSize() ? 1 : 0);
        int outlineHeight = info.getHeight() << (info.getDoubleSize() ? 1 : 0);
        for (int j = 0; j < outlineWidth; j++) {
            int _x = (j + info.getX() + xOffs) & 0x1FF;
            int _y = (info.getY() + yOffs) & 0xFF;
            int _y2 = (_y + outlineHeight - 1) & 0xFF;
            px[_x + _y * 512] = 0xFE000000;
            px[_x + _y2 * 512] = 0xFE000000;
        }
        for (int j = 0; j < outlineHeight; j++) {
            int _x = (info.getX() + xOffs) & 0x1FF;
            int _y = (info.getY() + j + yOffs) & 0xFF;
            int _x2 = (_x + outlineWidth - 1) & 0x1FF;
            px[_x + _y * 512] = 0xFE000000;
            px[_x2 + _y * 512] = 0xFE000000;
        }
    }

    // CellRenderCell
    protected int[] renderCell(int[] px, CellInfo cell, int mapping, int xOffs, int yOffs, boolean checker, int outline, float a, float b, float c, float d) {
        int[] block = new int[64 * 64];
        for (int i = cell.getOamCount() - 1; i >= 0; i--) {
            CellInfo.OAM info = cell.getOam(i);
            int[] entryWidth = new int[1];
            int[] entryHeight = new int[1];

            renderObj(info, mapping, cell, block, entryWidth, entryHeight, false);

            // HV flip? Only if not affine!
            flipCell(info, block);

            if (!info.getObjDisable()) {
                rotateScaleCell(px, info, block, xOffs, yOffs, a, b, c, d);

                // outline
                if (outline == -2 || outline == i) {
                    outlineCell(px, xOffs, yOffs, info);
                }
            }
        }

        // apply checker background
        if (checker) {
            for (int y = 0; y < 256; y++) {
                for (int x = 0; x < 512; x++) {
                    int index = y * 512 + x;
                    if (px[index] >>> 24 == 0) {
                        int p = ((x >> 2) ^ (y >> 2)) & 1;
                        px[index] = (p != 0) ? 0xFFFFFF : 0xC0C0C0;
                    }
                }
            }
        }
        return px;
    }

    public int REVERSE(int x) {
        return ((x)&0xFF00FF00)|(((x)&0xFF)<<16)|(((x)>>16)&0xFF);
    }

    @Override
    public BufferedImage getImage() {
        return getImage(0);
    }

    public BufferedImage getImage(int cellNum) {
        CellInfo cell = cells[cellNum];
        int[] frameBuffer = new int [256 * 512];
        int[] bits = renderCell(frameBuffer, cells[cellNum], cebkMappingMode, 256, 128, false, -1, 1.0f, 0.0f, 0.0f, 1.0f);

        if (Configuration.isShowCellBounds()) {
            int minX = cell.getMinX() + 256, maxX = cell.getMaxX() + 256 - 1;
            int minY = cell.getMinY() + 128, maxY = cell.getMaxY() + 128 - 1;
            minX = minX & 0x1FF;
            maxX = maxX & 0x1FF;
            minY = minY & 0xFF;
            maxY = maxY & 0xFF;

            for (int i = 0; i < 256; i++) {
                if (bits[i * 512 + minX] >> 24 != 0xFE) bits[i * 512 + minX] = 0xFF0000FF;
                if (bits[i * 512 + maxX] >> 24 != 0xFE) bits[i * 512 + maxX] = 0xFF0000FF;
            }
            for (int i = 0; i < 512; i++) {
                if (bits[minY * 512 + i] >> 24 != 0xFE) bits[minY * 512 + i] = 0xFF0000FF;
                if (bits[maxY * 512 + i] >> 24 != 0xFE) bits[maxY * 512 + i] = 0xFF0000FF;
            }
        }

        //draw solid color background if transparency disabled
        if (!Configuration.isRenderTransparent()) {
            int bgColor = 0;
            if (ncgr != null) {
                bgColor = getNCLR().getColor(0).getRGB();
            }
            bgColor = REVERSE(bgColor);
            for (int i = 0; i < 256 * 512; i++) {
                int c = bits[i];
                if ((c >> 24) == 0) {
                    bits[i] = bgColor;
                } else if ((c >> 24) == 0xFE) {
                    bits[i] = ((bgColor + 0x808080) & 0xFFFFFF) | 0xFE000000;
                }
            }
        }

        //draw editor guidelines if enabled
        if (Configuration.isShowGuidelines()) {
            //dotted lines at X=0 an Y=0
            int centerColor = 0xFF0000; //red
            int auxColor = 0x00FF00; //green
            int minorColor = 0x002F00;

            for (int i = 0; i < 512; i++) {
                //major guideline
                int c = bits[i + 128 * 512];
                if ((c >> 24) != 0xFE) {
                    if ((i & 1) == 1) {
                        bits[i + 128 * 512] ^= centerColor;
                    }
                }

                //auxiliary guidelines
                c = bits[i + 64 * 512];
                if ((c >> 24) != 0xFE){
                    if ((i & 1) == 1) {
                        bits[i + 64 * 512] ^= auxColor;
                    }
                }
                c = bits[i + 192 * 512];
                if ((c >> 24) != 0xFE) {
                    if ((i & 1) == 1) {
                        bits[i + 192 * 512] ^= auxColor;
                    }
                }

                //minor guidelines
                for (int j = 0; j < 256; j += 8) {
                    if (j == 64 || j == 128 || j == 192) continue;

                    c = bits[i + j * 512];
                    if ((c >> 24) != 0xFE) {
                        if ((i & 1) == 1) {
                            bits[i + j * 512] ^= minorColor;
                        }
                    }
                }
            }
            for (int i = 0; i < 256; i++) {
                //major guideline
                int c = bits[256 + i * 512];
                if ((c >> 24) != 0xFE) {
                    if ((i & 1) == 1) {
                        bits[256 + i * 512] ^= centerColor;
                    }
                }

                //auxiliary guidelines
                c = bits[128 + i * 512];
                if ((c >> 24) != 0xFE) {
                    if ((i & 1) == 1){
                        bits[128 + i * 512] ^= auxColor;
                    }
                }
                c = bits[384 + i * 512];
                if ((c >> 24) != 0xFE) {
                    if ((i & 1) == 1) bits[384 + i * 512] ^= auxColor;
                }

                //minor guidelines
                for (int j = 0; j < 512; j += 8) {
                    if (j == 128 || j == 256 || j == 384) continue;

                    c = bits[j + i * 512];
                    if ((c >> 24) != 0xFE) {
                        if ((i & 1) == 1){
                            bits[j + i * 512] ^= minorColor;
                        }
                    }
                }
            }
        }
        BufferedImage image = new BufferedImage(BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, bits, 0, BACKGROUND_WIDTH);

        CellInfo.OAM info = cell.getOam(0);
        int[] width = new int[1], height = new int[1];
        bits = new int [info.getWidth() * info.getHeight()];
        renderObj(info, cebkMappingMode, null, bits, width, height, true);

        image.setRGB(512/2 - info.getWidth()/2, 256/2 - info.getHeight()/2, info.getWidth(), info.getHeight(), bits, 0, info.getWidth());


        return image;
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
}
