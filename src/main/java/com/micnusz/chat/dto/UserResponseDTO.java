package com.micnusz.chat.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;

    public UserResponseDTO() {}

    public UserResponseDTO(Long id, String username) {
        this.id = id;
        this.username = username;
    }
}
