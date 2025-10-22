package com.micnusz.chat.dto;

import java.time.format.DateTimeFormatter;

import com.micnusz.chat.model.ChatRoom;

import lombok.Data;

@Data
public class ChatRoomResponseDTO {
    private Long id;
    private String name;
    private boolean requiresPassword; 
    private String createdBy;
    private String createdAt;
    private String slug;

    public ChatRoomResponseDTO() {}

    public ChatRoomResponseDTO(Long id, String name, boolean requiresPassword, String createdBy, String createdAt, String slug) {
        this.id = id;
        this.name = name;
        this.requiresPassword = requiresPassword;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.slug = slug;
    }

    public ChatRoomResponseDTO(ChatRoom room) {
        this.id = room.getId();
        this.name = room.getName();
        this.requiresPassword = room.getPassword() != null && !room.getPassword().isEmpty();
        this.createdBy = room.getCreatedBy() != null ? room.getCreatedBy().getUsername() : null;
        this.createdAt = room.getCreatedAt() != null
                ? room.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;
    }
}