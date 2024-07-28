package com.szadowsz.nds4j;

import com.szadowsz.nds4j.compression.CompFormat;
import com.szadowsz.nds4j.compression.JavaDSDecmp;
import com.szadowsz.nds4j.file.NFSFormat;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.BaseNFSFile;
import com.szadowsz.nds4j.file.bin.BinNFSFile;
import com.szadowsz.nds4j.file.bin.PlaceholderNFSFile;
import com.szadowsz.nds4j.file.nitro.*;
import com.szadowsz.nds4j.file.nitro.nanr.NANR;
import com.szadowsz.nds4j.file.nitro.ncer.NCER;
import com.szadowsz.nds4j.file.nitro.ncgr.NCGR;
import com.szadowsz.nds4j.file.nitro.nclr.NCLR;
import com.szadowsz.nds4j.file.nitro.nscr.NSCR;
import com.szadowsz.nds4j.reader.Buffer;
import com.szadowsz.nds4j.reader.HexInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Factory Class to read/build Nitro File Object Representations
 */
public class NFSFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(NFSFactory.class);

    /**
     * Attempt to read the File Format from an expected Nitro File header
     *
     * @param data file byte data
     * @return Expected Format
     */
    private static NFSFormat parseFileFormat(byte[] data) {
        if (data != null && data.length > 4) {
            String magic = new String(Arrays.copyOfRange(data, 0, 4), Charset.forName("Shift_JIS"));
            NFSFormat format = NFSFormat.valueOfLabel(magic);
            if(format!=null){
                return format;
            }
        }

        return NFSFormat.BINARY; // We fall back to expect it in some sort of binary format
    }

    /**
     * Convert raw data into a file obj
     *
     * @param path the file path, if not from a narc
     * @param name the file name to use
     * @param comp the detected compression format
     * @param magic the detected file format
     * @param compData the raw compressed data
     * @param data the raw uncompressed data
     * @return parsed file obj
     * @throws NitroException if the conversion fails
     */
    private static BaseNFSFile convert(String path, String name, CompFormat comp, NFSFormat magic, byte[] compData, byte[] data) throws NitroException, NitroException {
        switch (magic) {
            case NCGR -> { // Nintendo Character Graphic Resource
                return new NCGR(path, name, comp, compData, data);
            }
            case NCLR -> { // Nintendo CoLor Resource
                return new NCLR(path, name, comp, compData, data);
            }
            case NSCR -> { // Nintendo SCreen Resource
                return new NSCR(path, name, comp, compData, data);
            }
            case NCER -> { // Nintendo CEll Resource
                return new NCER(path, name, comp, compData, data);
            }
            case NANR -> { // Nintendo ANimation Resource
                return new NANR(path, name, comp, compData, data);
            }
            case BINARY -> { // Some Sort Of Data File
                if (data != null && data.length > 4) {
                    return new BinNFSFile(path, name, data);
                } else {
                    return new PlaceholderNFSFile(path, name);
                }
            }
            default -> {
                return new UnspecifiedNFSFile(magic, path, name, comp, compData, data);
            }
        }
    }

    /**
     * Extract a file obj from a Narc File
     *
     * @param narcName name of the narc file
     * @param index index of the file obj being extracted from the narc
     * @param fileCount the total number of files in the narc
     * @param compressedData the raw potentially compressed data
     * @return parsed file obj
     * @throws NitroException if file obj is unable to be parsed
     */
    public static BaseNFSFile fromNarc(String narcName, int index, long fileCount, byte[] compressedData) throws NitroException {
        String fileNameNoExt = narcName + "_" + String.format("%0" + String.valueOf(fileCount).length() + "d", index);
        CompFormat compFormat = CompFormat.NONE;
        int[] dataInt;
        byte[] data = null;
        try (HexInputStream input = new HexInputStream(new ByteArrayInputStream(compressedData))) {
            compFormat = JavaDSDecmp.supports(input);
            if (compFormat != CompFormat.NONE) {
                dataInt = JavaDSDecmp.decompress(input);
            } else {
                dataInt = input.readAllBytes();
            }
            data = new byte[dataInt.length];
            for (int i = 0; i < dataInt.length; i++) {
                data[i] = (byte) dataInt[i];
            }
        } catch (ArrayIndexOutOfBoundsException | IOException e) {
            LOGGER.warn("Failed to decompress " + fileNameNoExt, e);
        }
        NFSFormat magic = parseFileFormat(data);
        String fileName = fileNameNoExt + "." + magic.getExt()[0];
        return convert(null, fileName, compFormat, magic, compressedData, data);

    }

    /**
     * Extract a file obj from a File
     *
     * @param file java file representation to parse
     * @return parsed file obj
     * @throws NitroException if file obj is unable to be parsed
     */
    public static BaseNFSFile fromFile(File file) throws NitroException {
        String path = file.getAbsolutePath();
        String fileName = file.getName();
        CompFormat compFormat = CompFormat.NONE;
        int[] dataInt;
        byte[] compressedData = Buffer.readFile(path);
        byte[] data = null;
        try (HexInputStream input = new HexInputStream(new ByteArrayInputStream(compressedData))) {
            compFormat = JavaDSDecmp.supports(input);
            if (compFormat != CompFormat.NONE) {
                dataInt = JavaDSDecmp.decompress(input);
            } else {
                dataInt = input.readAllBytes();
            }
            data = new byte[dataInt.length];
            for (int i = 0; i < dataInt.length; i++) {
                data[i] = (byte) dataInt[i];
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to decompress " + fileName, e);
        }
        NFSFormat magic = parseFileFormat(data);
        return convert(path, fileName, compFormat, magic, compressedData, data);
    }
}