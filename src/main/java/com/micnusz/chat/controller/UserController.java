package com.micnusz.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.micnusz.chat.dto.AuthResponseDTO;
import com.micnusz.chat.dto.UserRequestDTO;
import com.micnusz.chat.dto.UserResponseDTO;
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
    private final JwtUtil jwtUtil;

    // REGISTERING USER
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registerUser(@RequestBody @Valid UserRequestDTO request) {
        UserResponseDTO response = userService.createUser(request);
        String token = jwtUtil.generateToken(response.getUsername());
        return ResponseEntity.ok(new AuthResponseDTO(response, token));
    }

    // LOGIN USER
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(@RequestBody @Valid UserRequestDTO request) {
        UserResponseDTO response = userService.loginUser(request.getUsername(), request.getPassword());
        String token = jwtUtil.generateToken(response.getUsername());
        return ResponseEntity.ok(new AuthResponseDTO(response, token));

    }
    
}


