package com.micnusz.chat.mapper;

import org.springframework.stereotype.Component;

import com.micnusz.chat.dto.UserRequestDTO;
import com.micnusz.chat.dto.UserResponseDTO;
import com.micnusz.chat.model.User;

@Component
public class UserMapper {


    public UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(user.getId(), user.getUsername());
    }

    public User toEntity(UserRequestDTO dto) {
        return new User(dto.getUsername()); 
    }
}
