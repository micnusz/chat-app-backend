package com.micnusz.chat.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micnusz.chat.dto.MessageRequestDTO;
import com.micnusz.chat.dto.MessageResponseDTO;
import com.micnusz.chat.exception.RoomNotFoundException;
import com.micnusz.chat.exception.UserNotFoundException;
import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.ChatRoomRepository;
import com.micnusz.chat.repository.UserRepository;
import com.micnusz.chat.service.MessagesService;
import com.micnusz.chat.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final JwtUtil jwtUtil;
    private final MessagesService messagesService;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            String path = session.getUri().getPath();
            String[] segments = path.split("/");
            Long roomId = Long.parseLong(segments[segments.length - 1]);

            String token = null;
            String query = session.getUri().getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("token=")) {
                        token = param.substring(6);
                        break;
                    }
                }
            }

            if (token == null || !jwtUtil.validateToken(token)) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
                return;
            }

            String username = jwtUtil.extractUsername(token);
            session.getAttributes().put("username", username);
            session.getAttributes().put("roomId", roomId);

            boolean alreadyConnected = sessions.stream()
                    .anyMatch(s -> roomId.equals(s.getAttributes().get("roomId")) &&
                                   username.equals(s.getAttributes().get("username")));

            sessions.add(session);

            if (!alreadyConnected) {
                MessageResponseDTO systemJoinMsg = new MessageResponseDTO();
                systemJoinMsg.setContent(username + " joined the chat");
                systemJoinMsg.setRoomId(roomId);
                systemJoinMsg.setUsername("System");
                broadcastToRoom(roomId, systemJoinMsg, session);
            }

            System.out.println("Connected: " + session.getId() + " as " + username + " in room " + roomId);
        } catch (Exception e) {
            session.close(CloseStatus.SERVER_ERROR.withReason("Connection error: " + e.getMessage()));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage wsMessage) throws Exception {
        try {
            String username = (String) session.getAttributes().get("username");
            if (username == null) {
                session.sendMessage(new TextMessage("User not authenticated"));
                return;
            }

            User sender = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException(username));

            MessageRequestDTO requestDTO = objectMapper.readValue(wsMessage.getPayload(), MessageRequestDTO.class);

            ChatRoom room = chatRoomRepository.findById(requestDTO.getRoomId())
                    .orElseThrow(() -> new RoomNotFoundException(requestDTO.getRoomId()));

            if (!Optional.ofNullable(room.getUsers()).orElse(Collections.emptyList()).contains(sender)) {
                session.sendMessage(new TextMessage("Access denied"));
                return;
            }

            MessageResponseDTO responseDTO = messagesService.saveMessage(sender, requestDTO, room);
            broadcastToRoom(room.getId(), responseDTO, session);

        } catch (Exception e) {
            session.sendMessage(new TextMessage("Error: " + e.getMessage()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            boolean removed;
            synchronized (sessions) {
                removed = sessions.remove(session);
            }
            if (!removed) return;

            String username = (String) session.getAttributes().get("username");
            Long roomId = (Long) session.getAttributes().get("roomId");
            if (username == null || roomId == null) return;

            boolean stillConnected;
            synchronized (sessions) {
                stillConnected = sessions.stream()
                        .anyMatch(s -> username.equals(s.getAttributes().get("username")) &&
                                       roomId.equals(s.getAttributes().get("roomId")) &&
                                       s.isOpen());
            }

            if (!stillConnected) {
                MessageResponseDTO systemLeaveMsg = new MessageResponseDTO();
                systemLeaveMsg.setContent(username + " left the chat");
                systemLeaveMsg.setRoomId(roomId);
                systemLeaveMsg.setUsername("System");
                broadcastToRoom(roomId, systemLeaveMsg, null);
            }

            System.out.println("Disconnected: " + session.getId() + " (" + username + ")");

        } catch (Exception e) {
            System.err.println("Error during WebSocket disconnect: " + e.getMessage());
        }
    }

    private void broadcastToRoom(Long roomId, MessageResponseDTO msg, WebSocketSession exclude) throws Exception {
        String json = objectMapper.writeValueAsString(msg);
        synchronized (sessions) {
            for (WebSocketSession s : sessions) {
                Long sRoomId = (Long) s.getAttributes().get("roomId");
                if (s.isOpen() && roomId.equals(sRoomId) && (exclude == null || !s.getId().equals(exclude.getId()))) {
                    s.sendMessage(new TextMessage(json));
                }
            }
        }
    }
}
