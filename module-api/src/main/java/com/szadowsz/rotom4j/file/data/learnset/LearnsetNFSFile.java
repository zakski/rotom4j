package com.szadowsz.rotom4j.file.data.learnset;

import com.szadowsz.rotom4j.exception.InvalidDataException;
import com.szadowsz.rotom4j.exception.InvalidFileException;
import com.szadowsz.rotom4j.file.data.DataFile;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.data.DataFormat;
import com.szadowsz.rotom4j.file.data.learnset.data.LearnsetEntry;
import com.szadowsz.rotom4j.binary.io.reader.Buffer;
import com.szadowsz.rotom4j.binary.io.reader.MemBuf;

import java.io.File;
import java.util.ArrayList;


/**
 * Class to represent binary Learnset data file
 */
public class LearnsetNFSFile extends DataFile {

    // Ways to Evolve
    protected ArrayList<LearnsetEntry> moveData = new ArrayList<>();

    /**
     * Learnset Data File Constructor
     *
     * @param path  the path of the file
     */
    public LearnsetNFSFile(String path) throws InvalidFileException, InvalidDataException {
        super(DataFormat.MOVES, path);
        processEntries();
    }

    private int getMoveId(short x) {
        return x & 0x1FF;
    }

    private int getLevelLearned(short x) {
        return (x >> 9) & 0x7F;
    }

    /**
     * Process the raw data into ways to learnset
     */
    protected void processEntries() {
        MemBuf dataBuf = MemBuf.create(data);
        MemBuf.MemBufReader reader = dataBuf.reader();

        short combinedValue;
        while ((combinedValue = reader.readShort()) != (short) 0xFFFF) {
            moveData.add(new LearnsetEntry(getMoveId(combinedValue), getLevelLearned(combinedValue)));
        }
    }

    /**
     * Number of moves Pokemon can learn
     *
     * @return Number Of Learnset moves
     */
    public int getNumMoves() {
        return moveData.size();
    }

    /**
     * Get Move Name
     *
     * @param index of Move
     * @return move ID
     */
    public int getMove(int index){
        return moveData.get(index).getMoveID();
    }

    /**
     * Get move learning level
     *
     * @param index of Move
     * @return  move learning level
     */
    public int getLevel(int index){
        return moveData.get(index).getLevel();
    }

    /**
     * Read Learnset Data from File
     *
     * @param path path to File
     * @return Learnset File Data
     * @throws NitroException if the read failed
     */
    public static LearnsetNFSFile fromFile(String path) throws NitroException {
        return new LearnsetNFSFile(path);
    }

}