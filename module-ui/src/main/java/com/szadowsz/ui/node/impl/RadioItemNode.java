package com.szadowsz.ui.node.impl;

import com.szadowsz.ui.input.mouse.GuiMouseEvent;
import com.szadowsz.ui.node.NodeType;

class RadioItemNode extends ToggleNode {

    final String valueString;

    RadioItemNode(String path, FolderNode folder, boolean valueBoolean, String valueString) {
        super(NodeType.TRANSIENT,path, folder, valueBoolean);
        this.valueString = valueString;
    }

    @Override
    public void mouseReleasedOverNodeEvent(GuiMouseEvent e){
        if(armed && !valueBoolean){ // can only toggle manually to true, toggle to false happens automatically
            valueBoolean = true;
            onValueChangingActionEnded();
        }
        armed = false;
    }
}
