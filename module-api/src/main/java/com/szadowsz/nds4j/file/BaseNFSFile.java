package com.szadowsz.nds4j.file;

import com.szadowsz.nds4j.data.NFSFormat;

/**
 * Base Object to represent Nitro/Nintendo Files
 */
public class BaseNFSFile {

    // File Info

    // Path to the File
    protected String path;
    // File name without extension
    protected String fileName;

    // File Format Info

    // Nitro Format
    protected NFSFormat magic;
    // Index of File extension type
    protected int extIndex;

    // Data Info
    protected byte[] rawData;

    /**
     * Create Nitro File Info from just a name
     *
     * @param name name of the file
     */
    public BaseNFSFile(String name) {
        if (name != null) {
            this.fileName = stripExtFromFileName(name);
            String ext = extractExtFromFileName(fileName);
            this.magic = NFSFormat.valueOfExt(ext);
            this.extIndex = this.magic.getExtIndex(ext);
        } else {
            this.fileName = "";
        }
    }

    /**
     *  Create Nitro File Info from a name, path and expected type
     *
     * @param magic expected file format
     * @param path path to the file
     * @param name name of the file
     */
    public BaseNFSFile(NFSFormat magic, String path, String name) {
        this.magic = magic;
        this.path = path;
        if (name != null) {
            this.fileName = stripExtFromFileName(name);
            String ext = extractExtFromFileName(fileName);
            this.extIndex = this.magic.getExtIndex(ext);
        } else {
            this.fileName = "";
        }
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
        return fileName;
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
    public String getFileName(){
        return getFileNameWithoutExt() + "." + magic.getExt()[extIndex];
    }


    /**
     * Gets the Magic denoting the File Format
     *
     * @return the file format magic
     */
    public NFSFormat getMagic() {
        return magic;
    }

    /**
     * Gets all the compressed byte data of the file (header and data sections)
     *
     * @return the raw data of the file
     */
    public byte[] getCompressedData(){
        return rawData;
    }

    /**
     * Gets all the byte data of the file (header and data sections)
     *
     * @return the raw data of the file
     */
    public byte[] getData(){
        return rawData;
    }

    /**
     * Set the name of file based on some external parsing method
     *
     * @param fileName name of the file to set
     */
    public void setFileName(String fileName) {
        String ext = extractExtFromFileName(fileName);
        NFSFormat byExt = NFSFormat.valueOfExt(ext);
        if (byExt.equals(magic)){
            this.fileName = stripExtFromFileName(fileName);
            this.extIndex = byExt.getExtIndex(ext);
        } else {
            throw new RuntimeException("BAD EXTENSION"); // TODO
        }
    }

    @Override
    public String toString(){
        return getFileName();
    }
}
