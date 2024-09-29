package com.old.ui.input.keys;

/**
 * Listener Trait compatible with the Keyboard and capable of enhancing controller that need to respond to key input.
 */
public interface KeyListener {

  /**
   * Method to handle a Key Pressed Event in a responsible manner.
   *
   * @param event the Processing Key Event that has occurred.
   */
  void keyPressedEvent(GuiKeyEvent event);

  /**
   * Method to handle a Key Released Event in a responsible manner.
   *
   * @param event the Processing Key Event that has occurred.
   */
  void keyReleasedEvent(GuiKeyEvent event);
}