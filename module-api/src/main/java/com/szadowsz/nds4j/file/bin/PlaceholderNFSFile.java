package com.szadowsz.nds4j.file.bin;

import com.szadowsz.nds4j.data.BinFormat;
import com.szadowsz.nds4j.data.NFSFormat;

/**
 * Class to represent an empty file that exists in the Narc
 */
public class PlaceholderNFSFile extends BinNFSFile {

    /**
     * Placeholder File Constructor
     *
     * @param path path of the file
     * @param name name of the file
     */
    public PlaceholderNFSFile(String path, String name) {
        super(BinFormat.PLACEHOLDER,path,name, new byte[0]);
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
        String ext = fileName.substring(pos+1);
        NFSFormat byExt = NFSFormat.valueOfExt(ext);
        this.fileName = stripExtFromFileName(fileName);
        for (int i = 0; i < byExt.getExt().length;i++){
            if (byExt.getExt()[i].equals(ext)){
                this.extIndex = i;
            }
        }
       this.magic = byExt;
    }
}
