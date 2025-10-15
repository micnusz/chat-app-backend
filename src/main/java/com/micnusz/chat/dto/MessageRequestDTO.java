package com.micnusz.chat.dto;

import lombok.Data;

@Data
public class MessageRequestDTO {
    private String username;  
    private String content;   
    private Long roomId;      
}
