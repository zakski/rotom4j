package com.szadowsz.gui.layout;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.component.group.RGroup;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.List;

/**
 *  Component that can be used as layout meta-data for an arbitrary component grouping.
 */
public abstract class RLayoutBase {

    /**
     * Calculate the size characteristics based on the layout
     *
     * @return width and height in a PVector
     */
    public abstract PVector calcPreferredSize(List<RComponent> components);


    public abstract RLayoutConfig getLayoutConfig();
}
