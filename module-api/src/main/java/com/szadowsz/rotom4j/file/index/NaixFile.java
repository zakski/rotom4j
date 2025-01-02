package com.szadowsz.rotom4j.file.index;

import com.szadowsz.rotom4j.file.NFSFormat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class NaixFile {

    private final List<String> fileNames;

    public NaixFile(String path) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(path), Charset.forName("Shift_JIS"));
        List<String> fileLines = lines.stream()
                .filter(l -> l.startsWith("\tNARC_"))
                .map(l -> l.substring(6))
                .map(l -> l.substring(0,Math.max(l.indexOf(" "),l.indexOf("\t"))))
                .map(l -> l.substring(l.indexOf("_")+1))
                .map(l -> l.replace("_lzh_","_"))
                .map(l -> l.replace("_lz_","_"))
                .map(l -> {
                    String reverse = new StringBuffer(l).reverse().toString();
                    reverse = reverse.replaceFirst("_",".");
                    return new StringBuffer(reverse).reverse().toString();
                })
                .map(l -> {
                    String[] split = l.split("\\.");
                    String ext = NFSFormat.valueOfExt(split[1])==null?split[1].toUpperCase():split[1];
                    return split[0] + "." + ext;
                })
                .toList();
        fileNames = fileLines;
    }

    public NaixFile(String narcName, String path) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(path), Charset.forName("Shift_JIS"));
        List<String> fileLines = lines.stream()
                .filter(l -> l.startsWith("\tNARC_"))
                .map(l -> l.substring(6))
                .map(l -> l.substring(0,Math.max(l.indexOf(" "),l.indexOf("\t"))))
                .map(l -> l.replaceFirst(narcName + "_",""))
                .map(l -> l.replace("_lzh_","_"))
                .map(l -> l.replace("_lz_","_"))
                .map(l -> {
                    String reverse = new StringBuffer(l).reverse().toString();
                    reverse = reverse.replaceFirst("_",".");
                    return new StringBuffer(reverse).reverse().toString();
                })
                .map(l -> {
                    String[] split = l.split("\\.");
                    String ext = NFSFormat.valueOfExt(split[1])==null?split[1].toUpperCase():split[1];
                    return split[0] + "." + ext;
                })
                .toList();
        fileNames = fileLines;
    }

    public List<String> getFileNames() {
        return fileNames;
    }
}
