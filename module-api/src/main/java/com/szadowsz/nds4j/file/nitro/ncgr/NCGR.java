package com.szadowsz.nds4j.file.nitro.ncgr;

import com.szadowsz.nds4j.NFSFactory;
import com.szadowsz.nds4j.compression.CompFormat;
import com.szadowsz.nds4j.file.NFSFormat;
import com.szadowsz.nds4j.file.ImageableWithPalette;
import com.szadowsz.nds4j.file.nitro.nclr.colors.ColorFormat;
import com.szadowsz.nds4j.file.nitro.ncgr.tiles.TileForm;
import com.szadowsz.nds4j.file.nitro.GenericNFSFile;
import com.szadowsz.nds4j.file.nitro.nclr.NCLR;
import com.szadowsz.nds4j.file.nitro.ncer.cells.CellInfo;
import com.szadowsz.nds4j.exception.InvalidFileException;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.ncer.NCER;
import com.szadowsz.binary.io.reader.MemBuf;
import com.szadowsz.nds4j.utils.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * An object representation of an NCGR file. <p>
 * An NCGR file is a Nintendo proprietary DS format used for storing graphics (images).
 */
public class NCGR extends GenericNFSFile implements ImageableWithPalette {
    private static final Logger logger = LoggerFactory.getLogger(NCGR.class);

    /**
     * Based on how an NCER or NSCR is set to read an NCGR file, or how the game is programmed to read an NCGR file,
     * it may use a specific palette index within the NCLR (palette) file. <p>
     * For example, elements of the opening sequence
     * in Pokémon HeartGold share a single NCLR file with multiple 16 color palettes stored consecutively inside it.
     * The tiles within the NSCR used to display them contain the palette index information.
     */
//    private int paletteIdx = 0;

    // first section: CHAR (CHARacter data)
    protected String charMagic;
    protected long charSectionSize;
    protected int charTilesHeight;
    protected int charTilesWidth;
    protected ColorFormat charBitDepth;
    protected int charUnknown1;
    protected int charMappingType;
    protected long charTiledFlag;
    protected long charTiledataSize;
    protected long charUnknown3;
    protected byte[] charData;
    protected byte[][] charTiledData;
    protected boolean sopc;

    // second section: SOPC
    protected String sopcMagic;
    protected long sopcSectionSize;
    protected long sopcUnknown1;
    protected int sopcCharSize;
    protected int sopcNChars;

    // image info
    protected BufferedImage image;

    protected byte[] original;
    protected int height;
    protected int width;

    protected byte[] imgTilesFlat;

    protected int tileSize;

    protected NCLR palette;

    protected TileForm order;

    public static boolean calcIsNCGR2D(int m) {
        return ((m) == NCER.GX_OBJVRAMMODE_CHAR_2D);
    }

    public static boolean calcIsNCGR1D(int m) {
        return (!calcIsNCGR2D(m));
    }

    /**
     * Generates an object representation of an NCGR file from a file on disk
     *
     * @param path a <code>String</code> containing the path to a NCGR file on disk
     * @return an <code>IndexedImage</code> object
     */
    public static NCGR fromFile(String path) throws NitroException {
        return fromFile(new File(path));
    }

    /**
     * Generates an object representation of an NCGR file from a file on disk
     *
     * @param file a <code>File</code> containing the path to a NCGR file on disk
     * @return an <code>IndexedImage</code> object
     */
    public static NCGR fromFile(File file) throws NitroException {
        return (NCGR) NFSFactory.fromFile(file);
    }

    public NCGR(int height, int width, int bitDepth, NCLR palette) throws NitroException {
        super(NFSFormat.NCGR, null, null, CompFormat.NONE, null, null);
        if (height % 8 != 0)
            throw new NitroException(String.format("%d was provided for image height, but a multiple of 8 is required.", height));

        if (width % 8 != 0)
            throw new NitroException(String.format("%d was provided for image width, but a multiple of 8 is required.", width));

        this.height = height;
        this.charTilesHeight = height / 8;
        this.width = width;
        this.charTilesWidth = width / 8;

        if (bitDepth != 4 && bitDepth != 8)
            bitDepth = 4;
        this.charBitDepth = ColorFormat.valueOf(bitDepth);

        charTiledataSize = (long) charTilesWidth * charTilesHeight;
        charTiledData = new byte[charTilesWidth * charTilesHeight][];
        charData = new byte[(int) charTiledataSize];

        this.palette = palette;


        image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
        setImagePixels();
    }

