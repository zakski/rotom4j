package com.szadowsz.nds4j.file.bin;

import com.szadowsz.nds4j.data.ref.Items;
import com.szadowsz.nds4j.data.ref.PokeDex;
import com.szadowsz.nds4j.reader.Buffer;
import com.szadowsz.nds4j.reader.MemBuf;
import com.szadowsz.nds4j.data.evo.EvoMethod;
import com.szadowsz.nds4j.data.evo.Evolution;
import com.szadowsz.nds4j.exception.NitroException;

import java.io.File;
import java.util.ArrayList;


/**
 * Class to represent binary evolution data file
 */
public class EvolutionNFSFile extends BinNFSFile {

    // Ways to Evolve
    protected ArrayList<Evolution> data = new ArrayList<>();

    /**
     * Evolution Data File Constructor
     *
     * @param path  the path of the file
     * @param name  the name of the file
     * @param bytes the raw data of the file
     */
    public EvolutionNFSFile(String path, String name, byte[] bytes) {
        super(path, name, bytes);
        processEntries();
    }

    /**
     * Process the raw data into ways to evolve
     */
    protected void processEntries() {
        MemBuf dataBuf = MemBuf.create(rawData);
        MemBuf.MemBufReader reader = dataBuf.reader();

        for (int i = 0; i < rawData.length / 6; i++) {
            data.add(new Evolution(reader.readShort(), reader.readShort(), reader.readShort()));
        }
    }

    /**
     * Number Of Evolutions
     *
     * @return number of ways to evolve
     */
    public int getNumEvolutions(){
        return data.size();
    }

    /**
     * Get method of Evolution
     *
     * @param index evolution number
     * @return way to evolve
     */
    public EvoMethod getMethod(int index){
        return data.get(index).getMethod();
    }

    /**
     * Get evolution requirement index
     *
     * @param index evolution number
     * @return index of requirement to evolve
     */
    public int getRequirement(int index){
        return data.get(index).getRequirement();
    }

    /**
     * Get Item or Level requirement to evolve, if any
     *
     * @param index evolution number
     * @return requirement string
     */
    public String getRequirementString(int index) {
        return switch (getMethod(index)) {
            case EVO_NONE -> "NONE";
            case EVO_ITEM, EVO_TRADE_ITEM -> Items.getItemNameByNo(index);
            default -> getRequirement(index) + " lvl.";
        };
    }

    /**
     * Get index of the Species it evolves into
     *
     * @param index evolution number
     * @return species number
     */
    public int getSpecies(int index){
        return data.get(index).getSpecies();
    }

    /**
     * Get name of the Species it evolves into
     *
     * @param index evolution number
     * @return species name
     */
    public String getSpeciesString(int index){
        return PokeDex.getPokemonNameByNo(getSpecies(index));
    }

    /**
     * Read Evolution Data from File
     *
     * @param path path to File
     * @return Evolution File Data
     * @throws NitroException if the read failed
     */
    public static EvolutionNFSFile fromFile(String path) throws NitroException {
        return fromFile(new File(path));
    }

    /**
     * Read Evolution Data from File
     *
     * @param file File Object to parse
     * @return Evolution File Data
     * @throws NitroException if the read failed
     */
    public static EvolutionNFSFile fromFile(File file) throws NitroException {
        String path = file.getAbsolutePath();
        String fileName = file.getName();
        byte[] data = Buffer.readFile(path);
        return new EvolutionNFSFile(path,fileName,data);
    }
}