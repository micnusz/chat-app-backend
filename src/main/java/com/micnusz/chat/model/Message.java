package com.micnusz.chat.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Message {
    private String content;
    private String sender;
    private String roomId;
    private LocalDateTime timestamp;
}
