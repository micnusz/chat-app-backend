package com.micnusz.chat.mapper;

import org.springframework.stereotype.Component;

import com.micnusz.chat.dto.MessageRequestDTO;
import com.micnusz.chat.dto.MessageResponseDTO;
import com.micnusz.chat.model.Message;
import com.micnusz.chat.model.User;

@Component
public class MessagesMapper {

    public Message toEntity(MessageRequestDTO dto, User sender) {
        return new Message(sender, dto.getMessage(), dto.getRoomId());
    }

    public MessageResponseDTO toDTO(Message message) {
        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setUsername(message.getSender().getUsername());
        dto.setMessage(message.getContent());
        dto.setRoomId(message.getRoomId());
        dto.setTimestamp(message.getTimestamp());
        return dto;
    }
}
