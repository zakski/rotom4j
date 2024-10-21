package com.szadowsz.gui.component;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RRoot;

import java.util.ArrayList;
import java.util.List;

public class RComponentTree {
    
    public RComponentTree(RotomGui rotomGui) {
    }

    public List<RComponent> getComponents() {
        return new ArrayList<>();
    }

    public RRoot getRoot() {
        return new RRoot();
    }

    public void setAllOtherNodesMouseOverToFalse(RComponent component) {
    }
}
