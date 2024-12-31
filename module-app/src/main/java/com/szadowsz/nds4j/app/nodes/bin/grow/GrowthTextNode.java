package com.szadowsz.nds4j.app.nodes.bin.grow;

import com.google.gson.JsonElement;
import com.old.ui.node.impl.FolderNode;
import com.old.ui.node.impl.TextNode;


public class GrowthTextNode extends TextNode {

    public GrowthTextNode(String path, FolderNode folder, String content, int col) {
        super(path, folder, content, col);
    }

    @Override
    public void overwriteState(JsonElement loadedNode) {
    }
}
