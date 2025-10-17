package com.micnusz.chat.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    return Optional.ofNullable(messagesRepository.findByChatRoomIdOrderByTimestampAsc(roomId))
                   .orElse(Collections.emptyList())
                   .stream()
                   .map(messagesMapper::toDTO)
                   .toList();
}


    
}
