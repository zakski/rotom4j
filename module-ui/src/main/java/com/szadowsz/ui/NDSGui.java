package com.szadowsz.ui;

import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.input.InputWatcherBackend;
import com.szadowsz.ui.store.FontStore;
import com.szadowsz.ui.store.NormColorStore;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import static com.szadowsz.ui.constants.GlobalReferences.app;
import static processing.core.PConstants.P2D;
import static processing.core.PConstants.P3D;

/**
 * GUI System Representation
 */
public class NDSGui {

    // Sole Instance of this GUI object
    private static NDSGui singleton;

    // Backend to handle mouse/keyboard events
    private InputWatcherBackend inputHandler;

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
        NormColorStore.init();
        FontStore.updateFont();
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

    }
}
