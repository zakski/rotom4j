/*
 * Copyright (c) 2023 Turtleisaac.
 *
 * This file is part of Nds4j.
 *
 * Nds4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nds4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nds4j. If not, see <https://www.gnu.org/licenses/>.
 */

package com.szadowsz.nds4j.writer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Arrays;

public class BinaryWriter {

    private final RandomAccessFile raf;
    private final byte[] buf = new byte[8];

    public static void writeFile(File file, int... bytes) throws IOException {
        BinaryWriter writer = new BinaryWriter(file);
        writer.writeBytes(bytes);
        writer.close();
    }

    public static void writeFile(File file, byte... bytes) throws IOException
    {
        BinaryWriter writer = new BinaryWriter(file);
        writer.write(bytes);
        writer.close();
    }

    public static void writeFile(String file, byte... bytes) throws IOException
    {
        writeFile(new File(file),bytes);
    }

    public static void writeFile(Path file, byte... bytes) throws IOException
    {
        writeFile(file.toFile(),bytes);
    }

    public BinaryWriter(File file) throws IOException {
        raf = new RandomAccessFile(file, "rw");
        raf.setLength(0);
    }

    public BinaryWriter(String fileName) throws IOException
    {
        this(new File(fileName));
    }

    public BinaryWriter(Path file) throws IOException {
        this(file.toFile());
    }



    public void setPosition(long pos) throws IOException {
        raf.seek(pos);
    }
    
    public long getPosition() throws IOException {
        return raf.getFilePointer();
    }
    
    public void skipBytes(int bytes) throws IOException {
        raf.skipBytes(bytes);
    }

    public void writeInt(int i) throws IOException {
        buf[0] = (byte) (i & 0xff);
        buf[1] = (byte) ((i >> 8) & 0xff);
        buf[2] = (byte) ((i >> 16) & 0xff);
        buf[3] = (byte) ((i >> 24) & 0xff);
        raf.write(buf, 0, 4);
    }

    public void writeInts(int... i) throws IOException {
        for(int in : i) {
            writeInt(in);
        }
    }

    public void writeLong(long i) throws IOException {
        buf[0] = (byte) (i & 0xff);
        buf[1] = (byte) ((i >> 8) & 0xff);
        buf[2] = (byte) ((i >> 16) & 0xff);
        buf[3] = (byte) ((i >> 24) & 0xff);
        buf[4] = (byte) ((i >> 32) & 0xff);
        buf[5] = (byte) ((i >> 40) & 0xff);
        buf[6] = (byte) ((i >> 48) & 0xff);
        buf[7] = (byte) ((i >> 56) & 0xff);
        raf.write(buf, 0, 8);
    }

    public void writeShort(short s) throws IOException {
        buf[0] = (byte) (s & 0xff);
        buf[1] = (byte) ((s >> 8) & 0xff);
        raf.write(buf, 0, 2);
    }

    public void writeByte(byte b) throws IOException {
        raf.write(b);
    }

    public void writeByte(int b) throws IOException
    {
        writeByte((byte) b);
    }

    public void writeBytes(int... bytes) throws IOException {
        for (int b : bytes) {
            raf.write(b);
        }
    }

    public void writeShorts(int... shorts) throws IOException {
        for (int s : shorts) {
            writeShort((short) (s & 0xffff));
        }
    }

    public void writeShorts(short... shorts) throws IOException {
        for (short s : shorts) {
            writeShort(s);
        }
    }

    public void write(byte... bytes) throws IOException {
        raf.write(bytes);
    }

    public void write(byte[] bytes, int offset, int length) throws IOException {
        raf.write(bytes, offset, length);
    }

    public void writeByteNumTimes(byte b, int num) throws IOException {
        byte[] bytes= new byte[num];
        Arrays.fill(bytes,b);
        write(bytes);
    }

    public void writeByteNumTimes(int b, int num) throws IOException {
        byte[] bytes= new byte[num];
        Arrays.fill(bytes,(byte) b);
        write(bytes);
    }

    public void writePadding(int num) throws IOException {
        writeByteNumTimes((byte) 0x00,num);
    }

    public void close() throws IOException {
        raf.close();
    }

    public void write(int i) {
    }
}
