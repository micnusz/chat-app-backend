package com.micnusz.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private UserResponseDTO user;
    private String token;
}
