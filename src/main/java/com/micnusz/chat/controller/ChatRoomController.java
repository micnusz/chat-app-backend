package com.micnusz.chat.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;


    @PostMapping("/rooms")
    public ChatRoom createChatRoom(@RequestBody ChatRoom chatRoom) {
        return chatRoomRepository.save(chatRoom);

    }
    
    @GetMapping("/rooms")
    public List<ChatRoom> getChatRoom() {
        return chatRoomRepository.findAll();
    }

    @GetMapping("/rooms/{id}")
    public ChatRoom getChatRoomById(@PathVariable Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId);
    }

    @DeleteMapping("/rooms/{id}")
    public void deleteChatRoomById(@PathVariable Long chatRoomId) {
        chatRoomRepository.deleteById(chatRoomId);
    }
    
    
    
    
}
