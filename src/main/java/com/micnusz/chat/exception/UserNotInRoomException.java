package com.micnusz.chat.exception;

public class UserNotInRoomException extends RuntimeException {
    public UserNotInRoomException(String username, Long roomId) {
        super("User with " + username + " username isn't in the room with " + roomId  + " id");
    }
}
