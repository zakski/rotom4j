package com.szadowsz.nds4j.file.nitro.nanr;

import com.szadowsz.nds4j.NFSFactory;
import com.szadowsz.nds4j.compression.CompFormat;
import com.szadowsz.nds4j.file.ComplexImageable;
import com.szadowsz.nds4j.file.NFSFormat;
import com.szadowsz.nds4j.file.nitro.nanr.anime.AnimeSequence;
import com.szadowsz.nds4j.file.nitro.nanr.anime.FrameData;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.GenericNFSFile;
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

    private NCGR ncgr;

    public NANR(String path, String fileName, CompFormat comp, byte[] compData, byte[] data) throws NitroException {
        super(NFSFormat.NANR, path, fileName, comp, compData, data);
        MemBuf dataBuf = MemBuf.create(data);
        MemBuf.MemBufReader reader = dataBuf.reader();
        int fileSize = dataBuf.writer().getPosition();
        logger.debug("\nNANR file, " + fileName + ", initialising with size of " + fileSize + " bytes");

        readGenericNtrHeader(reader);

        File[] ncgrs = new File(path).getParentFile().listFiles(f -> (f.getName().endsWith(".NCGR") ||
                f.getName().endsWith(".NCBR")) &&
                f.getName().substring(0, f.getName().lastIndexOf('.')).equals(this.fileName));
        if (ncgrs != null && ncgrs.length > 0) {
            logger.debug("Found corresponding NCGR file, " + ncgrs[0]);
            this.ncgr = NCGR.fromFile(ncgrs[0]);
            logger.debug("Read NCGR file\n");
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
        return ncgr;
    }

    @Override
    public NCLR getNCLR() {
        return (ncgr != null) ? ncgr.getNCLR() : NCLR.DEFAULT;
    }

    @Override
    public BufferedImage getImage() {
        return null;
    }

    @Override
    public void setNCGR(NCGR ncgr) {
        this.ncgr = ncgr;
    }

    @Override
    public void setNCLR(NCLR nclr) throws NitroException {
        if (ncgr != null) {
            ncgr.setNCLR(nclr);
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


    @Override
    protected void readFile(MemBuf.MemBufReader reader) throws NitroException {
//        int abnk = findBlockBySignature(reader, "ABNK");
//        logger.info("Found ABNK section @ " + abnk);

        abnkMagic = reader.readString(4);
        if (!abnkMagic.equals("KNBA")) {
            throw new RuntimeException("Not a valid NANR file.");
        }
        abnkSectionSize = reader.readUInt32();

        int start = reader.getPosition();
        logger.info("ABNK offset start @ " + start);

        nAnimations = reader.readUInt16();
        logger.info("ABNK Animations Count: " + nAnimations);

        nTotalFrames = reader.readUInt16();
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

            ani.nFrames = reader.readInt();
            ani.frames = new FrameData[ani.nFrames];
            logger.info("Animation " + i + " Frames: " + ani.nFrames);

            ani.type = reader.readUInt16();
            logger.info("Animation " + i + " Type: " + ani.type);
            switch (ani.type) {
                case 0:
                    ani.size = 4;
                    break;
                case 1:
                    ani.size = 16;
                    break;
                case 2:
                    ani.size = 8;
                    break;
            }
            ani.unknown1 = reader.readUInt16();
            ani.unknown2 = reader.readUInt32();
            ani.startFrameIndex = reader.readInt();

            int aniPos = reader.getPosition();
            logger.info("Animation " + i + " startFrameIndex: " + ani.startFrameIndex);
            int framePos = start + framesHeaderOffset + ani.startFrameIndex;
            logger.info("Animation " + i + " index: " + framePos);
            reader.setPosition(framePos);

            // Read Frames
            readFrames(reader, ani, frameHeadStart, i, frameDataStart);
            reader.setPosition(aniPos);
        }
    }

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
}
