package com.szadowsz.gui.utils;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.RShaderStore;
import com.szadowsz.gui.window.internal.RWindowImpl;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.opengl.PShader;

import java.awt.*;
import java.util.List;

import static processing.core.PApplet.*;

/**
 * Snap Window to Grid Utility
 */
public class RSnapToGrid {
   private static final String pointShaderPath = "guideGridPoints.glsl";

    private static final int VISIBILITY_ALWAYS = 0;
    private static final int VISIBILITY_ON_DRAG = 1;
    private static final int VISIBILITY_NEVER = 2;
    private static final int defaultVisibilityModeIndex = VISIBILITY_ON_DRAG;
    private static final float dragAlphaDelta = 0.05f;

    private static PShader pointShader;
    private static float pointColorRed, pointColorGreen, pointColorBlue;

   private static int selectedVisibilityModeIndex = defaultVisibilityModeIndex;
    private static float dragAlpha = 0;
    private static Color pointGridColor = Color.getHSBColor(1,1,0.5f);
    private static float pointWeight = 3f;
    private static float sdfCropDistance = 100;
    private static boolean shouldCenterPoints = true;
    private static int pointColorPrev = -1;

    static float cellSizeLastFrame = -1;
    static final List<String> availableVisibilityModes = new RArrayListBuilder<String>().add("always", "on drag", "never").build();

    public static boolean snapToGridEnabled = true;

    private RSnapToGrid(){
        // NOOP
    }

    /**
     * Method to update the drag alpha value
     *
     * @param draggedWindow a window being dragged
     */
    private static void updateAlpha(RWindowImpl draggedWindow) {
        float dragAlphaMax = pointGridColor.getAlpha();
        dragAlphaMax = constrain(dragAlphaMax, 0, 1);
        if(draggedWindow != null){
            dragAlpha = lerp(dragAlpha, dragAlphaMax, dragAlphaDelta);
        }else{
            dragAlpha = lerp(dragAlpha, 0, dragAlphaDelta);
        }
        dragAlpha = constrain(dragAlpha, 0, dragAlphaMax);
    }

    /**
     * Method to check if the cell size just changed
     *
     * @return true if the cell changed last frame, false otherwise
     */
    private static boolean hasCellSizeJustChanged() {
        boolean result = cellSizeLastFrame != RLayoutStore.getCell();
        cellSizeLastFrame = RLayoutStore.getCell();
        return result;
    }

    /**
     * Method to check if snapToGrid was just enabled
     *
     * @param previousState last state
     * @param currentState current state
     * @return true if snapToGrid was just enabled, false otherwise
     */
    private static boolean hasJustBeenEnabled(boolean previousState, boolean currentState) {
        return !previousState && currentState;
    }

    /**
     * Get Available Visibility Options
     *
     * @return a list of options
     */
    public static List<String> getOptions() {
        return availableVisibilityModes;
    }

    /**
     * Get Default Visibility Option
     *
     * @return the default option
     */
    public static String getDefaultVisibilityMode() {
        return getOptions().get(defaultVisibilityModeIndex);
    }

    /**
     * Set the Current Visibility Mode
     *
     * @param mode the mode to set
     */
    public static void setSelectedVisibilityMode(String mode) {
        if(!availableVisibilityModes.contains(mode)){
            return;
        }
        selectedVisibilityModeIndex = availableVisibilityModes.indexOf(mode);
    }

    /**
     * Method to snap coordinates to the grid
     *
     * @param inputX window x-coordinate
     * @param inputY window y-coordinate
     * @return grid vector coordinate
     */
    public static PVector trySnapToGrid(float inputX, float inputY){
        if(!snapToGridEnabled) {
            return new PVector(inputX, inputY);
        }
        float negativeModuloBuffer = RLayoutStore.getCell() * 60;
        inputX += negativeModuloBuffer;
        inputY += negativeModuloBuffer;
        int x = floor(inputX);
        int y = floor(inputY);
        if(x % RLayoutStore.getCell() > RLayoutStore.getCell() / 2 ){
            x += (int) RLayoutStore.getCell();
        }
        if(y % RLayoutStore.getCell() > RLayoutStore.getCell() / 2 ){
            y += (int) RLayoutStore.getCell();
        }
        while(x % RLayoutStore.getCell() != 0){
            x -= 1;
        }
        while(y % RLayoutStore.getCell() != 0){
            y -= 1;
        }
        return new PVector(x-negativeModuloBuffer, y-negativeModuloBuffer);
    }

    /**
     * Display snap to grid guide
     *
     * @param pg Processing Graphics Context
     * @param draggedWindow a window being dragged
     */
    public static void displayGuideAndApplyFilter(RotomGui gui, PGraphics pg, RWindowImpl draggedWindow){
        if(pointShader == null){
            pointShader = RShaderStore.getOrLoadShader(gui,pointShaderPath);
        }
        if(selectedVisibilityModeIndex == VISIBILITY_ON_DRAG){
            updateAlpha(draggedWindow);
        }
        if(selectedVisibilityModeIndex == VISIBILITY_NEVER){
            return;
        }
        pointShader.set("alpha", selectedVisibilityModeIndex == VISIBILITY_ALWAYS ? pointGridColor.getAlpha() : dragAlpha);
        pointShader.set("sdfCropEnabled", selectedVisibilityModeIndex == VISIBILITY_ON_DRAG);
        pointShader.set("shouldCenterPoints", shouldCenterPoints);
        pointShader.set("sdfCropDistance", sdfCropDistance);
        pointShader.set("gridCellSize", (float) floor(RLayoutStore.getCell()));
        int pointColor = pointGridColor.getRGB();
        if(pointColorPrev == -1 || pointColor != pointColorPrev){
            pointColorPrev = pointColor;
            pointColorRed = pointGridColor.getRed();
            pointColorGreen = pointGridColor.getGreen();
            pointColorBlue = pointGridColor.getBlue();
        }
        pointShader.set("pointColor", pointColorRed, pointColorGreen, pointColorBlue);
        pointShader.set("pointWeight", pointWeight);
        if(draggedWindow != null){
            pointShader.set("window", draggedWindow.getPosX(), draggedWindow.getPosY(), draggedWindow.getWidth(), draggedWindow.getHeight());
        }
        pg.filter(pointShader);
        pg.resetShader();
    }

//    public static void updateSettings() {
//        GlobalReferences.gui.pushFolder("grid");
//        boolean previousSnapToGridEnabled = snapToGridEnabled;
//        snapToGridEnabled = GlobalReferences.gui.toggle("snap to grid", true);
//        if(hasCellSizeJustChanged() || hasJustBeenEnabled(previousSnapToGridEnabled, snapToGridEnabled)){
//            WindowManager.snapAllStaticWindowsToGrid();
//        }
//        setSelectedVisibilityMode(GlobalReferences.gui.radio("show grid", getOptions(), getDefaultVisibilityMode()).valueString);
//        sdfCropDistance = GlobalReferences.gui.slider("drag range", sdfCropDistance);
//        pointGridColor = GlobalReferences.gui.colorPicker("point color", pointGridColor.hex);
//        pointWeight = GlobalReferences.gui.slider("point size", pointWeight);
//        shouldCenterPoints = GlobalReferences.gui.toggle("points centered", shouldCenterPoints);
//        GlobalReferences.gui.popFolder();
//    }
}
