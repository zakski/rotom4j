package com.szadowsz.gui;

import com.jogamp.newt.opengl.GLWindow;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.RComponentTree;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.component.group.RRoot;
import com.szadowsz.gui.component.input.toggle.RCheckbox;
import com.szadowsz.gui.component.input.toggle.RToggle;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.RInputHandler;
import com.szadowsz.gui.input.RInputListener;
import com.szadowsz.gui.utils.RContextLines;
import com.szadowsz.gui.utils.RSnapToGrid;
import com.szadowsz.gui.window.RWindowManager;
import com.szadowsz.gui.window.internal.RWindowInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import com.szadowsz.gui.config.*;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

import static processing.core.PApplet.tan;
import static processing.core.PConstants.*;

/**
 * GUI System Representation
 */
public class RotomGui {
    private static final Logger LOGGER = LoggerFactory.getLogger(RotomGui.class);

    protected final PApplet app;
    protected final GLWindow appWindow;

    // GUI Canvas Reference
    protected PGraphics guiCanvas; // TODO LazyGui
    // Guard against double draw
    protected int lastFrameCountGuiWasShown = -1;

    protected final RInputHandler inputHandler; // TODO LazyGui
    protected final RWindowManager winManager;
    protected final RComponentTree tree;

    protected final ArrayList<String> pathPrefix = new ArrayList<>(); // TODO LazyGui

    /**
     * Constructor for the RotomGui object which acts as a central hub for all GUI related methods within its' sketch.
     *
     * Meant to be initialized once in sketch setup() method
     *
     * Registers itself at end of the draw() method and displays the GUI whenever draw() ends.
     *
     * @param sketch main processing sketch class to display the GUI on and use keyboard and mouse input from
     * @param settings settings to apply
     */
    RotomGui(PApplet sketch, RotomGuiSettings settings) {
        app = sketch;
        appWindow = (GLWindow) app.getSurface().getNative();

        inputHandler = new RInputHandler(this);
        registerListeners();

        settings.applyEarlyStartupSettings();

        RThemeStore.init();
        RFontStore.init(sketch);
        winManager= new RWindowManager(this);
        tree = new RComponentTree(this);
        settings.applyLateStartupSettings();
        //        winManager.addRootWindow(settings.getUseToolbarAsRoot());
    }

    /**
     * Clears the global path prefix stack, removing all its elements.
     * <p>
     * Nothing will be prefixed in subsequent calls to control elements.
     * <p>
     * Happens every time draw() ends and RotomGui.draw() begins, in order for RotomGui to be certain of what the
     * current path is for its own control elements like the options folder and so users don't have to pop all of their
     * folders, since they get cleared every frame.
     */
    protected void clearStack(){
        pathPrefix.clear();
    }

    /**
     * Create Gui Canvas, if it's needed
     */
    protected void createGuiCanvasIfNecessary() {
        if (guiCanvas == null || guiCanvas.width != app.width || guiCanvas.height != app.height) {
            guiCanvas = app.createGraphics(app.width, app.height, P2D);
            guiCanvas.colorMode(HSB, 1, 1, 1, 1);
            int smoothValue = RLayoutStore.getSmoothingValue();
            if(smoothValue == 0){
                guiCanvas.noSmooth();
            }else{
                guiCanvas.smooth(smoothValue);
            }

            // dummy draw workaround for processing P2D PGraphics first draw loop bug where the canvas is unusable
            guiCanvas.beginDraw();
            guiCanvas.endDraw();
        }
    }

    /**
     * Register draw/input Methods with Processing
     */
    protected void registerListeners() {
        app.registerMethod("draw", this);
        app.registerMethod("keyEvent", this);
        app.registerMethod("mouseEvent",this);
    }

    protected void updateAllNodeValues() {
        List<RComponent> allNodes = tree.getAllNodesAsList();
        for(RComponent node : allNodes){
            node.updateValues();
        }
    }

    protected void updateOptions() {
//        gui.pushFolder(optionsFolderName);
//        LayoutStore.updateWindowOptions();
//        FontStore.updateFontOptions();
//        ThemeStore.updateThemePicker();
//        SnapToGrid.updateSettings();
//        ContextLines.updateSettings();
//        HotkeyStore.updateHotkeyToggles();
//        DelayStore.updateInputDelay();
//        MouseHiding.updateSettings();
//        gui.popFolder();
    }
    protected void resetPerspective() {
        float cameraFOV = PI / 3f;
        float cameraAspect = (float) app.width / (float) app.height;
        float cameraY = app.height / 2.0f;
        float cameraZ = cameraY / tan(PI*60/360);
        float cameraNear = cameraZ / 10;
        float cameraFar = cameraZ * 10;
        app.perspective(cameraFOV, cameraAspect, cameraNear, cameraFar);
    }

    protected void resetSketchMatrixInAnyRenderer() {
        if (app.sketchRenderer().equals(P3D)) {
            resetPerspective();
            app.camera();
            app.noLights();
        } else {
            app.resetMatrix();
        }
    }

    /**
     * Gets the current path prefix stack, inserting a forward slash after each folder name in the stack.
     * Mostly used internally by LazyGui, but it can also be useful for debugging.
     *
     * @return entire path prefix stack concatenated to one string
     */
    protected String getCurrentFolder(){ // TODO LazyGui
        if(pathPrefix.isEmpty()){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = pathPrefix.size() - 1; i >= 0; i--) {
            String folder = pathPrefix.get(i);
            sb.append(folder);
            sb.append("/");
        }
        return sb.toString();
    }

