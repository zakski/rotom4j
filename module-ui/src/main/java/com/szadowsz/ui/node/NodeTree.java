package com.szadowsz.ui.node;

import com.szadowsz.ui.node.impl.DropdownMenuNode;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.node.impl.ToolbarNode;

import java.util.*;

import static processing.core.PApplet.println;

public class NodeTree {
    private static FolderNode root;
    private static final Map<String, AbstractNode> nodesByPath = new HashMap<>();
    static final ArrayList<String> knownUnexpectedQueries = new ArrayList<>();

    private NodeTree() {

    }

    public static FolderNode getOrCreateRoot(boolean createToolbar) {
        if (root == null) {
            if (createToolbar) {
                root = new ToolbarNode("", null);
            } else {
                root = new FolderNode("", null);
            }
        }
        return root;
    }

    public static FolderNode getRoot() {
        if (root == null) {
            root = getOrCreateRoot(false);
        }
        return root;
    }

    public static FolderNode findParentFolderLazyInitPath(String nodePath) {
        String folderPath = NodePaths.getPathWithoutName(nodePath);
        lazyInitFolderPath(folderPath);
        AbstractNode pathParent = findNode(folderPath);
        return (FolderNode) pathParent;
    }

    public static AbstractNode findNode(String path) {
        if (nodesByPath.containsKey(path)) {
            return nodesByPath.get(path);
        }
        Queue<AbstractNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            AbstractNode node = queue.poll();
            if (node.path.equals(path)) {
                // do type cast collision detection here
                nodesByPath.put(path, node);
                return node;
            }
            if (node.type == NodeType.FOLDER) {
                FolderNode folder = (FolderNode) node;
                for (AbstractNode child : folder.children) {
                    queue.offer(child);
                }
            }
        }
        return null;
    }

    public static void lazyInitFolderPath(String path) {
        String[] split = NodePaths.splitByUnescapedSlashes(path);
        String runningPath = split[0];
        FolderNode parentFolder = null;
        for (int i = 0; i < split.length; i++) {
            AbstractNode n = findNode(runningPath);
            if (n == null) {
                if (parentFolder == null) {
                    parentFolder = root;
                }
                n = new FolderNode(runningPath, parentFolder);
                parentFolder.children.add(n);
                parentFolder = (FolderNode) n;
            } else if (n.type == NodeType.FOLDER) {
                parentFolder = (FolderNode) n;
            } else {
                println("Expected to find or to be able to create a folder at path \"" + runningPath + "\" but found an existing " + n.className + ". You cannot put any control elements there.");
            }
            if (i < split.length - 1) {
                runningPath += "/" + split[i + 1];
            }
        }
    }

    public static void lazyInitDropdownPath(String path) {
        String[] split = NodePaths.splitByUnescapedSlashes(path);
        String runningPath = split[0];
        FolderNode parentFolder = null;
        for (int i = 0; i < split.length; i++) {
            AbstractNode n = findNode(runningPath);
            if (n == null) {
                if (parentFolder == null) {
                    parentFolder = root;
                }
                n = new DropdownMenuNode(path, parentFolder);
                parentFolder.children.add(n);
                parentFolder = (FolderNode) n;
            } else if (n.type == NodeType.FOLDER) {
                parentFolder = (FolderNode) n;
            } else {
                println("Expected to find or to be able to create a folder at path \"" + runningPath + "\" but found an existing " + n.className + ". You cannot put any control elements there.");
            }
            if (i < split.length - 1) {
                runningPath += "/" + split[i + 1];
            }
        }
    }

    public static void insertNodeAtItsPath(AbstractNode node) {
        if (findNode(node.path) != null) {
            return;
        }
        String folderPath = NodePaths.getPathWithoutName(node.path);
        lazyInitFolderPath(folderPath);
        FolderNode folder = (FolderNode) findNode(folderPath);
        assert folder != null;
        folder.insertChild(node);
    }

    public static List<AbstractNode> getAllNodesAsList() {
        List<AbstractNode> result = new ArrayList<>();
        Queue<AbstractNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            AbstractNode node = queue.poll();
            result.add(node);
            if (node.type == NodeType.FOLDER) {
                FolderNode folder = (FolderNode) node;
                for (AbstractNode child : folder.children) {
                    queue.offer(child);
                }
            }
        }
        return result;
    }

    public static <T extends AbstractNode> List<T> getAllNodesAsList(Class<T> clazz) {
        List<T> result = new ArrayList<>();
        Queue<AbstractNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            AbstractNode node = queue.poll();
            try {
                result.add(clazz.cast(node));
            } catch (ClassCastException t) {

            }
            if (node.type == NodeType.FOLDER) {
                FolderNode folder = (FolderNode) node;
                for (AbstractNode child : folder.children) {
                    queue.offer(child);
                }
            }
        }
        return result;
    }

    public static void setAllNodesMouseOverToFalse() {
        setAllOtherNodesMouseOverToFalse(null);
    }

    public static void setAllOtherNodesMouseOverToFalse(AbstractNode nodeToKeep) {
        List<AbstractNode> allNodes = getAllNodesAsList();
        for (AbstractNode node : allNodes) {
            if (node == nodeToKeep) {
                continue;
            }
            node.isMouseOverNode = false;
        }
    }

    public static void setAllChildNodesMouseOverToFalse(FolderNode parentNode) {
        List<AbstractNode> childNodes = parentNode.children;
        for (AbstractNode node : childNodes) {
            node.isMouseOverNode = false;
            if (node instanceof FolderNode){
                setAllChildNodesMouseOverToFalse((FolderNode) node);
            }
        }
    }

    public static FolderNode findFirstOpenParentNodeRecursively(FolderNode node) {
        if (node == getRoot()) {
            return null;
        }
        if (node.isParentWindowOpen() && node.isVisible()) {
            return node;
        }
        return findFirstOpenParentNodeRecursively(node.parent);
    }

    public static <T extends AbstractNode> boolean isPathTakenByUnexpectedType(String path, Class<T> expectedType) {
        AbstractNode foundNode = findNode(path);
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

    public static void hideAtFullPath(String path) {
        AbstractNode node = findNode(path);
        if (node == null || node.equals(NodeTree.getRoot())) {
            return;
        }
        node.hideInlineNode();
    }

    public static void showAtFullPath(String path) {
        AbstractNode node = findNode(path);
        if (node == null || node.equals(NodeTree.getRoot())) {
            return;
        }
        node.showInlineNode();
    }

    public static boolean areAllParentsInlineVisible(AbstractNode node) {
        if (!node.isVisible()) {
            return false;
        }
        if (node.equals(getRoot())) {
            return true;
        }
        return areAllParentsInlineVisible(node.parent);
    }
}

