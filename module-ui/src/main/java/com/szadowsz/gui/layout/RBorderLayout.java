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
import com.szadowsz.gui.window.internal.RWindowInt;
import processing.core.PVector;

import java.util.*;

/**
 * BorderLayout imitates the BorderLayout class from AWT, allowing you to add a center component with optional 
 * components around it in top, bottom, left and right locations. The edge components will be sized at their preferred
 * size and the center component will take up whatever remains.
 * @author martin
 */
public class RBorderLayout extends RLayoutBase {

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

    @Override
    public PVector calcPreferredSize(List<RComponent> components) {
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
    public RLayoutConfig getLayoutConfig() {
        return new RLayoutConfig() {
        };
    }

    @Override
    public void setCompLayout(PVector area, List<RComponent> components) {
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
            topComponent.updateCoordinates(0,0,availableHorizontalSpace, topComponentHeight);
            availableVerticalSpace -= topComponentHeight;
        }

        //Next allocate the bottom
        if(layout.containsKey(RLocation.BOTTOM)) {
            RComponent bottomComponent = layout.get(RLocation.BOTTOM);
            float bottomComponentHeight = Math.min(bottomComponent.getPreferredSize().y, availableVerticalSpace);
            bottomComponent.updateCoordinates(0, area.y - bottomComponentHeight,availableHorizontalSpace, bottomComponentHeight);
            availableVerticalSpace -= bottomComponentHeight;
        }

        //Now divide the remaining space between LEFT, CENTER and RIGHT
        if(layout.containsKey(RLocation.LEFT)) {
            RComponent leftComponent = layout.get(RLocation.LEFT);
            leftComponentWidth = Math.min(leftComponent.getPreferredSize().x, availableHorizontalSpace);
            leftComponent.updateCoordinates(0, topComponentHeight,leftComponentWidth, availableVerticalSpace);
            availableHorizontalSpace -= leftComponentWidth;
        }
        if(layout.containsKey(RLocation.RIGHT)) {
            RComponent rightComponent = layout.get(RLocation.RIGHT);
            float rightComponentWidth = Math.min(rightComponent.getPreferredSize().x, availableHorizontalSpace);
            rightComponent.updateCoordinates(area.x - rightComponentWidth, topComponentHeight, rightComponentWidth, availableVerticalSpace);
            availableHorizontalSpace -= rightComponentWidth;
        }
        if(layout.containsKey(RLocation.CENTER)) {
            RComponent centerComponent = layout.get(RLocation.CENTER);
            centerComponent.updateCoordinates(leftComponentWidth, topComponentHeight,availableHorizontalSpace, availableVerticalSpace);
        }
        
        //Set the remaining components to 0x0
        for(RComponent component: components) {
            if(component.isVisible() && !layout.containsValue(component)) {
                component.updateCoordinates(0,0,0,0);
            }
        }
    }

    @Override
    public void setWinLayout(PVector area, List<RWindowInt> windows) {
        EnumMap<RLocation, RWindowInt> layout = makeWinLookupMap(windows);
        float availableHorizontalSpace = area.x;
        float availableVerticalSpace = area.y;

        //We'll need this later on
        float topWindowHeight = 0;
        float leftWindowWidth = 0;

        //First allocate the top
        if(layout.containsKey(RLocation.TOP)) {
            RWindowInt topWindow = layout.get(RLocation.TOP);
            topWindowHeight = Math.min(topWindow.getSize().y, availableVerticalSpace);
            topWindow.setBounds(0,0,availableHorizontalSpace, topWindowHeight);
            availableVerticalSpace -= topWindowHeight;
        }

        //Next allocate the bottom
        if(layout.containsKey(RLocation.BOTTOM)) {
            RWindowInt bottomWindow = layout.get(RLocation.BOTTOM);
            float bottomWindowHeight = Math.min(bottomWindow.getSize().y, availableVerticalSpace);
            bottomWindow.setBounds(0, area.y - bottomWindowHeight,availableHorizontalSpace, bottomWindowHeight);
            availableVerticalSpace -= bottomWindowHeight;
        }

        //Now divide the remaining space between LEFT, CENTER and RIGHT
        if(layout.containsKey(RLocation.LEFT)) {
            RWindowInt leftWindow = layout.get(RLocation.LEFT);
            leftWindowWidth = Math.min(leftWindow.getSize().x, availableHorizontalSpace);
            leftWindow.setBounds(0, topWindowHeight,leftWindowWidth, availableVerticalSpace);
            availableHorizontalSpace -= leftWindowWidth;
        }
        if(layout.containsKey(RLocation.RIGHT)) {
            RWindowInt rightWindow = layout.get(RLocation.RIGHT);
            float rightWindowWidth = Math.min(rightWindow.getSize().x, availableHorizontalSpace);
            rightWindow.setBounds(area.x - rightWindowWidth, topWindowHeight, rightWindowWidth, availableVerticalSpace);
            availableHorizontalSpace -= rightWindowWidth;
        }
        if(layout.containsKey(RLocation.CENTER)) {
            RWindowInt centerWindow = layout.get(RLocation.CENTER);
            centerWindow.setBounds(leftWindowWidth, topWindowHeight,availableHorizontalSpace, availableVerticalSpace);
        }

        //Set the remaining components to 0x0
        for(RWindowInt component: windows) {
            if(component.isVisible() && !layout.containsValue(component)) {
                component.setBounds(0,0,0,0);
            }
        }
    }

    private EnumMap<RLocation, RComponent> makeCompLookupMap(List<RComponent> components) {
        EnumMap<RLocation, RComponent> map = new EnumMap<>(RLocation.class);
        List<RComponent> unassignedComponents = new ArrayList<>();
        for(RComponent component: components) {
            if (!component.isVisible()) {
                continue;
            }
            if(component.getLayoutConfig() instanceof RLocation) {
                map.put((RLocation)component.getLayoutConfig(), component);
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

    private EnumMap<RLocation, RWindowInt> makeWinLookupMap(List<RWindowInt> windows) {
        EnumMap<RLocation, RWindowInt> map = new EnumMap<>(RLocation.class);
        List<RWindowInt> unassignedWindows = new ArrayList<>();
        for(RWindowInt window: windows) {
            if (!window.isVisible()) {
                continue;
            }
            if(window.getFolder().getLayoutConfig() instanceof RLocation) {
                map.put((RLocation)window.getFolder().getLayoutConfig(), window);
            }
            else {
                unassignedWindows.add(window);
            }
        }
        //Try to assign windows to available locations
        for(RWindowInt window: unassignedWindows) {
            for(RLocation location: AUTO_ASSIGN_ORDER) {
                if(!map.containsKey(location)) {
                    map.put(location, window);
                    break;
                }
            }
        }
        return map;
    }
}
