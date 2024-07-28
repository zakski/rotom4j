package com.szadowsz.nds4j.app.nodes.bin.grow;

import com.szadowsz.nds4j.file.bin.stats.GrowNFSFile;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.impl.FolderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.szadowsz.nds4j.file.bin.stats.GrowNFSFile.LEVELS;

public class GrowthFolderNode extends FolderNode {

    protected Logger LOGGER = LoggerFactory.getLogger(GrowthFolderNode.class);

    private final GrowNFSFile grow;

    public GrowthFolderNode(String path, FolderNode parent, GrowNFSFile grow) {
        super(path, parent, LayoutType.VERTICAL_X_COL);
        this.grow = grow;
        lazyInitNodes();
    }

    protected void lazyInitNodes() {
        if (!children.isEmpty()) {
            return;
        }
        for (int i = 0; i < LEVELS;i++) {
            int xp = grow.getXPForLevel(i);
            children.add(new GrowthTextNode(path + "/level_" + i, this, ""+xp,i%5));
        }
    }
}
