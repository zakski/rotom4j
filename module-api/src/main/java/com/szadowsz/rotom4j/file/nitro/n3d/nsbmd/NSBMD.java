package com.szadowsz.rotom4j.file.nitro.n3d.nsbmd;

import com.szadowsz.rotom4j.binary.array.ByteArrayEditableData;
import com.szadowsz.rotom4j.binary.io.reader.MemBuf;
import com.szadowsz.rotom4j.exception.InvalidFileException;
import com.szadowsz.rotom4j.exception.NitroException;
import com.szadowsz.rotom4j.file.RotomFormat;
import com.szadowsz.rotom4j.file.nitro.BaseNFSFile;

public class NSBMD extends BaseNFSFile {

    public NSBMD(String filePath) throws InvalidFileException {
        super(RotomFormat.NSBMD, filePath);
    }

    public NSBMD(String name, ByteArrayEditableData compData) throws InvalidFileException {
        super(RotomFormat.NSBMD, name, compData);
    }

    @Override
    protected void readFile(MemBuf.MemBufReader reader) throws NitroException {

    }
}
