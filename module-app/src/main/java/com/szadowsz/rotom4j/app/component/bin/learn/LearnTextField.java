package com.szadowsz.rotom4j.app.component.bin.learn;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.component.text.RTextField;


public class LearnTextField extends RTextField {

    public LearnTextField(RotomGui gui, String path, RFolder folder, String content) {
        super(gui, path, folder, Integer.MAX_VALUE,0);
        setText(content);
        shouldDisplayHeaderRow = false;
    }
}
