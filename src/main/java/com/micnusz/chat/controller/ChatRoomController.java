package com.micnusz.chat.controller;

import java.util.List;

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
import com.micnusz.chat.service.ChatRoomService;

import lombok.RequiredArgsConstructor;




@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;


    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponseDTO> createChatRoom(@RequestBody ChatRoomRequestDTO request,
            Authentication authentication) {
        String username = authentication.getName();
        ChatRoomResponseDTO response = chatRoomService.createRoom(request, username);
        return ResponseEntity.ok(response);
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
