package com.micnusz.chat.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;

    public User createUser(User user) {
        Optional<User> existing = userRepository.findByUsername(user.getUsername());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("User with the same username already exists.");
        }
        return userRepository.save(user);
    }

    public User loginUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with '" + username + "'username does not exists."));
    }

     public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    
    
}
