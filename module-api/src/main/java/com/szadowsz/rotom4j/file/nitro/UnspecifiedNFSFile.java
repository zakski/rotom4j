package com.szadowsz.rotom4j.file.nitro;

import com.szadowsz.binary.array.ByteArrayData;
import com.szadowsz.binary.array.ByteArrayEditableData;
import com.szadowsz.rotom4j.compression.CompFormat;
import com.szadowsz.rotom4j.file.NFSFormat;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.binary.io.reader.MemBuf;

/**
 * Class to hold the data of Nitro files that are not supported specifically yet
 */
public class UnspecifiedNFSFile extends GenericNFSFile {

    /**
     * Constructor to Use after decompressing file data and assessing its contents
     *
     * @param magic the file type
     * @param path the path of the file
     * @param name the name of the file
     * @param comp the compression format used (if any)
     * @param compData the raw compressed data
     * @param data the raw uncompressed data
     */
    public UnspecifiedNFSFile(NFSFormat magic, String path, String name, CompFormat comp, byte[] compData, byte[] data) throws NitroException {
        super(magic,path,name,comp,compData,data);

        MemBuf buf = MemBuf.create(rawData.getData());

        MemBuf.MemBufReader reader = buf.reader();
        readGenericNtrHeader(reader);
        int headerLength = reader.getPosition();
        reader.setPosition(0);
        this.headerData = reader.readTo(headerLength);

        readFile(reader);

    }

    /**
     * Constructor to Use after decompressing file data and assessing its contents
     *
     * @param magic the file type
     * @param path the path of the file
     * @param name the name of the file
     * @param comp the compression format used (if any)
     * @param compData the raw compressed data
     * @param data the raw uncompressed data
     */
    public UnspecifiedNFSFile(NFSFormat magic, String path, String name, CompFormat comp, ByteArrayData compData, ByteArrayEditableData data) throws NitroException {
        super(magic,path,name,comp,compData,data);

        MemBuf buf = MemBuf.create(rawData.getData());

        MemBuf.MemBufReader reader = buf.reader();
        readGenericNtrHeader(reader);
        int headerLength = reader.getPosition();
        reader.setPosition(0);
        this.headerData = reader.readTo(headerLength);

        readFile(reader);

    }

    @Override
    protected void readFile(MemBuf.MemBufReader reader) {
        // NOOP
    }
}
