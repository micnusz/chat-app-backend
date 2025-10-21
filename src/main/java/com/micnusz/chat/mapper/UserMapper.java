package com.micnusz.chat.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.micnusz.chat.dto.UserRequestDTO;
import com.micnusz.chat.dto.UserResponseDTO;
import com.micnusz.chat.model.User;

@Component
public class UserMapper {

    public User toEntity(UserRequestDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }


    public UserResponseDTO toDto(User user) {
        return new UserResponseDTO(user.getId(), user.getUsername(),
                user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now());
    }

}
