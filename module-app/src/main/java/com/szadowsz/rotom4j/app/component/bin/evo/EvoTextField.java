package com.szadowsz.rotom4j.app.component.bin.evo;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.component.text.RTextField;


public class EvoTextField extends RTextField {

    public EvoTextField(RotomGui gui, String path, RFolder folder, String content) {
        super(gui, path, folder, Integer.MAX_VALUE, 0);
        setText(content);
    }
}
