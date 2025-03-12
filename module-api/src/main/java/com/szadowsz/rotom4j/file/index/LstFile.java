package com.szadowsz.rotom4j.file.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class LstFile {

    private final List<String> fileNames;

    public LstFile(String path) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(path));
        List<LstLine> unsortedFilenames = lines.stream().map(LstLine::new).toList();
        Integer sortBy = unsortedFilenames.get(0).shouldCompress;
        fileNames = unsortedFilenames.stream().sorted(Comparator.comparing(LstLine::getShouldCompress, (s1, s2) -> {
            return (sortBy==1)?Integer.compare(s2,s1):Integer.compare(s1,s2);
        })).map(l -> l.name).toList();
    }

    public LstFile(List<String> files) {
        fileNames = new ArrayList<>();
        for (String f : files){
            fileNames.add(f);
        }
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public List<String> getOutputList() {
        List<String> out = new ArrayList<>();
        for (String line : fileNames){
            out.add("\"" + line + "\"");
        }
        return out;
    }

    class LstLine implements Comparable<LstLine> {

        private final Integer shouldCompress;
        private final String name;

        LstLine(String line) {
            String[] fields = line.split(",");
            shouldCompress = Integer.valueOf(fields[0]);
            //"contest_bg\con_bg.NSCR"
            name = fields[1].substring(1, fields[1].length() - 1).replaceAll(".+" + Pattern.quote("\\"), "");
            System.out.println("");
        }

        public int getShouldCompress(){
            return shouldCompress;
        }

        @Override
        public int compareTo(LstLine other) {
            int first = Integer.compare(other.shouldCompress,shouldCompress);
            return first;
//            if (first != 0){
//                return first;
//            } else {
//                return name.compareTo(other.name);
//            }
        }
    }
}
