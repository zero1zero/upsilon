package com.vevo.upsilon.except;

public class UpsilonInitializationException extends RuntimeException {

    public UpsilonInitializationException(String message) {
        super(message);
    }

    public UpsilonInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
