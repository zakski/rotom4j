package com.szadowsz.rotom4j.component.control;

import com.szadowsz.rotom4j.file.RotomFile;
import com.szadowsz.rotom4j.file.nitro.BaseNFSFile;

import java.io.IOException;

interface FileLoader<N extends RotomFile> {

    N apply(String t) throws IOException;
}
