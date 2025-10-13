package com.micnusz.chat.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micnusz.chat.dto.MessageRequestDTO;
import com.micnusz.chat.dto.MessageResponseDTO;
import com.micnusz.chat.mapper.MessagesMapper;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.UserRepository;
import com.micnusz.chat.service.MessagesService;
import com.micnusz.chat.util.JwtUtil;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final JwtUtil jwtUtil;
    private final MessagesService messagesService;
    private final UserRepository userRepository;
    private final MessagesMapper messagesMapper; // dodany mapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Konstruktor teraz przyjmuje mapper
    public ChatWebSocketHandler(JwtUtil jwtUtil, MessagesService messagesService,
                                UserRepository userRepository, MessagesMapper messagesMapper) {
        this.jwtUtil = jwtUtil;
        this.messagesService = messagesService;
        this.userRepository = userRepository;
        this.messagesMapper = messagesMapper;
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

        // Parsujemy JSON przychodzący od frontendu do DTO
        MessageRequestDTO requestDTO = objectMapper.readValue(wsMessage.getPayload(), MessageRequestDTO.class);

        // Zapisujemy wiadomość do bazy przez serwis, teraz z użyciem DTO
        var savedMessage = messagesService.saveMessage(sender, requestDTO);

        // Mapujemy encję na DTO odpowiedzi
        MessageResponseDTO responseDTO = messagesMapper.toDTO(savedMessage);

        String json = objectMapper.writeValueAsString(responseDTO);

        // Wysyłamy do wszystkich połączonych klientów
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
