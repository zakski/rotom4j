package com.szadowsz.nds4j.app.nodes.bin.stats;

import com.google.gson.JsonElement;
import com.old.ui.node.impl.FolderNode;
import com.old.ui.node.impl.TextNode;


public class StatsTextNode extends TextNode {

    public StatsTextNode(String path, FolderNode folder, String content) {
        super(path, folder, content);
    }

    @Override
    public void overwriteState(JsonElement loadedNode) {
    }
}
