package com.szadowsz.rotom4j.file.nitro;

import com.szadowsz.rotom4j.binary.array.ByteArrayData;
import com.szadowsz.rotom4j.binary.array.ByteArrayEditableData;
import com.szadowsz.rotom4j.compression.CompFormat;
import com.szadowsz.rotom4j.exception.InvalidFileException;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.binary.io.reader.MemBuf;
import com.szadowsz.rotom4j.file.RotomFormat;

/**
 * Class to hold the data of Nitro files that are not supported specifically yet
 */
public class UnspecifiedNFSFile extends BaseNFSFile {

    /**
     * Constructor to Use after decompressing file data and assessing its contents
     *
     * @param magic     the file type
     * @param filePath  the path of the file
     */
    public UnspecifiedNFSFile(RotomFormat magic, String filePath) throws NitroException {
        super(magic,filePath);

        MemBuf buf = MemBuf.create(data);

        MemBuf.MemBufReader reader = buf.reader();
        readGenericNtrHeader(reader);
        int headerLength = reader.getPosition();
        reader.setPosition(0);
        this.headerData = reader.readTo(headerLength);

        readFile(reader);

    }

    public UnspecifiedNFSFile(RotomFormat magic, String name, ByteArrayEditableData compData) throws NitroException {
        super(magic, name, compData);
        MemBuf buf = MemBuf.create(data);

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
