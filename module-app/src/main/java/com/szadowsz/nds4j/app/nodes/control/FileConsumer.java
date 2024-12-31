package com.szadowsz.nds4j.app.nodes.control;

import com.szadowsz.nds4j.file.BaseNFSFile;

import java.io.IOException;

interface FileConsumer<N extends BaseNFSFile> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(N t) throws IOException;
}