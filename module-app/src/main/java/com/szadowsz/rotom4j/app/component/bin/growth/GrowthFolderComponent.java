package com.szadowsz.rotom4j.app.component.bin.growth;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.nds4j.file.bin.stats.GrowNFSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.szadowsz.nds4j.file.bin.stats.GrowNFSFile.LEVELS;

public class GrowthFolderComponent extends RFolder {

    protected Logger LOGGER = LoggerFactory.getLogger(GrowthFolderComponent.class);

    private final GrowNFSFile grow;

    public GrowthFolderComponent(RotomGui gui, String path, RFolder parent, GrowNFSFile grow) {
        super(gui, path, parent);
        this.grow = grow;
        lazyInitNodes();
    }

    protected void lazyInitNodes() {
        if (!children.isEmpty()) {
            return;
        }
        for (int i = 0; i < LEVELS;i++) {
            int xp = grow.getXPForLevel(i);
            children.add(new GrowthTextField(gui,path + "/level_" + i, this, ""+xp));
        }
    }
}
