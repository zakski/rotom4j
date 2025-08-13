package com.szadowsz.rotom4j.file.data;

import com.szadowsz.rotom4j.binary.array.ByteArrayEditableData;
import com.szadowsz.rotom4j.compression.CompFormat;
import com.szadowsz.rotom4j.exception.InvalidDataException;
import com.szadowsz.rotom4j.exception.InvalidFileException;
import com.szadowsz.rotom4j.file.RotomFile;
import com.szadowsz.rotom4j.file.RotomFormat;

import java.io.File;

/**
 * Class to represent a binary data file that exists in the Narc and doesn't have a Nitro Header
 */
public class DataFile extends RotomFile {

    protected DataFormat subType;

    /**
     * Data File Constructor
     *
     * @param subType how to interpret the binary data
     * @param path    the path of the file
     */
    public DataFile(DataFormat subType, String path) throws InvalidFileException, InvalidDataException {
        super(new File(path));
        this.subType = subType;
        if (!this.magic.equals(RotomFormat.BINARY)) {
            throw new InvalidDataException("Unsupported File encoding: " + magic.getLabel()[0] + ", should be " + this.magic.getLabel()[0]);
        }
    }

    /**
     * Data File Constructor
     *
     * @param subType how to interpret the binary data
     * @param name    the name of the file
     * @param bytes   the raw data of the file
     * @throws InvalidDataException
     */
    public DataFile(DataFormat subType, String name, ByteArrayEditableData bytes) throws InvalidDataException {
        super(name, bytes);
        this.subType = subType;
        if (!this.magic.equals(RotomFormat.BINARY)) {
            throw new InvalidDataException("Unsupported File encoding: " + magic.getLabel()[0] + ", should be " + this.magic.getLabel()[0]);
        }
    }


    /**
     * Data File Constructor
     *
     * @param subType how to interpret the binary data
     * @param name    the name of the file
     * @param detectedCompression compression determined, basically to avoid processing files that  start coincidentally with flags
     * @param bytes   the raw data of the file
     * @throws InvalidDataException
     */
    public DataFile(DataFormat subType, String name, CompFormat detectedCompression, ByteArrayEditableData bytes) throws InvalidDataException {
        super(name, detectedCompression, bytes);
        this.subType = subType;
        if (!this.magic.equals(RotomFormat.BINARY)) {
            throw new InvalidDataException("Unsupported File encoding: " + magic.getLabel()[0] + ", should be " + this.magic.getLabel()[0]);
        }
    }
}