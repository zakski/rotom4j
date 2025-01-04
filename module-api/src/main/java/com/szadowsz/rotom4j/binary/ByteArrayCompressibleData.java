package com.szadowsz.rotom4j.binary;

import com.szadowsz.rotom4j.binary.array.ByteArrayEditableData;
import com.szadowsz.rotom4j.binary.io.reader.HexInputStream;
import com.szadowsz.rotom4j.compression.CompFormat;
import com.szadowsz.rotom4j.compression.JavaDSDecmp;
import com.szadowsz.rotom4j.exception.InvalidFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 *
 */
public class ByteArrayCompressibleData extends ByteArrayEditableData {
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteArrayCompressibleData.class);

    protected final ByteArrayEditableData compressedData; // compressed data, whether or not the data was originally compressed
    protected final CompFormat compression;

    protected String filePath;
    protected String fileFullName;

    protected static byte[] uncompress(ByteArrayEditableData data) {
        int[] dataInt;
        try (HexInputStream input = new HexInputStream(new ByteArrayInputStream(data.getData()))) {
            CompFormat compFormat = JavaDSDecmp.supports(input);
            if (compFormat != CompFormat.NONE) {
                dataInt = JavaDSDecmp.decompress(input);
            } else {
                dataInt = input.readAllBytes();
            }
            byte[] uncompressedData = new byte[dataInt.length];
            for (int i = 0; i < dataInt.length; i++) {
                uncompressedData[i] = (byte) dataInt[i];
            }
            return uncompressedData;
        } catch (IOException e) {
            LOGGER.warn("Error decompressing data", e);
            return new byte[0];
        }
    }

    protected static CompFormat detectCompressionUsed(ByteArrayEditableData data) {
        CompFormat compFormat = CompFormat.NONE;
        try (HexInputStream input = new HexInputStream(new ByteArrayInputStream(data.getData()))) {
            compFormat = JavaDSDecmp.supports(input);
        } catch (IOException e) {
            LOGGER.error("Error checking decompression format", e);
        }
        return compFormat;
    }


    protected static ByteArrayEditableData readFile(File file) throws InvalidFileException {
        try (BufferedInputStream bs = new BufferedInputStream(new FileInputStream(file))) {
            return new ByteArrayEditableData(bs.readAllBytes());
        } catch (IOException e) {
            throw new InvalidFileException("Could not Load Nitro File " + file.getAbsolutePath(), e);
        }
    }

    public ByteArrayCompressibleData(ByteArrayEditableData data) {
        super(uncompress(data));
        compressedData = data;
        compression = detectCompressionUsed(compressedData);

    }

    public ByteArrayCompressibleData(byte[] data) {
        this(new ByteArrayEditableData(data));
    }

    public ByteArrayCompressibleData(File file) throws InvalidFileException {
        this(readFile(file));
        // TODO null handling
        this.filePath = file.getAbsolutePath();
        this.fileFullName = file.getName();
    }

    public ByteArrayCompressibleData(String filepath) throws InvalidFileException {
        this(new File(filepath));
        // TODO null handling
    }

    public ByteArrayCompressibleData() throws InvalidFileException {
        this((File) null); // TODO bit of a trap here
    }

    public byte[] getCompressedBytes() {
        return compressedData.getData();
    }

    public ByteArrayEditableData getCompressedData() { // TODO inconsistent naming/return type
        return compressedData;
    }

    public String getFileName() {
        return fileFullName;
    }

    public String getFilePath() {
        return filePath;
    }
}
