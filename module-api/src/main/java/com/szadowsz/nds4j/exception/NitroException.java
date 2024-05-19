package com.szadowsz.nds4j.exception;

import java.io.IOException;

public class NitroException extends IOException {

    public NitroException(){
        super();
    }

    public NitroException(String message){
        super(message);
    }

    public NitroException(String message, Exception innerException){
        super(message, innerException);
    }
}
