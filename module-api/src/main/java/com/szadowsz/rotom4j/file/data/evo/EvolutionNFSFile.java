package com.szadowsz.rotom4j.file.data.evo;

import com.szadowsz.rotom4j.exception.InvalidDataException;
import com.szadowsz.rotom4j.exception.InvalidFileException;
import com.szadowsz.rotom4j.file.data.DataFormat;
import com.szadowsz.rotom4j.ref.ItemDex;
import com.szadowsz.rotom4j.ref.PokeDex;
import com.szadowsz.rotom4j.file.data.DataFile;
import com.szadowsz.rotom4j.file.data.evo.data.EvoMethod;
import com.szadowsz.rotom4j.file.data.evo.data.Evolution;
import com.szadowsz.rotom4j.binary.io.reader.Buffer;
import com.szadowsz.rotom4j.binary.io.reader.MemBuf;
import com.szadowsz.rotom4j.exception.NitroException;

import java.io.File;
import java.util.ArrayList;


/**
 * Class to represent binary evolution data file
 */
public class EvolutionNFSFile extends DataFile {

    // Ways to Evolve
    protected ArrayList<Evolution> evoData = new ArrayList<>();

    /**
     * Evolution Data File Constructor
     *
     * @param path  the path of the file
     */
    public EvolutionNFSFile(String path) throws InvalidFileException, InvalidDataException {
        super(DataFormat.EVOLUTION,path);
        processEntries();
    }

    /**
     * Process the raw data into ways to evolve
     */
    protected void processEntries() {
        MemBuf dataBuf = MemBuf.create(data);
        MemBuf.MemBufReader reader = dataBuf.reader();

        for (int i = 0; i < getDataSize() / 6; i++) {
            evoData.add(new Evolution(reader.readShort(), reader.readShort(), reader.readShort()));
        }
    }

    /**
     * Number Of Evolutions
     *
     * @return number of ways to evolve
     */
    public int getNumEvolutions(){
        return evoData.size();
    }

    /**
     * Get method of Evolution
     *
     * @param index evolution number
     * @return way to evolve
     */
    public EvoMethod getMethod(int index){
        return evoData.get(index).getMethod();
    }

    /**
     * Get evolution requirement index
     *
     * @param index evolution number
     * @return index of requirement to evolve
     */
    public int getRequirement(int index){
        return evoData.get(index).getRequirement();
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
            case EVO_ITEM, EVO_TRADE_ITEM -> ItemDex.getItemNameByNo(index);
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
        return evoData.get(index).getSpecies();
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
        return new EvolutionNFSFile(path);
    }
}