package com.szadowsz.gui.component;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.theme.RTheme;
import com.szadowsz.gui.config.theme.RThemeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PVector;

/**
 * RComponent provides default behaviour for all components in RotomGui.
 * <p>
 * Every GUI element extends from this class in some way.
 */
public class RComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RComponent.class);

    // Reference to the RotomGui instance that owns this component
    protected final RotomGui gui;

    // Link to the parent folder (if null then it is a root component) // TODO root handling
    protected final RGroup parent; // TODO LazyGui & G4P

    protected final String path;  // TODO LazyGui
    protected final String name; // TODO LazyGui

    protected int localTheme = RThemeStore.getGlobalSchemeNum(); // TODO G4P
    protected RTheme palette = null;

    // Top left position of component in pixels (absolute)
    protected final PVector pos = new PVector(); // TODO LazyGui & G4P
    // Top left position of component in pixels (relative)
    protected final PVector relPos = new PVector();
    // Width and height of component in pixels
    protected final PVector size = new PVector(); // TODO LazyGui & G4P

    protected float heightInCells = 1; // TODO LazyGui // Shortcut for Layout Calculations


    protected boolean isDraggable = true; // TODO LazyGui

    protected boolean isDragged = false; // TODO LazyGui & G4P // Set to true when mouse is dragging, set to false on mouse released
    protected boolean isMouseOver = false; // TODO LazyGui // TODO Difference between mouse over and has focus
    // Is the component visible? (Not Parent Aware)
    protected boolean isVisible = true; // TODO LazyGui & G4P

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui the gui for the window that the component is drawn under
     * @param path the path in the component tree
     * @param parent the parent component reference
     */
    protected RComponent(RotomGui gui, String path, RGroup parent) {
        this.gui = gui;
        this.parent = parent;

        this.path = path;
        this.name = extractNameFromPath(path);

        this.palette = RThemeStore.getTheme(localTheme);

        // If overridden in Subclasses, both size.y and heightInCells should be changed
        size.y = heightInCells*RLayoutStore.getCell();
    }

    private String extractNameFromPath(String path) {
        if ("".equals(path)) { // this is the root component
            return gui.getSketch().getClass().getSimpleName(); // not using lowercase separated class name after all because it breaks what users expect to see
        }
        String[] split = RPaths.splitByUnescapesSlashesWithoutRemovingThem(path);
        if (split.length == 0) {
            return "";
        }
        String nameWithoutPrefixSlash = RPaths.getNameWithoutPrefixSlash(split[split.length - 1]);
        return RPaths.getDisplayStringWithoutEscapes(nameWithoutPrefixSlash);
    }

    private int calcHeightInCells(float minimumHeight){
        return ((int)(minimumHeight / RLayoutStore.getCell())) + ((minimumHeight % RLayoutStore.getCell() != 0) ? 1 : 0);
    }

    public void updateValues() {
    }

    /**
     * Get Component Name for ID
     *
     * @return Component name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the parent component. If null then this is a top-level component
     *
     * @return return parent, null if top level
     */
    public RGroup getParent() { // TODO LazyGui & G4P
        return parent;
    }

    /**
     * Get the parent component. If null then this is a top-level component
     *
     * @return return parent, null if top level
     */
    public RFolder getParentFolder() { // TODO LazyGui & G4P
        RGroup p = parent;
        while(p != null && !(p instanceof RFolder)){
            p = p.getParent();
        }
        return (RFolder) p;
    }

    /**
     * Method to check if this component is covered by the mouse
     *
     * @return true if covered, false otherwise
     */
    public boolean isMouseOver() {
        return isMouseOver;
    }

    /**
     * Method to check if this component is visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Method to check if this window of this component is visible, and if all parent nodes are visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isVisibleParentAware() { // TODO LazyGui
        boolean visible = isVisible();
        if (parent != null) {
            return visible && parent.isVisibleParentAware();
        }
        return visible;
    }

    /**
     * Method to check if the parent window of this component is visible
     *
     * @return true if visible, false otherwise
     */
    public boolean isParentWindowVisible(){ // TODO LazyGui TODO Needed?
        RFolder folder = getParentFolder();
        if(folder == null || folder.getWindow() == null){
            return !RLayoutStore.isGuiHidden();
        }
        return folder.isWindowVisible();
    }

    public void setHeight(float height){
        heightInCells = calcHeightInCells(height);
        size.y = heightInCells*RLayoutStore.getCell();
    }
}
