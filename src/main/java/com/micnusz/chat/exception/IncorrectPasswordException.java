package com.micnusz.chat.exception;

public class IncorrectPasswordException extends RuntimeException {
    
    public IncorrectPasswordException(String password) {
        super("Password " + password + " is incorrect.");
    }
}
