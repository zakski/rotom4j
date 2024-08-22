package com.szadowsz.gui;

import com.jogamp.newt.opengl.GLWindow;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.RComponentTree;
import com.szadowsz.gui.component.action.RButton;
import com.szadowsz.gui.component.folder.RDropdownMenu;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.component.folder.RToolbar;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RRoot;
import com.szadowsz.gui.component.input.toggle.RCheckbox;
import com.szadowsz.gui.component.input.toggle.RToggle;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.input.RInputHandler;
import com.szadowsz.gui.input.RInputListener;
import com.szadowsz.gui.layout.RBorderLayout;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLayoutConfig;
import com.szadowsz.gui.utils.RContextLines;
import com.szadowsz.gui.utils.RSnapToGrid;
import com.szadowsz.gui.window.RWindowManager;
import com.szadowsz.gui.window.internal.RWindowInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import com.szadowsz.gui.config.*;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

import static processing.core.PApplet.tan;
import static processing.core.PConstants.*;

/**
 * GUI System Representation
 */
public class RotomGui {
    private static final Logger LOGGER = LoggerFactory.getLogger(RotomGui.class);

    // Folder Stack Constants
    private static final int stackSizeWarningLevel = 64;

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

    // Folder Stack Warnings
    private boolean printedPushWarningAlready = false;
    private boolean printedPopWarningAlready = false;

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

