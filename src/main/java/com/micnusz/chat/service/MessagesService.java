package com.micnusz.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.micnusz.chat.dto.MessageRequestDTO;
import com.micnusz.chat.dto.MessageResponseDTO;
import com.micnusz.chat.mapper.MessagesMapper;
import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.model.Message;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.MessagesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessagesService {

    private final MessagesRepository messagesRepository;
    private final MessagesMapper messagesMapper;



    public Message saveMessage(User sender, MessageRequestDTO requestDTO, ChatRoom room) {
        Message message = new Message(sender, requestDTO.getContent(), room);
        return messagesRepository.save(message);
    }


    public List<MessageResponseDTO> getMessagesByRoomAsDTO(Long roomId) {
        List<Message> messages = messagesRepository.findByChatRoomIdOrderByTimestampAsc(roomId);
        return messages.stream()
                       .map(message -> messagesMapper.toDTO(message))
                       .toList();
    }

    
}
