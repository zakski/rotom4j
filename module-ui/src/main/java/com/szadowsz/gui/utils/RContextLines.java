package com.szadowsz.gui.utils;

import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.RComponentTree;
import com.szadowsz.gui.component.group.folder.RFolder;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.List;

/**
 * Utility Clas TO Draw Context lines
 */
public class RContextLines {
    public static final String NEVER = "never";
    public static final String ON_HOVER = "on hover";
    public static final String ALWAYS = "always";
    public static final int SHOW_CONTEXT_LINES_MODE_NEVER = 0;
    public static final int SHOW_CONTEXT_LINES_MODE_ON_HOVER = 1;
    public static final int SHOW_CONTEXT_LINES_ALWAYS = 2;
    public static final List<String> contextLinesOptions = new RArrayListBuilder<String>()
            .add(NEVER, ON_HOVER, ALWAYS).build();
    private static int showContextLinesMode;
    private static boolean shouldPickShortestLine;
    private static int lineStroke;
    private static float weight;
    private static float endpointRectSize;

//    public static void updateSettings() {
//        GlobalReferences.gui.pushFolder("context lines");
//        showContextLinesMode = contextLinesOptions.indexOf(
//                GlobalReferences.gui.radio("visibility", contextLinesOptions, ON_HOVER));
//        shouldPickShortestLine = GlobalReferences.gui.toggle("shortest line");
//        lineStroke = GlobalReferences.gui.colorPicker("color", NormColorStore.color(0.5f)).hex;
//        weight = GlobalReferences.gui.slider("weight", 1.2f);
//        endpointRectSize = GlobalReferences.gui.slider("end size", 3.5f);
//        GlobalReferences.gui.popFolder();
//    }

    /**
     * Draw Context Lines
     *
     * @param pg processing graphics reference
     * @param tree the component tree
     */
    public static void drawLines(PGraphics pg, RComponentTree tree){
        pg.pushStyle();
        pg.stroke(lineStroke);
        pg.fill(lineStroke);
        pg.strokeCap(PConstants.SQUARE);
        pg.strokeWeight(weight);
        List<RComponent> allNodes = tree.getComponents();
        if (showContextLinesMode == SHOW_CONTEXT_LINES_MODE_NEVER) {
            pg.popStyle();
            return;
        }
        for (RComponent node : allNodes) {
            if (!(node instanceof RFolder)) {
                continue;
            }
            RFolder folderNode = (RFolder) node;
            if (folderNode.getWindow() == null || !folderNode.isWindowVisible() || !folderNode.isVisible()) {
                continue;
            }
            boolean shouldShowLine = showContextLinesMode == SHOW_CONTEXT_LINES_ALWAYS || (folderNode.getWindow().isTitleHighlighted() && showContextLinesMode == SHOW_CONTEXT_LINES_MODE_ON_HOVER);
            if (shouldShowLine) {
                folderNode.getWindow().drawContextLine(pg, endpointRectSize, shouldPickShortestLine);
            }
        }
        pg.popStyle();
    }
}