    public NCGR(String path, String name, CompFormat comp, byte[] compData, byte[] data) throws NitroException {
        super(NFSFormat.NCGR, path, name, comp, compData, data);

        MemBuf buf = MemBuf.create(rawData);
        int fileSize = buf.writer().getPosition();
        logger.info("\nNCGR file, " + fileName + ", initialising with size of " + fileSize + " bytes");

        MemBuf.MemBufReader reader = buf.reader();
        readGenericNtrHeader(reader);
        int headerLength = reader.getPosition();
        reader.setPosition(0);
        this.headerData = reader.readTo(headerLength);

        File[] palettes = new File(path).getParentFile().listFiles(f -> f.getName().endsWith(".NCLR") &&
                f.getName().substring(0, f.getName().lastIndexOf('.')).equals(this.fileName));
        if (palettes.length > 0) {
            logger.info("Found corresponding NCLR file, " + palettes[0]);
            this.palette = NCLR.fromFile(palettes[0]);
            logger.info("Read NCLR file\n");
        } else {
            this.palette = NCLR.DEFAULT;
        }
        logger.info("Reading NCGR file data");
        readFile(reader);
    }

    /**
     * Gets the width of this <code>NCGR</code>
     *
     * @return an <code>int</code>
     */
    @Override
    public int getWidth() {
        return width;
    }

    /**
     * Gets the width of this <code>NCGR</code>
     *
     * @return an <code>int</code>
     */
    public int getTileWidth() {
        return charTilesWidth;
    }

    /**
     * Gets the height of this <code>NCGR</code>
     *
     * @return an <code>int</code>
     */
    @Override
    public int getHeight() {
        return height;
    }

    /**
     * Gets the height of this <code>NCGR</code>
     *
     * @return an <code>int</code>
     */
    public int getTileHeight() {
        return charTilesHeight;
    }

    public byte[] getCharData() {
        return charData;
    }

    public TileForm getTileOrder() {
        return order;
    }

    /**
     * Creates a <code>BufferedImage</code> using this <code>IndexedImage</code>
     *
     * @return a <code>BufferedImage</code> representation of this <code>IndexedImage</code>
     */
    @Override
    public BufferedImage getImage() {
        return image;
    }

    protected byte[] byteToBit4(byte data) {
        byte[] bit4 = new byte[2];

        bit4[0] = (byte) (data & 0x0F);
        bit4[1] = (byte) ((data & 0xF0) >> 4);

        return bit4;
    }

    protected Color getColor16(byte[] data, int pos) {
        if (data.length <= (pos / 2)) {
            return new Color(255, 255, 255, 0);
        }
        byte bit4 = data[pos / 2];
        int index = byteToBit4(bit4)[pos % 2];
        Color c;
        if (index > 0 || !Configuration.isRenderTransparent()) {
            c = palette.getColor(index);
        } else {
            logger.warn("NCGR Blank i=" + pos + " paletterIndex=" + index + " colours=" + palette.getNumColors());
            c = new Color(255, 255, 255, 0);
        }
        return c;
    }

    protected Color getColor256(byte[] data, int pos) {
        if (data.length > pos && palette.getNumColors() > data[pos]) {
            int index = Byte.toUnsignedInt(data[pos]);
            if (index > 0 || !Configuration.isRenderTransparent()) {
                return palette.getColor(index);
            } else {
                logger.warn("NCGR Blank i=" + pos + " paletterIndex=" + index + " colours=" + palette.getNumColors());
                return new Color(255, 255, 255, 0);
            }
        } else {
            logger.warn("NCGR Blank i=" + pos + " paletterIndex=" + data[pos] + " colours=" + palette.getNumColors());
            return new Color(255, 255, 255, 0);
        }
    }

