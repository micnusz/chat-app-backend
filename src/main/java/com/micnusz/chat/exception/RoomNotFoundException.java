package com.micnusz.chat.exception;

public class RoomNotFoundException extends RuntimeException {
    
    public RoomNotFoundException(Long roomId) {
        super("Room with " + roomId + " not found");
    }
}
