package com.szadowsz.gui.config;

public class RDelayStore {
    private static int keyboardBufferDelayMillis = 500;

    public static int getKeyboardBufferDelayMillis() {
        return keyboardBufferDelayMillis;
    }

    public static void setKeyboardBufferDelayMillis(int keyboardBufferDelayMillis) {
        RDelayStore.keyboardBufferDelayMillis = keyboardBufferDelayMillis;
    }
}
