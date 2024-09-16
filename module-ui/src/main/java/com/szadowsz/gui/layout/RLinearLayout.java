package com.szadowsz.gui.layout;

import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.config.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.window.internal.RSizeMode;
import com.szadowsz.gui.window.internal.RWindowInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Layout that puts all components on a single horizontally or vertical line.
 */
public class RLinearLayout extends RLayoutBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(RLinearLayout.class);

    /**
     * This enum type will decide the alignment of a component on the counter-axis, meaning the horizontal alignment on
     * vertical {@code RLinearLayout}s and vertical alignment on horizontal {@code RLinearLayout}s.
     */
    public enum Alignment { // TODO Lanterna
        /**
         * The component will be placed to the left (for vertical layouts) or top (for horizontal layouts)
         */
        BEGINNING,
        /**
         * The component will be placed horizontally centered (for vertical layouts) or vertically centered (for
         * horizontal layouts)
         */
        CENTER,
        /**
         * The component will be placed to the right (for vertical layouts) or bottom (for horizontal layouts)
         */
        END,
        /**
         * The component will be forced to take up all the horizontal space (for vertical layouts) or vertical space
         * (for horizontal layouts)
         */
        FILL,
    }

    /**
     * This enum type will what to do with a component if the container has extra space to offer. This can happen if the
     * window runs in full screen or the window has been programmatically set to a fixed size, above the preferred size
     * of the window.
     */
    public enum GrowPolicy { // TODO Lanterna
        /**
         * This is the default grow policy, the component will not become larger than the preferred size, even if the
         * container can offer more.
         */
        NONE,
        /**
         * With this grow policy, if the container has more space available then this component will be grown to fill
         * the extra space.
         */
        CAN_GROW,
    }

    private static class LinearLayoutData implements RLayoutConfig { // TODO Lanterna
        private final Alignment alignment;
        private final GrowPolicy growPolicy;

        public LinearLayoutData(Alignment alignment, GrowPolicy growPolicy) {
            this.alignment = alignment;
            this.growPolicy = growPolicy;
        }
    }

    /**
     * Creates a {@code LayoutData} for {@code RLinearLayout} that assigns a component to a particular alignment on its
     * counter-axis, meaning the horizontal alignment on vertical {@code RLinearLayout}s and vertical alignment on
     * horizontal {@code RLinearLayout}s.
     * @param alignment Alignment to store in the {@code LayoutData} object
     * @return {@code LayoutData} object created for {@code RLinearLayout}s with the specified alignment
     * @see Alignment
     */
    public static RLayoutConfig createLayoutData(Alignment alignment) { // TODO Lanterna
        return createLayoutData(alignment, GrowPolicy.NONE);
    }

    /**
     * Creates a {@code LayoutData} for {@code RLinearLayout} that assigns a component to a particular alignment on its
     * counter-axis, meaning the horizontal alignment on vertical {@code RLinearLayout}s and vertical alignment on
     * horizontal {@code RLinearLayout}s.
     * @param alignment Alignment to store in the {@code LayoutData} object
     * @param growPolicy When policy to apply to the component if the parent container has more space available along
     *                   the main axis.
     * @return {@code LayoutData} object created for {@code RLinearLayout}s with the specified alignment
     * @see Alignment
     */
    public static RLayoutConfig createLayoutData(Alignment alignment, GrowPolicy growPolicy) { // TODO Lanterna
        return new LinearLayoutData(alignment, growPolicy);
    }

    protected RGroup group;

    private final RDirection direction;
    private int spacing;
    private boolean changed;

    /**
     * Default Constructor, creates a vertical {@code RLinearLayout}
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     */
    public RLinearLayout() {
        this(RDirection.VERTICAL);
    }

    /**
     * Default Constructor, creates a vertical {@code RLinearLayout}
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     */
    public RLinearLayout(RGroup group) {
        this(RDirection.VERTICAL);
        this.group = group;
    }

    /**
     * Constructor, creates a {@code RLinearLayout} with a specified direction to position the components on

     * @param direction Direction for this {@code Direction}
     */
    public RLinearLayout(RDirection direction) { // TODO Lanterna
        this.direction = direction;
        this.spacing = direction == RDirection.HORIZONTAL ? 1 : 0;
        this.changed = true;
    }

    protected PVector calcPreferredSizeHorizontally(List<RComponent> components) { // TODO Lanterna
        float maxHeight = 0;
        float width = 0;
        for(RComponent component: components) {
            PVector preferredSize = component.getPreferredSize();
            if(maxHeight < preferredSize.y) {
                maxHeight = preferredSize.y;
            }
            width += preferredSize.x;
        }
        width += spacing * (components.size() - 1);
        return new PVector(Math.max(0,width), maxHeight);
    }

    protected PVector calcPreferredSizeVertically(String title, List<RComponent> components) { // TODO Lanterna
        float maxWidth = RFontStore.calcMainTextWidth(title, RLayoutStore.getCell()) + RLayoutStore.getCell();
        float height = 0;
        for(RComponent child: components) {
            PVector preferredSize = child.getPreferredSize();
            maxWidth = PApplet.max(maxWidth,preferredSize.x);
            height += preferredSize.y;
        }
        height += spacing * (components.size() - 1);
        return new PVector(maxWidth, Math.max(0, height));
    }

    @Override
    public PVector calcPreferredSize(String title, List<RComponent> components) { // TODO Lanterna
        // Filter out invisible components
        components = components.stream().filter(RComponent::isVisible).collect(Collectors.toList());

        if(direction == RDirection.VERTICAL) {
            return calcPreferredSizeVertically(title,components);
        }
        else {
            return calcPreferredSizeHorizontally(components);
        }
    }

    @Override
    public RGroup getGroup() {
        return group;
    }

    @Override
    public RLayoutConfig getLayoutConfig() {
        return createLayoutData(Alignment.BEGINNING,GrowPolicy.NONE);
    }

    /**
     * Sets the amount of empty space to put in between components. For horizontal layouts, this is number of columns
     * (by default 1) and for vertical layouts this is number of rows (by default 0).
     * @param spacing Spacing between components, either in number of columns or rows depending on the direction
     * @return Itself
     */
    public RLinearLayout setSpacing(int spacing) { // TODO Lanterna
        this.spacing = spacing;
        this.changed = true;
        return this;
    }

    /**
     * Returns the amount of empty space to put in between components. For horizontal layouts, this is number of columns
     * (by default 1) and for vertical layouts this is number of rows (by default 0).
     * @return Spacing between components, either in number of columns or rows depending on the direction
     */
    public int getSpacing() { // TODO Lanterna
        return spacing;
    }

    public RDirection getDirection() {
        return direction;
    }

