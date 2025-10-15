package com.micnusz.chat.dto;

import lombok.Data;

@Data
public class ChatRoomResponseDTO {
    private Long id;
    private String name;
    private String createdBy;
    private String createdAt; 
}
