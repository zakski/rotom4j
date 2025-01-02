package com.szadowsz.rotom4j.binary;

import com.szadowsz.binary.array.ByteArrayEditableData;
import com.szadowsz.binary.io.reader.HexInputStream;
import com.szadowsz.rotom4j.compression.CompFormat;
import com.szadowsz.rotom4j.compression.JavaDSDecmp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 */
public class ByteArrayCompressibleData extends ByteArrayEditableData {
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteArrayCompressibleData.class);

    public ByteArrayCompressibleData(byte[] data) {
        super(data);
    }

    public ByteArrayCompressibleData() {
        this(null);
    }

    public CompFormat getCompressionUsed() {
        CompFormat compFormat = CompFormat.NONE;
        try (HexInputStream input = new HexInputStream(new ByteArrayInputStream(data))) {
            compFormat = JavaDSDecmp.supports(input);
        } catch (IOException e) {
            LOGGER.error("Error checking decompression format", e);
        }
        return compFormat;
    }


    public ByteArrayEditableData uncompress() {
        int[] dataInt;
        try (HexInputStream input = new HexInputStream(new ByteArrayInputStream(data))) {
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
            return new ByteArrayEditableData(uncompressedData);
        } catch (IOException e) {
            LOGGER.warn("Error decompressing data", e);
            return new ByteArrayEditableData();
        }
    }
}
