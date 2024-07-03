//package com.szadowsz.nds4j.file.nitro;
//
//import java.util.Arrays;
//
//public class CellRenderer {
//
//    public static void chrGetChar(NCGR ncgr, int chno, CHAR_VRAM_TRANSFER transfer, byte[] out) {
//        // if transfer == null, don't simulate any VRAM transfer operation
//        if (transfer == null) {
//            if (chno < ncgr.nTiles) {
//                System.arraycopy(ncgr.tiles[chno], 0, out, 0, 64);
//            } else {
//                Arrays.fill(out, (byte) 0);
//            }
//            return;
//        }
//
//        // get character source address
//        int chrSize = 8 * ncgr.nBits;
//        int srcAddr = chno * chrSize;
//        if ((srcAddr + chrSize) < transfer.dstAddr || srcAddr >= (transfer.dstAddr + transfer.size)) {
//            if (chno < ncgr.nTiles) {
//                System.arraycopy(ncgr.tiles[chno], 0, out, 0, 64);
//            } else {
//                Arrays.fill(out, (byte) 0);
//            }
//            return;
//        }
//
//        // character is within the destination region. For bytes within the region, copy from src.
//        // TODO: handle bitmapped graphics transfers too
//        for (int i = 0; i < 64; i++) {
//            // copy ncgr.tiles[chno][i] to out[i]
//            int pxaddr = srcAddr + (i >> (ncgr.nBits == 4 ? 1 : 0));
//            if (pxaddr >= transfer.dstAddr && pxaddr < (transfer.dstAddr + transfer.size)) {
//                // in transfer destination
//                pxaddr = pxaddr - transfer.dstAddr + transfer.srcAddr;
//                int transferChr = pxaddr / chrSize;
//                int transferChrPxOffset = pxaddr % chrSize;
//                int pxno = transferChrPxOffset;
//                if (ncgr.nBits == 4) {
//                    pxno <<= 1;
//                    pxno += (i & 1);
//                }
//                out[i] = ncgr.tiles[transferChr][pxno];
//            } else {
//                // out of transfer destination
//                out[i] = ncgr.tiles[chno][i];
//            }
//        }
//    }
//
//    private static int chriRenderCharacter(byte[] chr, int depth, int palette, NCLR nclr, int[] out, boolean transparent) {
//        for (int i = 0; i < 64; i++) {
//            int index = chr[i] & 0xFF;
//            if (index != 0 || !transparent) {
//                int w = 0;
//                if (nclr != null && (index + (palette << depth)) < nclr.nColors) {
//                    w = nclr.colors[index + (palette << depth)];
//                }
//                out[i] = ColorConverter.convertFromDS(Integer.reverseBytes(w)) | 0xFF000000;
//            } else {
//                out[i] = 0;
//            }
//        }
//        return 0;
//    }
//
//    public static int chrRenderCharacter(NCGR ncgr, NCLR nclr, int chNo, int[] out, int previewPalette, boolean transparent) {
//        if (chNo < ncgr.nTiles) {
//            byte[] tile = ncgr.tiles[chNo];
//            return chriRenderCharacter(tile, ncgr.nBits, previewPalette, nclr, out, transparent);
//        } else {
//            Arrays.fill(out, 0);
//            return 1;
//        }
//    }
//
//    public static int chrRenderCharacterTransfer(NCGR ncgr, NCLR nclr, int chNo, CHAR_VRAM_TRANSFER transfer, int[] out, int palette, boolean transparent) {
//        // if transfer == null, render as normal
//        if (transfer == null) {
//            return chrRenderCharacter(ncgr, nclr, chNo, out, palette, transparent);
//        }
//
//        // else, read graphics and render
//        byte[] buf = new byte[64];
//        chrGetChar(ncgr, chNo, transfer, buf);
//        return chriRenderCharacter(buf, ncgr.nBits, palette, nclr, out, transparent);
//    }
//
//    public static int cellDecodeOamAttributes(NCER_CELL_INFO info, NCER_CELL cell, int oam) {
//        if (oam >= cell.nAttribs) {
//            return 1;
//        }
//
//        short attr0 = cell.attr[oam * 3];
//        short attr1 = cell.attr[oam * 3 + 1];
//        short attr2 = cell.attr[oam * 3 + 2];
//
//        info.x = attr1 & 0x1FF;
//        info.y = attr0 & 0xFF;
//        int shape = attr0 >> 14;
//        int size = attr1 >> 14;
//
//        int[] dimensions = CellUtil.getObjDimensions(shape, size);
//        info.width = dimensions[0];
//        info.height = dimensions[1];
//        info.size = size;
//        info.shape = shape;
//
//        info.characterName = attr2 & 0x3FF;
//        info.priority = (attr2 >> 10) & 0x3;
//        info.palette = (attr2 >> 12) & 0xF;
//        info.mode = (attr0 >> 10) & 3;
//        info.mosaic = ((attr0 >> 12) & 1) != 0;
//
//        boolean rotateScale = ((attr0 >> 8) & 1) != 0;
//        info.rotateScale = rotateScale;
//        if (rotateScale) {
//            info.flipX = false;
//            info.flipY = false;
//            info.doubleSize = ((attr0 >> 9) & 1) != 0;
//            info.disable = false;
//            info.matrix = (attr1 >> 9) & 0x1F;
//        } else {
//            info.flipX = ((attr1 >> 12) & 1) != 0;
//            info.flipY = ((attr1 >> 13) & 1) != 0;
//            info.doubleSize = false;
//            info.disable = ((attr0 >> 9) & 1) != 0;
//            info.matrix = 0;
//        }
//
//        boolean is8 = ((attr0 >> 13) & 1) != 0;
//        info.characterBits = 4;
//        if (is8) {
//            info.characterBits = 8;
//        }
//
//        return 0;
//    }
//
//    // ... (rest of the methods would be similarly translated)
//}
//import java.util.Arrays;
//
//class CellRenderer {
//    static void cellRenderObj(NCERCellInfo info, int mapping, NCGR ncgr, NCLR nclr, CharVramTransfer vramTransfer, int[] out, int[] width, int[] height, boolean checker) {
//        width[0] = info.width;
//        height[0] = info.height;
//
//        int tilesX = width[0] / 8;
//        int tilesY = height[0] / 8;
//
//        if (ncgr != null) {
//            int charSize = ncgr.nBits * 8;
//            int ncgrStart = NCGR.chname(info.characterName, mapping, ncgr.nBits);
//            for (int y = 0; y < tilesY; y++) {
//                for (int x = 0; x < tilesX; x++) {
//                    int[] block = new int[64];
//
//                    int bitsOffset = x * 8 + (y * 8 * tilesX * 8);
//                    int index;
//                    if (NCGR.is2D(mapping)) {
//                        int ncx = x + ncgrStart % ncgr.tilesX;
//                        int ncy = y + ncgrStart / ncgr.tilesX;
//                        index = ncx + ncgr.tilesX * ncy;
//                    } else {
//                        index = ncgrStart + x + y * tilesX;
//                    }
//
//                    ChrRenderer.renderCharacterTransfer(ncgr, nclr, index, vramTransfer, block, info.palette, true);
//                    for (int i = 0; i < 8; i++) {
//                        System.arraycopy(block, i * 8, out, bitsOffset + tilesX * 8 * i, 8);
//                    }
//                }
//            }
//        }
//
//        // render checker
//        if (checker) {
//            for (int i = 0; i < info.width * info.height; i++) {
//                int x = i % info.width;
//                int y = i / info.width;
//                int ch = ((x >> 2) ^ (y >> 2)) & 1;
//                int c = out[i];
//                if ((c & 0xFF000000) == 0) {
//                    out[i] = ch != 0 ? 0xFFFFFF : 0xC0C0C0;
//                }
//            }
//        }
//    }
//
//    static int[] cellRenderCell(int[] px, NCERCell cell, int mapping, NCGR ncgr, NCLR nclr, CharVramTransfer vramTransfer, int xOffs, int yOffs, boolean checker, int outline, float a, float b, float c, float d) {
//        int[] block = new int[64 * 64];
//        for (int i = cell.nAttribs - 1; i >= 0; i--) {
//            NCERCellInfo info = new NCERCellInfo();
//            int[] entryWidth = new int[1];
//            int[] entryHeight = new int[1];
//            CellDecoder.decodeOamAttributes(info, cell, i);
//
//            cellRenderObj(info, mapping, ncgr, nclr, vramTransfer, block, entryWidth, entryHeight, false);
//
//            // HV flip? Only if not affine!
//            if (!info.rotateScale) {
//                int[] temp = new int[64];
//                if (info.flipY) {
//                    for (int j = 0; j < info.height / 2; j++) {
//                        System.arraycopy(block, j * info.width, temp, 0, info.width);
//                        System.arraycopy(block, (info.height - 1 - j) * info.width, block, j * info.width, info.width);
//                        System.arraycopy(temp, 0, block, (info.height - 1 - j) * info.width, info.width);
//                    }
//                }
//                if (info.flipX) {
//                    for (int j = 0; j < info.width / 2; j++) {
//                        for (int k = 0; k < info.height; k++) {
//                            int left = block[j + k * info.width];
//                            block[j + k * info.width] = block[info.width - 1 - j + k * info.width];
//                            block[info.width - 1 - j + k * info.width] = left;
//                        }
//                    }
//                }
//            }
//
//            if (!info.disable) {
//                int x = info.x;
//                int y = info.y;
//                // adjust for double size
//                if (info.doubleSize) {
//                    x += info.width / 2;
//                    y += info.height / 2;
//                }
//                // copy data
//                if (!info.rotateScale) {
//                    for (int j = 0; j < info.height; j++) {
//                        int _y = (y + j + yOffs) & 0xFF;
//                        for (int k = 0; k < info.width; k++) {
//                            int _x = (x + k + xOffs) & 0x1FF;
//                            int col = block[j * info.width + k];
//                            if (col >>> 24 != 0) {
//                                px[_x + _y * 512] = block[j * info.width + k];
//                            }
//                        }
//                    }
//                } else {
//                    // transform about center
//                    int realWidth = info.width << (info.doubleSize ? 1 : 0);
//                    int realHeight = info.height << (info.doubleSize ? 1 : 0);
//                    int cx = realWidth / 2;
//                    int cy = realHeight / 2;
//                    int realX = x - (realWidth - info.width) / 2;
//                    int realY = y - (realHeight - info.height) / 2;
//                    for (int j = 0; j < realHeight; j++) {
//                        int destY = (realY + j + yOffs) & 0xFF;
//                        for (int k = 0; k < realWidth; k++) {
//                            int destX = (realX + k + xOffs) & 0x1FF;
//
//                            int srcX = (int) ((k - cx) * a + (j - cy) * b) + cx;
//                            int srcY = (int) ((k - cx) * c + (j - cy) * d) + cy;
//
//                            if (info.doubleSize) {
//                                srcX -= realWidth / 4;
//                                srcY -= realHeight / 4;
//                            }
//                            if (srcX >= 0 && srcY >= 0 && srcX < info.width && srcY < info.height) {
//                                int src = block[srcY * info.width + srcX];
//                                if (src >>> 24 != 0) px[destX + destY * 512] = src;
//                            }
//                        }
//                    }
//                }
//
//                // outline
//                if (outline == -2 || outline == i) {
//                    int outlineWidth = info.width << (info.doubleSize ? 1 : 0);
//                    int outlineHeight = info.height << (info.doubleSize ? 1 : 0);
//                    for (int j = 0; j < outlineWidth; j++) {
//                        int _x = (j + info.x + xOffs) & 0x1FF;
//                        int _y = (info.y + yOffs) & 0xFF;
//                        int _y2 = (_y + outlineHeight - 1) & 0xFF;
//                        px[_x + _y * 512] = 0xFE000000;
//                        px[_x + _y2 * 512] = 0xFE000000;
//                    }
//                    for (int j = 0; j < outlineHeight; j++) {
//                        int _x = (info.x + xOffs) & 0x1FF;
//                        int _y = (info.y + j + yOffs) & 0xFF;
//                        int _x2 = (_x + outlineWidth - 1) & 0x1FF;
//                        px[_x + _y * 512] = 0xFE000000;
//                        px[_x2 + _y * 512] = 0xFE000000;
//                    }
//                }
//            }
//        }
//
//        // apply checker background
//        if (checker) {
//            for (int y = 0; y < 256; y++) {
//                for (int x = 0; x < 512; x++) {
//                    int index = y * 512 + x;
//                    if (px[index] >>> 24 == 0) {
//                        int p = ((x >> 2) ^ (y >> 2)) & 1;
//                        px[index] = (p != 0) ? 0xFFFFFF : 0xC0C0C0;
//                    }
//                }
//            }
//        }
//        return px;
//    }
//}
//public class NcerViewer extends JPanel {
//    private NCER ncer;
//    private NCGR ncgr;
//    private NCLR nclr;
//    private int cell;
//    private int oam;
//    private boolean showObjOutline;
//    private boolean showCellBounds;
//    private boolean showGuidelines;
//    private int[] frameBuffer;
//    private Configuration configuration;
//
//    public NcerViewer() {
//        frameBuffer = new int[256 * 512];
//    }
//
//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        Graphics2D g2d = (Graphics2D) g;
//
//        NCER_CELL cell = ncer.cells[this.cell];
//        NCER_CELL_INFO info = new NCER_CELL_INFO();
//        CellDecodeOamAttributes(info, cell, oam);
//
//        Arrays.fill(frameBuffer, 0);
//        CHAR_VRAM_TRANSFER transferEntry = null;
//        if (ncer.vramTransfer != null) {
//            transferEntry = ncer.vramTransfer[this.cell];
//        }
//        int[] bits = CellRenderCell(frameBuffer, ncer.cells[this.cell], ncer.mappingMode, ncgr, nclr, transferEntry,
//                256, 128, configuration.renderTransparent, showObjOutline ? oam : -1, 1.0f, 0.0f, 0.0f, 1.0f);
//
//        // draw lines if needed
//        if (showCellBounds) {
//            int minX = (cell.minX + 256) & 0x1FF;
//            int maxX = (cell.maxX + 256 - 1) & 0x1FF;
//            int minY = (cell.minY + 128) & 0xFF;
//            int maxY = (cell.maxY + 128 - 1) & 0xFF;
//
//            for (int i = 0; i < 256; i++) {
//                if ((bits[i * 512 + minX] >>> 24) != 0xFE) bits[i * 512 + minX] = 0xFF0000FF;
//                if ((bits[i * 512 + maxX] >>> 24) != 0xFE) bits[i * 512 + maxX] = 0xFF0000FF;
//            }
//            for (int i = 0; i < 512; i++) {
//                if ((bits[minY * 512 + i] >>> 24) != 0xFE) bits[minY * 512 + i] = 0xFF0000FF;
//                if ((bits[maxY * 512 + i] >>> 24) != 0xFE) bits[maxY * 512 + i] = 0xFF0000FF;
//            }
//        }
//
//        // draw solid color background if transparency disabled
//        if (!configuration.renderTransparent) {
//            int bgColor = 0;
//            if (nclr != null) bgColor = ColorConvertFromDS(nclr.colors[0]);
//            bgColor = Integer.reverseBytes(bgColor);
//            for (int i = 0; i < 256 * 512; i++) {
//                int c = bits[i];
//                if ((c >>> 24) == 0) bits[i] = bgColor;
//                else if ((c >>> 24) == 0xFE) bits[i] = ((bgColor + 0x808080) & 0xFFFFFF) | 0xFE000000;
//            }
//        }
//
//        // draw editor guidelines if enabled
//        if (showGuidelines) {
//            // dotted lines at X=0 and Y=0
//            int centerColor = 0xFF0000; // red
//            int auxColor = 0x00FF00; // green
//            int minorColor = 0x002F00;
//
//            for (int i = 0; i < 512; i++) {
//                // major guideline
//                int c = bits[i + 128 * 512];
//                if ((c >>> 24) != 0xFE) if ((i & 1) != 0) bits[i + 128 * 512] ^= centerColor;
//
//                // auxiliary guidelines
//                c = bits[i + 64 * 512];
//                if ((c >>> 24) != 0xFE) if ((i & 1) != 0) bits[i + 64 * 512] ^= auxColor;
//                c = bits[i + 192 * 512];
//                if ((c >>> 24) != 0xFE) if ((i & 1) != 0) bits[i + 192 * 512] ^= auxColor;
//
//                // minor guidelines
//                for (int j = 0; j < 256; j += 8) {
//                    if (j == 64 || j == 128 || j == 192) continue;
//
//                    c = bits[i + j * 512];
//                    if ((c >>> 24) != 0xFE) if ((i & 1) != 0) bits[i + j * 512] ^= minorColor;
//                }
//            }
//            for (int i = 0; i < 256; i++) {
//                // major guideline
//                int c = bits[256 + i * 512];
//                if ((c >>> 24) != 0xFE) if ((i & 1) != 0) bits[256 + i * 512] ^= centerColor;
//
//                // auxiliary guidelines
//                c = bits[128 + i * 512];
//                if ((c >>> 24) != 0xFE) if ((i & 1) != 0) bits[128 + i * 512] ^= auxColor;
//                c = bits[384 + i * 512];
//                if ((c >>> 24) != 0xFE) if ((i & 1) != 0) bits[384 + i * 512] ^= auxColor;
//
//                // minor guidelines
//                for (int j = 0; j < 512; j += 8) {
//                    if (j == 128 || j == 256 || j == 384) continue;
//
//                    c = bits[j + i * 512];
//                    if ((c >>> 24) != 0xFE) if ((i & 1) != 0) bits[j + i * 512] ^= minorColor;
//                }
//            }
//        }
//
//        BufferedImage image = new BufferedImage(512, 256, BufferedImage.TYPE_INT_ARGB);
//        image.setRGB(0, 0, 512, 256, bits, 0, 512);
//        g2d.drawImage(image, 0, 0, null);
//
//        int width, height;
//        int[] objBits = new int[info.width * info.height];
//        CellRenderObj(info, ncer.mappingMode, ncgr, nclr, null, objBits, width, height, 1);
//        BufferedImage objImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        objImage.setRGB(0, 0, width, height, objBits, 0, width);
//        g2d.drawImage(objImage, 512 - 69, 256 + 5, null);
//    }
//
//    // Other methods and classes (NCER, NCGR, NCLR, etc.) need to be implemented
//}