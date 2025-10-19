package com.micnusz.chat.exception;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("Username: " + username + " is already taken.");
    }
    
}
