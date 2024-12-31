package com.szadowsz.gui.input.keys;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class to represent a key combination used in the UI for some function.
 */
public class RKeyChord {
    /**
     * List of keys that make up this shortcut
     */
    private final int[] keys;

    public RKeyChord(int[] keys) {
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

    public boolean containedBy(Set<Integer> heldKeys) {
       return Arrays.stream(keys).allMatch(heldKeys::contains);
    }

    public boolean containedBy(Map<Integer, Long> heldKeys) {
        return containedBy(heldKeys.keySet());
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
        if (!(that instanceof RKeyChord)) {
            return false;
        }
        return Arrays.compare(this.keys, ((RKeyChord) that).keys) == 0;
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
