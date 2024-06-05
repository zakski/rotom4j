package com.szadowsz.nds4j.file.bin;

import com.szadowsz.nds4j.data.learnset.LearnsetEntry;
import com.szadowsz.nds4j.data.ref.Items;
import com.szadowsz.nds4j.data.ref.PokeDex;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.reader.Buffer;
import com.szadowsz.nds4j.reader.MemBuf;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;


/**
 * Class to represent binary Learnset data file
 */
public class LearnsetNFSFile extends BinNFSFile {

    // Ways to Evolve
    protected ArrayList<LearnsetEntry> data = new ArrayList<>();

    /**
     * Learnset Data File Constructor
     *
     * @param path  the path of the file
     * @param name  the name of the file
     * @param bytes the raw data of the file
     */
    public LearnsetNFSFile(String path, String name, byte[] bytes) {
        super(path, name, bytes);
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
        MemBuf dataBuf = MemBuf.create(rawData);
        MemBuf.MemBufReader reader = dataBuf.reader();

        short combinedValue;
        while ((combinedValue = reader.readShort()) != (short) 0xFFFF) {
            data.add(new LearnsetEntry(getMoveId(combinedValue), getLevelLearned(combinedValue)));
        }
    }

    /**
     * Number of moves Pokemon can learn
     *
     * @return Number Of Learnset moves
     */
    public int getNumMoves() {
        return data.size();
    }

    /**
     * Get Move Name
     *
     * @param index of Move
     * @return move ID
     */
    public int getMove(int index){
        return data.get(index).getMoveID();
    }

    /**
     * Get move learning level
     *
     * @param index of Move
     * @return  move learning level
     */
    public int getLevel(int index){
        return data.get(index).getLevel();
    }

    /**
     * Read Learnset Data from File
     *
     * @param path path to File
     * @return Learnset File Data
     * @throws NitroException if the read failed
     */
    public static LearnsetNFSFile fromFile(String path) throws NitroException {
        return fromFile(new File(path));
    }

    /**
     * Read Learnset Data from File
     *
     * @param file File Object to parse
     * @return Learnset File Data
     * @throws NitroException if the read failed
     */
    public static LearnsetNFSFile fromFile(File file) throws NitroException {
        String path = file.getAbsolutePath();
        String fileName = file.getName();
        byte[] data = Buffer.readFile(path);
        return new LearnsetNFSFile(path,fileName,data);
    }
}