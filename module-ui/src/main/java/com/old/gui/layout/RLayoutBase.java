package com.old.gui.layout;

import com.old.gui.component.RComponent;
import com.old.gui.component.group.RGroup;
import com.old.gui.window.internal.RWindowInt;
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
    public abstract PVector calcPreferredSize(String title, List<RComponent> components);

    public abstract RGroup getGroup(); // TODO Me


    public abstract RLayoutConfig getLayoutConfig(); // TODO Lanterna

    /**
     * Given a size constraint, update the location and size of each component in the component list by laying them out
     * in the available area.
     * <p>
     * This method will call {@code setPosition(..)} and {@code setSize(..)} on the Components.
     *
     * @param area Size available to this layout manager to lay out the components on
     * @param components List of components to lay out
     */
    public abstract void setCompLayout(PVector start, PVector area, List<RComponent> components); // TODO Lanterna

    /**
     * Given a size constraint, update the location and size of each component in the component list by laying them out
     * in the available area.
     * <p>
     * This method will call {@code setPosition(..)} and {@code setSize(..)} on the Components.
     *
     * @param area Size available to this layout manager to lay out the components on
     * @param components List of components to lay out
     */
    public abstract void setWinLayout(PVector area, List<RWindowInt> windows); // TODO Lanterna

    public abstract void setGroup(RGroup group); // TODO Me

}