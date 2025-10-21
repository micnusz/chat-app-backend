package com.micnusz.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UserRequestDTO {

    @NotBlank(message = "Username cannot be empty.")
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 6, max = 30, message = "Password must be between 6 and 30 characters.")
    private String password;


    public UserRequestDTO() {}

    public UserRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
