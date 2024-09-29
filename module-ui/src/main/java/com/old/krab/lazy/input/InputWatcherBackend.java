package com.old.krab.lazy.input;

import com.old.krab.lazy.KeyState;
import com.old.krab.lazy.input.keys.ChordFunction;
import com.old.krab.lazy.input.keys.KeyChord;
import com.old.krab.lazy.input.keys.KeyListener;
import com.old.krab.lazy.input.keys.Keyboard;
import com.old.krab.lazy.input.mouse.Mouse;
import com.old.krab.lazy.input.mouse.MouseListener;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import static com.old.krab.lazy.stores.GlobalReferences.app;
import static processing.core.PApplet.println;

/**
 * Class that watches and serves individual key and mouse state at runtime.
 * This is a user-facing utility meant to be used through its Input.java wrapper
 * The GUI uses a different paralell UserInputPublisher to pass the processing events into the windows and control elements.
 */
public class InputWatcherBackend {

    private static InputWatcherBackend singleton;
    private boolean debugKeys = false;

    private Keyboard keys;
    private Mouse mouse;

    private InputWatcherBackend() {
        registerListeners();
        keys = new Keyboard(5000L,33L);
        mouse = new Mouse(app.mouseX,app.mouseY);
    }

    public static InputWatcherBackend getInstance() {
        if (singleton == null) {
            singleton = new InputWatcherBackend();
        }
        return singleton;
    }




    private void registerListeners() {
        // the reference passed here is the only reason to have this be a singleton instance rather than a fully static class with no instance
        app.registerMethod("keyEvent", this);
        app.registerMethod("mouseEvent",this);
        app.registerMethod("post", this);
    }

    public static void registerKeyListener(KeyListener listener) {
        getInstance().keys.attachHandler(listener);
    }

    public static void registerMouseListener(MouseListener listener) {
        getInstance().mouse.attachHandler(listener);
    }

    public static void registerKeyChord(KeyChord chord, ChordFunction func){
        getInstance().keys.mapShortcut(chord,func);
    }

    @SuppressWarnings("unused")
    public void post() {
        postHandleKeyPresses();
        postHandleKeyReleases();
        recoverFromFocusLoss();
    }

    private void recoverFromFocusLoss() {
        getInstance().keys.clearKeys();
    }

    public static PVector mousePos(){
        return getInstance().mouse.getCurrent();
    }

    public static PVector mousePosLastFrame(){
        return getInstance().mouse.getPrevious();
    }

    public static PVector mouseDelta() {
        return getInstance().mouse.getDelta();
    }

    @SuppressWarnings("unused")
    public void keyEvent(KeyEvent event) {
        if (debugKeys) {
            println(keyEventString(event));
        }
        keys.keyEvent(event);
    }

    public void mouseEvent(MouseEvent event) {
        mouse.mouseEvent(event);
    }

    public static KeyState getKeyStateByCode(int keyCode) {
        boolean pressed = getInstance().keys.isKeyPressed(keyCode);
        boolean held = getInstance().keys.isKeyHeld(keyCode);


        return new KeyState(held||pressed,pressed,getInstance().keys.isKeyReleased(keyCode));
    }

    private void postHandleKeyPresses() {
    }

    private void postHandleKeyReleases() {
    }

    public static int[] getAllDownCodes() {
        return getInstance().keys.getDownedKeys();
    }

    public static void setDebugKeys(boolean shouldDebugKeys) {
        getInstance().debugKeys = shouldDebugKeys;
    }

    String keyEventString(KeyEvent event) {
        String actionString;
        switch (event.getAction()) {
            case KeyEvent.PRESS:
                actionString = "PRESS";
                break;
            case KeyEvent.TYPE:
                actionString = "TYPE";
                break;
            case KeyEvent.RELEASE:
                actionString = "RELEASE";
                break;
            default:
                actionString = "UNKNOWN (" + event.getAction() + ")";
        }
        return String.format("<KeyEvent | action: %s| key: %s| code: %s>",
                padString(actionString, "RELEASE ".length()),
                padString(String.valueOf(event.getKey()), 2),
                padString(String.valueOf(event.getKeyCode()), 3));
    }

    String padString(String toPad, int count) {
        StringBuilder sb = new StringBuilder(toPad);
        for (int i = sb.length(); i < count; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