    private void tryLogStackWarning(String method) {
        if(pathPrefix.size() >= stackSizeWarningLevel && !printedPushWarningAlready){
            LOGGER.warn("Too many calls to {} - stack size reached the warning limit of " + stackSizeWarningLevel +
                    ", possibly due to runaway recursion",method);
            printedPushWarningAlready = true;
        }
    }
    /**
     * Gets the current path prefix stack, inserting a forward slash after each folder name in the stack.
     * Mostly used internally by LazyGui, but it can also be useful for debugging.
     *
     * @return entire path prefix stack concatenated to one string
     */
    protected String getCurrentPath(){ // TODO LazyGui
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
     * <p>
     * RotomGui will enforce itself being drawn only once per frame internally, which can be useful for including the gui
     * in a recording.
     * <p>
     * If it does get called manually, it will get drawn when requested and then skip its automatic execution for that
     * frame.
     * <p>
     *  Resets any potential hint(DISABLE_DEPTH_TEST) to the default hint(ENABLE_DEPTH_TEST) when done,
     *  because it needs the DISABLE_DEPTH_TEST to draw the GUI over 3D scenes and has currently no way to save or query
     *  the original hint state.
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
            RSnapToGrid.displayGuideAndApplyFilter(this,guiCanvas, getWindowBeingDraggedIfAny());
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
     * Updates and draws the GUI on the main processing canvas.
     * <p>
     * Gets called automatically at the end of draw() by default, but can also be called manually to display the GUI at
     * a better time during the frame.
     * <p>
     * The GUI will not draw itself multiple times per one frame, so the automatic execution is skipped when this is
     * called manually.
     * <p>
     * Must stay public because otherwise this registering won't work: app.registerMethod("draw", this);
     * <p>
     * Calls {@link RotomGui#draw(PGraphics) draw(PGraphics)} internally with the default sketch PGraphics.
     * @see RotomGui#draw(PGraphics)
     */
    public final void draw() {
        draw(app.g);
    }

    /**
     * Keyboard Event Processing
     *
     * @param event key event
     */
    public void keyEvent(KeyEvent event) {
        inputHandler.keyEvent(event);
    }

    /**
     * Mouse Event Processing
     *
     * @param event mouse event
     */
    public void mouseEvent(MouseEvent event) {
        inputHandler.mouseEvent(event);
    }

    protected StringBuilder pushPathToStack(String folderName) {
        String slashSafeFolderName = folderName;
        if(slashSafeFolderName.startsWith("/")){
            // remove leading slash
            slashSafeFolderName = slashSafeFolderName.substring(1);
        }
        if(slashSafeFolderName.endsWith("/") && !slashSafeFolderName.endsWith("\\/")){
            // remove trailing slash if un-escaped
            slashSafeFolderName = slashSafeFolderName.substring(0, slashSafeFolderName.length()-1);
        }
        pathPrefix.addFirst(slashSafeFolderName);
        StringBuilder builder = new StringBuilder();
        for (int i = pathPrefix.size()-1; i >= 0; i--){
            builder.append(pathPrefix.get(i));
            if (i > 0)
                builder.append("/");
        }
        return builder;
    }

    public RDropdownMenu pushDropdown(String barName) {
        tryLogStackWarning("pushToolbar(String)");
        StringBuilder builder = pushPathToStack(barName);
        tree.initDropdowForPath(builder.toString());
        return (RDropdownMenu) tree.find(builder.toString());
    }

    /**
     * Pushes a folder name to the global path prefix stack.
     * Can be used multiple times in pairs just like pushMatrix() and popMatrix().
     * Removes leading and trailing slashes to enforce consistency, but allows slashes to appear either escaped or anywhere else inside the string.
     * Any GUI control element call will apply all the folders in the stack as a prefix to their own path parameter.
     * This is useful for not repeating the whole path string every time you want to call a control element.
     *
     * @param folderName one folder's name to push to the stack
     * @return folder
     */
    public RFolder pushFolder(String folderName){
        tryLogStackWarning("pushFolder(String)");
        StringBuilder builder = pushPathToStack(folderName);
        tree.initFolderForPath(builder.toString());
        RFolder folder = (RFolder) tree.find(builder.toString());
        if (folder.getParent() instanceof RRoot){
            this.getWinManager().uncoverOrCreateWindow(folder);
        }
        return folder;
    }

    public RFolder pushPane(String paneName) {
        tryLogStackWarning("pushPane(String)");
        StringBuilder builder = pushPathToStack(paneName);
        tree.initFolderForPath(builder.toString());
        RFolder pane = (RFolder) tree.find(builder.toString());
        if (pane.getParent() instanceof RRoot){
            this.getWinManager().uncoverOrCreateWindow(pane);
        }
        return pane;
    }

    public RFolder pushPane(String paneName, RLayoutConfig config) {
        tryLogStackWarning("pushPane(String,RLayoutConfig)");
        StringBuilder builder = pushPathToStack(paneName);
        tree.initFolderForPath(builder.toString());
        RFolder pane = (RFolder) tree.find(builder.toString());
        pane.setLayoutConfig(config);
        if (pane.getParent() instanceof RRoot){
            this.getWinManager().uncoverOrCreateWindow(pane);
        }
        return pane;
    }

    public RToolbar pushToolbar(String barName) {
        tryLogStackWarning("pushToolbar(String)");
        StringBuilder builder = pushPathToStack(barName);
        tree.initToolbarForPath(builder.toString());
        RToolbar toolbar = (RToolbar) tree.find(builder.toString());
        if (toolbar.getParent() instanceof RRoot){
            this.getWinManager().uncoverOrCreateWindow(toolbar);
        }
        return toolbar;
    }

    public RToolbar pushToolbar(String barName, RLayoutConfig config) {
        tryLogStackWarning("pushToolbar(String,RLayoutConfig)");
        StringBuilder builder = pushPathToStack(barName);
        tree.initToolbarForPath(builder.toString());
        RToolbar toolbar = (RToolbar) tree.find(builder.toString());
        toolbar.setLayoutConfig(config);
        if (toolbar.getParent() instanceof RRoot){
            this.getWinManager().uncoverOrCreateWindow(toolbar);
        }
        return toolbar;
    }



    /**
     * Gets a button component at the specified location. Initializes the button if needed.
     *
     * @param path forward slash separated unique path to the control element
     * @return button
     */
    public RButton button(String path) {  // TODO LazyGui
        String fullPath = getCurrentPath() + path;
        if(tree.isPathTakenByUnexpectedType(fullPath, RButton.class)){
            return null;
        }
        RButton node = (RButton) tree.find(fullPath);
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
        String fullPath = getCurrentPath() + path;
        if(tree.isPathTakenByUnexpectedType(fullPath, RCheckbox.class)){
            return null;
        }
        RCheckbox node = (RCheckbox) tree.find(fullPath);
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
        String fullPath = getCurrentPath() + path;
        if(tree.isPathTakenByUnexpectedType(fullPath, RToggle.class)){
            return null;
        }
        RToggle node = (RToggle) tree.find(fullPath);
        if (node == null) {
            RFolder folder = tree.findParentFolderLazyInitPath(fullPath);
            node = new RToggle(this,fullPath, folder, startingValue);
            tree.insertNodeAtItsPath(node);
        }
        return node;
    }

    /**
     * Pops the last pushed folder name from the global path prefix stack.
     * Can be used multiple times in pairs just like pushMatrix() and popMatrix().
     * Warns once when the stack is empty and popFolder() is attempted.
     * Any GUI control element call will apply all the folders in the stack as a prefix to their own path parameter.
     * This is useful for not repeating the whole path string every time you want to call a control element.
     */
    public void popWindow(){
        if(pathPrefix.isEmpty() && printedPopWarningAlready){
            LOGGER.warn("Too many calls to popFolder() - there is nothing to pop");
            printedPopWarningAlready = true;
        }
        if(!pathPrefix.isEmpty()){
            pathPrefix.remove(0);
        }
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

    public void setLayout(RLayoutBase layout) {
        String path = getCurrentPath();
        RGroup group = (RGroup) tree.find(path);

        if (group == null) {
            LOGGER.warn("Path For Layout Does Not Currently Exist: {}", path);
        } else if (group instanceof RRoot root){
            root.setLayout(layout);
        } else if (!group.canChangeLayout() || layout instanceof RBorderLayout) {
            LOGGER.warn("Layout Cannot Be Set For Path: {}", path);
        } else {
            group.setLayout(layout);
        }
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
