package com.micnusz.chat.exception;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String username) {
    super("User '" + username + "' is not authorized to access this room.");
}

    
}
