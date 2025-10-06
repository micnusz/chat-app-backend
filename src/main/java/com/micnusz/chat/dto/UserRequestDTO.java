package com.micnusz.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UserRequestDTO {

    @NotBlank(message = "Username cannot be empty.")
    @Size(min = 3, max = 20)
    private String username;

    public UserRequestDTO() {}

    public UserRequestDTO(String username) {
        this.username = username;
    }

}
