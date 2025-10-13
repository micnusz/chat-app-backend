package com.micnusz.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.micnusz.chat.dto.MessageRequestDTO;
import com.micnusz.chat.model.Message;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.MessagesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessagesService {

    private final MessagesRepository messagesRepository;

    public Message saveMessage(User sender, MessageRequestDTO dto) {
        Message message = new Message(sender, dto.getMessage(), dto.getRoomId());
        return messagesRepository.save(message);
    }

    public List<Message> getMessagesByRoom(String roomId) {
        return messagesRepository.findByRoomIdOrderByTimestampAsc(roomId);
    }
}
