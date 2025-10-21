package com.micnusz.chat.dto;

import lombok.Data;

@Data
public class MessageResponseDTO {
    private Long id;          
    private String username;  
    private String content;   
    private Long roomId;      
    private String timestamp; 

    public MessageResponseDTO() {}

    public MessageResponseDTO(Long id, String username, String content, Long roomId, String timestamp) {
        this.id = id;
        this.username = username;
        this.content = content;
        this.roomId = roomId;
        this.timestamp = timestamp;
    }
}
