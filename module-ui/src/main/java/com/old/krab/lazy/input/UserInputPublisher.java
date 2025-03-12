package com.old.krab.lazy.input;

import com.old.krab.lazy.input.keys.KeyChord;
import com.old.krab.lazy.input.keys.KeyListener;
import com.old.krab.lazy.input.keys.LazyKeyEvent;
import com.old.krab.lazy.input.mouse.LazyMouseEvent;
import com.old.krab.lazy.input.mouse.MouseListener;
import com.old.krab.lazy.stores.UndoRedoStore;

import java.awt.event.KeyEvent;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Internal LazyGui class used to register with PApplet user input events.
 * Must be public for PApplet to be able to reach it, but not meant to be used or even looked at by library users.
 */
public class UserInputPublisher implements KeyListener,MouseListener {
    public static boolean mouseFallsThroughThisFrame = false;
    private static UserInputPublisher singleton;
    private final CopyOnWriteArrayList<UserInputSubscriber> subscribers = new CopyOnWriteArrayList<>();

    public static void initSingleton() {
        if (singleton == null) {
            singleton = new UserInputPublisher();
        }
    }

    private UserInputPublisher() {
        registerListeners();
    }

    private void registerListeners() {
        InputWatcherBackend.registerKeyListener(this);
        InputWatcherBackend.registerMouseListener(this);

        // Undo
        InputWatcherBackend.registerKeyChord(new KeyChord(new int[]{KeyEvent.VK_CONTROL, KeyEvent.VK_Z}), e -> {
            UndoRedoStore.undo();
            e.consume();
        });
        // Redo
        InputWatcherBackend.registerKeyChord(new KeyChord(new int[]{KeyEvent.VK_CONTROL, KeyEvent.VK_Y}), e -> {
            UndoRedoStore.redo();
            e.consume();
        });
    }

    public static void subscribe(UserInputSubscriber subscriber) {
        singleton.subscribers.add(0, subscriber);
    }

    public static void setFocus(UserInputSubscriber subscriber){
        singleton.subscribers.remove(subscriber);
        singleton.subscribers.add(0, subscriber);
    }

    public void keyPressedEvent(LazyKeyEvent event) {
        for (UserInputSubscriber subscriber : subscribers) {
            subscriber.keyPressed(event);
            if (event.isConsumed()) {
                break;
            }
        }
    }

    public void keyReleasedEvent(LazyKeyEvent event) {
        for (UserInputSubscriber subscriber : subscribers) {
            subscriber.keyReleased(event);
            if (event.isConsumed()) {
                break;
            }
        }
    }

    public void mousePressedEvent(LazyMouseEvent event) {
         for (UserInputSubscriber subscriber : subscribers) {
            subscriber.mousePressed(event);
            if (event.isConsumed()) {
                break;
            }
        }
        mouseFallsThroughThisFrame = !event.isConsumed();
    }

    public void mouseReleasedEvent(LazyMouseEvent event) {
         for (UserInputSubscriber subscriber : subscribers) {
            subscriber.mouseReleased(event);
            if (event.isConsumed()) {
                break;
            }
        }
        mouseFallsThroughThisFrame = !event.isConsumed();
    }

    public void mouseMovedEvent(LazyMouseEvent event) {
        for (UserInputSubscriber subscriber : subscribers) {
            subscriber.mouseMoved(event);
            if (event.isConsumed()) {
                break;
            }
        }
        mouseFallsThroughThisFrame = !event.isConsumed();
    }

    public void mouseDraggedEvent(LazyMouseEvent event) {
         for (UserInputSubscriber subscriber : subscribers) {
            subscriber.mouseDragged(event);
            if (event.isConsumed()) {
                break;
            }
        }
        mouseFallsThroughThisFrame = !event.isConsumed();
    }

    public void mouseWheelEvent(LazyMouseEvent event) {
        for (UserInputSubscriber subscriber : subscribers) {
            subscriber.mouseWheelMoved(event);
            if (event.isConsumed()) {
                break;
            }
        }
        mouseFallsThroughThisFrame = !event.isConsumed();
    }
}
