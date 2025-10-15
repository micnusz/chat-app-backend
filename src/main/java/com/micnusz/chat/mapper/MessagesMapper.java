package com.micnusz.chat.mapper;

import org.springframework.stereotype.Component;

import com.micnusz.chat.dto.MessageRequestDTO;
import com.micnusz.chat.dto.MessageResponseDTO;
import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.model.Message;
import com.micnusz.chat.model.User;

@Component
public class MessagesMapper {

    public Message toEntity(MessageRequestDTO dto, User sender, ChatRoom chatRoom) {
        return new Message(sender, dto.getContent(), chatRoom);
    }

    public MessageResponseDTO toDTO(Message message) {
        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setUsername(message.getSender().getUsername());
        dto.setContent(message.getContent());
         dto.setRoomId(message.getChatRoom().getId());
        dto.setTimestamp(message.getTimestamp());
        return dto;
    }
}
