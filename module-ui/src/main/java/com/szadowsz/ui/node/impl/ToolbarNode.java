package com.szadowsz.ui.node.impl;

import com.szadowsz.ui.node.LayoutType;

public class ToolbarNode extends FolderNode {

    public ToolbarNode(String path, FolderNode parent) {
        super(path, parent, LayoutType.HORIZONAL);
    }

    @Override
    public boolean shouldDrawTitle() {
        return false;
    }
}
