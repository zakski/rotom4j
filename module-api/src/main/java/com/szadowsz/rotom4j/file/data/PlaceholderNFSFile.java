package com.szadowsz.rotom4j.file.data;

import com.szadowsz.rotom4j.binary.array.ByteArrayEditableData;
import com.szadowsz.rotom4j.exception.InvalidDataException;
import com.szadowsz.rotom4j.exception.InvalidFileException;
import com.szadowsz.rotom4j.file.RotomFormat;

/**
 * Class to represent an empty file that exists in the Narc
 */
public class PlaceholderNFSFile extends DataFile {

    /**
     * Placeholder File Constructor
     *
     * @param name name of the file
     */
    public PlaceholderNFSFile(String name) throws InvalidDataException {
        super(DataFormat.PLACEHOLDER, name, new ByteArrayEditableData());
    }

    /**
     * Placeholder File Constructor
     *
     * @param path path of the file
     */
    public PlaceholderNFSFile(String path, long datalength) throws InvalidDataException, InvalidFileException {
        super(DataFormat.PLACEHOLDER, path);
    }
    /**
     * Set the name of file based on some external parsing method
     * <p>
     * We override this to avoid checking the extension of the name against the file's magic
     *
     * @param fileName name of the file to set
     */
    @Override
    public void setFileName(String fileName) {
        int pos = fileName.lastIndexOf(".");
        String ext = fileName.substring(pos + 1);
        RotomFormat byExt = RotomFormat.valueOfExt(ext);
        this.objName = stripExtFromFileName(fileName);
        for (int i = 0; i < byExt.getExt().length; i++) {
            if (byExt.getExt()[i].equals(ext)) {
                this.extIndex = i;
            }
        }
        this.magic = byExt;
        this.fileFullName = getFileName();
    }
}
