package com.szadowsz.rotom4j;

import com.szadowsz.rotom4j.binary.array.ByteArrayEditableData;
import com.szadowsz.rotom4j.compression.CompFormat;
import com.szadowsz.rotom4j.compression.JavaDSDecmp;
import com.szadowsz.rotom4j.file.RotomFile;
import com.szadowsz.rotom4j.file.RotomFormat;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.data.DataFile;
import com.szadowsz.rotom4j.file.data.DataFormat;
import com.szadowsz.rotom4j.file.data.PlaceholderNFSFile;
import com.szadowsz.rotom4j.file.nitro.UnspecifiedNFSFile;
import com.szadowsz.rotom4j.file.nitro.nanr.NANR;
import com.szadowsz.rotom4j.file.nitro.ncer.NCER;
import com.szadowsz.rotom4j.file.nitro.ncgr.NCGR;
import com.szadowsz.rotom4j.file.nitro.nclr.NCLR;
import com.szadowsz.rotom4j.file.nitro.nscr.NSCR;
import com.szadowsz.rotom4j.binary.io.reader.Buffer;
import com.szadowsz.rotom4j.binary.io.reader.HexInputStream;
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
    private static RotomFormat parseFileFormat(byte[] data) {
        if (data != null && data.length > 4) {
            String magic = new String(Arrays.copyOfRange(data, 0, 4), Charset.forName("Shift_JIS"));
            RotomFormat format = RotomFormat.valueOfLabel(magic);
            if(format!=null){
                return format;
            }
        }

        return RotomFormat.BINARY; // We fall back to expect it in some sort of binary format
    }

    /**
     * Convert raw data into a file obj
     *
     * @param magic the detected file format
     * @param path the file path
     * @param datalength the uncompressed? data length
     * @return parsed file obj
     * @throws NitroException if the conversion fails
     */
    private static RotomFile convertFromFile(RotomFormat magic, String path, long datalength) throws NitroException {
        switch (magic) {
            case NCGR -> { // Nintendo Character Graphic Resource
                return new NCGR(path);
            }
            case NCLR -> { // Nintendo CoLor Resource
                return new NCLR(path);
            }
            case NSCR -> { // Nintendo SCreen Resource
                return new NSCR(path);
            }
            case NCER -> { // Nintendo CEll Resource
                return new NCER(path);
            }
            case NANR -> { // Nintendo ANimation Resource
                return new NANR(path);
            }
            case BINARY -> { // Some Sort Of Data File
                if (datalength > 4) {
                    return new DataFile(DataFormat.UNSPECIFIED,path);
                } else {
                    return new PlaceholderNFSFile(path);
                }
            }
            default -> {
                return new UnspecifiedNFSFile(magic, path);
            }
        }
    }

    /**
     * Convert raw data into a file obj
     *
     * @param magic the detected file format
     * @param name the file name to use
     * @param comp the detected compression format
     * @param compData the compressed data
     * @param data the uncompressed data
     * @return parsed file obj
     * @throws NitroException if the conversion fails
     */
    private static RotomFile convertFromBinary(RotomFormat magic, String name, CompFormat comp, ByteArrayEditableData compData, ByteArrayEditableData data) throws NitroException {
        switch (magic) {
            case NCGR -> { // Nintendo Character Graphic Resource
                return new NCGR(name, compData);
            }
            case NCLR -> { // Nintendo CoLor Resource
                return new NCLR(name, compData);
            }
            case NSCR -> { // Nintendo SCreen Resource
                return new NSCR(name, compData);
            }
            case NCER -> { // Nintendo CEll Resource
                return new NCER(name, compData);
            }
            case NANR -> { // Nintendo ANimation Resource
                return new NANR(name, compData);
            }
            case BINARY -> { // Some Sort Of Data File
                if (data != null && data.getDataSize() > 4) {
                    return new DataFile(DataFormat.UNSPECIFIED,name, comp, compData);
                } else {
                    return new PlaceholderNFSFile(name);
                }
            }
            default -> {
                return new UnspecifiedNFSFile(magic, name, compData);
            }
        }
    }

    /**
     * Convert raw data into a file obj
     *
     * @param magic the detected file format
     * @param path the file path, if not from a narc
     * @param name the file name to use
     * @param comp the detected compression format
     * @param compData the compressed data
     * @param data the uncompressed data
     * @return parsed file obj
     * @throws NitroException if the conversion fails
     */
    private static RotomFile convert(RotomFormat magic, String path, String name, CompFormat comp, ByteArrayEditableData compData, ByteArrayEditableData data) throws NitroException {
        if (path != null) {
           return convertFromFile(magic, path, ((data != null)?data.getDataSize():0L));
        } else {
            return convertFromBinary(magic, name, comp, compData, data);
        }
    }


    /**
     * Convert raw data into a file obj
     *
     * @param magic the detected file format
     * @param path the file path, if not from a narc
     * @param name the file name to use
     * @param comp the detected compression format
     * @param compData the raw compressed data
     * @param data the raw uncompressed data
     * @return parsed file obj
     * @throws NitroException if the conversion fails
     */
    private static RotomFile convert(RotomFormat magic, String path, String name, CompFormat comp,  byte[] compData, byte[] data) throws NitroException, NitroException {
        return convert(magic,path,name,comp,new ByteArrayEditableData(compData),new ByteArrayEditableData(data));
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
    public static RotomFile fromNarc(String narcName, int index, long fileCount, byte[] compressedData) throws NitroException {
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
            compFormat = CompFormat.UNKNOWN;
            data = compressedData;
        }
        RotomFormat magic = parseFileFormat(data);
        return convert(magic, null, fileNameNoExt, compFormat, compressedData, data);

    }

    /**
     * Extract a file obj from a File
     *
     * @param file java file representation to parse
     * @return parsed file obj
     * @throws NitroException if file obj is unable to be parsed
     */
    public static RotomFile fromFile(File file) throws NitroException {
        String path = file.getAbsolutePath();
        String fileName = file.getName();
        CompFormat compFormat = CompFormat.NONE;
        // TODO currently after some refactoring, we now read the data twice and we should avoid that
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
            LOGGER.error("Failed to detect Compression method for " + fileName, e);
        }
        RotomFormat magic = parseFileFormat(data); // TODO shorter decompression to just read the magic
        return convert(magic, path, fileName, compFormat, compressedData, data);
    }
}