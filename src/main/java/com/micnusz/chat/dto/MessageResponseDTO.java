package com.micnusz.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    private Long id;         
    private String username;  
    private String content;   
    private Long roomId;      
    private String timestamp; 
}
