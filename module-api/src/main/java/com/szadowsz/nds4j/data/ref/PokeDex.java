package com.szadowsz.nds4j.data.ref;

import com.szadowsz.nds4j.NFSConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class PokeDex {

    private static final Map<Integer, String> FEB17_DEX = loadFile("genivdex_feb17.csv");

    private static final Map<Integer, String> FINAL_DEX = loadFile("genivdex_final.csv");

    private PokeDex(){}

    private static InputStreamReader getStreamReader(ClassLoader classloader, String file) {
        return new InputStreamReader(classloader.getResourceAsStream(file), StandardCharsets.UTF_8);
    }

    private static Map<Integer, String> loadFile(String file) {
        Map<Integer,String> dex = new HashMap<>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStreamReader sr = getStreamReader(classloader, file); BufferedReader br = new BufferedReader(sr)) {

            for (String line; (line = br.readLine()) != null; ) {
                String[] kvPair = line.split(",");
                dex.put(Integer.parseInt(kvPair[0]),kvPair[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dex;
    }

    public static String getPokemonNameByNo(int index) {
        return switch (NFSConstants.getExpectedRom()){
            case FEB17 -> FEB17_DEX.getOrDefault(index,"NONE");
            case FINAL -> FINAL_DEX.getOrDefault(index,"NONE");
        };
    }
}


