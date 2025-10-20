package com.micnusz.chat.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.micnusz.chat.dto.ChatRoomRequestDTO;
import com.micnusz.chat.dto.ChatRoomResponseDTO;
import com.micnusz.chat.dto.MessageResponseDTO;
import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.ChatRoomRepository;
import com.micnusz.chat.repository.UserRepository;
import com.micnusz.chat.service.ChatRoomService;
import com.micnusz.chat.service.MessagesService;

import lombok.RequiredArgsConstructor;






@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final MessagesService messagesService;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;


    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponseDTO> createChatRoom(@RequestBody ChatRoomRequestDTO request,
            Authentication authentication) {
        String username = authentication.getName();
        ChatRoomResponseDTO response = chatRoomService.createRoom(request, username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<?> joinRoom(@PathVariable Long roomId,
            @RequestBody(required = false) Map<String, String> body, Authentication authentication) {
                
            String username = authentication.getName();
            String providedPassword = body != null ? body.get("password") : null;

            chatRoomService.joinRoom(roomId, username, providedPassword);

        return ResponseEntity.ok(Map.of("message", "Joined room successfully"));

    }
    
    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable Long roomId, Authentication authentication) {
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        if (!chatRoom.getUsers().contains(user)) {
            return ResponseEntity.status(400).body(Map.of("message", "User is not in the room"));
        }

        chatRoom.getUsers().remove(user);
        chatRoomRepository.save(chatRoom);

        return ResponseEntity.ok(Map.of("message", "Left room successfully"));
    }
    
    
    
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponseDTO>> getAllRooms() {
        return ResponseEntity.ok(chatRoomService.getAllRooms());
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<ChatRoomResponseDTO> getRoomById(@PathVariable Long id, Authentication authentication) {
        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chatroom not found with id: " + id));
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (chatRoom.getPassword() != null && !chatRoom.getPassword().isEmpty()
                && !chatRoom.getUsers().contains(user)) {
            return ResponseEntity.status(403).body(null);
        }
        return ResponseEntity.ok(chatRoomService.getRoomById(id));
    }
    
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(@PathVariable Long roomId) {
        List<MessageResponseDTO> dtos = messagesService.getMessagesByRoomAsDTO(roomId);
        return ResponseEntity.ok(dtos != null ? dtos : Collections.emptyList());
    }

    
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable Long id) {
        chatRoomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
    
    
    
    
}
