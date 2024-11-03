package com.szadowsz.gui.layout;

import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.window.pane.RWindowPane;
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

    /**
     * Get the Group that owns the layout
     *
     * @return the group the layout belongs to
     */
    public abstract RGroup getGroup(); // TODO Me


    /**
     * The current layout config info
     *
     * @return layout config
     */
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
    public abstract void setCompLayout(PVector start, PVector area, List<RComponent> components);

    /**
     * Given a size constraint, update the location and size of each component in the component list by laying them out
     * in the available area.
     * <p>
     * This method will call {@code setPosition(..)} and {@code setSize(..)} on the Components.
     *
     * @param area Size available to this layout manager to lay out the components on
     * @param windows List of windows to lay out
     */
    public abstract void setWinLayout(PVector area, List<RWindowPane> windows);

    /**
     * Set who owns the layout
     *
     * @param group the group to be owned by
     */
    public abstract void setGroup(RGroup group); // TODO Me

}