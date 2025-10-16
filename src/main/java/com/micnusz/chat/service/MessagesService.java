package com.micnusz.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.micnusz.chat.dto.MessageRequestDTO;
import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.model.Message;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.MessagesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessagesService {

    private final MessagesRepository messagesRepository;


    public Message saveMessage(User sender, MessageRequestDTO requestDTO, ChatRoom room) {
        Message message = new Message(sender, requestDTO.getContent(), room);
        return messagesRepository.save(message);
    }


    public List<Message> getMessagesByRoom(Long roomId) {
        return messagesRepository.findByChatRoomIdOrderByTimestampAsc(roomId);
    }
}
