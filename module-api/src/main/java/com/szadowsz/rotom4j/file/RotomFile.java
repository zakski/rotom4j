package com.szadowsz.rotom4j.file;

import com.szadowsz.rotom4j.binary.array.ByteArrayData;
import com.szadowsz.rotom4j.binary.array.ByteArrayEditableData;
import com.szadowsz.rotom4j.compression.CompFormat;
import com.szadowsz.rotom4j.exception.InvalidFileException;
import com.szadowsz.rotom4j.binary.ByteArrayCompressibleData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;

/**
 * Base Object to represent Binary and Nitro/Nintendo Files
 */
public abstract class RotomFile extends ByteArrayCompressibleData {
    private static final Logger LOGGER = LoggerFactory.getLogger(RotomFile.class);

    // name of file stripped of its extension
    protected String objName;

    // Binary/Nitro Format
    protected RotomFormat magic;
    // Index of File extension type
    protected int extIndex;


    public RotomFile(String name, ByteArrayEditableData compData) {
        super(compData);
        this.objName = name;
        String ext = new String (Arrays.copyOf(data,4));
        this.magic = RotomFormat.valueOfExt(ext);
        if (this.magic == null) {
            this.magic = RotomFormat.BINARY;
        }
    }

    public RotomFile(File file) throws InvalidFileException {
        super(file);
        if (fileFullName != null) {
            this.objName = stripExtFromFileName(fileFullName);
            String ext = extractExtFromFileName(fileFullName);
            this.magic = RotomFormat.valueOfExt(ext);
            this.extIndex = this.magic.getExtIndex(ext);
        } else {
            this.objName = "";
        }
    }

    public RotomFile(String filePath) throws InvalidFileException {
        this(new File(filePath));
    }

    /**
     * Removes the extension from the full file name
     *
     * @param fileName full name of the file
     * @return name of the file without its extension
     */
    protected final String stripExtFromFileName(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos > 0 && pos < (fileName.length() - 1)) { // If '.' is not the first or last character.
            fileName = fileName.substring(0, pos);
        }
        return fileName;
    }

    /**
     * Strip and return the value of the extension from the file name
     *
     * @param fileName name of the file
     * @return the last extension in the file name
     */
    protected final String extractExtFromFileName(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos > 0 && pos < (fileName.length() - 1)) { // If '.' is not the first or last character.
            return fileName.substring(pos+1);
        }
        return "";
    }

    /**
     * Gets partial name of the file
     *
     * @return name of the file without its extension
     */
    public String getFileNameWithoutExt() {
        return objName;
    }

    /**
     * Gets the extension of the file
     *
     * @return the extension name of the file
     */
    public String getExt() {
        return magic.getExt()[extIndex];
    }

    /**
     * Constructs and returns the full name of the file
     *
     * @return full name of the file
     */
    @Override
    public String getFileName(){
        return getFileNameWithoutExt() + "." + magic.getExt()[extIndex];
    }


    /**
     * Gets the Magic denoting the File Format
     *
     * @return the file format magic
     */
    public RotomFormat getMagic() {
        return magic;
    }

    /**
     * Set the name of file based on some external parsing method
     *
     * @param fileName name of the file to set
     */
    public void setFileName(String fileName) {
        String ext = extractExtFromFileName(fileName);
        RotomFormat byExt = RotomFormat.valueOfExt(ext);
        if (byExt.equals(magic)){
            this.objName = stripExtFromFileName(fileName);
            this.extIndex = byExt.getExtIndex(ext);
            this.fileFullName = getFileName();
        } else {
            throw new RuntimeException("BAD EXTENSION"); // TODO
        }
    }

    @Override
    public String toString(){
        return getFileName();
    }
}
