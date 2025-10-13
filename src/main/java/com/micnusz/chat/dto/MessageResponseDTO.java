package com.micnusz.chat.dto;

import lombok.Data;

@Data
public class MessageResponseDTO {

    private String username;
    private String message;
    private String roomId;
    private String timestamp;

     public MessageResponseDTO() {} 

    public MessageResponseDTO(String username, String message, String roomId, String timestamp) {
        this.username = username;
        this.message = message;
        this.roomId = roomId;
        this.timestamp = timestamp;
    }
}
