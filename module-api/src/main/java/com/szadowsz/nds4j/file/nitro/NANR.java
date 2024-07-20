package com.szadowsz.nds4j.file.nitro;

import com.szadowsz.nds4j.NFSFactory;
import com.szadowsz.nds4j.compression.CompFormat;
import com.szadowsz.nds4j.data.ComplexImageable;
import com.szadowsz.nds4j.data.NFSFormat;
import com.szadowsz.nds4j.data.nfs.anime.AnimeSequence;
import com.szadowsz.nds4j.data.nfs.anime.FrameData;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.reader.MemBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;

public class NANR extends GenericNFSFile implements ComplexImageable {
    private static final Logger logger = LoggerFactory.getLogger(NANR.class);

    // ABNK  section
    private String abnkMagic;
    private long abnkSectionSize;

    private int nSequences;
    private int nTotalFrames;
    private long sequenceArrayOffset;
    private long frameArrayOffset;
    private long animationOffset;
    private byte[] padding;

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
        return (ncgr != null)?ncgr.getNCLR():NCLR.DEFAULT;
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
        if (ncgr != null){
            ncgr.setNCLR(nclr);
        }
    }

    @Override
    protected void readFile(MemBuf.MemBufReader reader) throws NitroException {
        abnkMagic = reader.readString(4);
        if (!abnkMagic.equals("ABNK")) {
            throw new RuntimeException("Not a valid NANR file.");
        }
        abnkSectionSize = reader.readUInt32();

        int abnk = findBlockBySignature(reader, "ABNK");
        logger.info("Found ABNK section @ " + abnk);

        nSequences = reader.readUInt16();
        nTotalFrames = reader.readUInt16();
        sequenceArrayOffset = reader.readUInt32();
        frameArrayOffset = reader.readUInt32();
        animationOffset = reader.readUInt32();
        padding = reader.readBytes(8);
        logger.info("Current Position " + reader.getPosition());

        // Bank header
        sequences = new AnimeSequence[nSequences];
        for (int i = 0; i < nSequences; i++) {
            AnimeSequence ani = sequences[i] = new AnimeSequence();
            ani.nFrames = reader.readInt();
            ani.type = reader.readUInt16();
//            ani.unknown1 = br.ReadUInt16();
//            ani.unknown2 = br.ReadUInt16();
//            ani.unknown3 = br.ReadUInt16();
//            ani.offset_frame = br.ReadUInt32();

            // Frame header
            ani.frames = new FrameData[ani.nFrames];
            for (int j = 0; j < ani.nFrames; j++) {
                FrameData frame = ani.frames[j] = new FrameData();

//                br.BaseStream.Position = 0x18 + nanr.abnk.offset1 + j * 0x08 + ani.offset_frame;
//                frame.offset_data = br.ReadUInt32();
//                frame.unknown1 = br.ReadUInt16();
//                frame.constant = br.ReadUInt16();
//
//                // Frame data
//                br.BaseStream.Position = 0x18 + nanr.abnk.offset2 + frame.offset_data;
//                frame.data.nCell = br.ReadUInt16();
            }
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
