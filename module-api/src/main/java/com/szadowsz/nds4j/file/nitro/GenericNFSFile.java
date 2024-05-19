package com.szadowsz.nds4j.file.nitro;

import com.szadowsz.nds4j.compression.CompFormat;
import com.szadowsz.nds4j.compression.JavaDSDecmp;
import com.szadowsz.nds4j.data.NFSFormat;
import com.szadowsz.nds4j.data.nfs.Endianness;
import com.szadowsz.nds4j.exception.InvalidDataException;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.BaseNFSFile;
import com.szadowsz.nds4j.reader.HexInputStream;
import com.szadowsz.nds4j.reader.MemBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Generic Representation Of Nitro Files
 * <p>
 * Archive-related:
 *      NARC (Nitro ARChive)                            → Basically a Zip File
 * 2D-related:
 *      NCLR (Nitro CoLoR)                              → Color Palette                 -> NCLR class
 *      NCGR/NCBR (Nitro Character Graphic Resource)    → Graphical Tiles               -> NCGR class
 *      NBGR (Nitro Basic Graphic Resource)             → Graphical Tiles               -> NCGR class
 *      NSCR (Nitro SCreen Resource)                    → Maps/Images                   -> NSCR class
 *      NCER (Nitro CEll Resource)                      → Tile Arrangement Information
 *      NANR (Nitro ANimation Resource)                 → Animation Data
 *      NFTR (Nitro Font Table Resource)                → Fonts
 * 3D-related:
 *      NSBMD (Nitro Sdk Binary Model Data)             → 3D Polygonal Model data
 *      NSBTX (Nitro Sdk Binary TeXture)                → Texture image and palette data
 *      NSBCA (Nitro Sdk Binary Character Animation)    → Skeletal animation data
 *      NSBTP (Nitro Sdk Binary Texture Pattern)        → Texture-swapping animations
 *      NSBTA (Nitro Sdk Binary Texture Animation)      → UV-change animations
 *      NSBMA (Nitro Sdk Binary Material Animation)     → Material-change animations
 *      NSBVA (Nitro Sdk Binary Visibility Animation)   → Visibility animations
 *      SPA (Unknown) → Particles
 */
