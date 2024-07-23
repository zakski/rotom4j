package com.szadowsz.nds4j.file.nitro.nanr;

import com.szadowsz.nds4j.NFSFactory;
import com.szadowsz.nds4j.compression.CompFormat;
import com.szadowsz.nds4j.file.ComplexImageable;
import com.szadowsz.nds4j.file.NFSFormat;
import com.szadowsz.nds4j.file.nitro.nanr.anime.AnimeSequence;
import com.szadowsz.nds4j.file.nitro.nanr.anime.FrameData;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.GenericNFSFile;
import com.szadowsz.nds4j.file.nitro.ncer.NCER;
import com.szadowsz.nds4j.file.nitro.nclr.NCLR;
import com.szadowsz.nds4j.file.nitro.ncgr.NCGR;
import com.szadowsz.nds4j.reader.MemBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;

public class NANR extends GenericNFSFile implements ComplexImageable {
    private static final Logger logger = LoggerFactory.getLogger(NANR.class);

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

    public NANR(String path, String fileName, CompFormat comp, byte[] compData, byte[] data) throws NitroException {
        super(NFSFormat.NANR, path, fileName, comp, compData, data);
        MemBuf dataBuf = MemBuf.create(data);
        MemBuf.MemBufReader reader = dataBuf.reader();
        int fileSize = dataBuf.writer().getPosition();
        logger.debug("\nNANR file, " + fileName + ", initialising with size of " + fileSize + " bytes");

        readGenericNtrHeader(reader);

        File[] ncers = new File(path).getParentFile().listFiles(f -> f.getName().endsWith(".NCER")  &&
                f.getName().substring(0, f.getName().lastIndexOf('.')).equals(this.fileName));
        if (ncers != null && ncers.length > 0) {
            logger.debug("Found corresponding NCER file, " + ncers[0]);
            this.ncer = NCER.fromFile(ncers[0]);
            logger.debug("Read NCER file\n");
        }

        // reader position is now 0x10
        readFile(reader);
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
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
        return null;
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
                frame.transform = reader.readBytes(10);
                frame.xDisplace = reader.readUInt16();
                frame.yDisplace = reader.readUInt16();
            }

            if (frame.type>0){
                logger.info("Animation " + aniNum + " Frames " + j + " xDisplace: " + frame.xDisplace);
                logger.info("Animation " + aniNum + " Frames " + j + " yDisplace: " + frame.yDisplace);
            }
        }
    }

