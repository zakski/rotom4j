package com.szadowsz.nds4j.file.bin.core;

/**
 * Exception for out of bounds issues with binary data manipulation operations.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class OutOfBoundsException extends RuntimeException {

    public OutOfBoundsException() {
    }

    public OutOfBoundsException(String message) {
        super(message);
    }

    public OutOfBoundsException(String message, Throwable cause) {
        super(message, cause);
    }

    public OutOfBoundsException(Throwable cause) {
        super(cause);
    }

    public OutOfBoundsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
