package com.szadowsz.nds4j.file.nitro;

import com.szadowsz.nds4j.NFSFactory;
import com.szadowsz.nds4j.compression.CompFormat;
import com.szadowsz.nds4j.data.NFSFormat;
import com.szadowsz.nds4j.data.nfs.ColorFormat;
import com.szadowsz.nds4j.exception.InvalidFileException;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.reader.MemBuf;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class NSCR extends GenericNFSFile {

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
    private byte[] charData;
    private ColorFormat charBitDepth;
    private int tileSize;
    private int bitDepth;
    private float zoom;
    private byte[] imgTiles;

    public NSCR(String path, String fileName, CompFormat comp, byte[] compData, byte[] data) throws NitroException {
        super(NFSFormat.NSCR, path, fileName, comp, compData, data);

        MemBuf buf = MemBuf.create(rawData);

        MemBuf.MemBufReader reader = buf.reader();
        readGenericNtrHeader(reader);
        int headerLength = reader.getPosition();
        reader.setPosition(0);
        this.headerData = reader.readTo(headerLength);

        zoom = 1.0f;

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
            BufferedImage after = new BufferedImage(
                    Math.round(image.getWidth() * zoom),
                    Math.round(image.getHeight() * zoom),
                    TYPE_INT_RGB);
            AffineTransform at = new AffineTransform();
            at.scale(zoom, zoom);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            after = scaleOp.filter(image, after);

            return after;
        } else {
            return null;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public NCGR getNSGR() {
        return this.ncgr;
    }

    public void setNCGR(NCGR ncgr) {
        this.ncgr = ncgr;
    }

    public void setNCLR(NCLR nclr) {
        this.ncgr.setPalette(nclr);
    }

    public void setZoom(float valueFloat) {
        zoom = valueFloat;
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

        //byte[] bits = getIndexedNscrBits(tileBase);
        //image =  renderIndexedNscr(bits);
        //public int[] renderNscrBits(NCLR nclr, int tileBase, int[] widthHeight, int tileMarks, int hlStart, int hlEnd, int hlMode, int selStartX, int selStartY, int selEndX, int selEndY, boolean transparent) {
        //            DWORD *bits = renderNscrBits(this, ncgr, ncgr.getPalette(), tileBase, &outWidth, &outHeight, -1, -1, -1, PALVIEWER_SELMODE_2D,
//                    -1, -1, -1, -1, -1);
        try {
            image = renderNscr(tileBase, false, -1, -1, -1, -1, PALVIEWER_SELMODE_2D, 1, -1, -1, -1, -1, false);
        } catch (Exception e){
            throw new InvalidFileException("Image Rendering Failed",e);
        }
//        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        NTFS[] currMap = new NTFS[mapData.length];
//        for (int i = 0; i < mapData.length; i++) {
//            currMap[i] = new NTFS(mapData[i]);
//        }
//        Byte[] tiles = applyMap(currMap, ncgr.getCharData(), ncgr.getBitDepth(), ncgr.getTileSize());
//        setImageData(tiles, ncgr.getCharBitDepth(), ncgr.getTileSize());

    }
//						if (nclr->nColors <= 256) {
//        //write 8bpp indexed
//        COLOR32 palette[256] = { 0 };
//        int transparentOutput = 1;
//        int paletteSize = 1 << ncgr->nBits;
//        if (nclr != NULL) {
//            for (int i = 0; i < min(nclr->nColors, 256); i++) {
//                int makeTransparent = transparentOutput && ((i % paletteSize) == 0);
//
//                palette[i] = ColorConvertFromDS(nclr->colors[i]);
//                if (!makeTransparent) palette[i] |= 0xFF000000;
//            }
//        }
//        unsigned char *bits = renderNscrIndexed(nscr, ncgr, data->tileBase, &width, &height, TRUE);
//        ImgWriteIndexed(bits, width, height, palette, 256, location);
//        free(bits);
//    }
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

        int tileData = data[y * nWidthTiles + x];
        int tileNumber = tileData & 0x3FF;
        int transform = (tileData >> 10) & 0x3;
        int paletteNumber = (tileData >> 12) & 0xF;
        if (tileNo != null) {
            tileNo[0] = tileNumber;
        }

        // Get palette and base character
        Color[] palette = nclr.getColorPalette(paletteNumber);
        tileNumber -= charBase;

        if (tileNumber >= ncgr.getTileCount() || tileNumber < 0) { // Let's just paint a transparent square
            if (!transparent) {
                Color bg = nclr.colors[0];//ColorConvertFromDS(CREVERSE(nclr.colors[0])) | 0xFF000000;
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
        image = new BufferedImage(width,height,TYPE_INT_RGB);
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

    public BufferedImage renderNscr(int tileBase, boolean drawGrid, int highlightNclr, int highlightTile, int hlStart, int hlEnd, int hlMode, int scale, int selStartX, int selStartY, int selEndX, int selEndY, boolean transparent) {
        if (ncgr == null) {
            return null;
        }

//        if (highlightNclr != -1) {
//            highlightNclr += tileBase;
//        }
        //public int[] renderNscrBits(NCLR nclr, int tileBase, int[] widthHeight, int tileMarks, int hlStart, int hlEnd, int hlMode, int selStartX, int selStartY, int selEndX, int selEndY, boolean transparent) {
        Color[] bits = renderNscrBits(tileBase,/* highlightNclr, hlStart, hlEnd, hlMode, selStartX, selStartY, selEndX, selEndY,*/ transparent);
//
//        int hovX = -1, hovY = -1;
//        if (highlightTile != -1) {
//            hovX = highlightTile % (this.width / 8);
//            hovY = highlightTile / (this.width / 8);
//        }
//        return createTileBitmap2(bits, hovX, hovY, scale, drawGrid, 8, false, true);
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

    class NTFS implements Cloneable {             // Nintedo Tile Format Screen
        public int nTile; //        0-9     (0-1023)    (a bit less in 256 color mode, because there'd be otherwise no room for the bg map)
         public int transform;
        public byte xFlip;  //      10    Horizontal Flip (0=Normal, 1=Mirrored)
        public byte yFlip;  //      11    Vertical Flip   (0=Normal, 1=Mirrored)

        public int nPalette; //    12-15    (0-15)      (Not used in 256 color/1 palette mode)

        public NTFS() {
        }

        public NTFS(NTFS original) {
            nPalette = original.nPalette;
            transform = original.transform;
            xFlip = original.xFlip;
            yFlip = original.yFlip;
            nTile = original.nTile;
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
