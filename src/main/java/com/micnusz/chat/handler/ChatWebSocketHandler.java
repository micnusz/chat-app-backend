package com.micnusz.chat.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.micnusz.chat.util.JwtUtil;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final JwtUtil jwtUtil;

    // inject JwtUtil via constructor
    public ChatWebSocketHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Extract JWT from query param: ws://localhost:8080/chat?token=...
        String query = session.getUri().getQuery(); // token=...
        String token = null;
        if (query != null && query.startsWith("token=")) {
            token = query.substring(6);
        }

        // Validate token
        if (token == null || !jwtUtil.validateToken(token)) {
            System.out.println("Invalid token, closing connection");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
            return;
        }

        // Save username in session attributes for later use
        String username = jwtUtil.extractUsername(token);
        session.getAttributes().put("username", username);

        sessions.add(session);
        System.out.println("Connected: " + session.getId() + " as " + username);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String username = (String) session.getAttributes().get("username");

        // Broadcast message with username prefix
        String payload = username + ": " + message.getPayload();
        System.out.println("Message: " + payload);

        synchronized (sessions) {
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(payload));
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
