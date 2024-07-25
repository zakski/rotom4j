package com.szadowsz.nds4j.file.nitro.nscr;

import com.szadowsz.nds4j.NFSFactory;
import com.szadowsz.nds4j.compression.CompFormat;
import com.szadowsz.nds4j.file.ImageableWithGraphic;
import com.szadowsz.nds4j.file.NFSFormat;
import com.szadowsz.nds4j.file.nitro.nscr.tiles.NTFS;
import com.szadowsz.nds4j.exception.InvalidFileException;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.GenericNFSFile;
import com.szadowsz.nds4j.file.nitro.nclr.NCLR;
import com.szadowsz.nds4j.file.nitro.ncgr.NCGR;
import com.szadowsz.nds4j.reader.MemBuf;
import com.szadowsz.nds4j.utils.Configuration;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class NSCR extends GenericNFSFile implements ImageableWithGraphic {

    // Block Header section
    private String id;                   // NRCS = 0x4E524353
    private long sectionSize;
    private int width;
    private int height;
    private long padding;              // Always 0x0
    private long dataSize;

    // Tile Data
    private NCGR ncgr;
    private NTFS[] mapData;
    private int[] data;
    private int tileBase;
    private int highestIndex;

    // image info
    private BufferedImage image;

    public NSCR(String path, String fileName, CompFormat comp, byte[] compData, byte[] data) throws NitroException {
        super(NFSFormat.NSCR, path, fileName, comp, compData, data);

        MemBuf buf = MemBuf.create(rawData);

        MemBuf.MemBufReader reader = buf.reader();
        readGenericNtrHeader(reader);
        int headerLength = reader.getPosition();
        reader.setPosition(0);
        this.headerData = reader.readTo(headerLength);

        File[] ncgrs = new File(path).getParentFile().listFiles(f -> (f.getName().endsWith(".NCGR") ||
                f.getName().endsWith(".NCBR")) &&
                f.getName().substring(0, f.getName().lastIndexOf('.')).equals(this.fileName));
        if (ncgrs.length > 0) {
            this.ncgr = NCGR.fromFile(ncgrs[0]);
        }

        readFile(reader);
    }

    /**
     * Creates a <code>BufferedImage</code> using this <code>NSCR</code>
     *
     * @return a <code>BufferedImage</code> representation of this <code>NSCR</code>
     */
    public BufferedImage getImage() {
        if (image != null) {
            return image;
        } else {
            return new BufferedImage(getWidth(),getHeight(),TYPE_INT_ARGB);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public NCGR getNCGR() {
        return this.ncgr;
    }

    public NCLR getNCLR(){
        return (ncgr!=null)?ncgr.getNCLR():NCLR.DEFAULT;
    }

    public void setNCGR(NCGR ncgr) {
        this.ncgr = ncgr;
    }

    @Override
    public void setNCLR(NCLR nclr) throws NitroException {
        if (ncgr != null) {
            this.ncgr.setNCLR(nclr);
            recolorImage();
        }
    }

    /**
     * Computes the highest character index in the screen file
     *
     * @return the highest character index in the screen file
     */
    protected int computeHighestCharacter() {
        int highest = 0;
        for (int i = 0; i < mapData.length; i++) {
            NTFS tile = mapData[i];
            int charNum = tile.nTile;
            if (charNum > highest) highest = charNum;
        }
        return highest;
    }

    public void recolorImage() throws NitroException {
        ncgr.recolorImage();
        createImage();
    }

    protected NTFS createMapInfo(int value) {
        NTFS mapInfo = new NTFS();

        mapInfo.nTile = value & 0x3FF;
        mapInfo.transform = (value >> 10) & 0x3;
        mapInfo.xFlip = (byte) ((value >> 10) & 1);
        mapInfo.yFlip = (byte) ((value >> 11) & 1);
        mapInfo.nPalette = ((value >> 12) & 0xF);

        return mapInfo;
    }

    protected void createImage() throws NitroException {
        if (ncgr == null) {
            return;
        }
        try {
            image = renderNscr(tileBase, Configuration.isRenderTransparent());
        } catch (Exception e){
            throw new InvalidFileException("Image Rendering Failed",e);
        }
    }

    public int nscrGetTileEx(int charBase, int x, int y, Color[] out, int[] tileNo, boolean transparent) {
        if (ncgr == null) {
            Arrays.fill(out, 0);
            return 0;
        }
        NCLR nclr = ncgr.getNCLR();

        int nWidthTiles = width >> 3;
        int nHeightTiles = height >> 3;
        if (x >= nWidthTiles || y >= nHeightTiles) {
            Arrays.fill(out, 0);
            return 1;
        }

        NTFS tileData = mapData[y * nWidthTiles + x];
        int tileNumber = tileData.nTile;
        int transform = tileData.transform;
        int paletteNumber = tileData.nPalette;
        if (tileNo != null) {
            tileNo[0] = tileNumber;
        }

        // Get palette and base character
        Color[] palette = nclr.getColorPalette(paletteNumber);
        tileNumber -= charBase;

        if (tileNumber >= ncgr.getTileCount() || tileNumber < 0) { // Let's just paint a transparent square
            if (!transparent) {
                Color bg = nclr.getColor(0);//ColorConvertFromDS(CREVERSE(nclr.colors[0])) | 0xFF000000;
                Arrays.fill(out, bg);
            } else {
                Arrays.fill(out, 0);
            }
            return 0;
        }

        Color[] charbuf = new Color[64];
        byte[] ncgrTile = ncgr.getTiles()[tileNumber];
        for (int i = 0; i < 64; i++) {
            if (ncgrTile[i] != 0 || !transparent) {
                int colIndex = ncgrTile[i] & 0xFF;
                Color c;
                if (colIndex > 0 && colIndex < nclr.getNumColors()) {
                    c = palette[colIndex];
                } else if (colIndex == 0 && !transparent){
                    c = nclr.getColor(0);
                }  else {
                    c = new Color(0);
                }
                charbuf[i] = c;
            } else {
                charbuf[i] = new Color(0);
            }
        }

        // Copy out
        if (transform == 0) {
            // Copy straight
            System.arraycopy(charbuf, 0, out, 0, charbuf.length);
        } else {
            // Complement X and/or Y coordinates when copying
            int srcXor = 0;
            if ((transform & TILE_FLIPX) != 0) srcXor ^= 7;
            if ((transform & TILE_FLIPY) != 0) srcXor ^= 7 << 3;
            for (int i = 0; i < 64; i++) {
                int src = i ^ srcXor;
                out[i] = charbuf[src];
            }
        }
        return 0;
    }


    private static final int TILE_FLIPX = 1;
    private static final int TILE_FLIPY = 2;

    private static final int PALVIEWER_SELMODE_2D = 1;

    public Color[] renderNscrBits(int tileBase, boolean transparent) {
        image = new BufferedImage(width,height,TYPE_INT_ARGB);
        Color[] bits = new Color[width * height];
        Arrays.fill(bits, new Color(0));

        int tilesX = width >> 3;
        int tilesY = height >> 3;

        Color[] block = new Color[64];

        for (int y = 0; y < tilesY; y++) {
            int offsetY = y << 3;
            for (int x = 0; x < tilesX; x++) {
                int offsetX = x << 3;

                int[] tileNo = new int[1];
                nscrGetTileEx(tileBase, x, y, block, tileNo, transparent);
                int dwDest = x * 8 + y * 8 * width;

                for (int h = 0; h < 8; h++) {
                    int src = h << 3;
                    int dest = dwDest + h * width;
                    System.arraycopy(block, src, bits, dest, 8/*32 bytes*/);
                    for (int w = 0; w < 8; w++) {
                        image.setRGB(offsetX+w, offsetY+h, block[src+w].getRGB());
                    }
                }
            }
        }

        return bits;
    }

    public BufferedImage renderNscr(int tileBase, boolean transparent) {
        if (ncgr == null) {
            return null;
        }
        renderNscrBits(tileBase, transparent);
        return image;
    }

    @Override
    protected void readFile(MemBuf.MemBufReader reader) throws NitroException {
        // headerSize ||| uint32_t offset = *(uint16_t *) (buffer + 0xC);
        // NnsG2dGetSectionByMagic
        byte[] idBytes = reader.readBytes(4);
        this.id = new String(idBytes, StandardCharsets.UTF_8); // *(uint32_t *) (buffer + offset);
        this.sectionSize = reader.readUInt32(); // uint32_t thisBlockSize = *(uint32_t *) (block + 4);
        // NnsG2dFindBlockBySignature
        this.width = reader.readUInt16(); // *(uint16_t *) (scrn + 0x0);
        this.height = reader.readUInt16(); // *(uint16_t *) (scrn + 0x2);
        this.padding = reader.readUInt32(); //  *(uint32_t *) (scrn + 0x4);
        this.dataSize = reader.readUInt32(); // int tileDataSize = *(uint32_t *) (sChar + 0x10);
        this.data = new int[(int) dataSize / 2]; // 0xC -> 0xC + dwDataSize
        this.mapData = new NTFS[(int) dataSize / 2]; // better formatted data

        // datasize is in bytes, we want them as uint16s so we take two bytes at a time
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = reader.readUInt16();
            this.mapData[i] = createMapInfo(this.data[i]);
        }
        this.highestIndex = computeHighestCharacter();
        if (this.ncgr != null && this.highestIndex >= this.ncgr.getTiles().length) {
            this.tileBase = this.highestIndex + 1 - this.ncgr.getTiles().length;
        }
        if (this.ncgr != null) {
            createImage();
        }
    }

    /**
     * Generates an object representation of an NSCR file from a file on disk
     *
     * @param path a <code>String</code> containing the path to a NSCR file on disk
     * @return an <code>IndexedImage</code> object
     */
    public static NSCR fromFile(String path) throws NitroException {
        return fromFile(new File(path));
    }

    /**
     * Generates an object representation of an NSCR file from a file on disk
     *
     * @param file a <code>File</code> containing the path to a NSCR file on disk
     * @return an <code>IndexedImage</code> object
     */
    public static NSCR fromFile(File file) throws NitroException {
        return (NSCR) NFSFactory.fromFile(file);
    }
}
