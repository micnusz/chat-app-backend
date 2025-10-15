package com.micnusz.chat.dto;

import lombok.Data;

@Data
public class ChatRoomRequestDTO {
    private String name;
    private String password; 
    private String createdByUsername; 
}
