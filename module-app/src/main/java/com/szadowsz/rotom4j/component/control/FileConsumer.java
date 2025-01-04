package com.szadowsz.rotom4j.component.control;

import com.szadowsz.rotom4j.file.RotomFile;
import com.szadowsz.rotom4j.file.nitro.BaseNFSFile;

import java.io.IOException;

interface FileConsumer<N extends RotomFile> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(N t) throws IOException;
}