package com.old.ui;

import com.old.ui.constants.GlobalReferences;
import com.old.ui.constants.theme.ThemeStore;
import com.old.ui.input.InputWatcherBackend;
import com.old.ui.node.AbstractNode;
import com.old.ui.node.NodePaths;
import com.old.ui.node.NodeTree;
import com.old.ui.node.NodeType;
import com.old.ui.node.impl.*;
import com.old.ui.store.*;
import com.old.ui.utils.ContextLines;
import com.old.ui.window.SnapToGrid;
import com.old.ui.window.Window;
import com.old.ui.window.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

import static com.old.ui.constants.GlobalReferences.app;
import static com.old.ui.node.NodeTree.*;
import static processing.core.PApplet.tan;
import static processing.core.PConstants.*;

/**
 * GUI System Representation
 */
public class NDSGui {

    private static final Logger LOGGER = LoggerFactory.getLogger(NDSGui.class);

    // Sole Instance of this GUI object
    private static NDSGui singleton;

    // GUI Canvas Reference
    private PGraphics guiCanvas;

    // Backend to handle mouse/keyboard events
    private InputWatcherBackend inputHandler;

    // Guard against double draw
    private int lastFrameCountGuiWasShown = -1;

    private final ArrayList<String> pathPrefix = new ArrayList<>();

    //
    static final String optionsFolderName = "options";
    //
    static final String savesFolderName = "saves";

    // Folder Stack Constants
    private static final int stackSizeWarningLevel = 64;
    private boolean printedPushWarningAlready = false;
    private boolean printedPopWarningAlready = false;


    /**
     * Main constructor for the NDSGui object which acts as a central hub for all GUI related methods.
     * Meant to be initialized once in setup() with <code>new NDSGui(this)</code>.
     * Registers itself at end of the draw() method and displays the GUI whenever draw() ends.
     *
     * @param sketch the sketch that uses this gui, should be 'this' from the calling side
     */
    public NDSGui(PApplet sketch){
        this(sketch, new NDSGuiSettings());
    }

    /**
     * Constructor for the NDSGui object which acts as a central hub for all GUI related methods.
     * Meant to be initialized once in setup() with <code>new NDSGui(this)</code>.
     * Registers itself at end of the draw() method and displays the GUI whenever draw() ends.
     *
     * @param sketch main processing sketch class to display the GUI on and use keyboard and mouse input from
     * @param settings settings to apply (loading a save on startup will overwrite them)
     * @see NDSGuiSettings
     */
    public NDSGui(PApplet sketch, NDSGuiSettings settings) {
        if(singleton != null && singleton != this){
            throw new IllegalStateException("You already initialized a NDSGui object, please don't create any more with 'new NDSGui(this)'." +
                    " It's meant to work similar to a singleton, there cannot be more than 1 instance running in any given program," +
                    " because it breaks mouse and key events and it would be confusing to work with multiple GUI instances." +
                    " The control element separation and grouping you're probably looking for can be achieved by using more folders rather than creating a whole new GUI object." +
                    "\n");
        }
        singleton = this;
        if (!sketch.sketchRenderer().equals(P2D) && !sketch.sketchRenderer().equals(P3D)) {
            throw new IllegalArgumentException("The NDSGui library requires the P2D or P3D renderer. Please set the sketch renderer to P2D or P3D before initializing NDSGui.");
        }
        GlobalReferences.init(this,sketch);
        registerListeners();
        if(settings == null){
            settings = new NDSGuiSettings();
        }
        settings.applyEarlyStartupSettings();
        NormColorStore.init();
        ThemeStore.init();
        FontStore.updateFont();
        WindowManager.addRootWindow(settings.getUseToolbarAsRoot());
        settings.applyLateStartupSettings();
    }

    /**
     * Clears the global path prefix stack, removing all its elements.
     * Nothing will be prefixed in subsequent calls to control elements.
     * Also happens every time draw() ends and LazyGui.draw() begins,
     * in order for LazyGui to be certain of what the current path is for its own control elements like the options folder
     * and so the library user doesn't have to pop all of their folders, since they get cleared every frame.
     */
    private void clearFolder(){
        pathPrefix.clear();
    }

