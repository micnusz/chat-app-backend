package com.micnusz.chat.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.MessagesRepository;
import com.micnusz.chat.repository.UserRepository;
import com.micnusz.chat.util.JwtUtil;





public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final JwtUtil jwtUtil;
    private final MessagesRepository messagesRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatWebSocketHandler(JwtUtil jwtUtil, MessagesRepository messagesRepository, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.messagesRepository = messagesRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery(); // np. token=...
        String token = null;
        if (query != null && query.startsWith("token=")) {
            token = query.substring(6);
        }

        if (token == null || !jwtUtil.validateToken(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
            return;
        }

        String username = jwtUtil.extractUsername(token);
        session.getAttributes().put("username", username);

        sessions.add(session);
        System.out.println("Connected: " + session.getId() + " as " + username);
    }

    @Override
protected void handleTextMessage(WebSocketSession session, TextMessage wsMessage) throws Exception {
    String username = (String) session.getAttributes().get("username");

    User sender = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Tworzymy encję z czystą treścią wiadomości
    com.micnusz.chat.model.Message msg = new com.micnusz.chat.model.Message(
            sender,          // powiązany użytkownik
            wsMessage.getPayload(), // tylko tekst wiadomości
            "default-room"   // roomId
    );

    messagesRepository.save(msg); // zapis do bazy

    // JSON dla frontendu
    Map<String, String> payload = new HashMap<>();
    payload.put("username", sender.getUsername());
    payload.put("message", wsMessage.getPayload()); // tylko tekst
    String json = objectMapper.writeValueAsString(payload);

    synchronized (sessions) {
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }
}


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        String username = (String) session.getAttributes().get("username");
        System.out.println("Disconnected: " + session.getId() + " (" + username + ")");
    }
}