    protected void process16ColorPalette(byte[] tiles, int width, int height) {
        int pos = 0;
        image = new BufferedImage(width, height, TYPE_INT_ARGB);
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                Color color = getColor16(tiles, pos++);
                image.setRGB(w, h, color.getRGB());
            }
        }
    }

    protected void process256ColorPalette(byte[] tiles, int width, int height) {
        int pos = 0;
        image = new BufferedImage(width, height, TYPE_INT_ARGB);
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                Color color = getColor256(tiles, pos++);
                image.setRGB(w, h, color.getRGB());
            }
        }
    }


    @Override
    public NCLR getNCLR() {
        return palette;
    }

    public byte[][] getTiles() {
        return charTiledData;
    }

    public int getBitDepth() {
        return this.charBitDepth.bits;
    }

    public int getTileCount() {
        return charTilesHeight * charTilesWidth;
    }

    @Override
    public void setNCLR(NCLR palette) throws NitroException {
        this.palette = palette;
        recolorImage();
    }

    protected void setImagePixels(byte[] tiles, ColorFormat format, int width, int height) throws InvalidFileException {
        if (tiles.length == 0) {
            return;
        }
        switch (format) {
            case colors16 -> process16ColorPalette(tiles, width, height);
            case colors256 -> process256ColorPalette(tiles, width, height);
            default -> throw new InvalidFileException("Unsupported Colour Format: " + format);
        }
    }

    public void setImagePixels() throws InvalidFileException {
        if (order == TileForm.Horizontal) {
            imgTilesFlat = linealToHorizontal(charData, width, height, this.charBitDepth.bits, tileSize);
        } else {
            imgTilesFlat = charData;
        }
        setImagePixels(imgTilesFlat, charBitDepth, width, height);
    }

    public void setImageData(byte[] tiles, int width, int height, ColorFormat format, TileForm form, int tileSize) throws InvalidFileException {
        this.charData = tiles;
        this.charBitDepth = format;
        this.order = form;
        this.tileSize = tileSize;

        this.width = width;
        this.height = height;

        // Get the original data for changes in startByte
        original = tiles.clone();
        setImagePixels();
    }

    int guessTileWidth(int nTiles) {
        int width = 1;

        //if tile count is a multiple of 32, use it
        if (nTiles % 32 == 0) {
            return 32;
        }

        //iterate factors
        for (int i = 1; i < nTiles; i++) {
            if (i * i > nTiles) break;
            if (nTiles % i == 0) width = i;
        }
        int height = nTiles / width;
        if (width > height) {
            return width; //prioritize wide over tall output
        }
        return height;
    }


    protected byte[] renderTile(int chno, CellInfo transfer) {
        byte[] out = new byte[64];
        // if transfer == null, don't simulate any VRAM transfer operation
        if (transfer == null) {
            if (chno < getTileCount()) {
                System.arraycopy(charTiledData[chno], 0, out, 0, 64);
            }
            return out;
        }

        // get character source address
        int chrSize = 8 * getBitDepth();
        int srcAddr = chno * chrSize;
        if ((srcAddr + chrSize) < transfer.getPartitionOffset() || srcAddr >= (transfer.getPartitionOffset() + transfer.getPartitionSize())) {
            if (chno < getTileCount()) {
                System.arraycopy(charTiledData[chno], 0, out, 0, 64);
            }
            return out;
        }

        // character is within the destination region. For bytes within the region, copy from src.
        // TODO: handle bitmapped graphics transfers too
        for (int i = 0; i < 64; i++) {
            // copy charTiledData[chno][i] to out[i]
            int pxaddr = srcAddr + (i >> (getBitDepth() == 4 ? 1 : 0));
            if (pxaddr >= transfer.getPartitionOffset() && pxaddr < (transfer.getPartitionOffset() + transfer.getPartitionSize())) {
                // in transfer destination
                pxaddr = pxaddr - transfer.getPartitionOffset() + 0;//transfer.srcAddr;
                int transferChr = pxaddr / chrSize;
                int transferChrPxOffset = pxaddr % chrSize;
                int pxno = transferChrPxOffset;
                if (getBitDepth() == 4) {
                    pxno <<= 1;
                    pxno += (i & 1);
                }
                out[i] = charTiledData[transferChr][pxno];
            } else {
                // out of transfer destination
                out[i] = charTiledData[chno][i];
            }
        }
        return out;
    }


    protected Color[] renderTile(byte[] tile, int palNum) throws NitroException {
        if (palNum > 0) {
            logger.warn("NCGR renderTile, unsupported palette " + palNum);
        }

        Color[] out = new Color[tile.length];
        for (int i = 0; i < tile.length; i++) {
            int index = tile[i] & 0xFF;
            if (index != 0 || !Configuration.isRenderTransparent()) {
                Color tmp = new Color(255, 255, 255, 0);
                 if (palette != null && (index + (palNum << charBitDepth.bits)) < palette.getNumColors()) {
                     tmp = palette.getColor(index + (palNum << charBitDepth.bits));
                } else {
                     logger.info("NCGR Blanking Tile @ i=" + i + ", palette index=" + tile[i] + ", palette index(u)=" + index + ", palette shift=" + (palNum << charBitDepth.bits));
                 }
                out[i] = tmp;
            } else {
                out[i] = new Color(255, 255, 255, 0);
            }
//            switch (charBitDepth) {
//                case colors16 -> out[i] = getColor16(tile, i);
//                case colors256 -> out[i] = getColor256(tile, i);
//                default -> throw new NitroException("Unsupported Colour Format: " + charBitDepth);
//            }
        }
        return out;
    }

    protected Color[] renderTile(int tileNo, int palNum) throws NitroException {
        if (tileNo < getTileCount()) {
            return renderTile(charTiledData[tileNo], palNum);
        } else {
            logger.warn("NCGR Blanking Tile Transfer, tile " + tileNo);
            Color[] out = new Color[64];
            Arrays.fill(out, new Color(0));
            return out;
        }
    }

    public Color[] renderTile(int tileNo, boolean transfer, CellInfo transferInfo, int palNum) throws NitroException {
        logger.debug("NCGR Tile Info, tile=" + tileNo + ", vram=" + transfer + ", palette=" + palNum + ", bitDepth=" + charBitDepth);
        // if transfer == null, render as normal
        if (!transfer) {
            return renderTile(tileNo, palNum);
        }

        // else, read graphics and render
        byte[] buf = renderTile(tileNo, transferInfo);
        return renderTile(buf, palNum);
    }

    protected void readData(MemBuf.MemBufReader reader) {
        int nChars = getTileCount();
        int nPresentTiles = (int) charTiledataSize >> 5;
        if (charBitDepth.bits == 8) {
            nPresentTiles >>= 1;
        }
        if (calcIsNCGR1D(charMappingType) || nChars != nPresentTiles) {
            nChars = nPresentTiles;
            charTilesWidth = guessTileWidth(nChars);
            charTilesHeight = nChars / charTilesWidth;
            this.height = charTilesHeight * 8;
            this.width = charTilesWidth * 8;

        }
        charTiledData = new byte[nChars][];
        charData = new byte[(int) charTiledataSize];

        int bufferIndex = 0;
        for (int i = 0; i < nChars; i++) {
            charTiledData[i] = new byte[64];
            byte[] tile = charTiledData[i];

            if (charBitDepth.bits == 8) {
                //8-bit graphics: no need to unpack
                for (int j = 0; j < 64; j++) {
                    int data = reader.readByte() & 0xff;
                    tile[j] = (byte) data;
                    charData[bufferIndex++] = (byte) data;
                }
            } else if (charBitDepth.bits == 4) {
                //4-bit graphics: unpack
                for (int j = 0; j < 32; j++) {
                    int data = reader.readByte()  & 0xff;
                    logger.debug("NCGR Tile 4-byte pre-unpack=" + data);
                    tile[j * 2] = (byte) (data & 0xF);
                    logger.debug("NCGR Tile 4-byte index=" + (j * 2)  + " lower endian=" +  tile[j * 2]);
                    tile[j * 2 + 1] = (byte) (data >> 4);
                    logger.debug("NCGR Tile 4-byte index=" + (j * 2 + 1)  + " upper endian=" + tile[j * 2 + 1]);
                    charData[bufferIndex++] = (byte) data;
                }
            }
        }
    }

    @Override
    protected void readFile(MemBuf.MemBufReader reader) throws InvalidFileException {
        this.sopc = this.numBlocks == 2;

        // Read the first section: CHAR (CHARacter data)
        charMagic = reader.readString(4);  // reader position is now 0x10 (0x0)
        if (!charMagic.equals("RAHC")) {
            throw new RuntimeException("Not a valid NCGR file.");
        }
        charSectionSize = reader.readUInt32(); // 0x14 (0x4)

        charTilesHeight = reader.readUInt16(); // 0x18 (0x8)
        charTilesWidth = reader.readUInt16();  // 0x1A (0xA)
        this.height = this.charTilesHeight * 8;
        this.width = this.charTilesWidth * 8;

        charBitDepth = ColorFormat.valueOf(reader.readInt()); // 0x1C (0xC)

        charUnknown1 = reader.readUInt16();    // 0x20 (0x10)
        charMappingType = reader.readUInt16(); // 0x22 (0x12)

        charTiledFlag = reader.readUInt32();   // 0x24 (0x14)
        if ((charTiledFlag & 0xFF) == 0x0) {
            order = TileForm.Horizontal;
        } else {
            order = TileForm.Lineal;
        }
        charTiledataSize = reader.readUInt32(); // 0x28 (0x18)
        charUnknown3 = reader.readUInt32();     // 0x2C (0x1C)
        readData(reader);

        // Read the second section: SOPC
        if (sopc && reader.getBuffer().length > 0) {
            sopcMagic = reader.readString(4);  // (0x0)
            sopcSectionSize = reader.readUInt32(); // (0x4)
            sopcUnknown1 = reader.readUInt32();    // (0x8)
            sopcCharSize = reader.readUInt16();    // (0xC)
            sopcNChars = reader.readUInt16();      // (0xE)
        }

        setImageData(charData, width, height, charBitDepth, order, 8);
    }

    public void recolorImage() throws NitroException {
        setImagePixels();
    }

    protected byte[] linealToHorizontal(byte[] lineal, int width, int height, int bpp, int tile_size) {
        byte[] horizontal = new byte[lineal.length];
        int tile_width = tile_size * bpp / 8;   // Calculate the number of byte per line in the tile
        // pixels per line * bits per pixel / 8 bits per byte
        int tilesX = width / tile_size;
        int tilesY = height / tile_size;


        //       ht
        // wt     0      ...  tilesX
        //       ...
        //      tileY
        //       h
        // w     0      ...  tile_width (8)
        //       ...
        //      tile_size (8)
        // In a lineal sequence
        // lineal[0] = horizontal[0]
        // lineal[1] = horizontal[(1 + 0 * 8 * 30) + 0 * 8 + 0 * 30 * 8 * 8] = horizontal[1]
        // lineal[8] = horizontal[(0 + 1 * 8 * 30) + 0 * 8 + 0 * 30 * 8 * 8] = horizontal[240]
        // lineal[64] = horizontal[(0 + 0 * 8 * 30) + 1 * 8 + 0 * 30 * 8 * 8] = horizontal[8]
        int pos = 0;
        for (int ht = 0; ht < tilesY; ht++) {
            for (int wt = 0; wt < tilesX; wt++) {
                // Get the tile data
                for (int h = 0; h < tile_size; h++) {
                    for (int w = 0; w < tile_width; w++) {
                        int index = (w + h * tile_width * tilesX) + wt * tile_width + ht * tilesX * tile_size * tile_width;
                        if (index >= lineal.length)
                            continue;
                        if (pos >= lineal.length)
                            continue;

                        horizontal[index] = lineal[pos++];
                    }
                }
            }
        }
        return horizontal;
    }
}
