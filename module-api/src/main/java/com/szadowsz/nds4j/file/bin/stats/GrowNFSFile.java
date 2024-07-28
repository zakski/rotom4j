package com.szadowsz.nds4j.file.bin.stats;

import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.bin.BinNFSFile;
import com.szadowsz.nds4j.reader.Buffer;
import com.szadowsz.nds4j.reader.MemBuf;

import java.io.File;


/**
 * Class to represent binary Growth data file
 */
public class GrowNFSFile extends BinNFSFile {

    public static final int LEVELS = 100;

    // EXP needed for level
    protected final int[] xp = new int[LEVELS];

    /**
     * Growth Data File Constructor
     *
     * @param path  the path of the file
     * @param name  the name of the file
     * @param bytes the raw data of the file
     */
    public GrowNFSFile(String path, String name, byte[] bytes) {
        super(path, name, bytes);
        processEntries();
    }

    public static byte reverse(byte b) {
        int res = 0;
        for (int bi = b, i = 0; i < 8; i++, bi >>>= 1)
            res = (res << 1) | (bi & 1);

        return (byte) res;
    }

    /**
     * Process the raw data into XP Required for Levels
     */
    protected void processEntries() {
        MemBuf dataBuf = MemBuf.create(rawData);
        MemBuf.MemBufReader reader = dataBuf.reader();
        reader.skip(8);
        xp[0]=0;
        for (int i = 1; i < LEVELS; i++){
            xp[i] = reader.readInt();
        }
    }

    /**
     * XP Required for Level
     *
     * @return Number XP Required
     */
    public int getXPForLevel(int level) {
        return xp[level];
    }

    /**
     * Read Learnset Data from File
     *
     * @param path path to File
     * @return Learnset File Data
     * @throws NitroException if the read failed
     */
    public static GrowNFSFile fromFile(String path) throws NitroException {
        return fromFile(new File(path));
    }

    /**
     * Read Learnset Data from File
     *
     * @param file File Object to parse
     * @return Learnset File Data
     * @throws NitroException if the read failed
     */
    public static GrowNFSFile fromFile(File file) throws NitroException {
        String path = file.getAbsolutePath();
        String fileName = file.getName();
        byte[] data = Buffer.readFile(path);
        return new GrowNFSFile(path,fileName,data);
    }
}