package com.szadowsz.nds4j.app.nodes.bin.learn;

import com.szadowsz.nds4j.file.bin.LearnsetNFSFile;
import com.szadowsz.ui.node.impl.FolderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LearnFolderNode extends FolderNode {

    protected Logger LOGGER = LoggerFactory.getLogger(LearnFolderNode.class);

    private LearnsetNFSFile learn;

    public LearnFolderNode(String path, FolderNode parent, LearnsetNFSFile learn) {
        super(path, parent);
        this.learn = learn;
        lazyInitNodes();
    }

    protected void lazyInitNodes() {
        if (!children.isEmpty()) {
            return;
        }
        for (int i = 0; i < learn.getNumMoves();i++) {
            children.add(new LearnTextNode(path + "/move_" + i, this, ""+learn.getMove(i)));
            children.add(new LearnTextNode(path + "/level_" + i, this, ""+learn.getLevel(i)));
        }
    }
}
