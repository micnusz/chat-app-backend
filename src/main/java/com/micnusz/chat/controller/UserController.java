package com.micnusz.chat.controller;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.micnusz.chat.dto.UserRequestDTO;
import com.micnusz.chat.dto.UserResponseDTO;
import com.micnusz.chat.service.UserService;
import com.micnusz.chat.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "https://micnusz-chatapp.vercel.app", allowCredentials="true")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    private static final long ACCESS_TOKEN_EXP = 15 * 60 * 1000; // 5 min
    private static final long REFRESH_TOKEN_EXP = 7 * 24 * 60 * 60 * 1000; // 7 days

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody @Valid UserRequestDTO request, HttpServletResponse response) {
        UserResponseDTO user = userService.createUser(request);
        setTokens(response, user.getUsername());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> loginUser(@RequestBody @Valid UserRequestDTO request, HttpServletResponse response) {
        UserResponseDTO user = userService.loginUser(request.getUsername(), request.getPassword());
        if (user == null) return ResponseEntity.status(401).build();
        setTokens(response, user.getUsername());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserResponseDTO> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        String username = jwtUtil.extractUsername(refreshToken);
        setAccessToken(response, username);

        UserResponseDTO user = userService.returnUserByUsername(username);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        UserResponseDTO user = userService.returnCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(user);
    }

    private void setTokens(HttpServletResponse response, String username) {
        setAccessToken(response, username);
        setRefreshToken(response, username);
    }

    private void setAccessToken(HttpServletResponse response, String username) {
        String token = jwtUtil.generateToken(username, ACCESS_TOKEN_EXP);
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(true) // dev localhost
                .sameSite("None")
                .path("/")
                .domain("micnusz-chatapp.vercel.app")  
                .maxAge(ACCESS_TOKEN_EXP / 1000)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void setRefreshToken(HttpServletResponse response, String username) {
        String token = jwtUtil.generateToken(username, REFRESH_TOKEN_EXP);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true) // dev
                .sameSite("None")
                .path("/")
                .domain("micnusz-chatapp.vercel.app")  
                .maxAge(REFRESH_TOKEN_EXP / 1000)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
