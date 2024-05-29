package com.szadowsz.nds4j.app.nodes.bin.stats;

import com.szadowsz.nds4j.data.personal.*;
import com.szadowsz.nds4j.data.ref.Items;
import com.szadowsz.nds4j.file.bin.StatsNFSFile;
import com.szadowsz.ui.node.impl.FolderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsFolderNode extends FolderNode {

    protected Logger LOGGER = LoggerFactory.getLogger(StatsFolderNode.class);

    private StatsNFSFile stats;

    public StatsFolderNode(String path, FolderNode parent, StatsNFSFile stats) {
        super(path, parent);
        this.stats = stats;
        lazyInitNodes();
    }

    protected String getContent(int i) {
        String field = StatsNFSFile.fields[i];
        return switch (field){
            case "Ability 1", "Ability 2" -> PkAbility.values()[stats.getField(field)].name();
            case "Uncommon Item","Rare Item" -> Items.getItemNameByNo(stats.getField(field));
            case "Type 1", "Type 2" -> PkType.values()[stats.getField(field)].name();
            case "Egg Group 1", "Egg Group 2" -> PkEggGroup.values()[stats.getField(field)-1].name();
            case "Exp Rate" -> ExpRate.values()[stats.getField(field)].name();
            case "Dex Colour" -> DexColor.values()[stats.getField(field)].name();
            default -> "" + stats.getField(field);
        };
    }

    protected void lazyInitNodes() {
        if (!children.isEmpty()) {
            return;
        }
        for (int i = 0; i < StatsNFSFile.fields.length;i++) {
            children.add(new StatsTextNode(path + "/" + StatsNFSFile.fields[i], this, getContent(i)));
        }
    }
}
