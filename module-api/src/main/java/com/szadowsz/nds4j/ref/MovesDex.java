package com.szadowsz.nds4j.ref;

import com.szadowsz.nds4j.utils.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MovesDex {

   private static final Map<Integer, String> FINAL_MOVES = loadFile("moves_final.csv");

    private MovesDex() {
    }

    private static InputStreamReader getStreamReader(ClassLoader classloader, String file) {
        return new InputStreamReader(classloader.getResourceAsStream(file), StandardCharsets.UTF_8);
    }

    private static Map<Integer, String> loadFile(String file) {
        Map<Integer, String> dex = new HashMap<>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStreamReader sr = getStreamReader(classloader, file); BufferedReader br = new BufferedReader(sr)) {

            for (String line; (line = br.readLine()) != null; ) {
                String[] kvPair = line.split(",");
                dex.put(Integer.parseInt(kvPair[0]), kvPair[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dex;
    }

    public static String getPokemonNameByNo(int index) {
        return switch (Configuration.getExpectedRom()) {
            default -> FINAL_MOVES.getOrDefault(index, "NONE");
        };
    }
}
