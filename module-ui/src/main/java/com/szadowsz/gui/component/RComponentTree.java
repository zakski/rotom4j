package com.szadowsz.gui.component;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.folder.RDropdownMenu;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.component.folder.RToolbar;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RRoot;


import java.util.*;

import static processing.core.PApplet.println;

public class RComponentTree {
    private final RotomGui gui;

    private final Map<String, RComponent> nodesByPath = new HashMap<>();
    private final ArrayList<String> knownUnexpectedQueries = new ArrayList<>();

    private RRoot root;

    public RComponentTree(RotomGui gui) {
        this.gui = gui;
    }

    public RRoot getRoot() {
        if (root == null) {
            root = new RRoot(gui);
        }
        return root;
    }

    public RFolder findParentFolderLazyInitPath(String nodePath) {
        String folderPath = RPaths.getPathWithoutName(nodePath);
        initFolderForPath(folderPath);
        RComponent pathParent = find(folderPath);
        return (RFolder) pathParent;
    }

    public RComponent find(String path) {
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
    public void initDropdowForPath(String path) {
        String[] split = RPaths.splitByUnescapedSlashes(path);
        String runningPath = split[0];
        RGroup parent = null;
        for (int i = 0; i < split.length; i++) {
            RComponent n = find(runningPath);
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
                println("Expected to find or to be able to create a dropdown at path \"" + runningPath + "\" but found an existing " + n.className + ". You cannot put any control elements there.");
            }
            if (i < split.length - 1) {
                runningPath += "/" + split[i + 1];
            }
        }
    }

    public void initFolderForPath(String path) {
        String[] split = RPaths.splitByUnescapedSlashes(path);
        String runningPath = split[0];
        RGroup parent = null;
        for (int i = 0; i < split.length; i++) {
            RComponent n = find(runningPath);
            if (n == null) {
                if (parent == null) {
                    parent = root;
                }
                n = new RFolder(gui,runningPath, parent);
                parent.getChildren().add(n);
                parent = (RGroup) n;
            } else if (n instanceof RFolder) {
                parent = (RFolder) n;
            } else {
                println("Expected to find or to be able to create a folder at path \"" + runningPath + "\" but found an existing " + n.className + ". You cannot put any control elements there.");
            }
            if (i < split.length - 1) {
                runningPath += "/" + split[i + 1];
            }
        }
    }

    public void initToolbarForPath(String path) {
        String[] split = RPaths.splitByUnescapedSlashes(path);
        String runningPath = split[0];
        RGroup parent = null;
        for (int i = 0; i < split.length; i++) {
            RComponent n = find(runningPath);
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
                println("Expected to find or to be able to create a toolbar at path \"" + runningPath + "\" but found an existing " + n.className + ". You cannot put any control elements there.");
            }
            if (i < split.length - 1) {
                runningPath += "/" + split[i + 1];
            }
        }
    }

    public void insertNodeAtItsPath(RComponent node) {
        if (find(node.path) != null) {
            return;
        }
        String folderPath = RPaths.getPathWithoutName(node.path);
        initFolderForPath(folderPath);
        RFolder folder = (RFolder) find(folderPath);
        assert folder != null;
        folder.insertChild(node);
    }

    public List<RComponent> getAllNodesAsList() {
        List<RComponent> result = new ArrayList<>();
        Queue<RComponent> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            RComponent node = queue.poll();
            result.add(node);
            if (node instanceof RFolder folder) {
                for (RComponent child : folder.getChildren()) {
                    queue.offer(child);
                }
            }
        }
        return result;
    }

    public <T extends RComponent> List<T> getAllNodesAsList(Class<T> clazz) {
        List<T> result = new ArrayList<>();
        Queue<RComponent> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            RComponent node = queue.poll();
            try {
                result.add(clazz.cast(node));
            } catch (ClassCastException t) {

            }
            if (node instanceof RFolder folder) {
                for (RComponent child : folder.getChildren()) {
                    queue.offer(child);
                }
            }
        }
        return result;
    }



    public void setAllOtherNodesMouseOverToFalse(RComponent nodeToKeep) {
        List<RComponent> allNodes = getAllNodesAsList();
        for (RComponent node : allNodes) {
            if (node == nodeToKeep) {
                continue;
            }
            node.setMouseOver(false);
        }
    }

    public void setAllMouseOverToFalse(RFolder parentNode) {
        List<RComponent> childNodes = parentNode.getChildren();
        for (RComponent node : childNodes) {
            node.setMouseOver(false);
            if (node instanceof RFolder){
                setAllMouseOverToFalse((RFolder) node);
            }
        }
    }

    public void setAllMouseOverToFalse() {
        setAllOtherNodesMouseOverToFalse(null);
    }

    public RFolder findFirstOpenParentNodeRecursively(RGroup node) {
        if (node == getRoot()) {
            return null;
        }
        if (node instanceof RFolder && node.isParentWindowOpen() && node.isVisible()) {
            return (RFolder) node;
        }
        return findFirstOpenParentNodeRecursively(node.parent);
    }

    public <T extends RComponent> boolean isPathTakenByUnexpectedType(String path, Class<T> expectedType) {
        RComponent foundNode = find(path);
        if (foundNode == null) {
            return false;
        }
        String expectedTypeName = expectedType.getSimpleName();
        String uniquePathAndTypeQuery = path + " - " + expectedTypeName;
        if (knownUnexpectedQueries.contains(uniquePathAndTypeQuery)) {
            // return early when this is a known conflict, don't spam the path conflict error in console
            return true;
        }
        try {
            expectedType.cast(foundNode);
        } catch (Exception ex) {
            println("Path conflict warning: You tried to register a new " + expectedTypeName + " at \"" + path + "\"" +
                    " but that path is already in use by a " + foundNode.className + "." +
                    "\n\tThe original " + foundNode.className + " will still work as expected," +
                    " but the new " + expectedTypeName + " will not be shown and it will always return a default value." +
                    "\n\tNDSGui paths must be unique, so please use a different path for one of them."
            );
            knownUnexpectedQueries.add(uniquePathAndTypeQuery);
            return true;
        }
        return false;
    }

    public void hideAtFullPath(String path) {
        RComponent node = find(path);
        if (node == null || node.equals(getRoot())) {
            return;
        }
        node.hide();
    }

    public void showAtFullPath(String path) {
        RComponent node = find(path);
        if (node == null || node.equals(getRoot())) {
            return;
        }
        node.show();
    }

    public boolean areAllParentsInlineVisible(RComponent node) {
        if (!node.isVisible()) {
            return false;
        }
        if (node.equals(getRoot())) {
            return true;
        }
        return areAllParentsInlineVisible(node.parent);
    }
}

