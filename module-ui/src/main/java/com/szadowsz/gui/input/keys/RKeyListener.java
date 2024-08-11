package com.szadowsz.gui.input.keys;

public interface RKeyListener {

    /**
     * Event when a Key is Pressed
     *
     * @param e event data
     */
    void keyPressed(RKeyEvent e);

    /**
     * Event when a Key is Released
     *
     *
     * @param e event data
     */
    void keyReleased(RKeyEvent e);

    /**
     * Event when a Key is Typed
     * <p>
     * "Key typed" events are higher-level and generally do not depend on the platform or keyboard layout. They are
     * generated when a Unicode character is entered, and are the preferred way to find out about character input. In
     * the simplest case, a key typed event is produced by a single key press (e.g., 'a').
     * <p>
     * Often, however, characters are produced by series of key presses (e.g., 'shift' + 'a'), and the mapping from key
     * pressed events to key typed events may be many-to-one or many-to-many.
     * <p>
     * Key releases are not usually necessary to generate a key typed event, but there are some cases where the key
     * typed event is not generated until a key is released (e.g., entering ASCII sequences via the Alt-Numpad method in
     * Windows).
     * <p>
     * No key typed events are generated for keys that don't generate Unicode characters (e.g., action keys, modifier
     * keys, etc.).
     *
     * @param e event data
     */
    void keyTyped(RKeyEvent e);
}
