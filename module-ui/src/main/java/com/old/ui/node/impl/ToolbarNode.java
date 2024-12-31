package com.old.ui.node.impl;

import com.old.ui.node.LayoutType;

public class ToolbarNode extends FolderNode {

    public ToolbarNode(String path, FolderNode parent) {
        super(path, parent, LayoutType.HORIZONAL);
    }

    @Override
    public boolean shouldDrawTitle() {
        return false;
    }
}
