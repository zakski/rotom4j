package com.szadowsz.rotom4j.app.component.bin.stats;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.rotom4j.file.data.stats.StatsNFSFile;
import com.szadowsz.rotom4j.ref.ItemDex;
import com.szadowsz.rotom4j.file.data.stats.data.PkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsFolderComponent extends RFolder {

    protected Logger LOGGER = LoggerFactory.getLogger(StatsFolderComponent.class);

    private StatsNFSFile stats;

    public StatsFolderComponent(RotomGui gui, String path, RFolder parent, StatsNFSFile stats) {
        super(gui, path, parent);
        this.stats = stats;
        lazyInitNodes();
    }

    protected String getContent(int i) {
        String field = StatsNFSFile.fields[i];
        return switch (field){
            case "Ability 1", "Ability 2" -> com.szadowsz.rotom4j.file.data.stats.data.PkAbility.values()[stats.getField(field)].name();
            case "Uncommon Item","Rare Item" -> ItemDex.getItemNameByNo(stats.getField(field));
            case "Type 1", "Type 2" -> PkType.values()[stats.getField(field)].name();
            case "Egg Group 1", "Egg Group 2" -> com.szadowsz.rotom4j.file.data.stats.data.PkEggGroup.values()[stats.getField(field)-1].name();
            case "Exp Rate" -> com.szadowsz.rotom4j.file.data.stats.data.ExpRate.values()[stats.getField(field)].name();
            case "Dex Colour" -> com.szadowsz.rotom4j.file.data.stats.data.DexColor.values()[stats.getField(field)].name();
            default -> "" + stats.getField(field);
        };
    }

    protected void lazyInitNodes() {
        if (!children.isEmpty()) {
            return;
        }
        for (int i = 0; i < StatsNFSFile.fields.length;i++) {
            children.add(new StatsTextField(gui,path + "/" + StatsNFSFile.fields[i], this, getContent(i)));
        }
    }
}
