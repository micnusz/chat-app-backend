package com.micnusz.chat.dto;

import lombok.Data;

@Data
public class MessageResponseDTO {
    private Long id;          // ID wiadomości
    private String username;  // autor
    private String content;   // treść
    private Long roomId;      // pokój
    private String timestamp; // czas wysłania

    public MessageResponseDTO() {}

    public MessageResponseDTO(Long id, String username, String content, Long roomId, String timestamp) {
        this.id = id;
        this.username = username;
        this.content = content;
        this.roomId = roomId;
        this.timestamp = timestamp;
    }
}
