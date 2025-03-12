package com.szadowsz.rotom4j.file.data.learnset.data;

public class LearnsetEntry {
    private int moveID;
    private int level;

    public LearnsetEntry() {
        moveID = 0;
        level = 0;
    }

    public LearnsetEntry(int moveID, int level) {
        this.moveID = moveID;
        this.level = level;
    }

    public int getMoveID() {
        return moveID;
    }

    public void setMoveID(int moveID) {
        this.moveID = moveID;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}