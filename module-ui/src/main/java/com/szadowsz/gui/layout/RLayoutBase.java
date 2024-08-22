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

    /**
     * Given a size constraint, update the location and size of each component in the component list by laying them out
     * in the available area.
     * <p>
     * This method will call {@code setPosition(..)} and {@code setSize(..)} on the Components.
     *
     * @param area Size available to this layout manager to lay out the components on
     * @param components List of components to lay out
     */
    public abstract void setLayout(PVector area, List<RComponent> components); // TODO Lanterna

}