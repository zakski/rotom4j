package com.szadowsz.gui.config;

/**
 * Storage for Keyboard Buffer Delay Values
 */
public class RDelayStore {
    private static int keyboardBufferDelayMillis = 500;

    /**
     * Get the delay value before the keyboard buffer should be processed
     *
     * @return delay value in milliseconds
     */
    public static int getKeyboardBufferDelayMs() {
        return keyboardBufferDelayMillis;
    }

    /**
     * Set the delay value before the keyboard buffer should be processed
     *
     * @param keyboardBufferDelayMillis delay value in milliseconds
     */
    public static void setKeyboardBufferDelayMillis(int keyboardBufferDelayMillis) {
        RDelayStore.keyboardBufferDelayMillis = keyboardBufferDelayMillis;
    }
}
