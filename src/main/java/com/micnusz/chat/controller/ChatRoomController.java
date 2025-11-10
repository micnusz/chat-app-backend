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
import com.micnusz.chat.dto.MessageResponseDTO;
import com.micnusz.chat.service.ChatRoomService;
import com.micnusz.chat.service.MessagesService;

import lombok.RequiredArgsConstructor;






@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final MessagesService messagesService;


    //CREATING ROOM
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponseDTO> createChatRoom(@RequestBody ChatRoomRequestDTO request,
            Authentication authentication) {
        String username = authentication.getName();
        ChatRoomResponseDTO response = chatRoomService.createRoom(request, username);
        return ResponseEntity.ok(response);
    }

    //JOINING ROOM/{ID}
    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<?> joinRoom(@PathVariable Long roomId,
            @RequestBody(required = false) Map<String, String> body, Authentication authentication) {

        String username = authentication.getName();
        String providedPassword = body != null ? body.get("password") : null;

        chatRoomService.joinRoom(roomId, username, providedPassword);

        return ResponseEntity.ok(Map.of("message", "Joined room successfully"));

    }

    //LEAVING ROOM/{ID}
    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable Long roomId, Authentication authentication) {
        String username = authentication.getName();

        chatRoomService.leaveRoom(roomId, username);

        return ResponseEntity.ok(Map.of("message", "Left room successfully"));
    }

    //GETTING ALL ROOMS
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponseDTO>> getAllRooms() {
        return ResponseEntity.ok(chatRoomService.getAllRooms());
    }

    //GETTING ROOM/{ID}
    @GetMapping("/rooms/{id}")
    public ResponseEntity<ChatRoomResponseDTO> getRoomById(@PathVariable Long id, Authentication authentication) {

        String username = authentication.getName();
        ChatRoomResponseDTO response = chatRoomService.getRoomById(id, username);

        return ResponseEntity.ok(response);

    }

    // GETTING MESSAGES IN ROOM/{ID}
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(@PathVariable Long roomId,
            Authentication authentication) {
        String username = authentication.getName();
        List<MessageResponseDTO> response = messagesService.getMessagesByRoomAsDTO(roomId, username);
        return ResponseEntity.ok(response);
    }

    // DELETING ROOM/{ID}
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable Long id) {
        chatRoomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

}
