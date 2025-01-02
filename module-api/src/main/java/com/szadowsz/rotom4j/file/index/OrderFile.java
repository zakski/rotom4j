package com.szadowsz.rotom4j.file.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OrderFile {

    private final List<String> fileNames;

    public OrderFile(String path) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(path));
        List<OrderFile.OrderLine> unsortedFilenames = lines.stream().map(OrderFile.OrderLine::new).toList();
        Integer sortBy = unsortedFilenames.get(0).shouldCompress;
        fileNames = unsortedFilenames.stream().sorted(Comparator.comparing(OrderFile.OrderLine::getShouldCompress, (s1, s2) -> {
            return (sortBy==1)?Integer.compare(s2,s1):Integer.compare(s1,s2);
        })).map(l -> l.name).toList();
    }

    public OrderFile(List<String> files) {
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

    class OrderLine implements Comparable<OrderFile.OrderLine> {

        private final Integer shouldCompress;
        private final String name;

        OrderLine(String line) {
            name = line;
            shouldCompress = 0;
        }

        public int getShouldCompress(){
            return shouldCompress;
        }

        @Override
        public int compareTo(OrderFile.OrderLine other) {
            int first = Integer.compare(other.shouldCompress,shouldCompress);
            return first;
        }
    }
}
