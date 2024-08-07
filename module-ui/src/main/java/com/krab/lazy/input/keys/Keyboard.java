package com.krab.lazy.input.keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * General Key handling
 *
 * @author Zakski : 06/09/2015.
 */
public class Keyboard {

    /**
     * Time interval constant, anything pressed for less than this is not considered held.
     */
    protected long holdTime;
    /**
     * Time interval constant, anything released for less than this is still considered released.
     */
    protected long releaseTime;

    protected static final Logger LOGGER = LoggerFactory.getLogger(Keyboard.class);

    /**
     * List to store keys as they are pressed and released, as well as how long they have been held for.
     */
    protected Map<Integer, Long> keys = new ConcurrentHashMap<>();
    protected Map<Integer, Long> releasedKeys = new ConcurrentHashMap<>();

    /**
     * List to store current keyboard shortcuts.
     */
    protected Map<KeyChord, ChordFunction> chords = new ConcurrentHashMap<>();

    protected List<KeyListener> handlers = new CopyOnWriteArrayList<>();

    public Keyboard(long timeBeforeConsideredHeld, long timeUntilNotReleased) {
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
        clearKeys(); // clear them so we don't inadvertently trigger a new shortcut.
        chords.putAll(shortcuts);
        LOGGER.info("Applied new keyboard shortcuts");
    }

    /**
     * Method to register a key press
     *
     * @param code - code of the key to be added
     * @return true if the key has been added, false otherwise
     */
    public boolean pressKey(int code)  {
        if (!keys.containsKey(code)) {
            LOGGER.info("Pressed key: {}", KeyEvent.getKeyText(code));
            keys.put(code, System.currentTimeMillis());
            releasedKeys.remove(code);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method to release a key
     *
     * @param code - code of the key to be added
     * @return true if the key has been removed, false otherwise
     */
    public boolean releaseKey(int code) {
        if (keys.containsKey(code)) {
            long time = keys.remove(code);
            releasedKeys.put(code, System.currentTimeMillis());
            LOGGER.info("Released key: {}, Held for {}", KeyEvent.getKeyText(code),time);
            return true;
        } else {
            LOGGER.warn("Couldn't Release key: {}", KeyEvent.getKeyText(code));
            return false;
        }
    }

    /**
     * Clears all currently held keys.
     */
    public void clearKeys() {
        keys.clear();
        releasedKeys.clear();
    }

    /**
     * Method to process KeyEvents as they happen
     *
     * @param event - the latest event to process
     */
    public void keyEvent(processing.event.KeyEvent event)  {
        LazyKeyEvent e = new LazyKeyEvent(event);
        if (event.getAction() == processing.event.KeyEvent.PRESS) {
            pressKey(event.getKeyCode());
            ChordFunction chordFunc = chords.get(new KeyChord(getDownedKeys()));
            if (chordFunc != null) {
                chordFunc.chordShortcut(e);
            }
           handlers.forEach(l -> l.keyPressedEvent(e));
        } else if (event.getAction() == processing.event.KeyEvent.RELEASE) {
            releaseKey(event.getKeyCode());
            handlers.forEach(l -> l.keyReleasedEvent(e));
        }
    }

    public void attachHandler(KeyListener handle) {
        handlers.add(handle);
    }

    public void detachHandler(KeyListener handle){
        handlers.remove(handle);
    }
}