    /**
     * Create Gui Canvas, if it's needed
     */
    private void createGuiCanvasIfNecessary() {
        if (guiCanvas == null || guiCanvas.width != app.width || guiCanvas.height != app.height) {
            guiCanvas = app.createGraphics(app.width, app.height, P2D);
            guiCanvas.colorMode(HSB, 1, 1, 1, 1);
            int smoothValue = LayoutStore.getSmoothingValue();
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

    private Window getWindowBeingDraggedIfAny() {
        List<AbstractNode> allNodes = NodeTree.getAllNodesAsList();
        for(AbstractNode node : allNodes){
            if(node.type == NodeType.FOLDER){
                FolderNode folder = (FolderNode) node;
                if(folder.window != null && folder.window.isBeingDragged()){
                    return folder.window;
                }
            }
        }
        return null;
    }

    /**
     * Register draw/input Methods with Processing
     */
    private void registerListeners() {
        inputHandler = InputWatcherBackend.getInstance();

        app.registerMethod("draw", this);
        app.registerMethod("keyEvent", this);
        app.registerMethod("mouseEvent",this);
    //    app.registerMethod("post", this);
    }

    private void resetPerspective() {
        float cameraFOV = PI / 3f;
        float cameraAspect = (float) app.width / (float) app.height;
        float cameraY = app.height / 2.0f;
        float cameraZ = cameraY / tan(PI*60/360);
        float cameraNear = cameraZ / 10;
        float cameraFar = cameraZ * 10;
        app.perspective(cameraFOV, cameraAspect, cameraNear, cameraFar);
    }

    private void resetSketchMatrixInAnyRenderer() {
        if (app.sketchRenderer().equals(P3D)) {
            resetPerspective();
            app.camera();
            app.noLights();
        } else {
            app.resetMatrix();
        }
    }

    private void updateAllNodeValues() {
        List<AbstractNode> allNodes = NodeTree.getAllNodesAsList();
        for(AbstractNode node : allNodes){
            node.updateValuesRegardlessOfParentWindowOpenness();
        }
    }

    private void updateOptionsFolder() {
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

    /**
     * Updates and draws the GUI on the main processing canvas.
     * Gets called automatically at the end of draw() by default, but can also be called manually to display the GUI at a better time during the frame.
     * The GUI will not draw itself multiple times per one frame, so the automatic execution is skipped when this is called manually.
     * Must stay public because otherwise this registering won't work: app.registerMethod("draw", this);
     * Calls {@link NDSGui#draw(PGraphics) draw(PGraphics)} internally with the default sketch PGraphics.
     * @see NDSGui#draw(PGraphics)
     */
    public final void draw() {
        draw(app.g);
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
    public void draw(PGraphics targetCanvas) {
        if(lastFrameCountGuiWasShown == app.frameCount){
            // we are at the end of the user's sketch draw(), but the gui has already been displayed this frame
            clearFolder();
            return;
        }
        lastFrameCountGuiWasShown = app.frameCount;
        if(app.frameCount == 1){
            FolderNode root = NodeTree.getRoot();
            root.window.resizeForContents();
        }
        createGuiCanvasIfNecessary();
        updateAllNodeValues();
        guiCanvas.beginDraw();
        guiCanvas.clear();
        clearFolder();
        updateOptionsFolder();
        if (!LayoutStore.isGuiHidden()) {
            SnapToGrid.displayGuideAndApplyFilter(guiCanvas, getWindowBeingDraggedIfAny());
            ContextLines.drawLines(guiCanvas);
            WindowManager.updateAndDrawWindows(guiCanvas);
        }
        guiCanvas.endDraw();
        resetSketchMatrixInAnyRenderer();
        targetCanvas.hint(DISABLE_DEPTH_TEST);
        targetCanvas.pushStyle();
        targetCanvas.imageMode(CORNER);
        targetCanvas.image(guiCanvas, 0, 0);
        targetCanvas.popStyle();
        targetCanvas.hint(ENABLE_DEPTH_TEST);
        JsonSaveStore.updateEndlessLoopDetection();
        ChangeListener.onFrameFinished();
    }

    /**
     * Hides the folder at the current path prefix stack.
     * See {@link #hide hide(String path)}
     */
    public void hideCurrentFolder(){
        String fullPath = getFolder();
        if(fullPath.endsWith("/")){
            fullPath = fullPath.substring(0, fullPath.length() - 1);
        }
        NodeTree.hideAtFullPath(fullPath);
    }

    /**
     * Hide any chosen element or folder except the root window. Hides both the row and any affected opened windows under that node.
     * The GUI then skips it while drawing, but still returns its values and allows interaction from code as if it was still visible.
     * Can be called once in `setup()` or repeatedly every frame, the result is the same.
     * Does not initialize a control and has no effect on controls that have not been initialized yet.
     * @param path path to the control or folder being hidden - it will get prefixed by the current path prefix stack to get the full path
     */
    public void hide(String path){
        if("".equals(path) || "/".equals(path)){
            hideCurrentFolder();
            return;
        }
        String fullPath = getFolder() + path;
        NodeTree.hideAtFullPath(fullPath);
    }

    /**
     * Utility method for displaying the GUI before draw() ends for the purposes of recording.
     * Does not update the gui, only returns the previous frame's gui canvas.
     * Can be confusing when displayed due to seeing duplicated GUI images with slightly different content.
     * @return previous frame's gui canvas
     */
    public PGraphics getGuiCanvas() {
        return guiCanvas;
    }

    /**
     * Gets the current path prefix stack, inserting a forward slash after each folder name in the stack.
     * Mostly used internally by LazyGui, but it can also be useful for debugging.
     *
     * @return entire path prefix stack concatenated to one string
     */
    public String getFolder(){
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

    public void setFolder(String folder){
        pathPrefix.clear();
        if (folder != null && !folder.isEmpty()){
            String[] split = NodePaths.splitByUnescapedSlashes(folder);
            for (int i = split.length-1; i >=0;i--){
                pathPrefix.add(split[i]);
            }
        }
    }

    /**
     * Gets the value of a button control element and sets it to false.
     * Lazily initializes the button if needed.
     *
     * @param path forward slash separated unique path to the control element
     * @return button
     */
    public ButtonNode button(String path) {
        String fullPath = getFolder() + path;
        if(isPathTakenByUnexpectedType(fullPath, ButtonNode.class)){
            return null;
        }
        ButtonNode node = (ButtonNode) findNode(fullPath);
        if (node == null) {
            FolderNode folder = NodeTree.findParentFolderLazyInitPath(fullPath);
            node = new ButtonNode(fullPath, folder);
            insertNodeAtItsPath(node);
        }
        return node;
    }

    /**
     * Gets the current value of a toggle control element.
     * lazily initializes it if needed and sets its value to the specified parameter default.
     *
     * @param path forward slash separated unique path to the control element
     * @param defaultValue default value of the toggle
     * @return current value of the toggle
     */
    public ToggleNode toggle(String path, boolean defaultValue) {
        String fullPath = getFolder() + path;
        if(isPathTakenByUnexpectedType(fullPath, ButtonNode.class)){
            return null;
        }
        ToggleNode node = (ToggleNode) findNode(fullPath);
        if (node == null) {
            FolderNode folder = NodeTree.findParentFolderLazyInitPath(fullPath);
            node = new ToggleNode(fullPath, folder, defaultValue);
            insertNodeAtItsPath(node);
        }
        return node;
    }

    /**
     * Gets the currently selected string from a list of options in a gui control element.
     * Lazily initializes the radio element if needed - any later changes in the options parameter will be ignored.
     * Sets the default value to the first value in the list.
     *
     * @param path forward slash separated unique path to the control element
     * @param options list of options to display
     * @return currently selected string
     */
    public RadioFolderNode radio(String path, List<String> options) {
        return radio(path, options.toArray(new String[0]), null);
    }

    /**
     * Gets the currently selected string from a list of options in a gui control element.
     * Lazily initializes the radio element if needed - any later changes in the options parameter will be ignored.
     * Sets the default value to the specified parameter value, which must be contained in the options list, or it will be ignored.
     *
     * @param path forward slash separated unique path to the control element
     * @param options list of options to display
     * @param defaultOption default option to select, which must also be found in the options, or it will be ignored
     * @return currently selected string
     */
    public RadioFolderNode radio(String path, List<String> options, String defaultOption) {
        return radio(path, options.toArray(new String[0]), defaultOption);
    }

    /**
     * Gets the currently selected string from an array of options in a gui control element.
     * Lazily initializes the radio element if needed - any later changes in the options parameter will be ignored.
     * Sets the default value to the specified parameter value, which must be contained in the options array, or it will be ignored.
     *
     * @param path forward slash separated unique path to the control element
     * @param options list of options to display
     * @return currently selected string
     */
    public RadioFolderNode radio(String path, String[] options) {
        return radio(path, options, null);
    }

    /**
     * Gets the currently selected string from an array of options in a gui control element.
     * Lazily initializes the radio element if needed - any later changes in the options parameter will be ignored.
     * Sets the default value to the specified parameter value, which must be contained in the options array, or it will be ignored.
     *
     * @param path forward slash separated unique path to the control element
     * @param options list of options to display
     * @param defaultOption default option to select, must also be found in options, or it is ignored
     * @return currently selected string
     */
    public RadioFolderNode radio(String path, String[] options, String defaultOption) {
        String fullPath = getFolder() + path;
        if(isPathTakenByUnexpectedType(fullPath, RadioFolderNode.class)){
            return null;//defaultOption == null ? options[0] : defaultOption;
        }
        RadioFolderNode node = (RadioFolderNode) findNode(fullPath);
        if (node == null) {
            FolderNode parentFolder = NodeTree.findParentFolderLazyInitPath(fullPath);
            node = new RadioFolderNode(fullPath, parentFolder, options, defaultOption);
            insertNodeAtItsPath(node);
        }
        return node;
    }

    /**
     * Gets the current value of a text input element.
     * Lazily initializes the text input element if needed with its content set to an empty string.
     *
     * @param path forward slash separated unique path to the control element
     * @return current value of a string input element
     */
    public TextNode text(String path){
        return text(path, "");
    }

    /**
     * Gets the current value of an editable text field element.
     * Lazily initializes the string input if needed with the specified default.
     *
     * @param path forward slash separated unique path to the control element
     * @param content default value for the text content
     * @return current value of a string input element
     */
    public TextNode text(String path, String content){
        return getTextNodeValue(path, content);
    }

    private TextNode getTextNodeValue(String path, String content){
        String fullPath = getFolder() + path;
        if(isPathTakenByUnexpectedType(fullPath, TextNode.class)){
            return null;
        }
        TextNode node = (TextNode) findNode(fullPath);
        if (node == null) {
            FolderNode folder = NodeTree.findParentFolderLazyInitPath(fullPath);
            node = new TextNode(fullPath, folder, content);
            insertNodeAtItsPath(node);
        }
        return node;
    }
    /**
     * Pushes a folder name to the global path prefix stack.
     * Can be used multiple times in pairs just like pushMatrix() and popMatrix().
     * Removes leading and trailing slashes to enforce consistency, but allows slashes to appear either escaped or anywhere else inside the string.
     * Any GUI control element call will apply all the folders in the stack as a prefix to their own path parameter.
     * This is useful for not repeating the whole path string every time you want to call a control element.
     *
     * @param folderName one folder's name to push to the stack
     * @return
     * @see NDSGui#getFolder()
     */
    public FolderNode pushDropdown(String folderName){
        if(pathPrefix.size() >= stackSizeWarningLevel && !printedPushWarningAlready){
            LOGGER.warn("Too many calls to pushFolder() - stack size reached the warning limit of " + stackSizeWarningLevel +
                    ", possibly due to runaway recursion");
            printedPushWarningAlready = true;
        }
        String slashSafeFolderName = folderName;
        if(slashSafeFolderName.startsWith("/")){
            // remove leading slash
            slashSafeFolderName = slashSafeFolderName.substring(1);
        }
        if(slashSafeFolderName.endsWith("/") && !slashSafeFolderName.endsWith("\\/")){
            // remove trailing slash if un-escaped
            slashSafeFolderName = slashSafeFolderName.substring(0, slashSafeFolderName.length()-1);
        }
        pathPrefix.add(0, slashSafeFolderName);
        StringBuilder builder = new StringBuilder();
        for (int i = pathPrefix.size()-1; i >= 0; i--){
            builder.append(pathPrefix.get(i));
            if (i > 0)
                builder.append("/");
        }
        NodeTree.lazyInitDropdownPath(builder.toString());
        return (DropdownMenuNode) findNode(builder.toString());
    }

    /**
     * Pushes a folder name to the global path prefix stack.
     * Can be used multiple times in pairs just like pushMatrix() and popMatrix().
     * Removes leading and trailing slashes to enforce consistency, but allows slashes to appear either escaped or anywhere else inside the string.
     * Any GUI control element call will apply all the folders in the stack as a prefix to their own path parameter.
     * This is useful for not repeating the whole path string every time you want to call a control element.
     *
     * @param folderName one folder's name to push to the stack
     * @return
     * @see NDSGui#getFolder()
     */
    public FolderNode pushFolder(String folderName){
        if(pathPrefix.size() >= stackSizeWarningLevel && !printedPushWarningAlready){
            LOGGER.warn("Too many calls to pushFolder() - stack size reached the warning limit of " + stackSizeWarningLevel +
                    ", possibly due to runaway recursion");
            printedPushWarningAlready = true;
        }
        String slashSafeFolderName = folderName;
        if(slashSafeFolderName.startsWith("/")){
            // remove leading slash
            slashSafeFolderName = slashSafeFolderName.substring(1);
        }
        if(slashSafeFolderName.endsWith("/") && !slashSafeFolderName.endsWith("\\/")){
            // remove trailing slash if un-escaped
            slashSafeFolderName = slashSafeFolderName.substring(0, slashSafeFolderName.length()-1);
        }
        pathPrefix.add(0, slashSafeFolderName);
        StringBuilder builder = new StringBuilder();
        for (int i = pathPrefix.size()-1; i >= 0; i--){
            builder.append(pathPrefix.get(i));
            if (i > 0)
                builder.append("/");
        }
        NodeTree.lazyInitFolderPath(builder.toString());
        return (FolderNode) findNode(builder.toString());
    }

    /**
     * Pops the last pushed folder name from the global path prefix stack.
     * Can be used multiple times in pairs just like pushMatrix() and popMatrix().
     * Warns once when the stack is empty and popFolder() is attempted.
     * Any GUI control element call will apply all the folders in the stack as a prefix to their own path parameter.
     * This is useful for not repeating the whole path string every time you want to call a control element.
     */
    public void popFolder(){
        if(pathPrefix.isEmpty() && printedPopWarningAlready){
            LOGGER.warn("Too many calls to popFolder() - there is nothing to pop");
            printedPopWarningAlready = true;
        }
        if(!pathPrefix.isEmpty()){
            pathPrefix.remove(0);
        }
    }

    /**
     * Pops the last pushed folder name from the global path prefix stack.
     * Can be used multiple times in pairs just like pushMatrix() and popMatrix().
     * Warns once when the stack is empty and popFolder() is attempted.
     * Any GUI control element call will apply all the folders in the stack as a prefix to their own path parameter.
     * This is useful for not repeating the whole path string every time you want to call a control element.
     */
    public void popDropdown(){
        popFolder();
    }
}
