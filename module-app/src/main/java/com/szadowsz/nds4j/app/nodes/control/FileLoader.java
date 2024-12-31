package com.szadowsz.nds4j.app.nodes.control;

import com.szadowsz.nds4j.file.BaseNFSFile;

import java.io.IOException;

interface FileLoader<N extends BaseNFSFile> {

    N apply(String t) throws IOException;
}
