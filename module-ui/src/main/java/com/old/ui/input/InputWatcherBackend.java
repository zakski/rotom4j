package com.old.ui.input;

import com.old.ui.input.keys.*;
import com.old.ui.input.mouse.GuiMouseEvent;
import com.old.ui.input.mouse.Mouse;
import com.old.ui.input.mouse.MouseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.concurrent.CopyOnWriteArrayList;

import static com.old.ui.constants.GlobalReferences.app;

/**
 * Class that watches and serves individual key and mouse state at runtime.
 * This is a user-facing utility meant to be used through its Input.java wrapper
 * The GUI uses a different paralell UserInputPublisher to pass the processing events into the windows and control elements.
 */
public class InputWatcherBackend implements KeyListener, MouseListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(InputWatcherBackend.class);

    public static boolean mouseFallsThroughThisFrame = false;

    private static InputWatcherBackend singleton;

    private final Keyboard keys;
    private final Mouse mouse;
    private final CopyOnWriteArrayList<UserInputSubscriber> subscribers = new CopyOnWriteArrayList<>();

    private boolean debugKeys = false;


    private InputWatcherBackend() {
        keys = new Keyboard(5000L,33L);
        mouse = new Mouse(app.mouseX,app.mouseY);
        registerKeyListener(this);
        registerMouseListener(this);
//        InputWatcherBackend.registerKeyChord(new KeyChord(new int[]{java.awt.event.KeyEvent.VK_CONTROL, java.awt.event.KeyEvent.VK_Z}), e -> {
//            UndoRedoStore.undo();
//            e.consume();
//        });
//        // Redo
//        InputWatcherBackend.registerKeyChord(new KeyChord(new int[]{java.awt.event.KeyEvent.VK_CONTROL, java.awt.event.KeyEvent.VK_Y}), e -> {
//            UndoRedoStore.redo();
//            e.consume();
//        });
    }

    public static InputWatcherBackend getInstance() {
        if (singleton == null) {
            singleton = new InputWatcherBackend();
        }
        return singleton;
    }

    private void recoverFromFocusLoss() {
        getInstance().keys.clearKeys();
    }

    public void registerKeyListener(KeyListener listener) {
        keys.attachHandler(listener);
    }

    public void registerMouseListener(MouseListener listener) {
        mouse.attachHandler(listener);
    }

    public static void registerKeyChord(KeyChord chord, ChordFunction func){
        getInstance().keys.mapShortcut(chord,func);
    }

    public static int[] getAllDownCodes() {
        return getInstance().keys.getDownedKeys();
    }

    public static void setDebugKeys(boolean shouldDebugKeys) {
        getInstance().debugKeys = shouldDebugKeys;
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

    public static void subscribe(UserInputSubscriber subscriber) {
        singleton.subscribers.add(0, subscriber);
    }

    public static void setFocus(UserInputSubscriber subscriber){
        singleton.subscribers.remove(subscriber);
        singleton.subscribers.add(0, subscriber);
    }

    private void postHandleKeyPresses() {
    }

    private void postHandleKeyReleases() {
    }

    public void keyEvent(KeyEvent event) {
        if (debugKeys) {
            LOGGER.info(keyEventString(event));
        }
        keys.keyEvent(event);
    }

    public void keyPressedEvent(GuiKeyEvent event) {
        for (UserInputSubscriber subscriber : subscribers) {
            subscriber.keyPressed(event);
            if (event.isConsumed()) {
                break;
            }
        }
    }

    public void keyReleasedEvent(GuiKeyEvent event) {
        for (UserInputSubscriber subscriber : subscribers) {
            subscriber.keyReleased(event);
            if (event.isConsumed()) {
                break;
            }
        }
    }

    public void mouseEvent(MouseEvent event) {
        mouse.mouseEvent(event);
    }

    public void mousePressedEvent(GuiMouseEvent event) {
        for (UserInputSubscriber subscriber : subscribers) {
            subscriber.mousePressed(event);
            if (event.isConsumed()) {
                break;
            }
        }
        mouseFallsThroughThisFrame = !event.isConsumed();
    }

    public void mouseReleasedEvent(GuiMouseEvent event) {
        for (UserInputSubscriber subscriber : subscribers) {
            subscriber.mouseReleased(event);
            if (event.isConsumed()) {
                break;
            }
        }
        mouseFallsThroughThisFrame = !event.isConsumed();
    }

    public void mouseMovedEvent(GuiMouseEvent event) {
        for (UserInputSubscriber subscriber : subscribers) {
            subscriber.mouseMoved(event);
            if (event.isConsumed()) {
                break;
            }
        }
        mouseFallsThroughThisFrame = !event.isConsumed();
    }

    public void mouseDraggedEvent(GuiMouseEvent event) {
        for (UserInputSubscriber subscriber : subscribers) {
            subscriber.mouseDragged(event);
            if (event.isConsumed()) {
                break;
            }
        }
        mouseFallsThroughThisFrame = !event.isConsumed();
    }

    public void mouseWheelEvent(GuiMouseEvent event) {
        for (UserInputSubscriber subscriber : subscribers) {
            subscriber.mouseWheelMoved(event);
            if (event.isConsumed()) {
                break;
            }
        }
        mouseFallsThroughThisFrame = !event.isConsumed();
    }

    public void post() {
        postHandleKeyPresses();
        postHandleKeyReleases();
        recoverFromFocusLoss();
    }

    public static KeyState getKeyStateByCode(int keyCode) {
        boolean pressed = getInstance().keys.isKeyPressed(keyCode);
        boolean held = getInstance().keys.isKeyHeld(keyCode);


        return new KeyState(held||pressed,pressed,getInstance().keys.isKeyReleased(keyCode));
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
