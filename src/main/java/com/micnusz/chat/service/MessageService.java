package com.micnusz.chat.service;

import org.springframework.stereotype.Service;

import com.micnusz.chat.model.Message;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.MessagesRepository;

import lombok.Data;

@Service
@Data
public class MessageService {
    
    private final MessagesRepository messagesRepository;


    public Message saveMessage(User sender, String content, String roomId) {
        Message msg = new Message(sender, content, roomId);
        return messagesRepository.save(msg);
    }

}
