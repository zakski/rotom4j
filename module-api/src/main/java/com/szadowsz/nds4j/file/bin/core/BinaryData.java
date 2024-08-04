package com.szadowsz.nds4j.file.bin.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for binary data - read-only sequence of bytes.
 *
 * Provides methods to read whole or part of the data to array or stream.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface BinaryData {

    /**
     * Returns true if data are empty.
     *
     * @return true if data empty
     */
    boolean isEmpty();

    /**
     * Returns size of data or -1 if size is not available.
     *
     * @return size of data in bytes
     */
    long getDataSize();

    /**
     * Returns particular byte from data.
     *
     * @param position position
     * @return byte on requested position
     */
    byte getByte(long position);

    /**
     * Creates copy of all data.
     *
     * @return copy of data
     */
    BinaryData copy();

    /**
     * Creates copy of given area.
     *
     * @param startFrom position to start copy from
     * @param length length of area
     * @return copy of data
     */
    BinaryData copy(long startFrom, long length);

    /**
     * Creates copy of given area into array of bytes.
     *
     * @param startFrom position to start copy from
     * @param target target byte array
     * @param offset offset position in target
     * @param length length of area to copy
     */
    void copyToArray(long startFrom, byte[] target, int offset, int length);

    /**
     * Saves/copies all data to given stream.
     *
     * @param outputStream output stream
     * @throws java.io.IOException if input/output error
     */
    void saveToStream(OutputStream outputStream) throws IOException;

    /**
     * Provides handler for input stream generation.
     *
     * @return new instance of input stream
     */
    InputStream getDataInputStream();

    /**
     * Disposes all allocated data if possible.
     */
    void dispose();
}
