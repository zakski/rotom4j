package com.szadowsz.gui.component;

import com.szadowsz.gui.component.group.folder.RDropdownMenu;
import com.szadowsz.gui.component.group.folder.RPanel;
import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RRoot;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.component.group.folder.RToolbar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Utility Class to manage the tree hierarchy of the components
 */
public class RComponentTree {
    private static final Logger LOGGER = LoggerFactory.getLogger(RComponentTree.class);

    // GUI manager reference
    private final RotomGui gui;

    private final Map<String, RComponent> nodesByPath = new HashMap<>();
    private final ArrayList<String> knownUnexpectedQueries = new ArrayList<>();

    // Top Level Node for the Sketch Window
    private RRoot root;

    /**
     * Default Constructor
     *
     * @param rotomGui gui to refer back to
     */
    public RComponentTree(RotomGui rotomGui) {
        this.gui = rotomGui;
    }

    /**
     * Constructs a flat list of all components
     *
     * @return an Arraylist of components
     */
    public List<RComponent> getComponents() {
        List<RComponent> result = new ArrayList<>();
        Queue<RComponent> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            RComponent node = queue.poll();
            result.add(node);
            if (node instanceof RGroup group) {
                for (RComponent child : group.getChildren()) {
                    queue.offer(child);
                }
            }
        }
        return result;
    }

    /**
     * Finds Component By Path
     *
     * @param path the path to search for
     * @return the component found, null otherwise
     */
    public RComponent getComponent(String path) {
        if (nodesByPath.containsKey(path)) {
            return nodesByPath.get(path);
        }
        Queue<RComponent> queue = new LinkedList<>();
        queue.offer(getRoot());
        while (!queue.isEmpty()) {
            RComponent node = queue.poll();
            if (node.path.equals(path)) {
                // do type cast collision detection here
                nodesByPath.put(path, node);
                return node;
            }
            if (node instanceof RFolder folder) {
                for (RComponent child : folder.getChildren()) {
                    queue.offer(child);
                }
            }
            if (node instanceof RGroup group) {
                for (RComponent child : group.getChildren()) {
                    queue.offer(child);
                }
            }
        }
        return null;

    }

    /**
     * Finds Parent Folder Component By Path
     *
     * @param childPath the path to look for the parent of
     * @return the parent folder if found, null or a class cast exception otherwise
     */
    public RFolder getParentFolder(String childPath) {
        String folderPath = RPaths.getParentPath(childPath);
        // TODO needs to handle RGroups to properly find the folder
        initFolderForPath(folderPath);
        RComponent pathParent = getComponent(folderPath);
        return (RFolder) pathParent;
    }

    /**
     * Get or Create the Root component for the Window
     *
     * @return the root component
     */
    public RRoot getRoot() {
        if (root == null) {
            root = new RRoot(gui);
        }
        return root;
    }

    /**
     * Check if the path provided has a component of the expected class registered
     *
     * @param path the path to investigate
     * @param expectedType Class instance of the expected type
     * @return true if the expected class does not match the actual class, false otherwise
     * @param <T> the expected Class
     */
    public <T extends RComponent> boolean isPathTakenByUnexpectedType(String path, Class<T> expectedType) {
        RComponent found = getComponent(path);
        if (found == null) {
            return false;
        }
        String expectedTypeName = expectedType.getSimpleName();
        String uniquePathAndTypeQuery = path + " - " + expectedTypeName;
        if (knownUnexpectedQueries.contains(uniquePathAndTypeQuery)) {
            // return early when this is a known conflict, don't spam the path conflict error in console
            return true;
        }
        try {
            expectedType.cast(found);
        } catch (Exception ex) {
            LOGGER.warn("Path conflict warning: You tried to register a new " + expectedTypeName + " at \"" + path + "\"" +
                    " but that path is already in use by a " + found.getClassName() + "." +
                    "\n\tThe original " + found.getClassName() + " will still work as expected," +
                    " but the new " + expectedTypeName + " will not be shown and it will always return a default value." +
                    "\n\tNDSGui paths must be unique, so please use a different path for one of them."
            );
            knownUnexpectedQueries.add(uniquePathAndTypeQuery);
            return true;
        }
        return false;
    }

    /**
     * Method to set every other component's mouse over field to false
     *
     * @param mouseOver the component to leave alone
     */
    public void setAllOtherMouseOversToFalse(RComponent mouseOver) {
        List<RComponent> components = getComponents();
        for (RComponent component : components) {
            if (component == mouseOver) {
                continue;
            }
            component.setMouseOver(false);
        }
    }

    /**
     * Method to create, if necessary, an RFolder at a given path
     *
     * @param path path to initialise at
     */
    public void initFolderForPath(String path) {
        String[] split = RPaths.splitByUnescapedSlashes(path);
        String runningPath = split[0];
        RGroup parent = null;
        for (int i = 0; i < split.length; i++) {
            RComponent n = getComponent(runningPath);
            if (n == null) {
                if (parent == null) {
                    parent = root;
                }
                n = new RFolder(gui, runningPath, parent);
                parent.getChildren().add(n);
                parent = (RGroup) n;
            } else if (n instanceof RFolder) {
                parent = (RFolder) n;
            } else {
                LOGGER.warn("Expected to find or to be able to create a folder at path \"{}\" but found an existing {}. You cannot put any control elements there.", runningPath, n.className);
                // TODO consider Exception to throw here
            }
            if (i < split.length - 1) {
                runningPath += "/" + split[i + 1];
            }
        }
    }

    /**
     * Method to create, if necessary, an RDropdownMenu at a given path
     *
     * @param path path to initialise at
     */
    public void initDropdowForPath(String path) {
        String[] split = RPaths.splitByUnescapedSlashes(path);
        String runningPath = split[0];
        RGroup parent = null;
        for (int i = 0; i < split.length; i++) {
            RComponent n = getComponent(runningPath);
            if (n == null) {
                if (parent == null) {
                    parent = root;
                }
                n = new RDropdownMenu(gui, path, parent);
                parent.insertChild(n);
                parent = (RGroup) n;
            } else if (n instanceof RFolder) {
                parent = (RFolder) n;
            } else {
                LOGGER.warn("Expected to find or to be able to create a dropdown at path \"{}\" but found an existing {}. You cannot put any control elements there.", runningPath, n.className);
            }
            if (i < split.length - 1) {
                runningPath += "/" + split[i + 1];
            }
        }
    }

    /**
     * Method to create, if necessary, an RPanel at a given path
     *
     * @param path path to initialise at
     */
    public void initPanelForPath(String path) {
        String[] split = RPaths.splitByUnescapedSlashes(path);
        String runningPath = split[0];
        RGroup parent = null;
        for (int i = 0; i < split.length; i++) {
            RComponent n = getComponent(runningPath);
            if (n == null) {
                if (parent == null) {
                    parent = root;
                }
                n = new RPanel(gui,runningPath, parent);
                parent.getChildren().add(n);
                parent = (RGroup) n;
            } else if (n instanceof RPanel) {
                parent = (RFolder) n;
            } else {
                LOGGER.warn("Expected to find or to be able to create a pane at path \"{}\" but found an existing {}. You cannot put any control elements there.", runningPath, n.className);
            }
            if (i < split.length - 1) {
                runningPath += "/" + split[i + 1];
            }
        }
    }

    /**
     * Method to create, if necessary, an RToolbar at a given path
     *
     * @param path path to initialise at
     */
    public void initToolbarForPath(String path) {
        String[] split = RPaths.splitByUnescapedSlashes(path);
        String runningPath = split[0];
        RGroup parent = null;
        for (int i = 0; i < split.length; i++) {
            RComponent n = getComponent(runningPath);
            if (n == null) {
                if (parent == null) {
                    parent = root;
                }
                n = new RToolbar(gui,runningPath, parent);
                parent.getChildren().add(n);
                parent = (RGroup) n;
            } else if (n instanceof RFolder) {
                parent = (RFolder) n;
            } else {
                LOGGER.warn("Expected to find or to be able to create a toolbar at path \"{}\" but found an existing {}. You cannot put any control elements there.", runningPath, n.className);
            }
            if (i < split.length - 1) {
                runningPath += "/" + split[i + 1];
            }
        }
    }

    /**
     * Method to attempt inserting a new component at its set path
     *
     * @param component the component to insert
     */
    public void insertAtPath(RComponent component) {
        if (getComponent(component.path) != null) {
            return;
        }
        String folderPath = RPaths.getParentPath(component.path);
        initFolderForPath(folderPath);
        RFolder folder = (RFolder) getComponent(folderPath);
        assert folder != null;
        folder.insertChild(component);
        root.resizeForContents();
    }
}
