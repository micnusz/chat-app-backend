package com.micnusz.chat.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.micnusz.chat.dto.UserRequestDTO;
import com.micnusz.chat.dto.UserResponseDTO;
import com.micnusz.chat.exception.IncorrectPasswordException;
import com.micnusz.chat.exception.UserNotFoundException;
import com.micnusz.chat.exception.UsernameAlreadyExistsException;
import com.micnusz.chat.mapper.UserMapper;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // CREATE USER
    @Transactional
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
            throw new IncorrectPasswordException(rawPassword);
        }

        return userMapper.toDto(user);
    }

    // USED BY SPRING SECURITY (JWT FILTER)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("USER") // lub z encji jeśli masz role
                .build();
    }

    // RETURN CURRENT USER BASED ON CONTEXT
    public UserResponseDTO returnCurrentUser() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            String username = userDetails.getUsername();
            return returnUserByUsername(username);
        }

        if (principal instanceof String username) {
            return returnUserByUsername(username);
        }

        return null;
    }

    public UserResponseDTO returnUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        return userMapper.toDto(user);
    }
}
