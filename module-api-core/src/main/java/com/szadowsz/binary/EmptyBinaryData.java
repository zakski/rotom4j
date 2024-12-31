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
package com.szadowsz.binary;

import com.szadowsz.binary.exception.OutOfBoundsException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Empty read-only binary data.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class EmptyBinaryData implements BinaryData {

    public static final EmptyBinaryData INSTANCE = new EmptyBinaryData();

    @Override
    public byte getByte(long position) {
        throw new OutOfBoundsException();
    }

    @Override
    public InputStream getDataInputStream() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };
    }

    @Override
    public long getDataSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }



    @Override
    public BinaryData copy() {
        return this;
    }

    @Override
    public BinaryData copy(long startFrom, long length) {
        if (startFrom == 0 && length == 0) {
            return this;
        }

        throw new OutOfBoundsException();
    }

    @Override
    public void copyToArray(long startFrom, byte[] target, int offset, int length) {
        if (startFrom == 0 && length == 0) {
            return;
        }

        throw new OutOfBoundsException();
    }

    @Override
    public void saveToStream(OutputStream outputStream) throws IOException {
    }

    @Override
    public void dispose() {
    }
}
