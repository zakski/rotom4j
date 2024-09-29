package com.old.gui.input;

import com.old.gui.input.keys.RKeyEvent;
import com.old.gui.input.keys.RKeyListener;
import com.old.gui.input.mouse.RMouseEvent;
import com.old.gui.input.mouse.RMouseListener;

/**
 * Joint Key and Mouse Listener
 */
public interface RInputListener extends RKeyListener, RMouseListener {

    @Override
    default void keyPressed(RKeyEvent e) {
        // NOOP
    }

    @Override
    default void keyReleased(RKeyEvent e) {
        // NOOP
    }

    @Override
    default void keyTyped(RKeyEvent e) {
        // NOOP
    }

    @Override
    default void mouseClicked(RMouseEvent e) {
        // NOOP
    }

    @Override
    default void mouseDragged(RMouseEvent e) {
        // NOOP
    }

    @Override
    default void mouseEntered(RMouseEvent e) {
        // NOOP
    }

    @Override
    default void mouseExited(RMouseEvent e) {
        // NOOP
    }

    @Override
    default void mousePressed(RMouseEvent e) {
        // NOOP
    }

    @Override
    default void mouseReleased(RMouseEvent e) {
        // NOOP
    }

    @Override
    default void mouseMoved(RMouseEvent e) {
        // NOOP
    }

    @Override
    default void mouseWheel(RMouseEvent e) {
        // NOOP
    }
}
