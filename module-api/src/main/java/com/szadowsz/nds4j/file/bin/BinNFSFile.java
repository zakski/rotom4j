package com.szadowsz.nds4j.file.bin;

import com.szadowsz.nds4j.data.BinFormat;
import com.szadowsz.nds4j.data.NFSFormat;
import com.szadowsz.nds4j.file.BaseNFSFile;

/**
 * Class to represent a binary data file that exists in the Narc and doesn't have a Nitro Header
 */
public class BinNFSFile extends BaseNFSFile {

    protected BinFormat subType;

    /**
     * Data File Constructor
     *
     * @param path the path of the file
     * @param name the name of the file
     * @param bytes the raw data of the file
     */
    public BinNFSFile(String path, String name, byte[] bytes) {
        this( BinFormat.UNSPECIFIED,path,name,bytes);
    }

    /**
     * Data File Constructor
     *
     * @param subType how to interpret the binary data
     * @param path the path of the file
     * @param name the name of the file
     * @param bytes the raw data of the file
     */
    public BinNFSFile(BinFormat subType, String path, String name, byte[] bytes) {
        super( NFSFormat.BINARY,path,name);
        this.subType = subType;
        rawData = bytes;
    }
}
