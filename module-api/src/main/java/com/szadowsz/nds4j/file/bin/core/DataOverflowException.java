package com.szadowsz.nds4j.file.bin.core;

/**
 * Exception for overflow situation where more data is inserted/added than it is
 * allowed to handle.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class DataOverflowException extends RuntimeException {

    public DataOverflowException() {
    }

    public DataOverflowException(String message) {
        super(message);
    }

    public DataOverflowException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataOverflowException(Throwable cause) {
        super(cause);
    }

    public DataOverflowException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
