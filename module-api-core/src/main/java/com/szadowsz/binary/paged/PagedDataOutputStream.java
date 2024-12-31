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
package com.szadowsz.binary.paged;

import com.szadowsz.binary.FinishableStream;
import com.szadowsz.binary.SeekableStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream for paged data.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class PagedDataOutputStream extends OutputStream implements SeekableStream, FinishableStream {

    private final PagedData data;
    private long position = 0;

    public PagedDataOutputStream(PagedData data) {
        this.data = data;
    }

    @Override
    public void write(int value) throws IOException {
        long dataSize = data.getDataSize();
        if (position == dataSize) {
            dataSize++;
            data.setDataSize(dataSize);
        }
        data.setByte(position++, (byte) value);
    }

    @Override
    public void write(byte[] input, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }

        long dataSize = data.getDataSize();
        if (position + len > dataSize) {
            data.setDataSize(position + len);
        }

        int length = len;
        int offset = off;
        while (length > 0) {
            byte[] page = data.getPage((int) (position / data.getPageSize()));
            int srcPos = (int) (position % data.getPageSize());
            int copyLength = page.length - srcPos;
            if (copyLength > length) {
                copyLength = length;
            }

            if (copyLength == 0) {
                return;
            }

            System.arraycopy(input, offset, page, srcPos, copyLength);
            length -= copyLength;
            position += copyLength;
            offset += copyLength;
        }
    }

    @Override
    public void seek(long position) throws IOException {
        this.position = position;
    }

    @Override
    public long getStreamSize() {
        return data.getDataSize();
    }

    @Override
    public long getProcessedSize() {
        return position;
    }

    @Override
    public void close() throws IOException {
        finish();
    }

    @Override
    public long finish() throws IOException {
        position = data.getDataSize();
        return position;
    }
}