//    @Override
//    public boolean hasChanged() {
//        return changed;
//    }

    @Override
    public void setGroup(RGroup group) {
        this.group = group;
    }

    @Override
    public void setCompLayout(PVector start, PVector area, List<RComponent> components) { // TODO Lanterna
        // Filter out invisible components
        components = components.stream().filter(RComponent::isVisible).collect(Collectors.toList());

        if(direction == RDirection.VERTICAL) { // TODO Lanterna
            doFlexibleVerticalLayout(start,area, components);
        } else {
            doFlexibleHorizontalLayout(start,area, components);
        }
        this.changed = false;
    }

    @Override
    public void setWinLayout(PVector area, List<RWindowInt> windows) { // TODO Lanterna
        // Filter out invisible windows
        windows = windows.stream().filter(RWindowInt::isVisible).collect(Collectors.toList());

        if(direction == RDirection.VERTICAL) { // TODO Lanterna
            doFlexibleWinVerticalLayout(area, windows);
        } else {
            doFlexibleWinHorizontalLayout(area, windows);
        }
        this.changed = false;
    }

//    private float suggestWidthForVerticalLayout(String title, List<RComponent> components, float availableHorizontalSpace) {
//        float titleTextWidth = RFontStore.calcMainTextWidth(title, RLayoutStore.getCell());
//        float minimumSpaceTotal = titleTextWidth + RLayoutStore.getCell();
//        float spaceForName = RLayoutStore.getCell() * 2;
//        float spaceForValue = RLayoutStore.getCell() * 2;
//        spaceForName = PApplet.max(spaceForName, titleTextWidth);
//        for (RComponent child : components) {
//            PVector preferredSize = child.getPreferredSize();
//            float nameTextWidth = child.calcNameTextWidth();
//            spaceForName = PApplet.max(spaceForName, nameTextWidth);
//            float valueTextWidth = child.calcValueWidth();
//            spaceForValue = PApplet.max(spaceForValue, valueTextWidth);
//        }
//        return PApplet.constrain(spaceForName + spaceForValue, minimumSpaceTotal, availableHorizontalSpace);
//    }

    private void doFlexibleVerticalLayout(PVector start, PVector area, List<RComponent> components) { // TODO Lanterna
        float availableVerticalSpace = area.y;
        float availableHorizontalSpace = area.x;
        LOGGER.trace("{} Available Space [{},{}]",group.getName(),availableHorizontalSpace,availableVerticalSpace);

        final Map<RComponent, PVector> fittingMap = new IdentityHashMap<>();

        float totalRequiredVerticalSpace = 0;

        //float expectedWidth = suggestWidthForVerticalLayout(group.getName(), components, availableHorizontalSpace);
        //LOGGER.debug("{} Expected Width {}",group.getName(),expectedWidth);

        for (RComponent component: components) {
            Alignment alignment = Alignment.BEGINNING;
            if (component instanceof RGroup) {
                RLayoutConfig layoutData = component.getCompLayoutConfig();
                if (layoutData instanceof LinearLayoutData) {
                    alignment = ((LinearLayoutData) layoutData).alignment;
                }
            }

            PVector preferredSize = component.getPreferredSize();
            LOGGER.trace("{} Component Preferred Size [{},{}]",component.getName(),preferredSize.x,preferredSize.y);
            PVector fittingSize = new PVector(
                    Math.min(availableHorizontalSpace, preferredSize.x),
                    preferredSize.y);
            LOGGER.trace("{} Component Fitting Size [{},{}]",component.getName(),fittingSize.x,fittingSize.y);
            if(alignment == Alignment.FILL) {
                fittingSize.x = (availableHorizontalSpace);
            }

            fittingMap.put(component, fittingSize);
            totalRequiredVerticalSpace += fittingSize.y + spacing;
        }
        if (!components.isEmpty()) {
            // Remove the last spacing
            totalRequiredVerticalSpace -= spacing;
        }

        // If we can't fit everything, trim the down the size of the largest components until it fits
        if (availableVerticalSpace < totalRequiredVerticalSpace) {
            List<RComponent> copyOfComponents = new ArrayList<>(components);
            Collections.reverse(copyOfComponents);
            copyOfComponents.sort((o1, o2) -> {
                // Reverse sort
                return -Float.compare(fittingMap.get(o1).y, fittingMap.get(o2).y);
            });

            while (availableVerticalSpace < totalRequiredVerticalSpace) {
                float largestSize = fittingMap.get(copyOfComponents.get(0)).y;
                for (RComponent largeComponent: copyOfComponents) {
                    PVector currentSize = fittingMap.get(largeComponent);
                    if (largestSize > currentSize.y) {
                        break;
                    }
                    fittingMap.put(largeComponent, currentSize.add(0,-1));
                    totalRequiredVerticalSpace--;
                    if (availableVerticalSpace >= totalRequiredVerticalSpace) {
                        break;
                    }
                }
            }
        }

        // If we have more space available than we need, grow components to fill
        if (availableVerticalSpace > totalRequiredVerticalSpace) {
            boolean resizedOneComponent = false;
            while (availableVerticalSpace > totalRequiredVerticalSpace) {
                for(RComponent component: components) {
                    LinearLayoutData layoutData = null;
                    if (!(component instanceof RFolder) && component instanceof RGroup group) {
                        RLayoutConfig config = group.getCompLayoutConfig();
                        layoutData = ((LinearLayoutData) config);
                    }
                    final PVector currentSize = fittingMap.get(component);
                    if (layoutData != null && layoutData.growPolicy == GrowPolicy.CAN_GROW) {
                        fittingMap.put(component, currentSize.add(0,1));
                        availableVerticalSpace--;
                        resizedOneComponent = true;
                    }
                    if (availableVerticalSpace <= totalRequiredVerticalSpace) {
                        break;
                    }
                }
                if (!resizedOneComponent) {
                    break;
                }
            }
        }

        // Assign the sizes and positions
        float topPosition = 0;
        for(RComponent component: components) {
            Alignment alignment = Alignment.BEGINNING;
            if (component instanceof RGroup) {
                RLayoutConfig layoutData = ((RGroup) component).getCompLayoutConfig();
                if (layoutData instanceof LinearLayoutData) {
                    alignment = ((LinearLayoutData) layoutData).alignment;
                }
            }

            PVector decidedSize = fittingMap.get(component);
            PVector position = component.getPosition();
            position.y = topPosition;
            switch(alignment) {
                case END:
                    position.x = (availableHorizontalSpace - decidedSize.x);
                    break;
                case CENTER:
                    position.x = ((availableHorizontalSpace - decidedSize.x) / 2);
                    break;
                case BEGINNING:
                default:
                    position.x = 0;
                    break;
            }
            component.updateCoordinates(start, position, decidedSize);
            topPosition += decidedSize.y + spacing;
        }
    }

    private void doFlexibleWinVerticalLayout(PVector area, List<RWindowInt> windows) { // TODO Lanterna
        float availableVerticalSpace = area.y;
        float availableHorizontalSpace = area.x;
        final Map<RWindowInt, PVector> fittingMap = new IdentityHashMap<>();
        float totalRequiredVerticalSpace = 0;

        for (RWindowInt window: windows) {
            Alignment alignment = Alignment.BEGINNING;
            RLayoutConfig layoutData = window.getLayoutConfig();
            if (layoutData instanceof LinearLayoutData) {
                alignment = ((LinearLayoutData) layoutData).alignment;
            }

            PVector preferredSize = window.getSize();
            PVector fittingSize = new PVector(
                    Math.min(availableHorizontalSpace, preferredSize.x),
                    preferredSize.y);
            if(alignment == Alignment.FILL) {
                fittingSize.x = (availableHorizontalSpace);
            }

            fittingMap.put(window, fittingSize);
            totalRequiredVerticalSpace += fittingSize.y + spacing;
        }
        if (!windows.isEmpty()) {
            // Remove the last spacing
            totalRequiredVerticalSpace -= spacing;
        }

        // If we can't fit everything, trim the down the size of the largest windows until it fits
        if (availableVerticalSpace < totalRequiredVerticalSpace) {
            List<RWindowInt> copyOfWindows = new ArrayList<>(windows);
            Collections.reverse(copyOfWindows);
            copyOfWindows.sort((o1, o2) -> {
                // Reverse sort
                return -Float.compare(fittingMap.get(o1).y, fittingMap.get(o2).y);
            });

            while (availableVerticalSpace < totalRequiredVerticalSpace) {
                float largestSize = fittingMap.get(copyOfWindows.get(0)).y;
                for (RWindowInt largeWindow: copyOfWindows) {
                    PVector currentSize = fittingMap.get(largeWindow);
                    if (largestSize > currentSize.y) {
                        break;
                    }
                    fittingMap.put(largeWindow, currentSize.add(0,-1));
                    totalRequiredVerticalSpace--;
                    if (availableHorizontalSpace >= totalRequiredVerticalSpace) {
                        break;
                    }
                }
            }
        }

        // If we have more space available than we need, grow windows to fill
        if (availableVerticalSpace > totalRequiredVerticalSpace) {
            boolean resizedOneWindow = false;
            while (availableVerticalSpace > totalRequiredVerticalSpace) {
                for(RWindowInt window: windows) {
                    LinearLayoutData layoutData = (LinearLayoutData) window.getLayoutConfig();
                    final PVector currentSize = fittingMap.get(window);
                    if (layoutData != null && layoutData.growPolicy == GrowPolicy.CAN_GROW) {
                        fittingMap.put(window, currentSize.add(0,1));
                        availableVerticalSpace--;
                        resizedOneWindow = true;
                    }
                    if (availableVerticalSpace <= totalRequiredVerticalSpace) {
                        break;
                    }
                }
                if (!resizedOneWindow) {
                    break;
                }
            }
        }

        // Assign the sizes and positions
        float topPosition = 0;
        for(RWindowInt window: windows) {
            Alignment alignment = Alignment.BEGINNING;
            RLayoutConfig layoutData = window.getLayoutConfig();
            if (layoutData instanceof LinearLayoutData) {
                alignment = ((LinearLayoutData) layoutData).alignment;
            }

            PVector decidedSize = fittingMap.get(window);
            PVector position = window.getPos();
            position.y = topPosition;
            switch(alignment) {
                case END:
                    position.x = (availableHorizontalSpace - decidedSize.x);
                    break;
                case CENTER:
                    position.x = ((availableHorizontalSpace - decidedSize.x) / 2);
                    break;
                case BEGINNING:
                default:
                    position.x = 0;
                    break;
            }
            window.setBounds(position.x,position.y,decidedSize.x,decidedSize.y, RSizeMode.LAYOUT);
            topPosition += decidedSize.y + spacing;
        }
    }

    private void doFlexibleHorizontalLayout(PVector start, PVector area, List<RComponent> components) { // TODO Lanterna
        float availableVerticalSpace = area.y;
        float availableHorizontalSpace = area.x;
        final Map<RComponent, PVector> fittingMap = new IdentityHashMap<>();
        int totalRequiredHorizontalSpace = 0;

        for (RComponent component: components) {
            Alignment alignment = Alignment.BEGINNING;
            if (component instanceof RGroup) {
                RLayoutConfig layoutData = component.getCompLayoutConfig();
                if (layoutData instanceof LinearLayoutData) {
                    alignment = ((LinearLayoutData) layoutData).alignment;
                }
            }

            PVector preferredSize = component.getPreferredSize();
            PVector fittingSize = new PVector(
                    preferredSize.x,
                    Math.min(availableVerticalSpace, preferredSize.y));
            if(alignment == Alignment.FILL) {
                fittingSize.y = (availableVerticalSpace);
            }

            fittingMap.put(component, fittingSize);
            totalRequiredHorizontalSpace += fittingSize.x + spacing;
        }
        if (!components.isEmpty()) {
            // Remove the last spacing
            totalRequiredHorizontalSpace -= spacing;
        }

        // If we can't fit everything, trim the down the size of the largest components until it fits
        if (availableHorizontalSpace < totalRequiredHorizontalSpace) {
            List<RComponent> copyOfComponents = new ArrayList<>(components);
            Collections.reverse(copyOfComponents);
            copyOfComponents.sort((o1, o2) -> {
                // Reverse sort
                return -Float.compare(fittingMap.get(o1).x, fittingMap.get(o2).x);
            });

            while (availableHorizontalSpace < totalRequiredHorizontalSpace) {
                float largestSize = fittingMap.get(copyOfComponents.get(0)).x;
                for (RComponent largeComponent: copyOfComponents) {
                    PVector currentSize = fittingMap.get(largeComponent);
                    if (largestSize > currentSize.x) {
                        break;
                    }
                    fittingMap.put(largeComponent, currentSize.add(-1,0));
                    totalRequiredHorizontalSpace--;
                    if (availableHorizontalSpace >= totalRequiredHorizontalSpace) {
                        break;
                    }
                }
            }
        }

        // If we have more space available than we need, grow components to fill
        if (availableHorizontalSpace > totalRequiredHorizontalSpace) {
            boolean resizedOneComponent = false;
            while (availableHorizontalSpace > totalRequiredHorizontalSpace) {
                for(RComponent component: components) {
                    LinearLayoutData layoutData = null;
                    if (component instanceof RGroup) {
                        layoutData = (LinearLayoutData) ((RGroup) component).getCompLayoutConfig();
                    }
                    final PVector currentSize = fittingMap.get(component);
                    if (layoutData != null && layoutData.growPolicy == GrowPolicy.CAN_GROW) {
                        fittingMap.put(component, currentSize.add(1,0));
                        availableHorizontalSpace--;
                        resizedOneComponent = true;
                    }
                    if (availableHorizontalSpace <= totalRequiredHorizontalSpace) {
                        break;
                    }
                }
                if (!resizedOneComponent) {
                    break;
                }
            }
        }

        // Assign the sizes and positions
        float leftPosition = 0;
        for(RComponent component: components) {
            Alignment alignment = Alignment.BEGINNING;
            if (component instanceof RGroup) {
                RLayoutConfig layoutData = ((RGroup) component).getCompLayoutConfig();
                if (layoutData instanceof LinearLayoutData) {
                    alignment = ((LinearLayoutData) layoutData).alignment;
                }
            }

            PVector decidedSize = fittingMap.get(component);
            PVector position = component.getPosition();
            position.x = (leftPosition);
            switch(alignment) {
                case END:
                    position.y = (availableVerticalSpace - decidedSize.y);
                    break;
                case CENTER:
                    position.y = ((availableVerticalSpace - decidedSize.y) / 2);
                    break;
                case BEGINNING:
                default:
                    position.y = 0;
                    break;
            }
            component.updateCoordinates(start, position, decidedSize);
            leftPosition += decidedSize.x + spacing;
        }
    }

    private void doFlexibleWinHorizontalLayout(PVector area, List<RWindowInt> windows) { // TODO Lanterna
        float availableVerticalSpace = area.y;
        float availableHorizontalSpace = area.x;
        final Map<RWindowInt, PVector> fittingMap = new IdentityHashMap<>();
        int totalRequiredHorizontalSpace = 0;

        for (RWindowInt window: windows) {
            Alignment alignment = Alignment.BEGINNING;
            RLayoutConfig layoutData = window.getLayoutConfig();
            if (layoutData instanceof LinearLayoutData) {
                alignment = ((LinearLayoutData) layoutData).alignment;
            }

            PVector preferredSize = window.getSize();
            PVector fittingSize = new PVector(
                    preferredSize.x,
                    Math.min(availableVerticalSpace, preferredSize.y));
            if(alignment == Alignment.FILL) {
                fittingSize.y = (availableVerticalSpace);
            }

            fittingMap.put(window, fittingSize);
            totalRequiredHorizontalSpace += fittingSize.x + spacing;
        }
        if (!windows.isEmpty()) {
            // Remove the last spacing
            totalRequiredHorizontalSpace -= spacing;
        }

        // If we can't fit everything, trim the down the size of the largest windows until it fits
        if (availableHorizontalSpace < totalRequiredHorizontalSpace) {
            List<RWindowInt> copyOfWindows = new ArrayList<>(windows);
            Collections.reverse(copyOfWindows);
            copyOfWindows.sort((o1, o2) -> {
                // Reverse sort
                return -Float.compare(fittingMap.get(o1).x, fittingMap.get(o2).x);
            });

            while (availableHorizontalSpace < totalRequiredHorizontalSpace) {
                float largestSize = fittingMap.get(copyOfWindows.get(0)).x;
                for (RWindowInt largeWindow: copyOfWindows) {
                    PVector currentSize = fittingMap.get(largeWindow);
                    if (largestSize > currentSize.x) {
                        break;
                    }
                    fittingMap.put(largeWindow, currentSize.add(-1,0));
                    totalRequiredHorizontalSpace--;
                    if (availableHorizontalSpace >= totalRequiredHorizontalSpace) {
                        break;
                    }
                }
            }
        }

        // If we have more space available than we need, grow windows to fill
        if (availableHorizontalSpace > totalRequiredHorizontalSpace) {
            boolean resizedOneWindow = false;
            while (availableHorizontalSpace > totalRequiredHorizontalSpace) {
                for(RWindowInt window: windows) {
                    LinearLayoutData layoutData =  (LinearLayoutData) window.getLayoutConfig();
                    final PVector currentSize = fittingMap.get(window);
                    if (layoutData != null && layoutData.growPolicy == GrowPolicy.CAN_GROW) {
                        fittingMap.put(window, currentSize.add(1,0));
                        availableHorizontalSpace--;
                        resizedOneWindow = true;
                    }
                    if (availableHorizontalSpace <= totalRequiredHorizontalSpace) {
                        break;
                    }
                }
                if (!resizedOneWindow) {
                    break;
                }
            }
        }

        // Assign the sizes and positions
        float leftPosition = 0;
        for(RWindowInt window: windows) {
            Alignment alignment = Alignment.BEGINNING;
            RLayoutConfig layoutData = window.getLayoutConfig();
            if (layoutData instanceof LinearLayoutData) {
                alignment = ((LinearLayoutData) layoutData).alignment;
            }

            PVector decidedSize = fittingMap.get(window);
            PVector position = window.getPos();
            position.x = (leftPosition);
            switch(alignment) {
                case END:
                    position.y = (availableVerticalSpace - decidedSize.y);
                    break;
                case CENTER:
                    position.y = ((availableVerticalSpace - decidedSize.y) / 2);
                    break;
                case BEGINNING:
                default:
                    position.y = 0;
                    break;
            }
            window.setBounds(position.x,position.y,decidedSize.x,decidedSize.y, RSizeMode.LAYOUT);
            leftPosition += decidedSize.x + spacing;
        }
    }

    @Override
    public String toString() {
        return "Linear{" +
                "direction=" + direction +
                ", spacing=" + spacing +
                ", changed=" + changed +
                '}';
    }
}
