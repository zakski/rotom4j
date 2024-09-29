package com.old.gui.exception;

/**
 * An exception thrown during the operation or creation of Windows
 */
public class RWindowException extends RuntimeException {

    /**
     * Standard Constructor
     *
     * @param message error message
     */
    public RWindowException(String message) {
        super(message);
    }
}
