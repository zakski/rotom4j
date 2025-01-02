package com.szadowsz.rotom4j;

import com.szadowsz.binary.array.ByteArrayEditableData;
import com.szadowsz.rotom4j.compression.CompFormat;
import com.szadowsz.rotom4j.exception.InvalidFileException;
import com.szadowsz.rotom4j.file.BaseNFSFile;
import com.szadowsz.rotom4j.binary.ByteArrayCompressibleData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class RotomFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(RotomFile.class);

    // We keep these two separate to allow for error recovery when editing the raw data
    protected final ByteArrayCompressibleData data; // raw malleable data, not directly used
    protected final ByteArrayEditableData uncompressedData; // uncompressed data, whether or not the data was originally compressed
    protected BaseNFSFile nfsFile; // formatted version of the data

    protected String name; // current name of the file
    protected String path; // current path of the file


    public RotomFile(File file) throws InvalidFileException {
        this.name = file.getName();
        this.path = file.getAbsolutePath();
        try (BufferedInputStream bs = new BufferedInputStream(new FileInputStream (file))){
            this.data = new ByteArrayCompressibleData(bs.readAllBytes());
            CompFormat format = data.getCompressionUsed();
            this.uncompressedData = data.uncompress();
            this.nfsFile = NFSFactory.fromBinary(path,name,format,data, uncompressedData);
        } catch (IOException e) {
            throw new InvalidFileException("Could not Load Nitro File " + file.getAbsolutePath(),e);
        }

    }

    public RotomFile(String filepath) throws InvalidFileException {
        this(new File(filepath));
    }
}
