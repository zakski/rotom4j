package com.krab.lazy.nodes.impl;

import com.krab.lazy.input.mouse.LazyMouseEvent;
import com.krab.lazy.nodes.NodeType;

class RadioItemNode extends ToggleNode {

    final String valueString;

    RadioItemNode(String path, FolderNode folder, boolean valueBoolean, String valueString) {
        super(path, folder, valueBoolean);
        this.type = NodeType.TRANSIENT;
        this.valueString = valueString;
    }

    @Override
    public void mouseReleasedOverNodeEvent(LazyMouseEvent e){
        if(armed && !valueBoolean){ // can only toggle manually to true, toggle to false happens automatically
            valueBoolean = true;
            onValueChangingActionEnded();
        }
        armed = false;
    }
}
