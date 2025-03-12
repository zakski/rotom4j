package com.szadowsz.rotom4j.file.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ScrFile {

    private final List<String> fileNames;

    public ScrFile(String path) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(path));
        fileNames = lines.stream().map(l -> l.substring(1,l.length()-1)).toList();
    }

    public List<String> getFileNames() {
        return fileNames;
    }
}