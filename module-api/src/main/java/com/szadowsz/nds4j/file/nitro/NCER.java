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

import java.io.File;
import java.nio.charset.StandardCharsets;

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
    private int cebkUnused;
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
        logger.info("\nNCER file, " + fileName + ", initialising with size of " + fileSize + " bytes");

        readGenericNtrHeader(reader);

        File[] ncgrs = new File(path).getParentFile().listFiles(f -> (f.getName().endsWith(".NCGR") ||
                f.getName().endsWith(".NCBR")) &&
                f.getName().substring(0, f.getName().lastIndexOf('.')).equals(this.fileName));
        if (ncgrs.length > 0) {
            logger.info("Found corresponding NCGR file, " + ncgrs[0]);
            this.ncgr = NCGR.fromFile(ncgrs[0]);
            logger.info("Read NCGR file\n");
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
            logger.info("Cell @ " + i +" index @ " + reader.getPosition());

            int nOAMEntries = reader.readUInt16();
            logger.info("Cell @ " + i + " OAM entry count: " + nOAMEntries);
            int cellAttr = reader.readUInt16();
            logger.info("Cell @ " + i + " Cell attribute: " + cellAttr);
            int attrOffset = reader.readInt();
            logger.info("Cell @ " + i + " Cell attribute offset: " + attrOffset);

            CellPojo cell = cellPojos[i] = new CellPojo(nOAMEntries, cellAttr);

            if (nOAMEntries != 0) {
                int storedPos = reader.getPosition();
                reader.setPosition(start + cebkNumCells*perCellDataSize + attrOffset);
                for (int j = 0; j < nOAMEntries; j++) {
                    cell.setOamAttrs(j, reader.readShort(), reader.readShort(), reader.readShort());
                }
                logger.info("Cell @ " + i +" oam read index @ " + reader.getPosition());
                reader.setPosition(storedPos);
                if (cebkBankType == 1) {
                    cell.setBounds(reader.readShort(), reader.readShort(), reader.readShort(), reader.readShort());
                }
            } else {
                // Provide at least one OAM attribute for empty cells
                cell.setEmptyAttributes(); // Disable rendering
            }
        }
        logger.info("Cell read end index @ " + reader.getPosition());
    }

    private void readCellBank(MemBuf.MemBufReader reader) throws NitroException {
        //cell bank data
        cebkId = reader.readString(4); // 0x10
        if (!cebkId.equals("KBEC")) {
            throw new NitroException("Not a valid NCER file.");
        }
        cebkSectionSize = reader.readUInt32(); // 0x14
        logger.info("CEBK section size " + cebkSectionSize + " bytes");

        cebkNumCells = reader.readUInt16(); // 0x18
        logger.info("CEBK cell count " + cebkNumCells);

        cebkBankType = reader.readUInt16(); // 0x1A // 1 - with bounding rectangle, 0 - without
        int perCellDataSize = 8;
        if (cebkBankType == 1) {
            logger.info("With Bounding Rectangle");
            perCellDataSize += 8;
        } else {
            logger.info("Without Bounding Rectangle");
        }

        cebkDataOffset = reader.readUInt32(); // 0x1C
        logger.info("CEBK data offset " + cebkDataOffset + " bytes");

        cebkMappingType = (int) (reader.readUInt32() & 0xFF); // 0x20
        if (cebkMappingType < 5) {
            cebkMappingMode = mappingModes[cebkMappingType];
        } else {
            cebkMappingMode = GX_OBJVRAMMODE_CHAR_1D_32K;
        }
        logger.info("CEBK mapping type " + cebkBankType);

        logger.info("CEBK expected data per cell " + perCellDataSize + " bytes");

        // Check for VRAM transfer
        cebkPartitionDataOffset = reader.readInt(); // 0x24
        vramTransfer = cebkPartitionDataOffset != 0;
        logger.info("vram transfer expected=" + vramTransfer);

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
        logger.info("Read NCER data");
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

    public NCGR getNCGR() {
        return ncgr;
    }

    public NCLR getNCLR() {
        return (ncgr != null) ? ncgr.getNCLR() : null;
    }

    public void setNCGR(NCGR ncgr) {
        this.ncgr = ncgr;
    }

    public void setNCLR(NCLR nclr) {
        if (ncgr != null) {
            ncgr.setPalette(nclr);
        }
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
