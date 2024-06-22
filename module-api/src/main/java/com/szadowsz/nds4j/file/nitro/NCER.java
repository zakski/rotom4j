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
import com.szadowsz.nds4j.data.nfs.Cell;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.reader.MemBuf;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * An object representation of an NCER file
 */
public class NCER extends GenericNFSFile {
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

    private Cell[] cells;
    private NCGR ncgr;

    private String lablID;
    private int lablSectionSize;
  
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

    public static int[] getOamSize(Cell.OAM oam) {
        return oamSize[oam.getShape()][oam.getSize()];
    }
    
    private void readCells(MemBuf.MemBufReader reader, int[][] partitionData, int[] cellAttributes) {
        int storedPos;
        cells = new Cell[cebkNumBanks];

        for (int i = 0; i < cebkNumBanks; i++) {
            int cellCount = reader.readUInt16();
            cells[i] = new Cell(this, cellCount, partitionData[i][0], partitionData[i][1], cellAttributes[i]);
            cells[i].setAttributes(reader.readUInt16());
            int cellOffset = reader.readInt();
            cells[i].setMaxX(reader.readShort());
            cells[i].setMaxY(reader.readShort());
            cells[i].setMinX(reader.readShort());
            cells[i].setMinY(reader.readShort());

            storedPos = reader.getPosition();

            if (cebkBankType == 0) {
                reader.setPosition(reader.getPosition() + (cebkNumBanks - (i + 1)) * 8 + cellOffset);
            } else {
                reader.setPosition(reader.getPosition() + (cebkNumBanks - (i + 1)) * 0x10 + cellOffset);
            }

            // read OAMs
            for (int x = 0; x < cellCount; x++) {
                int yCoord = reader.readByte();
                byte attr0 = (byte) reader.readByte();
                short attr1 = reader.readShort();
                short attr2 = reader.readShort();

                cells[i].setOam(x,yCoord,attr0,attr1,attr2);
            }

            reader.setPosition(storedPos);
        }
    }

    private void readCellBank(MemBuf.MemBufReader reader) {
        //cell bank data
        cebkId = reader.readString(4); // 0x10
        if (!cebkId.equals("KBEC")) {
            throw new RuntimeException("Not a valid NCER file.");
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

        cebkPartitionDataOffset = reader.readInt(); // 0x24
        cebkUnused = reader.readInt();
        cebkTacuOffset = reader.readInt();

        vramTransfer = cebkPartitionDataOffset != 0;
        tacu = cebkTacuOffset != 0;

        int storedPos = reader.getPosition();

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

        int[] cellAttributes = new int[cebkNumBanks];
        if (tacu) {
            reader.setPosition(NTR_HEADER_SIZE + cebkTacuOffset + 8);

            String tacuMagic = reader.readString(4);

            if (!tacuMagic.equals("TACU")) {
                throw new RuntimeException("Not a valid RECN file.");
            }

            int tacuSize = reader.readInt();
            int numTacuCells = reader.readUInt16();

            if (numTacuCells != cebkNumBanks)
                throw new RuntimeException("Idk what to do here - tacu cell stuff");

            cellAttributes = new int[numTacuCells];
            int numAttributes = reader.readUInt16();

            if (numAttributes != 1)
                throw new RuntimeException("Idk what to do here - tacu attribute not 1?");

            int cellAttributeOffset = reader.readInt();
            reader.skip(cellAttributeOffset - 8);

            for (int i = 0; i < numTacuCells; i++) {
                cellAttributes[i] = reader.readInt();
            }
        }

        reader.setPosition(storedPos); // reader is now at 0x30
        readCells(reader, partitionData, cellAttributes);
    }

    private void readLabels(MemBuf.MemBufReader reader) {
        //label data
        lablID = reader.readString(4); // 0x10
        if (!lablID.equals("LBAL")) {
            throw new RuntimeException("Not a valid RECN file.");
        }
        lablSectionSize = reader.readInt();

        long[] stringOffsets = new long[cebkNumBanks + 1];
        stringOffsets[stringOffsets.length - 1] = lablSectionSize - 8 - (4L *cebkNumBanks);
        for (int i = 0; i < cebkNumBanks; i++) {
            long offset = reader.readUInt32();
            if (offset >= lablSectionSize - 8) {
                reader.setPosition(reader.getPosition() - 4);
                offset = -1;
            }
            stringOffsets[i] = offset;
        }

        for (int i = 0; i < stringOffsets.length - 1; i++) {
            if (stringOffsets[i] != -1) {
                cells[i].setName(reader.readString((int) (stringOffsets[i+1] - stringOffsets[i])).trim());
            }
        }
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
        return cells.length;
    }

    public Cell.CellImage getCellImage(int i) throws NitroException {
        Cell cell = cells[i];
        return cell.getImage();
    }

    public Cell.OAM.OamImage[] getCellImages(int i) throws NitroException {
        Cell cell = cells[i];
        return cell.getImages();
    }

    public BufferedImage getNcerImage(int i) throws NitroException {
        Cell cell = cells[i];

        BufferedImage output = new BufferedImage(cell.getWidth(), cell.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) output.getGraphics();

        Cell.OAM.OamImage[] images = cell.getImages();
        for (int x = 0; x < images.length; x++) {
            Cell.OAM oam = cell.getOams()[x];

            g.drawImage(images[x].getImage(), oam.getCoordX() + output.getWidth() / 2, oam.getCoordY() + output.getHeight() / 2, null);
        }
        g.dispose();

        return output;
    }

    public void recolorImage() throws NitroException {
        for (int i = 0; i < cells.length; i++) {
            Cell.OAM.OamImage[] images = cells[i].getImages();
            for (int j = 0; j < images.length; j++) {
                images[j].recolorImage();
            }
        }
    }

    /**
     * Generates an object representation of an NSCR file from a file on disk
     *
     * @param path a <code>String</code> containing the path to a NSCR file on disk
     * @return an <code>IndexedImage</code> object
     */
    public static NCER fromFile(String path) throws NitroException {
        return fromFile(new File(path));
    }

    /**
     * Generates an object representation of an NSCR file from a file on disk
     *
     * @param file a <code>File</code> containing the path to a NSCR file on disk
     * @return an <code>IndexedImage</code> object
     */
    public static NCER fromFile(File file) throws NitroException {
        return (NCER) NFSFactory.fromFile(file);
    }
}
