package com.szadowsz.nds4j;

import com.szadowsz.nds4j.compression.CompFormat;
import com.szadowsz.nds4j.compression.JavaDSDecmp;
import com.szadowsz.nds4j.data.NFSFormat;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.BaseNFSFile;
import com.szadowsz.nds4j.file.bin.BinNFSFile;
import com.szadowsz.nds4j.file.bin.PlaceholderNFSFile;
import com.szadowsz.nds4j.file.nitro.*;
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

    private static NFSFormat parseFileFormat(byte[] data) {
        if (data != null && data.length > 4) {
            String magic = new String(Arrays.copyOfRange(data, 0, 4), Charset.forName("Shift_JIS"));
            return NFSFormat.valueOfLabel(magic);
        } else {
            return NFSFormat.BINARY;
        }
    }

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
        } catch (IOException e) {
            LOGGER.warn("Failed to decompress " + fileNameNoExt, e);
        }
        NFSFormat magic = parseFileFormat(data);
        String fileName = fileNameNoExt + "." + magic.getExt()[0];
        return convert(null, fileName, compFormat, magic, compressedData, data);

    }

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

    private static BaseNFSFile convert(String path, String name, CompFormat comp, NFSFormat magic, byte[] compData, byte[] data) throws NitroException, NitroException {
        switch (magic) {
            case NCGR -> {
                return new NCGR(path, name, comp, compData, data);
            }
            case NCLR -> {
                return new NCLR(path, name, comp, compData, data);
            }
            case NSCR -> {
                return new NSCR(path, name, comp, compData, data);
            }
            case NCER -> {
                return new NCER(path, name, comp, compData, data);
            }
            case NANR -> {
                return new NANR(path, name, comp, compData, data);
            }
            case BINARY -> {
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
}