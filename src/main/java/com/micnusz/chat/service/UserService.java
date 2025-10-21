package com.micnusz.chat.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // CREATING USER
    public UserResponseDTO createUser(UserRequestDTO request) {
        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> { throw new UsernameAlreadyExistsException(request.getUsername()); });

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    // LOGIN USER
    public UserResponseDTO loginUser(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return userMapper.toDto(user);
    }    
    
}
