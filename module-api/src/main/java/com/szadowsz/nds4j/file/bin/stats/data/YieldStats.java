package com.szadowsz.nds4j.file.bin.stats.data;

public class YieldStats {

    public int hpEvYield; // u16:2
    public int atkEvYield; // u16:2
    public int defEvYield; // u16:2
    public int speedEvYield; // u16:2
    public int spAtkEvYield; // u16:2
    public int spDefEvYield; // u16:2

    public YieldStats(int evYield) {
        this.hpEvYield = getHpEv(evYield);
        this.atkEvYield = getAtkEv(evYield);
        this.defEvYield = getDefEv(evYield);
        this.speedEvYield = getSpeedEv(evYield);
        this.spAtkEvYield = getSpAtkEv(evYield);
        this.spDefEvYield = getSpDefEv(evYield);
    }

    private  int getHpEv(int x) {
        return x & 0x03;
    }

    private  int getAtkEv(int x) {
        return (x >> 2) & 0x03;
    }

    private  int getDefEv(int x) {
        return (x >> 4) & 0x03;
    }

    private  int getSpeedEv(int x) {
        return (x >> 6) & 0x03;
    }

    private  int getSpAtkEv(int x) {
        return (x >> 8) & 0x03;
    }

    private  int getSpDefEv(int x) {
        return (x >> 10) & 0x03;
    }
}
