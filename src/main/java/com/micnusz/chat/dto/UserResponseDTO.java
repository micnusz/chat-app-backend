package com.micnusz.chat.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private LocalDateTime createdAt;

    public UserResponseDTO() {}

    public UserResponseDTO(Long id, String username, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.createdAt = createdAt;
    }
}
