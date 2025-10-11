package com.micnusz.chat.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.micnusz.chat.dto.AuthResponseDTO;
import com.micnusz.chat.dto.UserRequestDTO;
import com.micnusz.chat.mapper.UserMapper;
import com.micnusz.chat.model.User;
import com.micnusz.chat.service.UserService;
import com.micnusz.chat.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public AuthResponseDTO registerUser(@RequestBody @Valid UserRequestDTO request) {
        User user = userService.createUser(userMapper.toEntity(request));
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponseDTO(userMapper.toResponse(user), token);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody @Valid UserRequestDTO request) {
        User user = userService.loginUser(request.getUsername());
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponseDTO(userMapper.toResponse(user), token);
    }

}
