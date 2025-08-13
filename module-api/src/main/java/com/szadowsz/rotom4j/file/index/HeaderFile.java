package com.szadowsz.rotom4j.file.index;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class HeaderFile {

    private final List<String> fileNames;

    public HeaderFile(String narcName, String path) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(path), Charset.forName("Shift_JIS"));
        if (lines.stream().anyMatch(s -> s.contains("enum {"))){
            List<String> fileLines = lines.stream()
                    .dropWhile(s -> !s.contains("enum {"))
                    .skip(1)
                    .takeWhile(s -> !s.equals("};"))
                    .map(l -> l.substring(1))
                    .map(l -> l.substring(0, Math.max(l.indexOf(" "), l.indexOf("\t")))
                            .replaceAll("NARC_","")
                            .replaceAll("narc_","")
                            .replaceAll(narcName.toLowerCase() + "_",""))
                    .toList();
            fileNames = fileLines.stream().map(HeaderLine::new).map(l -> l.name).toList();
        } else {
            List<String> fileLines = lines.stream()
                    .filter(l -> l.startsWith("#define") && !l.contains("_H_"))
                    .map(l -> l.substring(8)).map(l -> l.substring(0, Math.max(l.indexOf(" "), l.indexOf("\t")))).toList();
            fileNames = fileLines.stream().map(HeaderLine::new).map(l -> l.name).toList();
        }
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    class HeaderLine {

        private final Integer shouldCompress;
        private final String name;

        HeaderLine(String line) {
            int compress = line.lastIndexOf("_BIN");
            shouldCompress = (compress>=0)?1:0;
            String name = line;
            if (compress>0){
                name = line.substring(0,compress);
            }
            int ext = name.lastIndexOf("_");
            this.name = name.substring(0, ext).toLowerCase() + "." + name.substring(ext+1);
        }

        public int getShouldCompress() {
            return shouldCompress;
        }

    }
}
