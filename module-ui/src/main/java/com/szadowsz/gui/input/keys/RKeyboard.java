package com.szadowsz.gui.input.keys;

import com.szadowsz.gui.input.RInputListener;
import com.szadowsz.ui.input.keys.ChordFunction;
import com.szadowsz.ui.input.keys.KeyChord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.event.KeyEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RKeyboard {
    private static final Logger LOGGER = LoggerFactory.getLogger(RKeyboard.class);

    /**
     * List to store keys as they are pressed and released, as well as how long they have been held for.
     */
    private Map<Integer, Long> keys = new ConcurrentHashMap<>();
    private Map<Integer, Long> releasedKeys = new ConcurrentHashMap<>();

    /**
     * Time interval constant, anything pressed for less than this is not considered held.
     */
    private long holdTime;
    /**
     * Time interval constant, anything released for less than this is still considered released.
     */
    private long releaseTime;

    /**
     * List to store current keyboard shortcuts.
     */
    private Map<KeyChord, ChordFunction> chords = new ConcurrentHashMap<>();

    private final List<RKeyListener> handlers = new CopyOnWriteArrayList<>();

    public RKeyboard(long timeBeforeConsideredHeld, long timeUntilNotReleased) {
        holdTime = timeBeforeConsideredHeld;
        releaseTime = timeUntilNotReleased;
    }

    /**
     * Method to check for a key press
     *
     * @param code - code of the key to be checked
     * @return true if the key has been pressed, false otherwise
     */
    public boolean isKeyPressed(int code) {
        return keys.containsKey(code);
    }

    /**
     * Method to check if a key is held
     *
     * @param code - code of the key to be checked
     * @return true if the key is held, false otherwise
     */
    public boolean isKeyHeld(int code) {
        return keys.containsKey(code) && (System.currentTimeMillis() - keys.get(code) >= holdTime);
    }

    /**
     * Method to check if a key is released
     *
     * @param code - code of the key to be checked
     * @return true if the key is held, false otherwise
     */
    public boolean isKeyReleased(int code) {
        return releasedKeys.containsKey(code) && (System.currentTimeMillis() - releasedKeys.get(code) < releaseTime);
    }

    /**
     * Method to get the pressed keys.
     *
     * @return a list of keys currently pressed.
     */
    public int[] getPressedKeys() {
        return keys.entrySet().stream().filter(key -> System.currentTimeMillis() - key.getValue() < holdTime).mapToInt(Map.Entry::getKey).sorted().toArray();
    }

    /**
     * Method to get all held keys.
     *
     * @return a list of keys currently held down.
     */
    public int[] getHeldKeys() {
        return keys.entrySet().stream().filter(key -> System.currentTimeMillis() - key.getValue() >= holdTime).mapToInt(Map.Entry::getKey).sorted().toArray();
    }

    /**
     * Method to get all touched keys.
     *
     * @return a list of all keys currently down.
     */
    public int[] getDownedKeys() {
        return keys.keySet().stream().mapToInt(i -> i).sorted().toArray();
    }

    /**
     * Method to add a new shortcut to the layout
     *
     * @param chord    keys of the shortcut
     * @param function what the shortcut does
     */
    public void mapShortcut(KeyChord chord, ChordFunction function) {
        chords.put(chord, function);
        LOGGER.info("Added a new keyboard shortcut: " + chord.toString());
    }

    /**
     * Method to apply a new set of shortcuts
     *
     * @param shortcuts the new shortcut layout to apply
     */
    public void mapShortcuts(Map<KeyChord, ChordFunction> shortcuts) {
        clear(); // clear them so we don't inadvertently trigger a new shortcut.
        chords.putAll(shortcuts);
        LOGGER.info("Applied new keyboard shortcuts");
    }

    private void pressKey(RKeyEvent e) {
        if (!keys.containsKey(e.getKeyCode())) {
            LOGGER.info("Pressed key: {}", java.awt.event.KeyEvent.getKeyText(e.getKeyCode()));
            keys.put(e.getKeyCode(), System.currentTimeMillis());
            releasedKeys.remove(e.getKeyCode());
       }
        for (RKeyListener subscriber : handlers) {
            subscriber.keyPressed(e);
            if (e.isConsumed()) {
                break;
            }
        }
    }

    private void releaseKey(RKeyEvent e) {
        if (keys.containsKey(e.getKeyCode())) {
            long time = keys.remove(e.getKeyCode());
            releasedKeys.put(e.getKeyCode(), System.currentTimeMillis());
            LOGGER.info("Released key: {}, Held for {}", java.awt.event.KeyEvent.getKeyText(e.getKeyCode()),time);
        } else {
            LOGGER.warn("Couldn't Release key: {}", java.awt.event.KeyEvent.getKeyText(e.getKeyCode()));
         }
        for (RKeyListener subscriber : handlers) {
            subscriber.keyReleased(e);
            if (e.isConsumed()) {
                break;
            }
        }
    }

    private void typedKey(RKeyEvent e) {
        for (RKeyListener subscriber : handlers) {
            subscriber.keyTyped(e);
            if (e.isConsumed()) {
                break;
            }
        }
    }


    /**
     * Process the KeyEvent And notify aLl Subscribers
     *
     * @param event event to process
     */
    public void keyEvent(KeyEvent event)  {
        RKeyEvent e = new RKeyEvent(event);
        LOGGER.debug("KeyEvent: {}", e); // TODO Consider Need to Note Window here
        // Call reaction methods contextually
        switch (event.getAction()){
            case KeyEvent.PRESS -> pressKey(e);
            case KeyEvent.RELEASE -> releaseKey(e);
            case KeyEvent.TYPE -> typedKey(e);
        }
    }


    public void setFocus(RKeyListener subscriber) {
        handlers.remove(subscriber);
        handlers.addFirst(subscriber);
    }

    public void clear() {

    }

    public void subscribe(RKeyListener subscriber) {
        handlers.addFirst(subscriber);
    }
}
