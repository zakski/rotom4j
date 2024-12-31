package com.szadowsz.gui.component;

import java.util.Arrays;

/**
 * Utility Class to deal with Tree Path Manipulation
 */
@SuppressWarnings("RegExpRedundantEscape")
public class RPaths {

    static final String REGEX_UNESCAPED_SLASH_LOOKBEHIND = "(?<!\\\\)\\/";
    static final String REGEX_UNESCAPED_SLASH_LOOKAROUND = "(?<!\\\\)(?=\\/)";

    private RPaths(){
        // NOOP
    }

    /**
     * Utility Method to remove all escaped slashes from the component name
     *
     * @param nameWithEscapes name with escaped slashes
     * @return display name without escaped slashes
     */
    protected static String getDisplayStringWithoutEscapes(String nameWithEscapes) {
        return nameWithEscapes.replaceAll("\\\\/", "/");
    }

    /**
     * Utility Method to remove all slashes from the component name
     *
     *  @param name name with slashes
     * @return name without slashes
     */
    protected static String getNameWithoutPrefixSlash(String name) {
        return name.replaceAll(REGEX_UNESCAPED_SLASH_LOOKBEHIND, "");
    }

    /**
     * Utility Method to split path up into segments, without removing the slashes
     *
     * @param path the source path
     * @return An Array of path segments including slashes
     */
    protected static String[] splitByUnescapesSlashesWithoutRemovingThem(String path) {
        return path.split(REGEX_UNESCAPED_SLASH_LOOKAROUND);
    }

    /**
     * Utility Method to split path up into segments, and remove the slashes
     *
     * @param path the source path
     * @return An Array of path segments excluding slashes
     */
    public static String[] splitByUnescapedSlashes(String path) {
        return path.split(REGEX_UNESCAPED_SLASH_LOOKBEHIND);
    }

    /**
     * TODO Review Necessity
     *
     * @param path the source path
     * @return An Array of path segments
     */
    public static String[] splitFullPathWithoutEndAndRoot(String path) { // TODO Review Necessity
        String[] pathWithEnd = splitByUnescapedSlashes(path);
        return Arrays.copyOf(pathWithEnd, pathWithEnd.length - 1);
    }


    /**
     * Extract the Name of the Component from the path
     *
     * @param path the source path
     * @return name of the component
     */
    public static String getNameFromPath(String path) {
        String[] split = splitByUnescapesSlashesWithoutRemovingThem(path);
        if (split.length == 0) {
            return "";
        }
        String nameWithoutPrefixSlash = getNameWithoutPrefixSlash(split[split.length - 1]);
        return getDisplayStringWithoutEscapes(nameWithoutPrefixSlash);
    }

    /**
     * Extract the Parent Path from the Child
     *
     * @param childPath path of the child
     * @return path of the parent
     */
    public static String getParentPath(String childPath) {
        String[] split = splitByUnescapesSlashesWithoutRemovingThem(childPath);
        StringBuilder sum = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            sum.append(split[i]);
        }
        return sum.toString();
    }
}

