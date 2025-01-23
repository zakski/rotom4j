package com.szadowsz.rotom4j.component;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.bined.RBinEditor;
import com.szadowsz.gui.component.bined.RBinMain;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.rotom4j.file.RotomFile;

public class R4JEditor extends RBinEditor {

    protected R4JEditor(RotomGui gui, String path, RGroup parent, RotomFile data, boolean isCompressed) {
        super(gui, path, parent);

        contentData = (isCompressed)? data.getCompressedData() : data;

        initBounds();
        initComponents();
    }
}
