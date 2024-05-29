package com.szadowsz.nds4j.data.personal;

public class BaseStats {
    public int hp; // u8
    public int atk; // u8
    public int def; // u8
    public int speed; // u8
    public int spAtk; // u8
    public int spDef; // u8

    public BaseStats(int hp, int atk, int def, int speed, int spAtk, int spDef) {
        this.hp = hp;
        this.atk = atk;
        this.def = def;
        this.speed = speed;
        this.spAtk = spAtk;
        this.spDef = spDef;
    }
}
