package com.old.ui.input.keys;

/**
 * Interface to represent what should happen when the chord is activated.
 *
 * @author Zakski : 06/09/2015.
 */
public interface ChordFunction {

    /**
     * Method called when a chord is activated.
     */
    public void chordShortcut(GuiKeyEvent e);
}
