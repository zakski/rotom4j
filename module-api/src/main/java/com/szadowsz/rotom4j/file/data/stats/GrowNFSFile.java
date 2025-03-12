package com.szadowsz.rotom4j.file.data.stats;

import com.szadowsz.rotom4j.exception.InvalidDataException;
import com.szadowsz.rotom4j.exception.InvalidFileException;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.data.DataFile;
import com.szadowsz.rotom4j.binary.io.reader.Buffer;
import com.szadowsz.rotom4j.binary.io.reader.MemBuf;
import com.szadowsz.rotom4j.file.data.DataFormat;

import java.io.File;


/**
 * Class to represent binary Growth data file
 */
public class GrowNFSFile extends DataFile {

    public static final int LEVELS = 100;

    // EXP needed for level
    protected final int[] xp = new int[LEVELS];

    /**
     * Growth Data File Constructor
     *
     * @param path  the path of the file
     */
    public GrowNFSFile(String path) throws InvalidFileException, InvalidDataException {
        super(DataFormat.LEVEL_UP, path);
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
        MemBuf dataBuf = MemBuf.create(data);
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
        return new GrowNFSFile(path);
    }
}