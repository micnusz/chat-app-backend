package com.micnusz.chat.controller;

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
import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.ChatRoomRepository;
import com.micnusz.chat.repository.UserRepository;
import com.micnusz.chat.service.ChatRoomService;

import lombok.RequiredArgsConstructor;




@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
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
    public ResponseEntity<?> joinRoom(@PathVariable Long roomId, @RequestBody(required=false) Map<String, String> body, Authentication authentication) throws RuntimeException {
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
        String providedPassword = body != null ? body.get("password") : null;

        if (room.getPassword() != null && !room.getPassword().isEmpty()) {
            if (providedPassword == null || !providedPassword.equals(room.getPassword())) {
                return ResponseEntity.status(403).body(Map.of("message", "Incorrect password"));
            }
        }


    room.getUsers().add(user);
    chatRoomRepository.save(room);

    return ResponseEntity.ok(Map.of("message", "Joined room successfully"));

    }
    
    
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponseDTO>> getAllRooms() {
        return ResponseEntity.ok(chatRoomService.getAllRooms());
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<ChatRoomResponseDTO> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(chatRoomService.getRoomById(id));
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable Long id) {
        chatRoomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
    
    
    
    
}
