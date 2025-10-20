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
    private final MessagesMapper messagesMapper; 
    private final ObjectMapper objectMapper = new ObjectMapper();
    


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String path = session.getUri().getPath(); 
        String[] segments = path.split("/");
        Long roomId = Long.parseLong(segments[segments.length - 1]); 

        String query = session.getUri().getQuery(); 
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
    }



    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage wsMessage) throws Exception {
        String username = (String) session.getAttributes().get("username");
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MessageRequestDTO requestDTO = objectMapper.readValue(wsMessage.getPayload(), MessageRequestDTO.class);

        ChatRoom room = chatRoomRepository.findById(requestDTO.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + requestDTO.getRoomId()));
        var savedMessage = messagesService.saveMessage(sender, requestDTO, room);

        MessageResponseDTO responseDTO = messagesMapper.toDTO(savedMessage);

        String json = objectMapper.writeValueAsString(responseDTO);


        synchronized (sessions) {
            for (WebSocketSession s : sessions) {
                if (s.isOpen() && s.getAttributes().get("username") != null) {
                    String sessionUsername = (String) s.getAttributes().get("username");
                    Long sessionRoomId = (Long) s.getAttributes().get("roomId");
                    if (!session.getId().equals(s.getId()) && sessionRoomId.equals(room.getId())) {
                        s.sendMessage(new TextMessage(json));
                    }
                }
            }
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        boolean removed;
        synchronized (sessions) {
            removed = sessions.remove(session);
        }

        if (!removed) {
            return;
        }

        String username = (String) session.getAttributes().get("username");
        Long roomId = (Long) session.getAttributes().get("roomId");

        boolean stillConnected;
        synchronized (sessions) {
            stillConnected = sessions.stream().anyMatch(s -> {
                String sUsername = (String) s.getAttributes().get("username");
                Long sRoomId = (Long) s.getAttributes().get("roomId");
                return sUsername.equals(username) && sRoomId.equals(roomId) && s.isOpen();
            });
        }

        if (!stillConnected && username != null && roomId != null) {
            MessageResponseDTO systemLeaveMsg = new MessageResponseDTO();
            systemLeaveMsg.setContent(username + " left the chat");
            systemLeaveMsg.setRoomId(roomId);
            systemLeaveMsg.setUsername("System");
            broadcastToRoom(roomId, systemLeaveMsg, null);
        }

        System.out.println("Disconnected: " + session.getId() + " (" + username + ")");
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
