package com.szadowsz.gui.input.keys;

import com.szadowsz.gui.RotomGui;
import processing.event.KeyEvent;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class RKeyEvent {
    //private static final Logger LOGGER = LoggerFactory.getLogger(RKeyEvent.class);

    private final RotomGui gui;

    // Data About the Key Being Pressed
    private final char key;
    private final int keyCode;


    private final ConcurrentHashMap<Integer, Long> heldKeys;
    // Data About Metakeys
    private final boolean isShiftDown;
    private final boolean isControlDown;
    private final boolean isAltDown;
    private final int modifiers;

    // If A Component has consumed the event
    private boolean consumed = false;

    public RKeyEvent(RotomGui gui, ConcurrentHashMap<Integer, Long> heldKeys, KeyEvent e) {
        this.gui = gui;
        this.heldKeys = heldKeys;
        this.isShiftDown = e.isShiftDown();
        this.isControlDown = e.isControlDown();
        this.isAltDown = e.isAltDown();
        this.keyCode = e.getKeyCode();
        this.key = e.getKey();
        this.modifiers = e.getModifiers();
    }

    /**
     * Get Character Representation of the Event Key
     *
     * @return char of the key the event is for
     */
    public char getKey() {
        return key;
    }

    /**
     * Get KeyCode Representation of the Event Key
     *
     * @return int of the key the event is for
     */
    public int getKeyCode() {
        return keyCode;
    }

    /**
     * Check if Alt MetaKey is Down/Pressed
     *
     * @return true if down, false otherwise
     */
    public boolean isAltDown() {
        return isAltDown;
    }


    /**
     * Is Event Consumed
     */
    public boolean isConsumed() {
        return consumed;
    }


    /**
     * Check if Ctrl MetaKey is Down/Pressed
     *
     * @return true if down, false otherwise
     */
    public boolean isControlDown() {
        return isControlDown;
    }


    /**
     * Check if Shift MetaKey is Down/Pressed
     *
     * @return true if down, false otherwise
     */
    public boolean isShiftDown() {
        return isShiftDown;
    }

    public boolean hasChord(RKeyChord chord){
        return chord.containedBy(heldKeys);
    }

    public boolean hasModifiers() {
        return this.modifiers != 0;
    }


    /**
     * Mark the Event As Consumed
     */
    public void consume() {
        consumed = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RKeyEvent rKeyEvent)) return false;
        return key == rKeyEvent.key &&
                keyCode == rKeyEvent.keyCode &&
                isShiftDown == rKeyEvent.isShiftDown &&
                isControlDown == rKeyEvent.isControlDown &&
                isAltDown == rKeyEvent.isAltDown &&
                consumed == rKeyEvent.consumed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, keyCode, isShiftDown, isControlDown, isAltDown, consumed);
    }

    @Override
    public String toString() {
        return "RKeyEvent{" +
                "key=" + key +
                ", keyCode=" + keyCode +
                ", isShiftDown=" + isShiftDown +
                ", isControlDown=" + isControlDown +
                ", isAltDown=" + isAltDown +
                ", consumed=" + consumed +
                '}';
    }
}
