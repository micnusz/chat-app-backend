package com.micnusz.chat.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.micnusz.chat.dto.UserRequestDTO;
import com.micnusz.chat.dto.UserResponseDTO;
import com.micnusz.chat.exception.UserNotFoundException;
import com.micnusz.chat.exception.UsernameAlreadyExistsException;
import com.micnusz.chat.mapper.UserMapper;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // CREATING USER
    public UserResponseDTO createUser(UserRequestDTO request) {
        Optional<User> existing = userRepository.findByUsername(request.getUsername());
        if (existing.isPresent()) {
            throw new UsernameAlreadyExistsException(request.getUsername());
        }
        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    // LOGIN USER
    public UserResponseDTO loginUser(String username) {
       User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

       return userMapper.toDto(user);
    }    
    
}
