package com.szadowsz.rotom4j.file.nitro.nanr;

import com.szadowsz.rotom4j.binary.array.ByteArrayData;
import com.szadowsz.rotom4j.binary.array.ByteArrayEditableData;
import com.szadowsz.rotom4j.NFSFactory;
import com.szadowsz.rotom4j.compression.CompFormat;
import com.szadowsz.rotom4j.file.nitro.ImageableWithGraphic;
import com.szadowsz.rotom4j.file.RotomFormat;
import com.szadowsz.rotom4j.file.nitro.nanr.anime.AnimeSequence;
import com.szadowsz.rotom4j.file.nitro.nanr.anime.FrameData;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.nitro.BaseNFSFile;
import com.szadowsz.rotom4j.file.nitro.ncer.NCER;
import com.szadowsz.rotom4j.file.nitro.ncer.cells.CellInfo;
import com.szadowsz.rotom4j.file.nitro.nclr.NCLR;
import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;
import com.szadowsz.rotom4j.binary.io.reader.MemBuf;
import com.szadowsz.rotom4j.utils.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class NANR extends BaseNFSFile implements ImageableWithGraphic {
    private static final Logger logger = LoggerFactory.getLogger(NANR.class);

    protected static final int BACKGROUND_WIDTH = 512;
    protected static final int BACKGROUND_HEIGHT = 256;

    // ABNK

    // header
    private String abnkMagic;
    private long abnkSectionSize;

    // Data
    private int nAnimations;
    private int nTotalFrames;
    private int animationsOffset;
    private int framesHeaderOffset;
    private int framesDataOffset;
    private byte[] padding1;

    private AnimeSequence[] sequences;  //animation sequences

    private NCER ncer;

    // LABL section
    private String lablID;
    private long lablSectionSize;
    private byte[] labels;

    // UEXT section
    private String uextID;
    private long  uextSectionSize;
    private byte[] uextData;

    /**
     * Generates an object representation of an NANR file from a file on disk
     *
     * @param path a <code>String</code> containing the path to a NANR file on disk
     * @return an <code>NANR</code> object
     */
    public static NANR fromFile(String path) throws NitroException {
        return fromFile(new File(path));
    }

    /**
     * Generates an object representation of an NANR file from a file on disk
     *
     * @param file a <code>File</code> containing the path to a NANR file on disk
     * @return an <code>NANR</code> object
     */
    public static NANR fromFile(File file) throws NitroException {
        return (NANR) NFSFactory.fromFile(file);
    }

    public NANR(String path) throws NitroException {
        super(RotomFormat.NANR, path);
        MemBuf dataBuf = MemBuf.create(data);
        MemBuf.MemBufReader reader = dataBuf.reader();
        int fileSize = dataBuf.writer().getPosition();
        logger.debug("\nNANR file, " + fileFullName + ", initialising with size of " + fileSize + " bytes");

        readGenericNtrHeader(reader);

        File[] ncers = new File(path).getParentFile().listFiles(f -> f.getName().endsWith(".NCER")  &&
                f.getName().substring(0, f.getName().lastIndexOf('.')).equals(this.objName));
        if (ncers != null && ncers.length > 0) {
            logger.debug("Found corresponding NCER file, " + ncers[0]);
            this.ncer = NCER.fromFile(ncers[0]);
            logger.debug("Read NCER file\n");
        }

        // reader position is now 0x10
        readFile(reader);
    }

    public NANR(String fileName, ByteArrayEditableData compData) throws NitroException {
        super(RotomFormat.NANR, fileName, compData);
        MemBuf dataBuf = MemBuf.create(data);
        MemBuf.MemBufReader reader = dataBuf.reader();
        int fileSize = dataBuf.writer().getPosition();
        logger.debug("\nNANR file, " + fileName + ", initialising with size of " + fileSize + " bytes");

        readGenericNtrHeader(reader);

        // reader position is now 0x10
        readFile(reader);
    }

    private void readFrames(MemBuf.MemBufReader reader, AnimeSequence ani, int frameHeadStart, int aniNum, int frameDataStart) {
        for (int j = 0; j < ani.nFrames; j++) {
            FrameData frame = ani.frames[j] = new FrameData();

            // Frame header
            frame.headerPos = frameHeadStart + j * 8;
            reader.setPosition(frame.headerPos);

            frame.dataOffset = reader.readUInt32();
            logger.info("Animation " + aniNum + " Frame " + j + " Data Offset: " + frame.dataOffset);

            frame.frameDuration = reader.readUInt16();
            logger.info("Animation " + aniNum + " Frame " + j + " Duration: " + frame.frameDuration);

            frame.constant = reader.readUInt16();
            logger.info("Animation " + aniNum + " Frame " + j + " Constant: " + frame.constant);

            // Frame Data
            reader.setPosition(frameDataStart + frame.dataOffset);

            frame.cellIndex = reader.readUInt16();
            logger.info("Animation " + aniNum + " Frame " + j + " Cell: " + frame.cellIndex);

            int og = reader.getPosition();
            int tmp = reader.readUInt16();
            if (tmp == 52428) { // 0xCCCC
                frame.type = 0;
                logger.info("Animation " + aniNum + " Frame " + j + " Type: 0");
                frame.garbage = tmp;
            } else if (tmp == 48879) { // 0xBEEF
                frame.type = 2;
                logger.info("Animation " + aniNum + " Frame " + j + " Type: 2");
                frame.xDisplace = reader.readUInt16();
                frame.yDisplace = reader.readUInt16();
            } else {
                reader.setPosition(og);
                frame.type = 1;
                logger.info("Animation " + aniNum + " Frame " + j + " Type: 1");
                frame.rotation = reader.readUInt16();
                frame.scaleX = reader.readInt();
                frame.scaleY = reader.readInt();
                frame.xDisplace = reader.readUInt16();
                frame.yDisplace = reader.readUInt16();
            }

            if (frame.type>0){
                logger.info("Animation " + aniNum + " Frames " + j + " xDisplace: " + frame.xDisplace);
                logger.info("Animation " + aniNum + " Frames " + j + " yDisplace: " + frame.yDisplace);
            }
        }
    }

    @Override
    protected void readFile(MemBuf.MemBufReader reader) throws NitroException {
        abnkMagic = reader.readString(4);
        if (!abnkMagic.equals("KNBA")) {
            throw new RuntimeException("Not a valid NANR file.");
        }
        abnkSectionSize = reader.readUInt32();

        int start = reader.getPosition();
        logger.info("ABNK offset start @ " + start);

        int labl = findBlockBySignature(reader, "LABL");
        logger.info("Found LABL section @ " + labl);
        int uext = findBlockBySignature(reader, "UEXT");
        logger.info("Found UEXT section @ " + uext);

        nAnimations = reader.readUInt16();
        // options->sequenceCount = data[0x18] | (data[0x19] << 8);
        logger.info("ABNK Animations Count: " + nAnimations);

        nTotalFrames = reader.readUInt16();
        //    options->frameCount = data[0x1A] | (data[0x1B] << 8);
        logger.info("ABNK Frame Count: " + nTotalFrames);

        animationsOffset = reader.readInt();
        logger.info("ABNK Animations Offset: " + animationsOffset);
        int aniStart = start + animationsOffset;
        logger.info("ABNK Animations Pos: " + aniStart);

        framesHeaderOffset = reader.readInt();
        logger.info("ABNK Frame Header Offset: " + framesHeaderOffset);
        int frameHeadStart = start + framesHeaderOffset;
        logger.info("ABNK Frame Header Pos: " + frameHeadStart);

        framesDataOffset = reader.readInt();
        logger.info("ABNK Frame Data Offset: " + framesDataOffset);
        int frameDataStart = start + framesDataOffset;
        logger.info("ABNK Frame Data Pos: " + frameDataStart);

        padding1 = reader.readBytes(8);
        logger.info("Current Position " + reader.getPosition());

        // Animations
        sequences = new AnimeSequence[nAnimations];
        for (int i = 0; i < nAnimations; i++) {
            AnimeSequence ani = sequences[i] = new AnimeSequence();

            // options->sequenceData[i]->frameCount = data[offset] | (data[offset + 1] << 8);
            ani.nFrames = reader.readUInt16(); // 0x0 (0-2) sequenceArray[i].nFrames = byteBuffer.getShort(sequenceArrayOffset + i * 12);
            ani.frames = new FrameData[ani.nFrames];
            logger.info("Animation " + i + " Frames: " + ani.nFrames);

            // options->sequenceData[i]->loopStartFrame = data[offset + 2] | (data[offset + 3] << 8);
            ani.loopStartFrame = reader.readUInt16(); // 0x4 (0-2) sequenceArray[i].startFrameIndex = byteBuffer.getShort(sequenceArrayOffset + i * 12 + 2);

            // options->sequenceData[i]->animationElement = data[offset + 4] | (data[offset + 5] << 8);
            ani.animationElement = reader.readUInt16(); // 0x4 (0-2) sequenceArray[i].startFrameIndex = byteBuffer.getShort(sequenceArrayOffset + i * 12 + 2);
            logger.info("Animation " + i + " Element: " + ani.animationElement);

            // options->sequenceData[i]->animationType = data[offset + 6] | (data[offset + 7] << 8);
            ani.animationType = reader.readUInt16(); // 0x6 (0-2) sequenceArray[i].type = byteBuffer.getInt(sequenceArrayOffset + i * 12 + 4);
            logger.info("Animation " + i + " Type: " + ani.animationType);

            // options->sequenceData[i]->playbackMode = data[offset + 8] | (data[offset + 9] << 8) | (data[offset + 10] << 16) | (data[offset + 11] << 24);
            ani.playbackMode = reader.readInt(); // 0x8 (0-4) sequenceArray[i].mode = byteBuffer.getInt(sequenceArrayOffset + i * 12 + 8);
            logger.info("Animation " + i + " Playback Mode: " + ani.playbackMode);

            // frameOffsets[i] = data[offset + 12] | (data[offset + 13] << 8) | (data[offset + 14] << 16) | (data[offset + 15] << 24);
            ani.startFrameOffset = reader.readInt();  // 0xC (0-4)
            logger.info("Animation " + i + " startFrameOffset: " + ani.startFrameOffset);
            int framePos = start + framesHeaderOffset + ani.startFrameOffset;
            logger.info("Animation " + i + " Start Frame Index: " + framePos);

            int aniPos = reader.getPosition();
            logger.info("Animation " + i + " Current Pos: " + aniPos);
            reader.setPosition(framePos);
            logger.info("Animation " + i + " Frame 0 Pos: " + reader.getPosition());

            // Read Frames
            readFrames(reader, ani, frameHeadStart, i, frameDataStart);
            reader.setPosition(aniPos);
        }

        if (labl != -1){
            logger.debug("Reading LABL section @ " + labl);
            reader.setPosition(labl);
            lablID = reader.readString(4);
            lablSectionSize = reader.readUInt32();
            logger.debug("LABL section size " + lablSectionSize + " bytes");
            int sectionSize = (int) (lablSectionSize-8);
            labels = reader.readBytes(sectionSize);
            logger.debug("Labels " + new String(labels, StandardCharsets.UTF_8));
        }

        if (uext != -1) {
            logger.debug("Reading UEXT section @ " + uext);
            uextID = reader.readString(4);
            uextSectionSize = reader.readUInt32();
            logger.debug("UEXT section size " + uextSectionSize + " bytes");
            int sectionSize = (int) (uextSectionSize-8);
            uextData = reader.readBytes(sectionSize);
        }
    }

    protected int getDrawFrameIndex(AnimeSequence sequence, int frame) {
        if (sequence.nFrames == 0) {
            return 0;
        }
        int drawFrameIndex = sequence.startFrameOffset + frame;
        int mode = sequence.playbackMode;

        switch (sequence.playbackMode) {
            default -> drawFrameIndex = 0; // invalid
            case 1 -> drawFrameIndex = Math.min(drawFrameIndex, sequence.nFrames - 1);  // forward
            case 2 -> drawFrameIndex %= sequence.nFrames;  // forward loop
            case 3 -> { //reverse
                if (drawFrameIndex >= sequence.nFrames) {
                    drawFrameIndex = sequence.nFrames - sequence.frames[sequence.nFrames - 1].frameDuration - 1 - (drawFrameIndex - sequence.nFrames);
                }
                if (drawFrameIndex < 0) {
                    drawFrameIndex = 0;
                }
            }
            case 4 -> {//reverse loop
                //one way: sequence.nFrames
                //other way: sequence.nFrames - sequence.frames[sequence.nFrames - 1].nFrames - sequence.frames[0].nFrames
                int nCycleFrames = sequence.nFrames * 2 - sequence.frames[sequence.nFrames - 1].frameDuration - sequence.frames[0].frameDuration;
                if (nCycleFrames > 0) {
                    drawFrameIndex %= nCycleFrames;
                } else {
                    drawFrameIndex = 0;
                }

                if (drawFrameIndex >= sequence.nFrames)
                    drawFrameIndex = sequence.nFrames - 1 - sequence.frames[sequence.nFrames - 1].frameDuration - (drawFrameIndex - sequence.nFrames);
                if (drawFrameIndex < 0) drawFrameIndex = 0;

            }
        }
        return drawFrameIndex;
    }


    protected int getAnimationFrameFromFrame(AnimeSequence sequence, int drawFrameIndex) {
        for (int i = 0; i < sequence.frames.length; i++) {
            drawFrameIndex -= sequence.frames[i].frameDuration;
            if (drawFrameIndex < 0) {
                return i;
            }
        }
        return sequence.nFrames - 1;
    }

    protected Color[] drawFrame(int sequenceIndex, int frame, int ofsX, int ofsY) throws NitroException {
        if (ncer == null || ncer.getNCGR() == null){
            return null;
        }
        Color[] frameBuffer;
        AnimeSequence sequence = sequences[sequenceIndex];
        int animType = sequence.animationType & 0xFFFF;

        // frame is not referring to the frame index, but rather the current frame of animation
        // using play mode, clamp drawFrameIndex to the range [0, nTotalFrames)
        int drawFrameIndex = getDrawFrameIndex(sequence, frame);

        //next, determine the animation frame that contains drawFrameIndex.
        int frameIndex = getAnimationFrameFromFrame(sequence, drawFrameIndex);
        FrameData frameData = sequence.frames[frameIndex];

        //now, determine the type of frame and how to draw it.
        CellInfo cell = ncer.getCell(frameData.cellIndex);
        if (animType == 0) { //index
            int translateX = 256 - (cell.maxX + cell.minX) / 2, translateY = 128 - (cell.maxY + cell.minY) / 2;
            frameBuffer = ncer.renderCell(cell, ncer.getMappingMode(), translateX + ofsX, translateY + ofsY, false, 1.0f, 0.0f, 0.0f, 1.0f);
        } else if (animType == 1) { //SRT

            //TODO: implement SRT
            float rotation = ((float) frameData.rotation) / 65536.0f * (2.0f * 3.14159f);
            float scaleX = ((float) frameData.scaleX) / 4096.0f;
            float scaleY = ((float) frameData.scaleY) / 4096.0f;

            //compute transformation matrix (don't bother simulating OAM matrix slots)
            float sinR = (float) Math.sin(rotation);
            float cosR = (float) Math.cos(rotation);
            float a = cosR / scaleX;
            float b = sinR / scaleX;
            float c = -sinR / scaleY;
            float d = cosR / scaleY;

            int translateX = 256 - (cell.maxX + cell.minX) / 2, translateY = 128 - (cell.maxY + cell.minY) / 2;
            frameBuffer = ncer.renderCell(cell, ncer.getMappingMode(),translateX + frameData.xDisplace + ofsX, translateY + frameData.yDisplace + ofsY, false, a, b, c, d);
        } else { //index+translation
            int translateX = 256 - (cell.maxX + cell.minX) / 2, translateY = 128 - (cell.maxY + cell.minY) / 2;
            frameBuffer = ncer.renderCell(cell, ncer.getMappingMode(), translateX + frameData.xDisplace + ofsX, translateY + frameData.yDisplace + ofsY, false, 1.0f, 0.0f, 0.0f, 1.0f);
        }

        return frameBuffer;
    }

    @Override
    public int getWidth() {
        if (Configuration.isBackground()){
            return BACKGROUND_WIDTH;
        } else {
             CellInfo.OAM info =  ncer.getCell(0).getOam(0);
            return info.getWidth();
        }
    }

    public int getHeight(){
        if (Configuration.isBackground()){
            return BACKGROUND_HEIGHT;
        } else {
            CellInfo.OAM info = ncer.getCell(0).getOam(0);
            return info.getHeight();
        }
    }

    public NCER getNCER() {
        return ncer;
    }

    @Override
    public NCGR getNCGR() {
        return (ncer != null) ? ncer.getNCGR() : null;
    }

    @Override
    public NCLR getNCLR() {
        return (ncer != null) ? ncer.getNCLR() : NCLR.DEFAULT;
    }


    @Override
    public BufferedImage getImage() {
        return getImage(0,0);
    }

    public BufferedImage getImage(int sequence,int frame) {
        BufferedImage image = new BufferedImage(BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BufferedImage.TYPE_INT_ARGB);;
        if (sequences[sequence].nFrames > 0) {
            try {
                Color[] frameBuffer = drawFrame(sequence, frame, 0, 0);
                int[] bits = new int[frameBuffer.length];
                for (int i = 0; i < bits.length;i++){
                    bits[i] = frameBuffer[i].getRGB();
                }
                image.setRGB(0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, bits, 0, BACKGROUND_WIDTH);
            } catch (NitroException n){

            }
        }
        return image;
    }

    public void setNCER(NCER ncer) {
        this.ncer = ncer;
    }

    @Override
    public void setNCGR(NCGR ncgr) {
        if (ncer != null) {
            this.ncer.setNCGR(ncgr);
        }
    }

    @Override
    public void setNCLR(NCLR nclr) throws NitroException {
        if (ncer != null) {
            ncer.setNCLR(nclr);
        }
    }
}
