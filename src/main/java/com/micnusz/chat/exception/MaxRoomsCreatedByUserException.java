package com.micnusz.chat.exception;

public class MaxRoomsCreatedByUserException extends RuntimeException {
    public MaxRoomsCreatedByUserException(String username, int maxRooms) {
        super("User '" + username + "' reached the maximum room limit: " + maxRooms);
    }
}
