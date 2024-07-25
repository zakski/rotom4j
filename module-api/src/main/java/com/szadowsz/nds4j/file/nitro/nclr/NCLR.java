package com.szadowsz.nds4j.file.nitro.nclr;

import com.szadowsz.nds4j.NFSFactory;
import com.szadowsz.nds4j.compression.CompFormat;
import com.szadowsz.nds4j.file.Imageable;
import com.szadowsz.nds4j.file.NFSFormat;
import com.szadowsz.nds4j.file.nitro.nclr.colors.ColorFormat;
import com.szadowsz.nds4j.exception.InvalidFileException;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.GenericNFSFile;
import com.szadowsz.nds4j.reader.MemBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static com.szadowsz.nds4j.utils.ColorUtils.bgr555ToColor;
import static com.szadowsz.nds4j.utils.ColorUtils.colorToBGR555;

/**
 * An object representation of an NCLR file
 */
public class NCLR extends GenericNFSFile implements Imageable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NCLR.class);

    private static final byte[] PAL_HEADER = new byte[]{
            0x54, 0x54, 0x4C, 0x50, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00
    };

    // Default Color Palette for Sprites
    private static final Color[] PAL_16 = new Color[]{
        new Color(144, 144, 144),
                new Color(0, 0, 0),
                new Color(248, 232, 152),
                new Color(240, 224, 80),
                new Color(240, 184, 32),
                new Color(216, 144, 0),
                new Color(152, 80, 0),
                new Color(96, 48, 8),
                new Color(224, 144, 128),
                new Color(224, 88, 64),
                new Color(192, 32, 24),
                new Color(120, 40, 8),
                new Color(112, 122, 128),
                new Color(80, 80, 88),
                new Color(40, 40, 40),
                new Color(248, 248, 248)
    };

    private static final Color IR_COLOR = new Color(72, 144, 160);

    public static final NCLR DEFAULT = generate(256);

    // Palette Header Data

    // Color Bit Depth
    protected ColorFormat bitDepth;
    // Number Of Colors Per Palette
    protected int numColorsPerPalette;
    // Total Number Of Colors
    protected int numColors;
    protected int compNum = 0;
    protected boolean ir = false;

    // Palette Data

    // List of Colors
    protected Color[] colors;
    // List of Colors per Palette
    protected Color[][] paletteColours;

    /**
     * Generate Header Data for Default Palette
     *
     * @param writer data writer
     * @param length file length
     */
    protected static void generateHeader(MemBuf.MemBufWriter writer, long length) {
        int bom = 0xFEFF;
        int version = 1;
        writer.writeString(NFSFormat.NCLR.getLabel()[0]);
        writer.writeShort((short) bom);
        writer.writeShort((short) version);
        writer.writeUInt32(length);
        writer.writeShort((short) NTR_HEADER_SIZE);
        writer.writeShort((short) 1);
    }

    /**
     * Generate Data for Default Palette
     *
     * @param palettes number of palettes
     * @param numColors colors per palette
     * @return byte data
     */
    protected static byte[] generateData(int palettes, int numColors) {
        LOGGER.info("\nGenerating default NCLR with " + palettes + " palettes and " + numColors + "colours");
        MemBuf dataBuf = MemBuf.create();
        MemBuf.MemBufWriter writer = dataBuf.writer();
        Color[] colors;

        if (numColors == 16) {
            colors = PAL_16;
        } else {
            colors = new Color[palettes*numColors];
            for (int i = 0; i < palettes; i++) {
                for (int j = 0; j < numColors; j++) {
                    colors[i*numColors + j] = new Color((j * 8) % numColors, (j * 8) % numColors, (j * 8) % numColors);
                }
            }
        }
        numColors = colors.length;

        int size = numColors * 2; // two bytes per color
        int extSize = size + 0x18 + NTR_HEADER_SIZE;

        generateHeader(writer, extSize);

        // writer position is now 0x10

        writer.write(PAL_HEADER);
        int storedPos = writer.getPosition();

        writer.setPosition(NTR_HEADER_SIZE + 4);
        writer.writeInt(extSize - NTR_HEADER_SIZE); // 0x14
        writer.writeShort((short) ((numColors==16)?0x3:0x4)); // 0x18
        writer.writeByte((byte) (0)); // 0x1A

        writer.setPosition(NTR_HEADER_SIZE + 0x10);
        writer.writeInt(size);

        writer.setPosition(storedPos);

        for (Color color : colors) {
            writer.write(colorToBGR555(color));
        }

        return dataBuf.reader().getBuffer();
    }

    /**
     * Get the NCLR from a file
     *
     * @param path path of the NCLR file
     * @return NCLR object parsed from the file
     * @throws NitroException NCLR object load failed
     */
    public static NCLR fromFile(String path) throws NitroException {
        return fromFile(new File(path));
    }

    /**
     *  Get the NCLR from a file
     *
     * @param file file object representing NCLR
     * @return NCLR object parsed from the file
     * @throws NitroException NCLR object load failed
     */
    public static NCLR fromFile(File file) throws NitroException {
        return (NCLR) NFSFactory.fromFile(file); // TODO guarding
    }

    /**
     * Static Method to generate a new palette
     *
     * @param numColors number of colors needed
     * @return the Palette Object
     */
    public static NCLR generate(int numColors) {
        try {
            return new NCLR(numColors);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Constructor to Use after decompressing file data and assessing its contents
     *
     * @param path the path of the file
     * @param name the name of the file
     * @param comp compression format
     * @param compData the raw data, compressed
     * @param data the raw data, uncompressed
     * @throws NitroException NCLR object load failed
     */
    public NCLR(String path, String name, CompFormat comp, byte[] compData, byte[] data) throws NitroException {
        super(NFSFormat.NCLR, path, name, comp, compData, data);

        MemBuf buf = MemBuf.create();
        buf.writer().write(rawData);
        int fileSize = buf.writer().getPosition();
        LOGGER.info("\nNCLR file, " + fileName + ", initialising with size of " + fileSize + " bytes");

        MemBuf.MemBufReader reader = buf.reader();
        readGenericNtrHeader(reader);
        int headerLength = reader.getPosition();
        reader.setPosition(0);
        this.headerData = reader.readTo(headerLength);

        LOGGER.info("Reading NCLR file data");
        readFile(reader);
    }

    /**
     * Creates a default grayscale palette with the given number of colors
     *
     * @param numColors an <code>int</code>
     */
    protected NCLR(int numColors) throws IOException {
        super(NFSFormat.NCLR, null, generateData((numColors>16)?16:1,numColors));
    }

    @Override
    protected void readFile(MemBuf.MemBufReader reader) throws InvalidFileException {
        // reader position is now 0x10
        int indexPLTT = findBlockBySignature(reader,"PLTT");
        LOGGER.info("PLTT Index: " + indexPLTT);
        int indexPCMP = findBlockBySignature(reader,"PCMP");
        LOGGER.info("PCMP Index: " + indexPCMP);

        // 0x0 - palette data
        String paletteMagic = reader.readString(4);
        LOGGER.info("Palette Magic: " + paletteMagic);

        if (!paletteMagic.equals("TTLP")) {
            throw new InvalidFileException("Not a valid NCLR or NCPR file.");
        }

        if ((fileSize - 0x28) % 2 != 0) {
            throw new InvalidFileException(String.format("The file size (%d) is not a multiple of 2.\n", fileSize));
        }

        // 0x4 - Section Size
        long paletteSectionSize = reader.readUInt32();
        LOGGER.info("Palette Section Size: " + paletteSectionSize);

        // 0x8 - Palette Bit Depth
        bitDepth = ColorFormat.valueOf(reader.readUInt16());
        LOGGER.info("Palette Bit Depth: " + bitDepth.name());

        int compNum = reader.readByte();
        reader.skip(1);

        // 0xC - Padding? Always ( 0x000000)
        int paletteUnknown1 = reader.readInt();
        LOGGER.info("Padding: " + paletteUnknown1);

        // 0x10 - Palette Data Size
        long paletteLength = reader.readUInt32();

        if (paletteLength == 0 || paletteLength > paletteSectionSize) {
            paletteLength = paletteSectionSize - 0x18;
        }
        LOGGER.info("Palette Length: " + paletteLength);

        // 0x14 - Colors Per Palette
        long colorStartOffset = reader.readUInt32();
        LOGGER.info("Color Offset: " + colorStartOffset);

        // Set number of colours based on bit depth.
        this.numColorsPerPalette = (bitDepth == ColorFormat.colors16)? 16 : 256;
        this.numColors = numColorsPerPalette;
        LOGGER.info("Colors Per Palette: " + numColorsPerPalette);
        // Then adjust it based on the palette length
        if (paletteLength / 2 > numColorsPerPalette) {
            this.numColors = (int) (paletteLength / 2);
        }
        LOGGER.info("Total Number Of Colors: " + numColors);

        // Initialise Colours
        this.colors = new Color[numColors];

        if (numColors > 16 && numColors % numColorsPerPalette == 0){
            this.paletteColours = new Color[numColors/numColorsPerPalette][];
            for (int i = 0; i < numColors/numColorsPerPalette;i++) {
                this.paletteColours[i] = new Color[numColorsPerPalette];
            }
        }
        this.compNum = compNum;

        reader.setPosition(0x18 + colorStartOffset);
        for (int i = 0; i < colors.length; i++) {
            this.colors[i] = bgr555ToColor((byte) reader.readByte(), (byte) reader.readByte());
            if (numColors > 16 && numColors % numColorsPerPalette == 0){
                this.paletteColours[i/numColorsPerPalette][i%numColorsPerPalette] = this.colors[i];
            }
        }

        if (colors[colors.length - 1].equals(IR_COLOR)) {//honestly no clue why this is a thing
            this.ir = true;
        }
    }

    /**
     * Get a Color
     *
     * @param index color number
     * @return color
     */
    public Color getColor(int index) {
        if (index >= numColors) {
            return new Color(0);
        }
        return colors[index];
    }

    /**
     * Get a Color Palette
     *
     * @param index palette number
     * @return the array of colors representing the palette
     */
    public Color[] getColorPalette(int index) {
        if (index < paletteColours.length) {
            return paletteColours[index];
        } else {
            Color[] res = new Color[numColorsPerPalette];
            Arrays.fill(res,new Color(0));
            return res;
        }
    }

    /**
     * Get a Color from a Palette
     *
     * @param palIndex palette number
     * @param colIndex colour number
     * @return the color from the palette
     */
    public Color getColorInPalette(int palIndex, int colIndex) {
        if (palIndex < paletteColours.length){
            if (colIndex < numColorsPerPalette) {
                return paletteColours[palIndex][colIndex];
            }
        }
        return new Color(0);
    }

    @Override
    public int getWidth() {
        return 16*16;
    }

    @Override
    public int getHeight() {
        return (numColorsPerPalette/16)*16;
    }

    @Override
    public BufferedImage getImage() {
        return getImage(0);
    }

    public BufferedImage getImage(int paletteNum){
        Color[] palette = paletteColours[paletteNum];

        int heightInCol = palette.length/ 16;
        heightInCol = Math.max(1,heightInCol);

        BufferedImage image = new BufferedImage(16*16,heightInCol*16, BufferedImage.TYPE_INT_ARGB);

        for(int i = 0; i < numColorsPerPalette; i++) {
            Color c = palette[i];
            int [] rgbArray = new int[16*16];
            Arrays.fill(rgbArray,c.getRGB());

            // |00|01|02|03|04|05|06|07|08|09|10|11|12|13|14|15|
            // |16|
            // xpos = 0, ypos = 1
            // xpos = 0, ypos = 16

            // |00|01|02|03|04|05|06|07|08|09|10|11|12|13|14|15|
            // |16|17|
            // xpos = 1, ypos = 1
            // xpos = 16, ypos = 16

            int xpos = (i % 16)*16;
            int ypos = (i / 16) * 16;

            image.setRGB(xpos,ypos,16,16,rgbArray,0,16);
        }
        return image;
    }

    /**
     * Get Number of Colors stored in the NCLR
     *
     * @return color count
     */
    public int getNumColors() {
        return numColors;
    }

    /**
     * Get Number of Palettes stored in the NCLR
     *
     * @return palette count
     */
    public int getPaletteCount() {
        return paletteColours.length;
    }

    /**
     * Get Number of Colors in Palettes stored in the NCLR
     *
     * @return color count
     */
    public int getNumColorsPerPalette() {
        return numColorsPerPalette;
    }

    /**
     * Set Color
     *
     * @param index color number
     * @param color int representation of color
     */
    public void setColor(int index, int color) {
        colors[index] = new Color(color);
        paletteColours[index/numColorsPerPalette][index%numColorsPerPalette] = colors[index];
    }
}