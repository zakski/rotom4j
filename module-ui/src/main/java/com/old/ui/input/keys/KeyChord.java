package com.old.ui.input.keys;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Class to represent a key combination used in the UI for some function.
 *
 * @author Zakski : 06/09/2015.
 */
public class KeyChord {
    /**
     * List of keys that make up this shortcut
     */
    private final int[] keys;

    public KeyChord(int[] keys) {
        this.keys = Arrays.stream(keys).sorted().toArray();
    }

    /**
     * Method to check whether the key is in the chord.
     *
     * @param nkey the key to check
     * @return true if the key is in this chord, false otherwise
     */
    public boolean contains(int nkey) {
        return Arrays.binarySearch(this.keys, nkey) > 0;
    }

    /**
     * Method to get the hashcode of the keys.
     *
     * @return the hashcode.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(keys);
    }

    /**
     * Compares one chord key set to another.
     *
     * @param that the object to cmpare this one to.
     * @return true if they match, false otherwise.
     */
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof KeyChord)) {
            return false;
        }
        return Arrays.compare(this.keys, ((KeyChord) that).keys) == 0;
    }

    /**
     * Method to provide a string representation.
     *
     * @return string representation of all the keys.
     */
    @Override
    public String toString() {
        return Arrays.stream(this.keys)
                .mapToObj(code -> KeyEvent.getKeyText(code) + "(" + code + ")")
                .collect(Collectors.joining(", ", "[ ", " ]"));
    }
}
