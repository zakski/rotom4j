/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.szadowsz.binary.src;

import java.io.IOException;

/**
 * Data source interface.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface DataSource {

    /**
     * Returns single byte of the data.
     *
     * @param position data position
     * @return byte value
     * @throws IOException input output exception
     */
    byte getByte(long position) throws IOException;

    /**
     * Returns length of the data.
     *
     * @return length of the data
     * @throws IOException input output exception
     */
    long getDataLength() throws IOException;

    /**
     * Sets single byte of the data.
     *
     * @param position data position
     * @param value byte value
     * @throws IOException input output exception
     */
    void setByte(long position, byte value) throws IOException;

    /**
     * Sets data length.
     *
     * @param dataLength data length
     * @throws IOException input output exception
     */
    void setDataLength(long dataLength) throws IOException;


    /**
     * Reads data to buffer.
     *
     * @param position data position
     * @param buffer data buffer
     * @param offset buffer offset
     * @param length data length
     * @return length of data red
     * @throws IOException input output exception
     */
    int read(long position, byte[] buffer, int offset, int length) throws IOException;

    /**
     * Writes data from buffer.
     *
     * @param position data position
     * @param buffer data buffer
     * @param offset buffer offset
     * @param length data length
     * @throws IOException input output exception
     */
    void write(long position, byte[] buffer, int offset, int length) throws IOException;

    /**
     * Clears caches.
     */
    void clearCache();

    /**
     * Close data source.
     *
     * @throws IOException input output exception
     */
    void close() throws IOException;
}
