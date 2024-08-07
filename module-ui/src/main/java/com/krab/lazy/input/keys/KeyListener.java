package com.krab.lazy.input.keys;

/**
 * Listener Trait compatible with the Keyboard and capable of enhancing controller that need to respond to key input.
 */
public interface KeyListener {

  /**
   * Method to handle a Key Event in a responsible manner.
   *
   * @param event the Processing Key Event that has occurred.
   */
  void keyPressedEvent(LazyKeyEvent e);

  void keyReleasedEvent(LazyKeyEvent e);
}