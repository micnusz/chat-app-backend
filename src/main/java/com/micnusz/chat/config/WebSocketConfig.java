package com.micnusz.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.micnusz.chat.handler.ChatWebSocketHandler;
import com.micnusz.chat.repository.MessagesRepository;
import com.micnusz.chat.repository.UserRepository;
import com.micnusz.chat.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final JwtUtil jwtUtil;
    private final MessagesRepository messagesRepository;
    private final UserRepository userRepository;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(
                new ChatWebSocketHandler(jwtUtil, messagesRepository, userRepository),
                "/chat"
        ).setAllowedOrigins("*");
    }
}

