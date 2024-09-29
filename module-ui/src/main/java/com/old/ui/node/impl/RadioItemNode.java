package com.old.ui.node.impl;

import com.old.ui.input.mouse.GuiMouseEvent;
import com.old.ui.node.NodeType;

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
