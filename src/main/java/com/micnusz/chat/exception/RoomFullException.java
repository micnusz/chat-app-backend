package com.micnusz.chat.exception;

public class RoomFullException extends RuntimeException {
    public RoomFullException(Long roomId) {
        super("Chat room with ID " + roomId + " is full. Cannot join.");
    }
}
