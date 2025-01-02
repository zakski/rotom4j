package com.szadowsz.rotom4j.app.component.control;

import com.szadowsz.rotom4j.file.BaseNFSFile;

import java.io.IOException;

interface FileConsumer<N extends BaseNFSFile> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(N t) throws IOException;
}