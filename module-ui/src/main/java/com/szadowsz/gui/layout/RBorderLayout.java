/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.szadowsz.gui.layout;

import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.window.pane.RSizeMode;
import com.szadowsz.gui.window.pane.RWindowPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PVector;

import java.util.*;

/**
 * BorderLayout imitates the BorderLayout class from AWT, allowing you to add a center component with optional 
 * components around it in top, bottom, left and right locations. The edge components will be sized at their preferred
 * size and the center component will take up whatever remains.
 * @author martin
 */
public class RBorderLayout extends RLayoutBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(RBorderLayout.class);

    /**
     * This type is what you use as the layout data for components added to a panel using {@code BorderLayout} for its
     * layout manager. This values specified where inside the panel the component should be added.
     */
    public enum RLocation implements RLayoutConfig {
        /**
         * The component with this value as its layout data will occupy the center space, whatever is remaining after
         * the other components (if any) have allocated their space.
         */
        CENTER,
        /**
         * The component with this value as its layout data will occupy the left side of the container, attempting to
         * allocate the preferred width of the component and at least the preferred height, but could be more depending
         * on the other components added.
         */
        LEFT,
        /**
         * The component with this value as its layout data will occupy the right side of the container, attempting to
         * allocate the preferred width of the component and at least the preferred height, but could be more depending
         * on the other components added.
         */
        RIGHT,
        /**
         * The component with this value as its layout data will occupy the top side of the container, attempting to
         * allocate the preferred height of the component and at least the preferred width, but could be more depending
         * on the other components added.
         */
        TOP,
        /**
         * The component with this value as its layout data will occupy the bottom side of the container, attempting to
         * allocate the preferred height of the component and at least the preferred width, but could be more depending
         * on the other components added.
         */
        BOTTOM,
        ;
    }

    //When components don't have a location, we'll assign an available location based on this order
    private static final List<RLocation> AUTO_ASSIGN_ORDER = Collections.unmodifiableList(Arrays.asList(
            RLocation.CENTER,
            RLocation.TOP,
            RLocation.BOTTOM,
            RLocation.LEFT,
            RLocation.RIGHT));

    private RGroup group;

    private int topSpacing = 0;
    private int bottomSpacing = 0;
    private int leftSpacing = 0;
    private int rightSpacing = 0;

    /**
     * Create Component Location Lookup map
     *
     * @param components list of components
     * @return components organised by location
     */
    private EnumMap<RLocation, RComponent> makeCompLookupMap(List<RComponent> components) {
        EnumMap<RLocation, RComponent> map = new EnumMap<>(RLocation.class);
        List<RComponent> unassignedComponents = new ArrayList<>();
        for(RComponent component: components) {
            if (!component.isVisible()) {
                continue;
            }
            if(component.getCompLayoutConfig() instanceof RLocation) {
                map.put((RLocation)component.getCompLayoutConfig(), component);
            }
            else {
                unassignedComponents.add(component);
            }
        }
        //Try to assign components to available locations
        for(RComponent component: unassignedComponents) {
            for(RLocation location: AUTO_ASSIGN_ORDER) {
                if(!map.containsKey(location)) {
                    map.put(location, component);
                    break;
                }
            }
        }
        return map;
    }

    /**
     * Create Window Location Lookup map
     *
     * @param windows list of windows
     * @return windows organised by location
     */
    private EnumMap<RLocation, RWindowPane> makeWinLookupMap(List<RWindowPane> windows) {
        EnumMap<RLocation, RWindowPane> map = new EnumMap<>(RLocation.class);
        List<RWindowPane> unassignedWindows = new ArrayList<>();
        for(RWindowPane window: windows) {
            if (!window.isVisible()) {
                continue;
            }
            if(window.getFolder().getWinLayoutConfig() instanceof RLocation) {
                map.put((RLocation)window.getFolder().getWinLayoutConfig(), window);
            }
            else {
                unassignedWindows.add(window);
            }
        }
        //Try to assign windows to available locations
        for(RWindowPane window: unassignedWindows) {
            for(RLocation location: AUTO_ASSIGN_ORDER) {
                if(!map.containsKey(location)) {
                    map.put(location, window);
                    break;
                }
            }
        }
        return map;
    }

    @Override
    public PVector calcPreferredSize(String title, List<RComponent> components) {
        EnumMap<RLocation, RComponent> layout = makeCompLookupMap(components);
        float preferredHeight =
                (layout.containsKey(RLocation.TOP) ? layout.get(RLocation.TOP).getPreferredSize().y : 0)
                +
                Math.max(
                    layout.containsKey(RLocation.LEFT) ? layout.get(RLocation.LEFT).getPreferredSize().y : 0,
                    Math.max(
                        layout.containsKey(RLocation.CENTER) ? layout.get(RLocation.CENTER).getPreferredSize().y : 0,
                        layout.containsKey(RLocation.RIGHT) ? layout.get(RLocation.RIGHT).getPreferredSize().y : 0))
                +
                (layout.containsKey(RLocation.BOTTOM) ? layout.get(RLocation.BOTTOM).getPreferredSize().y : 0);

        float preferredWidth =
                Math.max(
                    (layout.containsKey(RLocation.LEFT) ? layout.get(RLocation.LEFT).getPreferredSize().x : 0) +
                        (layout.containsKey(RLocation.CENTER) ? layout.get(RLocation.CENTER).getPreferredSize().x : 0) +
                        (layout.containsKey(RLocation.RIGHT) ? layout.get(RLocation.RIGHT).getPreferredSize().x : 0),
                    Math.max(
                        layout.containsKey(RLocation.TOP) ? layout.get(RLocation.TOP).getPreferredSize().x : 0,
                        layout.containsKey(RLocation.BOTTOM) ? layout.get(RLocation.BOTTOM).getPreferredSize().x : 0));
        return new PVector(preferredWidth, preferredHeight);
    }

    @Override
    public RGroup getGroup() {
        return group;
    }

    @Override
    public RLayoutConfig getLayoutConfig() {
        return new RLayoutConfig() {
        }; // No Special config
    }

    @Override
    public void setCompLayout(PVector windowStart, PVector start, PVector area, List<RComponent> components) {
        setCompLayout(start, area, components);
    }

    @Override
    public void setCompLayout(PVector start, PVector area, List<RComponent> components) {
        EnumMap<RLocation, RComponent> layout = makeCompLookupMap(components);
        float availableHorizontalSpace = area.x;
        float availableVerticalSpace = area.y;
        
        //We'll need this later on
        float topComponentHeight = 0;
        float leftComponentWidth = 0;

        //First allocate the top
        if(layout.containsKey(RLocation.TOP)) {
            RComponent topComponent = layout.get(RLocation.TOP);
            topComponentHeight = Math.min(topComponent.getPreferredSize().y, availableVerticalSpace);
            topComponent.updateCoordinates(start.x,start.y,0,0,availableHorizontalSpace, topComponentHeight);
            availableVerticalSpace -= topComponentHeight;
        }

        //Next allocate the bottom
        if(layout.containsKey(RLocation.BOTTOM)) {
            RComponent bottomComponent = layout.get(RLocation.BOTTOM);
            float bottomComponentHeight = Math.min(bottomComponent.getPreferredSize().y, availableVerticalSpace);
            bottomComponent.updateCoordinates(start.x,start.y,0, area.y - bottomComponentHeight,availableHorizontalSpace, bottomComponentHeight);
            availableVerticalSpace -= bottomComponentHeight;
        }

        //Now divide the remaining space between LEFT, CENTER and RIGHT
        if(layout.containsKey(RLocation.LEFT)) {
            RComponent leftComponent = layout.get(RLocation.LEFT);
            leftComponentWidth = Math.min(leftComponent.getPreferredSize().x, availableHorizontalSpace - leftSpacing);
            leftComponent.updateCoordinates(
                    start.x,start.y,
                    0, topComponentHeight+topSpacing,
                    leftComponentWidth, availableVerticalSpace-topSpacing-bottomSpacing);
            availableHorizontalSpace -= leftComponentWidth;
        }
        if(layout.containsKey(RLocation.RIGHT)) {
            RComponent rightComponent = layout.get(RLocation.RIGHT);
            float rightComponentWidth = Math.min(rightComponent.getPreferredSize().x, availableHorizontalSpace - rightSpacing);
            rightComponent.updateCoordinates(
                    start.x,start.y,
                    area.x - rightComponentWidth, topComponentHeight + topSpacing,
                    rightComponentWidth, availableVerticalSpace - topSpacing-bottomSpacing);
            availableHorizontalSpace -= rightComponentWidth;
        }
        if(layout.containsKey(RLocation.CENTER)) {
            RComponent centerComponent = layout.get(RLocation.CENTER);
            centerComponent.updateCoordinates(
                    start.x,start.y,
                    leftComponentWidth + leftSpacing, topComponentHeight + topSpacing,
                    availableHorizontalSpace - leftSpacing-rightSpacing, availableVerticalSpace - topSpacing-bottomSpacing);
        }
        
        //Set the remaining components to 0x0
        for(RComponent component: components) {
            if(component.isVisible() && !layout.containsValue(component)) {
                component.updateCoordinates(start.x,start.y,0,0,0,0);
            }
        }
    }

    @Override
    public void setWinLayout(PVector area, List<RWindowPane> windows) {
        LOGGER.debug("Setting Border layout For Windows");
        EnumMap<RLocation, RWindowPane> layout = makeWinLookupMap(windows);
        float availableHorizontalSpace = area.x;
        float availableVerticalSpace = area.y;

        //We'll need this later on
        float topWindowHeight = 0;
        float leftWindowWidth = 0;

        //First allocate the top
        if(layout.containsKey(RLocation.TOP)) {
            LOGGER.debug("Allocating Space for Top of Win Border layout");
            RWindowPane topWindow = layout.get(RLocation.TOP);
            topWindowHeight = Math.min(topWindow.getSize().y, availableVerticalSpace);
            topWindow.setBounds(0,0,availableHorizontalSpace, topWindowHeight, RSizeMode.LAYOUT);
            availableVerticalSpace -= topWindowHeight;
        }

        //Next allocate the bottom
        if(layout.containsKey(RLocation.BOTTOM)) {
            LOGGER.debug("Allocating Space for Bottom of Win Border layout");
            RWindowPane bottomWindow = layout.get(RLocation.BOTTOM);
            float bottomWindowHeight = Math.min(bottomWindow.getSize().y, availableVerticalSpace);
            bottomWindow.setBounds(0, area.y - bottomWindowHeight,availableHorizontalSpace, bottomWindowHeight, RSizeMode.LAYOUT);
            availableVerticalSpace -= bottomWindowHeight;
        }

        //Now divide the remaining space between LEFT, CENTER and RIGHT
        if(layout.containsKey(RLocation.LEFT)) {
            LOGGER.debug("Allocating Space for Left of Win Border layout");
            RWindowPane leftWindow = layout.get(RLocation.LEFT);
            leftWindowWidth = Math.min(leftWindow.getSize().x, availableHorizontalSpace - leftSpacing);
            leftWindow.setBounds(0, topWindowHeight +topSpacing,leftWindowWidth, availableVerticalSpace -topSpacing-bottomSpacing, RSizeMode.LAYOUT);
            availableHorizontalSpace -= leftWindowWidth;
        }
        if(layout.containsKey(RLocation.RIGHT)) {
            LOGGER.debug("Allocating Space for Right of Win Border layout");
            RWindowPane rightWindow = layout.get(RLocation.RIGHT);
            float rightWindowWidth = Math.min(rightWindow.getSize().x, availableHorizontalSpace - rightSpacing);
            rightWindow.setBounds(area.x - rightWindowWidth, topWindowHeight+topSpacing, rightWindowWidth, availableVerticalSpace-topSpacing-bottomSpacing, RSizeMode.LAYOUT);
            availableHorizontalSpace -= rightWindowWidth;
        }
        if(layout.containsKey(RLocation.CENTER)) {
            LOGGER.debug("Allocating Space for Center of Win Border layout");
            RWindowPane centerWindow = layout.get(RLocation.CENTER);
            centerWindow.setBounds(leftWindowWidth+leftSpacing, topWindowHeight+topSpacing,availableHorizontalSpace-leftSpacing-rightSpacing, availableVerticalSpace-topSpacing-bottomSpacing, RSizeMode.LAYOUT);
        }

        //Set the remaining components to 0x0
        for(RWindowPane component: windows) {
            if(component.isVisible() && !layout.containsValue(component)) {
                component.setBounds(0,0,0,0, RSizeMode.LAYOUT);
            }
        }
    }

    @Override
    public void setGroup(RGroup group) {
        this.group = group;
    }

    /**
     * Set the expected borader spacing
     *
     * @param top spacing between the top component and others
     * @param bottom spacing between the bottom component and others
     * @param left spacing between the left and center
     * @param right spacing between the right and center
     */
    public void setSpacing(int top, int bottom, int left, int right) {
        this.topSpacing = top;
        this.bottomSpacing = bottom;
        this.leftSpacing = left;
        this.rightSpacing = right;
    }

    @Override
    public String toString() {
        return "Border{}";
    }
}
