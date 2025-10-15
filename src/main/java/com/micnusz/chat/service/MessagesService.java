package com.micnusz.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.micnusz.chat.dto.MessageRequestDTO;
import com.micnusz.chat.mapper.MessagesMapper;
import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.model.Message;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.ChatRoomRepository;
import com.micnusz.chat.repository.MessagesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessagesService {

    private final MessagesRepository messagesRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessagesMapper messagesMapper;


    public Message saveMessage(User sender, MessageRequestDTO dto) {
        ChatRoom chatRoom = chatRoomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("ChatRoom not found with id: " + dto.getRoomId()));

        Message message = messagesMapper.toEntity(dto, sender, chatRoom);
        return messagesRepository.save(message);
    }

    public List<Message> getMessagesByRoom(Long roomId) {
        return messagesRepository.findByRoomIdOrderByTimestampAsc(roomId);
    }
}
