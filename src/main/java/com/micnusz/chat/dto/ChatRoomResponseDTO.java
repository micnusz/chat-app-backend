package com.micnusz.chat.dto;

import java.time.format.DateTimeFormatter;

import com.micnusz.chat.model.ChatRoom;

import lombok.Data;

@Data
public class ChatRoomResponseDTO {
    private Long id;
    private String name;
    private String createdBy;
    private String createdAt;
    
    public ChatRoomResponseDTO() {}

    public ChatRoomResponseDTO(Long id, String name, String createdBy, String createdAt) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public ChatRoomResponseDTO(ChatRoom room) {
        this.id = room.getId();
        this.name = room.getName();
        this.createdBy = room.getCreatedBy() != null ? room.getCreatedBy().getUsername() : null;
        this.createdAt = room.getCreatedAt() != null
                ? room.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;
    }
}
