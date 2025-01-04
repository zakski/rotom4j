package com.szadowsz.rotom4j.component.bin.learn;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.rotom4j.file.data.learnset.LearnsetNFSFile;
import com.szadowsz.rotom4j.ref.MovesDex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LearnFolderComponent extends RFolder {

    protected Logger LOGGER = LoggerFactory.getLogger(LearnFolderComponent.class);

    private final LearnsetNFSFile learn;

    public LearnFolderComponent(RotomGui gui, String path, RFolder parent, LearnsetNFSFile learn) {
        super(gui, path, parent);
        this.learn = learn;
        lazyInitNodes();
    }

    protected void lazyInitNodes() {
        if (!children.isEmpty()) {
            return;
        }
        for (int i = 0; i < learn.getNumMoves();i++) {
            int moveIndex = learn.getMove(i);
            children.add(new LearnTextField(gui,path + "/move_" + i, this, MovesDex.getPokemonNameByNo(moveIndex)));
            children.add(new LearnTextField(gui,path + "/level_" + i, this, ""+learn.getLevel(i)));
        }
    }
}
