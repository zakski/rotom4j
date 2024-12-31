package com.old.krab.lazy.nodes.impl;



import com.old.krab.lazy.input.keys.LazyKeyEvent;
import com.old.krab.lazy.nodes.AbstractNode;
import com.old.krab.lazy.nodes.NodeType;
import processing.core.PGraphics;


class ColorPickerHexNode extends AbstractNode {

    final ColorPickerFolderNode parentColorPickerFolder;

    ColorPickerHexNode(String path, ColorPickerFolderNode parentFolder) {
        super(NodeType.VALUE, path, parentFolder);
        this.parentColorPickerFolder = parentFolder;
    }

    @Override
    protected void drawNodeBackground(PGraphics pg) {

    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        fillForegroundBasedOnMouseOver(pg);
        drawLeftText(pg, name);
        drawRightTextToNotOverflowLeftText(pg, parentColorPickerFolder.hexString, name, false);
    }

    @Override
    public void keyPressedOverNode(LazyKeyEvent e, float x, float y) {
        parentColorPickerFolder.keyPressedOverNode(e, x, y);
    }
}
