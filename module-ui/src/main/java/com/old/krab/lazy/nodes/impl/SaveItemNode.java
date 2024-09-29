package com.old.krab.lazy.nodes.impl;

import com.old.krab.lazy.nodes.AbstractNode;
import com.old.krab.lazy.nodes.NodeType;
import com.old.krab.lazy.stores.JsonSaveStore;
import com.old.krab.lazy.windows.WindowManager;
import processing.core.PGraphics;

class SaveItemNode extends AbstractNode {
    final String fileName;
    final SaveFolderNode saveFolderParent;

    SaveItemNode(String path, SaveFolderNode parent, String fileName) {
        super(NodeType.TRANSIENT, path, parent);
        saveFolderParent = parent;
        this.fileName = fileName;
    }

    protected void drawNodeBackground(PGraphics pg) {
        fillForegroundBasedOnMouseOver(pg);
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        drawLeftText(pg, name);
        drawRightTextToNotOverflowLeftText(pg, "load", name,true);
    }

    public void mousePressedOverNode(float x, float y) {
        JsonSaveStore.loadStateFromFilePath(fileName);
        WindowManager.setFocus(saveFolderParent.window);
        onValueChangingActionEnded();
    }
}