// void ReadNtrAnimation(char *path, struct JsonToAnimationOptions *options)
//{
//    for (int i = 0; i < options->sequenceCount; i++)
//    {
//        for (int j = 0; j < options->sequenceData[i]->frameCount; j++)
//        {
//            int frameOffset = offset + frameOffsets[i] + j * 0x8;
//            options->sequenceData[i]->frameData[j]->resultOffset = data[frameOffset] | (data[frameOffset + 1] << 8) | (data[frameOffset + 2] << 16) | (data[frameOffset + 3] << 24);
//            options->sequenceData[i]->frameData[j]->frameDelay = data[frameOffset + 4] | (data[frameOffset + 5] << 8);
//            //0xBEEF
//
//            //the following is messy
//            bool present = false;
//            //check for offset in array
//            for (int k = 0; k < options->frameCount; k++)
//            {
//                if (resultOffsets[k] == options->sequenceData[i]->frameData[j]->resultOffset)
//                {
//                    present = true;
//                    break;
//                }
//            }
//
//            //add data if not present
//            if (!present)
//            {
//                for (int k = 0; i < options->frameCount; k++)
//                {
//                    if (resultOffsets[k] == -1)
//                    {
//                        resultOffsets[k] = options->sequenceData[i]->frameData[j]->resultOffset;
//                        break;
//                    }
//                }
//            }
//        }
//    }
//
//    free(frameOffsets);
//
//    offset = 0x18 + (data[0x24] | (data[0x25] << 8) | (data[0x26] << 16) | (data[0x27] << 24)); //start of animation results
//
//    int k;
//
//    for (k = 0; k < options->frameCount; k++)
//    {
//        if (resultOffsets[k] == -1)
//            break;
//    }
//    options->resultCount = k;
//
//    free(resultOffsets);
//
//    options->animationResults = malloc(sizeof(struct AnimationResults *) * options->resultCount);
//
//    for (int i = 0; i < options->resultCount; i++)
//    {
//        options->animationResults[i] = malloc(sizeof(struct AnimationResults));
//    }
//
//    int resultOffset = 0;
//    for (int i = 0; i < options->resultCount; i++)
//    {
//        if (data[offset + 2] == 0xCC && data[offset + 3] == 0xCC)
//        {
//            options->animationResults[i]->resultType = 0;
//        }
//        else if (data[offset + 2] == 0xEF && data[offset + 3] == 0xBE)
//        {
//            options->animationResults[i]->resultType = 2;
//        }
//        else
//        {
//            options->animationResults[i]->resultType = 1;
//        }
//        for (int j = 0; j < options->sequenceCount; j++)
//        {
//            for (int k = 0; k < options->sequenceData[j]->frameCount; k++)
//            {
//                if (options->sequenceData[j]->frameData[k]->resultOffset == resultOffset)
//                {
//                    options->sequenceData[j]->frameData[k]->resultId = i;
//                }
//            }
//        }
//        switch (options->animationResults[i]->resultType)
//        {
//            case 0: //index
//                options->animationResults[i]->index = data[offset] | (data[offset + 1] << 8);
//                resultOffset += 0x4;
//                offset += 0x4;
//                break;
//
//            case 1: //SRT
//                options->animationResults[i]->dataSrt.index = data[offset] | (data[offset + 1] << 8);
//                options->animationResults[i]->dataSrt.rotation = data[offset + 2] | (data[offset + 3] << 8);
//                options->animationResults[i]->dataSrt.scaleX = data[offset + 4] | (data[offset + 5] << 8) | (data[offset + 6] << 16) | (data[offset + 7] << 24);
//                options->animationResults[i]->dataSrt.scaleY = data[offset + 8] | (data[offset + 9] << 8) | (data[offset + 10] << 16) | (data[offset + 11] << 24);
//                options->animationResults[i]->dataSrt.positionX = data[offset + 12] | (data[offset + 13] << 8);
//                options->animationResults[i]->dataSrt.positionY = data[offset + 14] | (data[offset + 15] << 8);
//                resultOffset += 0x10;
//                offset += 0x10;
//                break;
//
//            case 2: //T
//                options->animationResults[i]->dataT.index = data[offset] | (data[offset + 1] << 8);
//                options->animationResults[i]->dataT.positionX = data[offset + 4] | (data[offset + 5] << 8);
//                options->animationResults[i]->dataT.positionY = data[offset + 6] | (data[offset + 7] << 8);
//                resultOffset += 0x8;
//                offset += 0x8;
//                break;
//        }
//    }
//
//    if (options->labelEnabled)
//    {
//        options->labelCount = options->sequenceCount; //*should* be the same
//        options->labels = malloc(sizeof(char *) * options->labelCount);
//        offset += 0x8 + options->labelCount * 0x4; //skip to label data
//        for (int i = 0; i < options->labelCount; i++)
//        {
//            options->labels[i] = malloc(strlen((char *)data + offset) + 1);
//            strcpy(options->labels[i], (char *)data + offset);
//            offset += strlen((char *)data + offset) + 1;
//        }
//    }
//
//    free(data);
//}
//
    @Override
    protected void readFile(MemBuf.MemBufReader reader) throws NitroException {
        abnkMagic = reader.readString(4);
        if (!abnkMagic.equals("KNBA")) {
            throw new RuntimeException("Not a valid NANR file.");
        }
        abnkSectionSize = reader.readUInt32();

        int start = reader.getPosition();
        logger.info("ABNK offset start @ " + start);

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
            ani.playbackMode = reader.readUInt32(); // 0x8 (0-4) sequenceArray[i].mode = byteBuffer.getInt(sequenceArrayOffset + i * 12 + 8);
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
    }
}
