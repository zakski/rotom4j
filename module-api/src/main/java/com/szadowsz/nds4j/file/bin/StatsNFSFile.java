package com.szadowsz.nds4j.file.bin;

import com.szadowsz.nds4j.data.BinFormat;
import com.szadowsz.nds4j.data.personal.BaseStats;
import com.szadowsz.nds4j.data.personal.YieldStats;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.reader.Buffer;
import com.szadowsz.nds4j.reader.MemBuf;

import java.io.File;

public class StatsNFSFile extends BinNFSFile {
    private static final int NUMBER_TM_HM_BITS = 128;

    public static final String[] fields = new String[]{"HP","Attack","Defence","Special Attack","Special Defence","Speed",
            "Type 1", "Type 2", "Catch Rate", "Base Exp",
            "HP Yield","Attack Yield","Defence Yield","Special Attack Yield","Special Defence Yield","Speed Yield",
            "Uncommon Item","Rare Item",
            "Gender Ratio",
            "Egg Cycles",
            "Base Happiness",
            "Exp Rate",
            "Egg Group 1", "Egg Group 2",
            "Ability 1", "Ability 2",
            "Run Chance",
            "Dex Colour", "Flip",
    };

    private BaseStats stats;
    private int type1; // u8
    private int type2; // u8
    private int catchRate; // u8
    private int baseExp; // u8

    private YieldStats yieldStats;

    private int uncommonItem; // u16
    private int rareItem; // u16

    private int genderRatio; // u8

    private int eggCycles; // u8

    private int baseHappiness; // u8

    private int expRate; // u8

    private int eggGroup1; // u8
    private int eggGroup2; // u8

    private int ability1; // u8
    private int ability2; // u8

    private int runChance; // u8

    private int dexColor; // u8:7
    private boolean flip;  // u8:1

    private boolean[] tmCompatibility; // u8[16], each TM is a single bit

    public StatsNFSFile(String path, String name, byte[] bytes) {
        super(BinFormat.PERSONAL, path, name, bytes);

        MemBuf dataBuf = MemBuf.create(rawData);
        MemBuf.MemBufReader reader = dataBuf.reader();


        stats = new BaseStats(reader.readUInt8(),reader.readUInt8(),reader.readUInt8(),reader.readUInt8(),reader.readUInt8(),reader.readUInt8());
        type1 = reader.readUInt8();
        type2 = reader.readUInt8();
        catchRate = reader.readUInt8();
        baseExp = reader.readUInt8();

        yieldStats = new YieldStats(reader.readUInt16());

        uncommonItem = reader.readUInt16();
        rareItem = reader.readUInt16();

        genderRatio = reader.readUInt8();
        eggCycles = reader.readUInt8();
        baseHappiness = reader.readUInt8();
        expRate = reader.readUInt8();
        eggGroup1 = reader.readUInt8();
        eggGroup2 = reader.readUInt8();
        ability1 = reader.readUInt8();
        ability2 = reader.readUInt8();

        runChance = reader.readUInt8();

        int colorFlip = reader.readUInt8();
        dexColor = colorFlip & 0x7F;
        flip = ((colorFlip & 0x80) >> 7) == 1;

        reader.skip(2); // 2 bytes padding

        this.tmCompatibility = new boolean[NUMBER_TM_HM_BITS];
        if (reader.getBuffer().length == 16) {

            byte[] tmLearnset = reader.readBytes(16);

            for (int i = 0; i < NUMBER_TM_HM_BITS; i++) {
                this.tmCompatibility[i] = (tmLearnset[i / 8] & 1 << (i % 8)) != 0;
            }
        }
    }

    /**
     * Read Personal Stats Data from File
     *
     * @param path path to File
     * @return Evolution File Data
     * @throws NitroException if the read failed
     */
    public static StatsNFSFile fromFile(String path) throws NitroException {
        return fromFile(new File(path));
    }

    /**
     * Read Personal Stats Data from File
     *
     * @param file File Object to parse
     * @return Evolution File Data
     * @throws NitroException if the read failed
     */
    public static StatsNFSFile fromFile(File file) throws NitroException {
        String path = file.getAbsolutePath();
        String fileName = file.getName();
        byte[] data = Buffer.readFile(path);
        return new StatsNFSFile(path,fileName,data);
    }

    public int getField(String field){
        return switch (field){
            case "HP" -> stats.hp;
            case "Attack" -> stats.atk;
            case "Defence" -> stats.def;
            case "Special Attack" -> stats.spAtk;
            case "Special Defence" -> stats.spDef;
            case "Speed" -> stats.speed;
            case "Type 1" -> type1;
            case "Type 2" -> type2;
            case "Catch Rate" -> catchRate;
            case "Base Exp" -> baseExp;
            case "HP Yield" -> yieldStats.hpEvYield;
            case "Attack Yield" -> yieldStats.atkEvYield;
            case "Defence Yield" -> yieldStats.defEvYield;
            case "Special Attack Yield" -> yieldStats.spAtkEvYield;
            case "Special Defence Yield" -> yieldStats.spDefEvYield;
            case "Speed Yield" -> yieldStats.speedEvYield;
            case "Uncommon Item" -> uncommonItem;
            case "Rare Item" -> rareItem;
            case "Gender Ratio" -> genderRatio;
            case "Egg Cycles" -> eggCycles;
            case "Base Happiness" -> baseHappiness;
            case "Exp Rate" -> expRate;
            case "Egg Group 1" -> eggGroup1;
            case "Egg Group 2" -> eggGroup2;
            case "Ability 1" -> ability1;
            case "Ability 2" -> ability2;
            case "Run Chance" -> runChance;
            case "Dex Colour" -> dexColor;
            case "Flip" -> flip ? 1 : 0;
            default -> -1;
        };
    }
}