public abstract class GenericNFSFile extends BaseNFSFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericNFSFile.class);

    // Expected Header Size
    protected static final int NTR_HEADER_SIZE = 0x10;


    // Compressed Data Info

    // If it's compressed (shorthand)
    protected boolean isCompressed;

    // What format used to compress, if any
    protected CompFormat compressFormat;

    // The raw data in compressed format
    protected byte[] compressedData;


    // Header Data Info

    // BOM to identify what endianness to use
    protected int bom;
    // How to read the data
    protected Endianness.EndiannessType endiannessOfBeginning = Endianness.EndiannessType.LITTLE;
    // File Format version
    protected int version;
    // Actual File Size
    protected long fileSize;
    // Actual Header Size
    protected int headerSize;
    // Number of Data Sections
    protected int numBlocks;
    protected boolean isOld;

    // Raw header data
    protected byte[] headerData;

    /**
     * Constructor to Use before decompressing file data
     *
     * @param expectedMagic expected file type
     * @param name          the name of the file
     * @param data          the raw data, possibly compressed
     * @throws NitroException
     */
    public GenericNFSFile(NFSFormat expectedMagic, String name, byte[] data) throws NitroException {
        super(name);
        this.isCompressed = isCompressed(data);
        LOGGER.info("compressed=" + this.isCompressed);
        this.compressedData = data;
        this.rawData = isCompressed ? decompress(data) : data;

        MemBuf buf = MemBuf.create(rawData);
        MemBuf.MemBufReader reader = buf.reader();
        readGenericNtrHeader(expectedMagic, reader);
        readFile(reader);
    }

    /**
     * Constructor to Use after decompressing file data and assessing its contents
     *
     * @param magic    the file type
     * @param path     the path of the file
     * @param fileName the name of the file
     * @param comp     the compression format used (if any)
     * @param compData the raw compressed data
     * @param data     the raw uncompressed data
     */
    public GenericNFSFile(NFSFormat magic, String path, String fileName, CompFormat comp, byte[] compData, byte[] data) {
        super(magic, path, fileName);
        this.isCompressed = comp != CompFormat.NONE;
        LOGGER.info("compressed=" + this.isCompressed);
        this.compressFormat = comp;
        this.compressedData = compData;
        this.rawData = data;
    }

    @Override
    public final byte[] getCompressedData() {
        return compressedData;
    }


    /**
     * Determine whether the data is compressed by attempting to read the magic id
     *
     * @param data the data to inspect
     * @return true if we can't read the ID, false otherwise
     */
    protected boolean isCompressed(byte[] data) {
        String magic = new String(Arrays.copyOfRange(data, 0, 4), StandardCharsets.UTF_8);
        NFSFormat actualMagic = NFSFormat.valueOfLabel(magic);
        if (actualMagic == null) {
            return true;
        }
        return false;
    }

    /**
     * Method to construct and execute decompression of the file data.
     *
     * @param data the data to decompress
     * @return decompressed data
     */
    protected byte[] decompress(byte[] data) {
        LOGGER.info("Decompressing");
        byte[] decompressByte = new byte[0];
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(data);
            HexInputStream hexInput = new HexInputStream(input);
            int[] decompressInt = JavaDSDecmp.decompress(hexInput);
            decompressByte = new byte[decompressInt.length];
            for (int i = 0; i < decompressInt.length; i++) {
                decompressByte[i] = (byte) decompressInt[i];
            }
            LOGGER.info("Successful Decompression");
        } catch (Exception e) {
            LOGGER.info("Failed Decompression");
        }

        return decompressByte;
    }

    public int NnsG2dGetSectionByMagic(MemBuf.MemBufReader reader, int sectionMagic) {
        int original = reader.getPosition();
        int offset = headerSize;

        for (int i = 0; i <= numBlocks; i++) {
            if (offset + 8 > fileSize) {
                reader.setPosition(original);
                return -1;
            }
            reader.setPosition(offset);
            int magic = reader.readInt();
            reader.setPosition(offset + 4);
            int sectionSize = reader.readInt();
            ;
            if (isOld) {
                sectionSize += 8;
            }

            if (magic == sectionMagic) {
                reader.setPosition(original);
                return offset;
            }
            offset += sectionSize;
        }
        reader.setPosition(original);
        return -1;
    }

    public int NnsG2dFindBlockBySignature(MemBuf.MemBufReader reader, String sig) {
        int sig32 = 0;
        for (int i = 0; i < 4; i++) {
            int shift = 8 * ((endiannessOfBeginning == Endianness.EndiannessType.LITTLE) ? (3 - i) : i);
            sig32 |= (sig.charAt(i) & 0xFF) << shift;
        }

        return NnsG2dGetSectionByMagic(reader, sig32);
    }

    /**
     * Read the part of the Generic File Header common to both methods below
     * <p>
     * see Also the docs on the byte format
     *
     * @param reader the buffer to extrac the data from
     * @throws NitroException if the read of the header is unsuccessful
     */
    protected void readGenericNtrHeaderCommon(MemBuf.MemBufReader reader) throws NitroException {
        //  0x4 - BOM - Read Endian format
        bom = reader.readUInt16();

        // 0x6 - Read Version Constant
        version = reader.readUInt16();

        // some games use big endian, some use little - NSMB uses big for example, but Spirit Tracks uses little
        if (bom == 0xFFFE) {
            endiannessOfBeginning = Endianness.EndiannessType.BIG;
            version = (version & 0xFF) << 8 | version >> 8;
        }
        LOGGER.info("Bom: 0x" + Integer.toHexString(this.bom));
        // TODO
//        if (version != 1 && version != 2){
//            version = (version & 0xFF) << 8 | version >> 8;
//            if (version != 1 && version != 2) {
//                throw new InvalidDataException("Unsupported Version number: " + version);
//            }
//        }
        LOGGER.info("Version: " + this.version);

        // 0x8 - Read File size
        fileSize = reader.readUInt32();

        // 0xC - Read header size
        headerSize = reader.readUInt16();
        if (headerSize != NTR_HEADER_SIZE) {
            throw new InvalidDataException("Unsupported File header size: " + headerSize);
        }
        LOGGER.info("Header Size: " + Integer.toHexString(this.headerSize));

        // 0xE - Read number of sections
        numBlocks = reader.readUInt16();
        LOGGER.info("Number Of Sections: " + Integer.toHexString(this.numBlocks));

        // if file size = total block size plus header size, file is old NNS G2D
        isOld = headerSize + numBlocks * 8 == fileSize;
    }

    /**
     * Read the Generic File Header according to the format.
     * <p>
     * see Also the docs on the byte format
     *
     * @param reader the buffer to extrac the data from
     * @throws NitroException if the read of the header is unsuccessful
     */
    protected void readGenericNtrHeader(MemBuf.MemBufReader reader) throws NitroException {
        // 0x0 - Magic ID - Identifies the file format.
        byte[] magicBytes = reader.readBytes(4);
        NFSFormat magic = NFSFormat.valueOfLabel(new String(magicBytes, StandardCharsets.UTF_8));
        if (!this.magic.equals(magic)) {
            throw new InvalidDataException("Unsupported File encoding: " + magic.getLabel()[0] + ", should be " + this.magic.getLabel()[0]);
        }
        LOGGER.info("Supported File encoding: " + this.magic.getLabel()[0]);

        // Read the rest of the header in a common header method
        readGenericNtrHeaderCommon(reader);
    }


    /**
     * Read the Generic File Header according to the format.
     * <p>
     * see Also the docs on the byte format
     *
     * @param expectedMagic the magic we expect to read back to us
     * @param reader        the buffer to extract the data from
     * @throws NitroException if the read of the header is unsuccessful
     */
    protected void readGenericNtrHeader(NFSFormat expectedMagic, MemBuf.MemBufReader reader) throws NitroException {
        // Magic ID - Identifies the file format.
        byte[] magicBytes = reader.readBytes(4);
        NFSFormat magic = NFSFormat.valueOfLabel(new String(magicBytes, StandardCharsets.UTF_8));
        if (!expectedMagic.equals(magic)) {
            throw new InvalidDataException("Unsupported File encoding: " + magic.getLabel()[0] + ", should be " + expectedMagic.getLabel()[0]);
        }
        this.magic = magic;
        LOGGER.info("Supported File encoding: " + this.magic.getLabel()[0]);

        // Read the rest of the header in a common header method
        readGenericNtrHeaderCommon(reader);
    }

    /**
     * Read the data section of the file from a buffer
     *
     * @param reader the reader to access the data from
     * @throws NitroException if the file cannot be processed
     */
    protected abstract void readFile(MemBuf.MemBufReader reader) throws NitroException;
}
