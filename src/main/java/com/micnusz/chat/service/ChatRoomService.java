package com.micnusz.chat.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.micnusz.chat.dto.ChatRoomRequestDTO;
import com.micnusz.chat.dto.ChatRoomResponseDTO;
import com.micnusz.chat.mapper.ChatRoomMapper;
import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.ChatRoomRepository;
import com.micnusz.chat.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatRoomMapper chatRoomMapper;

    public ChatRoomResponseDTO createRoom(ChatRoomRequestDTO dto) {
        User creator = userRepository.findByUsername(dto.getCreatedByUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + dto.getCreatedByUsername()));
        ChatRoom chatRoom = chatRoomMapper.toEntity(dto, creator);
        ChatRoom saved = chatRoomRepository.save(chatRoom);
        return chatRoomMapper.toDto(saved);
    }

    public List<ChatRoomResponseDTO> getAllRooms() {
        return chatRoomRepository.findAll().stream().map(chatRoomMapper::toDto).collect(Collectors.toList());
    }

    public ChatRoomResponseDTO getRoomById(Long id) {
        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found with id: " + id));

        return chatRoomMapper.toDto(chatRoom);
    }

    public void deleteRoom(Long id) {
        if (!chatRoomRepository.existsById(id)) {
            throw new RuntimeException("Chatroom not found with id: " + id);
        }
        chatRoomRepository.deleteById(id);
    }
}


