package com.szadowsz.rotom4j.exception;

public class InvalidFileException extends NitroException {

	private static final long serialVersionUID = -8354901572139075536L;
	
	public InvalidFileException(String message){
		super(message);
	}
	public InvalidFileException(String message, Exception innerException){
		super(message, innerException);
	}

}
