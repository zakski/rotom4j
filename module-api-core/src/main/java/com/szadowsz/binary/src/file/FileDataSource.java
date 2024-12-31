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
package com.szadowsz.binary.src.file;

import com.szadowsz.binary.src.DataSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Data source for access to file resource locking it for exclusive access.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class FileDataSource implements DataSource {

    private final File file;
    private final RandomAccessFile accessFile;
    private final DeltaDataPageWindow window;
    private boolean closed = false;

    private final List<CacheClearListener> listeners = new ArrayList<>();

    public FileDataSource(File sourceFile, EditMode editMode) throws FileNotFoundException, IOException {
        file = sourceFile;
        accessFile = new RandomAccessFile(sourceFile, editMode.getFileAccessMode());
        window = new DeltaDataPageWindow(this);
    }

    public FileDataSource(File sourceFile) throws FileNotFoundException, IOException {
        this(sourceFile, EditMode.READ_WRITE);
    }

    /* package */ RandomAccessFile getAccessFile() {
        checkClosed();
        return accessFile;
    }

    @Override
    public byte getByte(long position) throws IOException {
        checkClosed();
        return window.getByte(position);
    }

    @Override
    public long getDataLength() throws IOException {
        checkClosed();
        return accessFile.length();
    }

    public File getFile() {
        return file;
    }

    @Override
    public void setByte(long position, byte value) throws IOException {
        checkClosed();
        accessFile.seek(position);
        accessFile.writeByte(value);
    }

    @Override
    public void setDataLength(long length) throws IOException {
        checkClosed();
        accessFile.setLength(length);
    }

    @Override
    public int read(long position, byte[] buffer, int offset, int length) throws IOException {
        accessFile.seek(position);
        return accessFile.read(buffer, offset, length);
    }

    @Override
    public void write(long position, byte[] buffer, int offset, int length) throws IOException {
        accessFile.seek(position);
        accessFile.write(buffer, offset, length);
    }

    /**
     * Clears cache windows.
     */
    @Override
    public void clearCache() {
        listeners.forEach(listener -> {
            listener.clearCache();
        });
    }

    @Override
    public void close() throws IOException {
        checkClosed();
        accessFile.close();
        closed = true;
    }

    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException("File was already closed");
        }
    }

    public void addCacheClearListener(CacheClearListener listener) {
        listeners.add(listener);
    }

    public void removeCacheClearListener(CacheClearListener listener) {
        listeners.remove(listener);
    }

    public static interface CacheClearListener {

        void clearCache();
    }

    public static enum EditMode {
        READ_WRITE("rw"),
        READ_ONLY("r");

        private final String fileAccessMode;

        private EditMode(String fileAccessMode) {
            this.fileAccessMode = fileAccessMode;
        }

        public String getFileAccessMode() {
            return fileAccessMode;
        }
    }
}
