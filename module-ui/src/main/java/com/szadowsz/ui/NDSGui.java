package com.szadowsz.ui;

import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.constants.theme.ThemeStore;
import com.szadowsz.ui.input.InputWatcherBackend;
import com.szadowsz.ui.node.AbstractNode;
import com.szadowsz.ui.node.NodeTree;
import com.szadowsz.ui.node.NodeType;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.store.FontStore;
import com.szadowsz.ui.store.JsonSaveStore;
import com.szadowsz.ui.store.LayoutStore;
import com.szadowsz.ui.store.NormColorStore;
import com.szadowsz.ui.utils.ContextLines;
import com.szadowsz.ui.window.SnapToGrid;
import com.szadowsz.ui.window.Window;
import com.szadowsz.ui.window.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

import static com.szadowsz.ui.constants.GlobalReferences.app;
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
                if(folder.window != null && folder.window.isBeingDraggedAround){
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
        app.registerMethod("draw", this);
        inputHandler = InputWatcherBackend.getInstance();
        app.registerMethod("keyEvent", this);
        app.registerMethod("mouseEvent",this);
        app.registerMethod("post", this);
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
     *
     */
    public void post() {
        inputHandler.post();
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
            root.window.windowSizeX = root.autosuggestWindowWidthForContents();
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
}