    protected RWindowInt getWindowBeingDraggedIfAny() { // TODO LazyGui
        List<RComponent> allNodes = tree.getAllNodesAsList();
        for(RComponent node : allNodes){
            if(node instanceof RFolder folder){
                if(folder.getWindow() != null && folder.getWindow().isDragged()){
                    return folder.getWindow();
                }
            }
        }
        return null;
    }

    /**
     * Updates and draws the GUI on the specified parameter canvas, assuming its size is identical to the main sketch size.
     * Gets called automatically at the end of draw().
     * NDSGui will enforce itself being drawn only once per frame internally, which can be useful for including the gui in a recording.
     * If it does get called manually, it will get drawn when requested and then skip its automatic execution for that frame.
     * <p>
     *  Resets any potential hint(DISABLE_DEPTH_TEST) to the default hint(ENABLE_DEPTH_TEST) when done,
     *  because it needs the DISABLE_DEPTH_TEST to draw the GUI over 3D scenes and has currently no way to save or query the original hint state.
     *
     * @param targetCanvas canvas to draw the GUI on
     */
    public void draw(PGraphics targetCanvas) { // TODO LazyGui
        if(lastFrameCountGuiWasShown == app.frameCount){
            // we are at the end of the user's sketch draw(), but the gui has already been displayed this frame
            clearStack();
            return;
        }
        lastFrameCountGuiWasShown = app.frameCount;
        if(app.frameCount == 1){
            RRoot root = tree.getRoot();
            root.resizeForContents();
        }
        createGuiCanvasIfNecessary();
        updateAllNodeValues();
        guiCanvas.beginDraw();
        guiCanvas.clear();
        clearStack();
        updateOptions();
        if (!RLayoutStore.isGuiHidden()) {
            RSnapToGrid.displayGuideAndApplyFilter(guiCanvas, getWindowBeingDraggedIfAny());
            RContextLines.drawLines(guiCanvas,tree);
            winManager.updateAndDrawWindows(guiCanvas);
        }
        guiCanvas.endDraw();
        resetSketchMatrixInAnyRenderer();
        targetCanvas.hint(DISABLE_DEPTH_TEST);
        targetCanvas.pushStyle();
        targetCanvas.imageMode(CORNER);
        targetCanvas.image(guiCanvas, 0, 0);
        targetCanvas.popStyle();
        targetCanvas.hint(ENABLE_DEPTH_TEST);
    }

   /**
     * Gets a button component at the specified location. Initializes the button if needed.
     *
     * @param path forward slash separated unique path to the control element
     * @return button
     */
    public RButton button(String path) {  // TODO LazyGui
        String fullPath = getCurrentFolder() + path;
        if(tree.isPathTakenByUnexpectedType(fullPath, RButton.class)){
            return null;
        }
        RButton node = (RButton) tree.findNode(fullPath);
        if (node == null) {
            RFolder folder = tree.findParentFolderLazyInitPath(fullPath);
            node = new RButton(this,fullPath, folder);
            tree.insertNodeAtItsPath(node);
        }
        return node;
    }

    /**
     * Gets a checkbox component at the specified location. Initializes it if needed and sets its value to the specified
     * starting parameter.
     *
     * @param path forward slash separated unique path to the control element
     * @param startingValue starting value of the toggle
     * @return the checkbox
     */
    public RCheckbox checkbox(String path, boolean startingValue) {  // TODO LazyGui
        String fullPath = getCurrentFolder() + path;
        if(tree.isPathTakenByUnexpectedType(fullPath, RCheckbox.class)){
            return null;
        }
        RCheckbox node = (RCheckbox) tree.findNode(fullPath);
        if (node == null) {
            RFolder folder = tree.findParentFolderLazyInitPath(fullPath);
            node = new RCheckbox(this,fullPath, folder, startingValue);
            tree.insertNodeAtItsPath(node);
        }
        return node;
    }

    /**
     * Gets a toggle component at the specified location. Initializes it if needed and sets its value to the specified
     * starting parameter.
     *
     * @param path forward slash separated unique path to the control element
     * @param startingValue starting value of the toggle
     * @return the toggle
     */
    public RToggle toggle(String path, boolean startingValue) {  // TODO LazyGui
        String fullPath = getCurrentFolder() + path;
        if(tree.isPathTakenByUnexpectedType(fullPath, RToggle.class)){
            return null;
        }
        RToggle node = (RToggle) tree.findNode(fullPath);
        if (node == null) {
            RFolder folder = tree.findParentFolderLazyInitPath(fullPath);
            node = new RToggle(this,fullPath, folder, startingValue);
            tree.insertNodeAtItsPath(node);
        }
        return node;
    }

    /**
     * Get the PApplet the GUi is displayed in
     *
     * @return the PApplet that the GUI is bound to
     */
    public PApplet getSketch(){
        return app;
    }

    /**
     * Get the PApplet the GUi is displayed in
     *
     * @return the PApplet that the GUI is bound to
     */
    public GLWindow getGLWindow(){
        return appWindow;
    }

    public RWindowManager getWinManager() {
        return winManager;
    }
    public RComponentTree getComponentTree() {
        return tree;
    }

    public void setFocus(RWindowInt window) { // TODO LazyGui
        winManager.setFocus(window);
        inputHandler.setFocus(window);
    }

    public void setAllMouseOverToFalse(RFolder folder) { // TODO LazyGui
        tree.setAllMouseOverToFalse(folder);
    }

    public void setAllMouseOverToFalse() { // TODO LazyGui
        tree.setAllMouseOverToFalse();
    }

    public void subscribe(RInputListener subscriber){
        inputHandler.subscribe(subscriber);
    }

    public void resetInput() {
        inputHandler.reset();
    }
}
