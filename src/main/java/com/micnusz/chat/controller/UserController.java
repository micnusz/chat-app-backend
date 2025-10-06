package com.micnusz.chat.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.micnusz.chat.dto.UserRequestDTO;
import com.micnusz.chat.dto.UserResponseDTO;
import com.micnusz.chat.mapper.UserMapper;
import com.micnusz.chat.model.User;
import com.micnusz.chat.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins= "http://localhost:3000")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final UserMapper userMapper;


    @PostMapping("/register")
    public UserResponseDTO registerUser(@RequestBody @Valid UserRequestDTO request) {
        User user = userService.createUser(userMapper.toEntity(request));
        return userMapper.toResponse(user);
    }


    @PostMapping("/login")
    public UserResponseDTO login(@RequestBody @Valid UserRequestDTO request) {
        User user = userService.loginUser(request.getUsername());
        return userMapper.toResponse(user);
}
    
    

}
